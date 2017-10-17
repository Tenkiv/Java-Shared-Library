package com.tenkiv.tekdaqc.communication.command.queue

import com.tenkiv.tekdaqc.communication.command.queue.values.ABaseQueueVal
import com.tenkiv.tekdaqc.communication.command.queue.values.IQueueObject
import com.tenkiv.tekdaqc.communication.data_points.AnalogInputCountData
import com.tenkiv.tekdaqc.communication.data_points.DigitalInputData
import com.tenkiv.tekdaqc.communication.message.ABoardMessage
import com.tenkiv.tekdaqc.communication.message.IMessageListener
import com.tenkiv.tekdaqc.communication.tasks.ITaskComplete
import com.tenkiv.tekdaqc.hardware.ATekdaqc
import com.tenkiv.tekdaqc.utility.TekdaqcCriticalError
import java.util.*
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock

/**
 * Manager for commands to the Tekdaqc, ensuring that they are executed and managing the resulting callbacks for success and failure.
 *
 * Constructor for [CommandQueueManager] which assigns which [ATekdaqc] this class manages.
 *
 * @param tekdaqc The Tekdaqc which is being managed by the command queue.
 *
 * @author Tenkiv (software@tenkiv.com)
 * *
 * @since v2.0.0.0
 */
class CommandQueueManager(private val mTekdaqc: ATekdaqc) : ICommandManager, IMessageListener {

    /**
     * The total number of allowable failures for a single command before a [TekdaqcCriticalError] is thrown.
     */
    private val maximumAllowedFailures = 3

    /**
     * Seconds to wait before declaring a command failure.
     */
    private val commandTimeout = 3L

    /**
     * The lock ensuring execution safety.
     */
    private val queueLock = ReentrantLock()

    /**
     * The lock condition.
     */
    private val commandCondition = queueLock.newCondition()

    /**
     * [ExecutorService] to handle threads.
     */
    private val executor: ExecutorService

    /**
     * [Deque] of [IQueueObject] to be turned into either callbacks or commands.
     */
    private val commandDeque: Deque<IQueueObject>

    /**
     * [Boolean] representing the current state of executor.
     */
    private val isTaskExecuting = AtomicBoolean(false)

    /**
     * If the last command sent timed out.
     */
    private val didTaskTimeout = AtomicBoolean(true)

    /**
     * The number of times a command has been attempted, but failed to get a response.
     */
    private val failureCount = AtomicInteger(0)

    /**
     * The last command sent to the Tekdaqc.
     */
    private var lastCommand: ABaseQueueVal? = null

    init {
        executor = Executors.newSingleThreadExecutor(Factory())
        commandDeque = LinkedBlockingDeque<IQueueObject>()
        mTekdaqc.messageBroadcaster.commandQueueAddListener(mTekdaqc, this)
    }

    override fun queueCommand(command: IQueueObject) {
        commandDeque.addLast(command)
        commandDeque.addLast(QueueCallback(true))
        tryCommand()
    }

    override fun queueTask(task: Task) {
        commandDeque.addAll(task.commandList)
        tryCommand()
    }

    private fun executeCommand() {
        //Update the fact that we're executing a command.
        isTaskExecuting.set(true)
        //If its a command we should execute it.

        val queueObject = commandDeque.poll()

        if (queueObject is ABaseQueueVal) {
            executeQueueValue(queueObject)
        } else if (queueObject is QueueCallback) {
            executeQueueCallback(queueObject)
        }
    }

    private fun executeQueueValue(queueObject: ABaseQueueVal){
        //Set last command in case we need to resend it.
        lastCommand = queueObject

        //Submit new writing thread to send command.
        executor.submit(CommandWriterThread(mTekdaqc, queueObject))
        //Lock the queue so we don't send more.
        queueLock.lock()
        try {
            //Max wait time for the lock.
            commandCondition.await(commandTimeout, TimeUnit.SECONDS)
        } finally {
            //If task timed out and we're still connected, attempt to send the command again.
            if (didTaskTimeout.get() && mTekdaqc.isConnected) {
                println("Interrupt Hit. Not Woken in time")
                //If failures is less then total failure count, resend.
                if (failureCount.get() < maximumAllowedFailures) {
                    //Get and increment failure count.
                    failureCount.getAndIncrement()
                    //Re-add the last command.
                    commandDeque.addFirst(lastCommand)
                    //Update the fact that we're not executing a command.
                    isTaskExecuting.set(false)
                } else {
                    //Throw a critical error if we run out of attempts.
                    mTekdaqc.criticalErrorNotification(TekdaqcCriticalError.FAILED_MAJOR_COMMAND)
                }
            }
            didTaskTimeout.set(true)
            queueLock.unlock()
            tryCommand()
        }
    }

    private fun executeQueueCallback(queueObject: QueueCallback){
        queueObject.success(mTekdaqc)
        isTaskExecuting.set(false)
        tryCommand()
    }

    override fun purge(forShutdown: Boolean) {
        queueLock.lock()
        if (forShutdown) {
            didTaskTimeout.set(false)
        }
        try {
            commandDeque.clear()
        } finally {
            queueLock.unlock()
        }
    }

    override fun getNumberQueued(): Int {
        queueLock.lock()
        try {
            return commandDeque.size
        } finally {
            queueLock.unlock()
        }
    }

    /**
     * Command to attempt running of a value in the [CommandQueueManager.commandDeque]. Will not execute
     * a new command if one is already running.
     */
    override fun tryCommand() {

        if (!isTaskExecuting.get()
                && commandDeque.size > 0
                && mTekdaqc.isConnected) {
            executeCommand()
        }
    }

    /**
     * Internal method which culls the [CommandQueueManager.commandDeque] until it polls a [QueueCallback].
     * This will clear out all [ABaseQueueVal] of a [Task] ensuring that commands that are dependant on each other will
     * not be executed. This will notify the [ITaskComplete] of the [Task] of failure.
     */
    private fun cullQueueUntilCallback() {
        if (commandDeque.size > 0) {
            val callback = commandDeque.poll() as? QueueCallback
            if (callback != null) {
                if (!callback.isInternalDelimiter) {
                    callback.failure(mTekdaqc)
                }
            } else {
                commandDeque.remove()
                cullQueueUntilCallback()
            }
        }
    }

    override fun onErrorMessageReceived(tekdaqc: ATekdaqc, message: ABoardMessage) {

        queueLock.lock()
        try {

            didTaskTimeout.set(false)

            cullQueueUntilCallback()

            isTaskExecuting.set(false)

            println("Error Task Response - " + message)

        } finally {
            commandCondition.signal()
            queueLock.unlock()
        }
    }

    override fun onStatusMessageReceived(tekdaqc: ATekdaqc, message: ABoardMessage) {
        queueLock.lock()
        try {
            failureCount.lazySet(0)

            didTaskTimeout.set(false)
            isTaskExecuting.set(false)
        } finally {
            commandCondition.signal()
            queueLock.unlock()
        }
    }

    override fun onDebugMessageReceived(tekdaqc: ATekdaqc, message: ABoardMessage) {

    }

    override fun onCommandDataMessageReceived(tekdaqc: ATekdaqc, message: ABoardMessage) {

    }

    override fun onAnalogInputDataReceived(tekdaqc: ATekdaqc, countData: AnalogInputCountData) {

    }

    override fun onDigitalInputDataReceived(tekdaqc: ATekdaqc, data: DigitalInputData) {

    }

    override fun onDigitalOutputDataReceived(tekdaqc: ATekdaqc, message: BooleanArray) {

    }

    /**
     * Custom thread factory for this class's internal executor. Ensures that all threads will have the appropriate
     * priority level and name.

     * @author Tenkiv (software@tenkiv.com)
     * *
     * @since v1.0.0.0
     */
    private inner class Factory : ThreadFactory {

        private val commandThreadName = "TEKDAQC_COMMAND_THREAD"

        private val commandThreadPriority = 4 // Equivalent to Android's Process.THREAD_PRIORITY_BACKGROUND

        override fun newThread(r: Runnable): Thread {
            val thread = Thread(r)
            thread.priority = commandThreadPriority
            thread.name = commandThreadName
            return thread
        }
    }
}
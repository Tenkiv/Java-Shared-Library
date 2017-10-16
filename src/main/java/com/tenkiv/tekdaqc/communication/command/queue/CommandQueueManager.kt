package com.tenkiv.tekdaqc.communication.command.queue

import com.tenkiv.tekdaqc.communication.command.queue.values.ABaseQueueVal
import com.tenkiv.tekdaqc.communication.command.queue.values.IQueueObject
import com.tenkiv.tekdaqc.communication.command.queue.values.QueueValue
import com.tenkiv.tekdaqc.communication.data_points.AnalogInputCountData
import com.tenkiv.tekdaqc.communication.data_points.DigitalInputData
import com.tenkiv.tekdaqc.communication.message.ABoardMessage
import com.tenkiv.tekdaqc.communication.message.IMessageListener
import com.tenkiv.tekdaqc.communication.tasks.ITaskComplete
import com.tenkiv.tekdaqc.hardware.AAnalogInput
import com.tenkiv.tekdaqc.hardware.ATekdaqc
import com.tenkiv.tekdaqc.utility.TekdaqcCriticalError

import java.io.BufferedOutputStream
import java.io.IOException
import java.util.Deque
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.Lock
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
     * The lock ensuring execution safety.
     */
    private val mQueueLock = ReentrantLock()

    /**
     * The lock condition.
     */
    private val mCommandCondition = mQueueLock.newCondition()

    /**
     * [ExecutorService] to handle threads.
     */
    private val mExecutor: ExecutorService

    /**
     * [Deque] of [IQueueObject] to be turned into either callbacks or commands.
     */
    private val mCommandDeque: Deque<IQueueObject>

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
    private val mFailureCount = AtomicInteger(0)

    /**
     * The last command sent to the Tekdaqc.
     */
    private var mLastCommand: ABaseQueueVal? = null

    init {
        mExecutor = Executors.newSingleThreadExecutor(Factory())
        mCommandDeque = LinkedBlockingDeque<IQueueObject>()
        mTekdaqc.messageBroadcaster.commandQueueAddListener(mTekdaqc,this)
    }

    override fun queueCommand(command: IQueueObject) {
        mCommandDeque.addLast(command)
        mCommandDeque.addLast(QueueCallback(true))
        tryCommand()
    }

    override fun queueTask(task: Task) {
        mCommandDeque.addAll(task.commandList)
        tryCommand()
    }

    private fun executeCommand() {
        //Update the fact that we're executing a command.
        isTaskExecuting.set(true)
        //If its a command we should execute it.

        val queueObject = mCommandDeque.poll()

        if (queueObject is ABaseQueueVal) {
            //Set last command in case we need to resend it.
            mLastCommand = queueObject

            //Submit new writing thread to send command.
            mExecutor.submit(CommandWriterThread(mTekdaqc,queueObject))
            //Lock the queue so we don't send more.
            mQueueLock.lock()
            try {
                //Max wait time for the lock.
                mCommandCondition.await(3, TimeUnit.SECONDS)
            } finally {
                //If task timed out and we're still connected, attempt to send the command again.
                if (didTaskTimeout.get() && mTekdaqc.isConnected) {
                    println("Interrupt Hit. Not Woken in time")
                    //If failures is less then total failure count, resend.
                    if (mFailureCount.get() < MAX_ALLOWABLE_FAILURES) {
                        //Get and increment failure count.
                        mFailureCount.getAndIncrement()
                        //Re-add the last command.
                        mCommandDeque.addFirst(mLastCommand)
                        //Update the fact that we're not executing a command.
                        isTaskExecuting.set(false)
                    } else {
                        //Throw a critical error if we run out of attempts.
                        mTekdaqc.criticalErrorNotification(TekdaqcCriticalError.FAILED_MAJOR_COMMAND)
                    }
                }
                didTaskTimeout.set(true)
                mQueueLock.unlock()
                tryCommand()
            }
            //If its a call back we should update the listener.
        } else if (queueObject is QueueCallback) {
            queueObject.success(mTekdaqc)
            isTaskExecuting.set(false)
            tryCommand()
        }
    }

    override fun purge(forShutdown: Boolean) {
        mQueueLock.lock()
        if (forShutdown) {
            didTaskTimeout.set(false)
        }
        try {
            mCommandDeque.clear()
        } finally {
            mQueueLock.unlock()
        }
    }

    override fun getNumberQueued(): Int {
        mQueueLock.lock()
        try {
            return mCommandDeque.size
        } finally {
            mQueueLock.unlock()
        }
    }

    /**
     * Command to attempt running of a value in the [CommandQueueManager.mCommandDeque]. Will not execute
     * a new command if one is already running.
     */
    override fun tryCommand() {

        if (!isTaskExecuting.get()
                && mCommandDeque.size > 0
                && mTekdaqc.isConnected) {
            /*object : Thread() {
                override fun run() {*/

            executeCommand()

                /*}
            }.start()*/
        }
    }

    /**
     * Internal method which culls the [CommandQueueManager.mCommandDeque] until it polls a [QueueCallback].
     * This will clear out all [ABaseQueueVal] of a [Task] ensuring that commands that are dependant on each other will
     * not be executed. This will notify the [ITaskComplete] of the [Task] of failure.
     */
    private fun cullQueueUntilCallback() {
        if (mCommandDeque.size > 0) {
            val callback = mCommandDeque.poll() as? QueueCallback
            if (callback != null) {
                if (!callback.isInternalDelimiter) {
                    callback.failure(mTekdaqc)
                }
            } else {
                mCommandDeque.remove()
                cullQueueUntilCallback()
            }
        }
    }

    override fun onErrorMessageReceived(tekdaqc: ATekdaqc, message: ABoardMessage) {

        mQueueLock.lock()
        try {

            didTaskTimeout.set(false)

            cullQueueUntilCallback()

            isTaskExecuting.set(false)

            println("Error Task Response - " + message)

        } finally {
            mCommandCondition.signal()
            mQueueLock.unlock()
        }
    }

    override fun onStatusMessageReceived(tekdaqc: ATekdaqc, message: ABoardMessage) {
        mQueueLock.lock()
        try {
            mFailureCount.lazySet(0)

            didTaskTimeout.set(false)
            isTaskExecuting.set(false)
        } finally {
            mCommandCondition.signal()
            mQueueLock.unlock()
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
    private class Factory : ThreadFactory {

        override fun newThread(r: Runnable): Thread {
            val thread = Thread(r)
            thread.priority = COMMAND_THREAD_PRIORITY
            thread.name = COMMAND_THREAD_NAME
            return thread
        }

        companion object {

            /**
             * The name for the threads.
             */
            private val COMMAND_THREAD_NAME = "TEKDAQC_COMMAND_THREAD"

            /**
             * The priority for the threads.
             */
            private val COMMAND_THREAD_PRIORITY = 4 // Equivalent to Android's Process.THREAD_PRIORITY_BACKGROUND
        }
    }

    companion object {

        /**
         * The total number of allowable failures for a single command before a [TekdaqcCriticalError] is thrown.
         */
        private val MAX_ALLOWABLE_FAILURES = 3

    }
}
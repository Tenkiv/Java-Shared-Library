package com.tenkiv.tekdaqc.communication.command.queue;

import com.tenkiv.tekdaqc.communication.command.queue.values.ABaseQueueVal;
import com.tenkiv.tekdaqc.communication.command.queue.values.IQueueObject;
import com.tenkiv.tekdaqc.communication.data_points.AnalogInputCountData;
import com.tenkiv.tekdaqc.communication.data_points.DigitalInputData;
import com.tenkiv.tekdaqc.communication.message.ABoardMessage;
import com.tenkiv.tekdaqc.communication.message.IMessageListener;
import com.tenkiv.tekdaqc.communication.tasks.ITaskComplete;
import com.tenkiv.tekdaqc.hardware.AAnalogInput;
import com.tenkiv.tekdaqc.hardware.ATekdaqc;
import com.tenkiv.tekdaqc.utility.TekdaqcCriticalError;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Deque;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Manager for commands to the Tekdaqc, ensuring that they are executed and managing the resulting callbacks for success and failure.
 *
 * @author Tenkiv (software@tenkiv.com)
 * @since v2.0.0.0
 */
public class CommandQueueManager implements ICommandManager, IMessageListener {

    /**
     * The lock ensuring execution safety.
     */
    private final Lock mQueueLock = new ReentrantLock();

    /**
     * The lock condition.
     */
    private final Condition mCommandCondition = mQueueLock.newCondition();

    /**
     * The {@link ATekdaqc} which the {@link CommandQueueManager} is running for.
     */
    private final ATekdaqc mTekdaqc;

    /**
     * {@link ExecutorService} to handle threads.
     */
    private ExecutorService mExecutor;

    /**
     * {@link Deque} of {@link IQueueObject} to be turned into either callbacks or commands.
     */
    private Deque<IQueueObject> mCommandDeque;

    /**
     * {@link Boolean} representing the current state of executor.
     */
    private final AtomicBoolean isTaskExecuting = new AtomicBoolean(false);

    /**
     * If the last command sent timed out.
     */
    private final AtomicBoolean didTaskTimeout = new AtomicBoolean(true);

    /**
     * The number of times a command has been attempted, but failed to get a response.
     */
    private final AtomicInteger mFailureCount = new AtomicInteger(0);

    /**
     * The total number of allowable failures for a single command before a {@link TekdaqcCriticalError} is thrown.
     */
    private static final int MAX_ALLOWABLE_FAILURES = 3;

    /**
     * The last command sent to the Tekdaqc.
     */
    private ABaseQueueVal mLastCommand;

    /**
     * Constructor for {@link CommandQueueManager} which assigns which {@link ATekdaqc} this class manages.
     *
     * @param tekdaqc The Tekdaqc which is being managed by the command queue.
     */
    public CommandQueueManager(ATekdaqc tekdaqc) {
        mTekdaqc = tekdaqc;
        mExecutor = Executors.newSingleThreadExecutor(new Factory());
        mCommandDeque = new LinkedBlockingDeque<>();
        mTekdaqc.addListener(this);
    }

    @Override
    public void queueCommand(IQueueObject command) {
        mCommandDeque.addLast(command);
        mCommandDeque.addLast(new QueueCallback(true));
        tryCommand();
    }

    @Override
    public void queueTask(Task task) {
        mCommandDeque.addAll(task.getCommandList());
        tryCommand();
    }

    /**
     * Internal method which sorts between {@link ABaseQueueVal} to be executed as commands and
     * {@link QueueCallback} to be called back to as notification.
     */
    private void executeCommand(){
        //Update the fact that we're executing a command.
        isTaskExecuting.set(true);
        //If its a command we should execute it.
        if (mCommandDeque.peek() instanceof ABaseQueueVal) {
            //Set last command in case we need to resend it.
            mLastCommand = (ABaseQueueVal) mCommandDeque.peek();

            System.out.println("Command Queued: "+new String(mLastCommand.generateCommandBytes()));

            //Submit new writing thread to send command.
            mExecutor.submit(new CommandWriterThread());
            //Lock the queue so we don't send more.
            System.out.println("Current Thread for Lock"+ Thread.currentThread().getName());

            mQueueLock.lock();
            try{
                //Max wait time for the lock.
                mCommandCondition.await(3, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                //If task timed out and we're still connected, attempt to send the command again.
                if(didTaskTimeout.get() && mTekdaqc.isConnected()){
                    System.out.println("Interrupt Hit. Not Woken in time");
                    //If failures is less then total failure count, resend.
                    if(mFailureCount.get() < MAX_ALLOWABLE_FAILURES) {
                        //Get and increment failure count.
                        mFailureCount.getAndIncrement();
                        //Re-add the last command.
                        mCommandDeque.addFirst(mLastCommand);
                        //Update the fact that we're not executing a command.
                        isTaskExecuting.set(false);
                    }else{
                        //Throw a critical error if we run aout of attempts.
                        mTekdaqc.criticalErrorNotification(TekdaqcCriticalError.FAILED_MAJOR_COMMAND);
                    }
                }
                didTaskTimeout.set(true);
                mQueueLock.unlock();
                tryCommand();
            }
        //If its a call back we should update the listener.
        } else if (mCommandDeque.peek() instanceof QueueCallback) {
            ((QueueCallback) mCommandDeque.poll()).success(mTekdaqc);
            isTaskExecuting.set(false);
            tryCommand();
        }
    }

    @Override
    public void purge(boolean forShutdown){
        mQueueLock.lock();
        if(forShutdown){
            didTaskTimeout.set(false);
        }
        try {
            mCommandDeque.clear();
        }finally {
            mQueueLock.unlock();
        }
    }

    @Override
    public int getNumberQueued() {
        mQueueLock.lock();
        try {
            return mCommandDeque.size();
        } finally {
            mQueueLock.unlock();
        }
    }

    /**
     * Command to attempt running of a value in the {@link CommandQueueManager#mCommandDeque}. Will not execute
     * a new command if one is already running.
     */
    public void tryCommand() {

        if (!isTaskExecuting.get()
                && mCommandDeque.size() > 0
                && mTekdaqc.isConnected()) {
            new Thread(){
                @Override
                public void run() {
                    executeCommand();
                }
            }.start();

        }
    }

    /**
     * Internal method which culls the {@link CommandQueueManager#mCommandDeque} until it polls a {@link QueueCallback}.
     * This will clear out all {@link ABaseQueueVal} of a {@link Task} ensuring that commands that are dependant on each other will
     * not be executed. This will notify the {@link ITaskComplete} of the {@link Task} of failure.
     */
    private void cullQueueUntilCallback() {
        if (mCommandDeque.size() > 0) {
            if (mCommandDeque.peek() instanceof QueueCallback) {
                final QueueCallback callback = (QueueCallback) mCommandDeque.poll();
                if (!callback.isInternalDelimiter()) {
                    callback.failure(mTekdaqc);
                }
            } else {
                mCommandDeque.remove();
                cullQueueUntilCallback();
            }
        }
    }

    @Override
    public void onErrorMessageReceived(ATekdaqc tekdaqc, ABoardMessage message) {

        mQueueLock.lock();
        try {

            didTaskTimeout.set(false);

            cullQueueUntilCallback();

            isTaskExecuting.set(false);

            System.out.println("Error Task Response - " + message);

        } finally {
            mCommandCondition.signal();
            mQueueLock.unlock();
        }
    }

    @Override
    public void onStatusMessageReceived(ATekdaqc tekdaqc, ABoardMessage message) {
        mQueueLock.lock();
        try {
            mFailureCount.lazySet(0);

            didTaskTimeout.set(false);
            isTaskExecuting.set(false);
        }finally{
            mCommandCondition.signal();
            mQueueLock.unlock();
        }
    }

    @Override
    public void onDebugMessageReceived(final ATekdaqc tekdaqc, final ABoardMessage message) {

    }

    @Override
    public void onCommandDataMessageReceived(final ATekdaqc tekdaqc, final ABoardMessage message) {

    }

    @Override
    public void onAnalogInputDataReceived(final ATekdaqc tekdaqc, final AnalogInputCountData countData) {

    }

    @Override
    public void onDigitalInputDataReceived(final ATekdaqc tekdaqc, final DigitalInputData data) {

    }

    @Override
    public void onDigitalOutputDataReceived(final ATekdaqc tekdaqc, final boolean[] message) {

    }

    /**
     * Custom thread factory for this class's internal executor. Ensures that all threads will have the appropriate
     * priority level and name.
     *
     * @author Jared Woolston (jwoolston@tenkiv.com)
     * @since v1.0.0.0
     */
    private static final class Factory implements ThreadFactory {

        /**
         * The name for the threads.
         */
        private static final String COMMAND_THREAD_NAME = "TEKDAQC_COMMAND_THREAD";

        /**
         * The priority for the threads.
         */
        private static final int COMMAND_THREAD_PRIORITY = 4; // Equivalent to Android's Process.THREAD_PRIORITY_BACKGROUND

        @Override
        public Thread newThread(final Runnable r) {
            final Thread thread = new Thread(r);
            thread.setPriority(COMMAND_THREAD_PRIORITY);
            thread.setName(COMMAND_THREAD_NAME);
            return thread;
        }
    }

    /**
     * Class which is executed by {@link CommandQueueManager#mExecutor}. Writes out generated {@link Byte} of {@link ABaseQueueVal}
     * to the {@link ATekdaqc#getOutputStream()}
     */
    private class CommandWriterThread extends Thread {

        private static final String COMMAND_WRITER_THREAD_NAME = "COMMAND_WRITER_THREAD_NAME";

        private CommandWriterThread() {
            super(COMMAND_WRITER_THREAD_NAME);
        }

        @Override
        public void run() {
            try {
                writeToStream((ABaseQueueVal) mCommandDeque.poll());
            } catch (Exception e) {
                e.printStackTrace();

                mTekdaqc.criticalErrorNotification(TekdaqcCriticalError.TERMINAL_CONNECTION_DISRUPTION);
            }
        }

        /**
         * Method to get and write out command bytes to Telnet.
         *
         * @param command The command to be executed
         * @throws IOException Generic exception to catch issues in Telnet.
         */
        private void writeToStream(final ABaseQueueVal command) throws IOException {
            final BufferedOutputStream out = new BufferedOutputStream(mTekdaqc.getOutputStream());
            out.write(command.generateCommandBytes());
            out.flush();
        }
    }
}
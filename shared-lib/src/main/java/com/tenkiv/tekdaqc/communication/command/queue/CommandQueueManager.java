package com.tenkiv.tekdaqc.communication.command.queue;

import com.tenkiv.tekdaqc.communication.command.queue.values.ABaseQueueVal;
import com.tenkiv.tekdaqc.communication.data_points.AnalogInputData;
import com.tenkiv.tekdaqc.communication.data_points.DigitalInputData;
import com.tenkiv.tekdaqc.communication.message.ABoardMessage;
import com.tenkiv.tekdaqc.communication.message.IMessageListener;
import com.tenkiv.tekdaqc.communication.tasks.ITaskComplete;
import com.tenkiv.tekdaqc.hardware.ATekdaqc;

import java.io.BufferedOutputStream;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
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

    private final Lock mQueueLock = new ReentrantLock();
    private final Condition mCommandCompletion = mQueueLock.newCondition();
    /**
     * The {@link ATekdaqc} which the {@link CommandQueueManager} is running for.
     */
    private final ATekdaqc mTekdaqc;
    /**
     * {@link ExecutorService} to handle threads.
     */
    private ExecutorService mExecutor;
    /**
     * {@link Queue} of {@link IQueueObject} to be turned into either callbacks or commands.
     */
    private Queue<IQueueObject> mCommandQueue;
    /**
     * {@link Boolean} representing the current state of executor.
     */
    private boolean isTaskExecuting = false;

    /**
     * Constructor for {@link CommandQueueManager} which assigns which {@link ATekdaqc} this class manages.
     *
     * @param tekdaqc The Tekdaqc which is being managed by the command queue.
     */
    public CommandQueueManager(final ATekdaqc tekdaqc) {
        mTekdaqc = tekdaqc;
        mExecutor = Executors.newSingleThreadExecutor(new Factory());
        mCommandQueue = new LinkedBlockingQueue<IQueueObject>();
        mTekdaqc.registerListener(this);
    }

    @Override
    public void queueCommand(final IQueueObject command) {
        mCommandQueue.add(command);
        mCommandQueue.add(new QueueCallback(true));
        tryCommand();
    }

    @Override
    public void queueTask(Task task) {
        mCommandQueue.addAll(task.getCommandList());
        tryCommand();
    }

    /**
     * Internal method which sorts between {@link ABaseQueueVal} to be executed as commands and
     * {@link QueueCallback} to be called back to as notification.
     */
    private void executeCommand() {
        isTaskExecuting = true;
        if (mCommandQueue.peek() instanceof ABaseQueueVal) {
            mExecutor.submit(new CommandWriterRunnable());
        } else if (mCommandQueue.peek() instanceof QueueCallback) {
            ((QueueCallback) mCommandQueue.poll()).success(mTekdaqc);
            mCommandCompletion.signalAll();
            isTaskExecuting = false;
            tryCommand();
        }

    }

    /**
     * Command to attempt running of a value in the {@link CommandQueueManager#mCommandQueue}. Will not execute
     * a new command if one is already running.
     */
    public void tryCommand() {
        if (!isTaskExecuting
                && mCommandQueue.size() > 0
                && mTekdaqc.isConnected()) {
            executeCommand();
        }
    }

    /**
     * Internal method which culls the {@link CommandQueueManager#mCommandQueue} until it polls a {@link QueueCallback}.
     * This will clear out all {@link ABaseQueueVal} of a {@link Task} ensuring that commands that are dependant on each other will
     * not be executed. This will notify the {@link ITaskComplete} of the {@link Task} of failure.
     */
    private void cullQueueUntilCallback() {
        if (mCommandQueue.size() > 0) {
            if (mCommandQueue.peek() instanceof QueueCallback) {
                QueueCallback callback = (QueueCallback) mCommandQueue.poll();
                if (!callback.isInternalDelimiter()) {
                    callback.failure(mTekdaqc);
                }
            } else {
                mCommandQueue.remove();
                cullQueueUntilCallback();
            }

        }
    }

    @Override
    public void onErrorMessageReceived(final ATekdaqc tekdaqc, final ABoardMessage message) {
        mQueueLock.lock();

        cullQueueUntilCallback();

        mCommandCompletion.signalAll();
        isTaskExecuting = false;
        tryCommand();
        System.out.println("Error Task Response - " + message);
        mQueueLock.unlock();
    }

    @Override
    public void onStatusMessageReceived(final ATekdaqc tekdaqc, final ABoardMessage message) {
        mQueueLock.lock();
        mCommandCompletion.signalAll();
        isTaskExecuting = false;
        tryCommand();

        mQueueLock.unlock();
    }

    @Override
    public void onDebugMessageReceived(final ATekdaqc tekdaqc, final ABoardMessage message) {

    }

    @Override
    public void onCommandDataMessageReceived(final ATekdaqc tekdaqc, final ABoardMessage message) {

    }

    @Override
    public void onAnalogInputDataReceived(final ATekdaqc tekdaqc, final AnalogInputData data) {

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
    protected class CommandWriterRunnable implements Runnable {

        @Override
        public void run() {
            try {
                writeToStream((ABaseQueueVal) mCommandQueue.poll());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * Method to get and write out command bytes to Telnet.
         *
         * @param command The command to be executed
         * @throws Exception Generic exception to catch issues in Telnet.
         */
        private void writeToStream(final ABaseQueueVal command) throws Exception {
            mQueueLock.lock();
            final BufferedOutputStream out = new BufferedOutputStream(mTekdaqc.getOutputStream());
            out.write(command.generateCommandBytes());
            out.flush();
            mCommandCompletion.signalAll();
            mQueueLock.unlock();
        }
    }

}
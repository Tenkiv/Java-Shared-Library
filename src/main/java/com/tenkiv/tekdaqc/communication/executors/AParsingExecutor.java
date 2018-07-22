package com.tenkiv.tekdaqc.communication.executors;

import com.tenkiv.tekdaqc.communication.ascii.executors.ThrowableExecutor;
import com.tenkiv.tekdaqc.communication.message.ABoardMessage;
import com.tenkiv.tekdaqc.hardware.ATekdaqc;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;

/**
 * Abstract Threaded executor for parsing of messages received from a {@link ATekdaqc}.
 *
 * @author Tenkiv (software@tenkiv.com)
 * @since v1.0.0.0
 */
public abstract class AParsingExecutor implements ITekdaqcExecutor {

    /**
     * The name for the threads.
     */
    private static final String PARSING_THREAD_NAME = "TEKDAQC_PARSING_THREAD";

    /**
     * The priority for the threads.
     */
    private static final int PARSING_THREAD_PRIORITY = 4; // Equivilant to Android's Process.THREAD_PRIORITY_BACKGROUND

    /**
     * The executor service.
     */
    protected final ExecutorService mExecutor;

    /**
     * Constructor.
     *
     * @param numThreads int The number of threads to use in the parsing pool.
     */
    public AParsingExecutor(final int numThreads) {
        mExecutor = new ThrowableExecutor(numThreads, new Factory());
    }

    @Override
    public void shutdown() {
        mExecutor.shutdownNow();
    }

    /**
     * Submit a raw message for parsing. When parsing is complete, the specified callback will be called with the result.
     *
     * @param messageData {@code byte}[] The raw message data to parse.
     * @param callback    {@link IParsingListener} The callback to be called when parsing is complete.
     */
    public abstract void parseMessage(final byte[] messageData, IParsingListener callback);

    /**
     * Interface to allow for various objects to request data parsing and receive the results.
     *
     * @since v1.0.0.0
     */
    public interface IParsingListener {

        /**
         * Called when parsing of a message is completed.
         *
         * @param message {@link ABoardMessage} The parsed message.
         */
        void onParsingComplete(final ABoardMessage message);

        void onMessageDetected(final String message);
    }

    /**
     * Parsing task to be submitted to the executor for messages. For each message which is received,
     * a new {@link AParsingTask} is generated and submitted for execution.
     *
     * @since v1.0.0.0
     */
    protected static abstract class AParsingTask implements Callable<Void> {

        /**
         * Callback for parsed message handling.
         */
        protected final IParsingListener mCallback;

        /**
         * Constructor.
         *
         * @param callback {@link IParsingListener} The callback for parsed messages.
         */
        public AParsingTask(final IParsingListener callback) {
            mCallback = callback;
        }
    }

    /**
     * Custom thread factory for this class's internal executor. Ensures that all threads will have the appropriate
     * priority level and name.
     *
     * @since v1.0.0.0
     */
    private static final class Factory implements ThreadFactory {

        @Override
        public Thread newThread(final Runnable r) {
            final Thread thread = new Thread(r);
            thread.setPriority(PARSING_THREAD_PRIORITY);
            thread.setName(PARSING_THREAD_NAME);
            return thread;
        }
    }
}
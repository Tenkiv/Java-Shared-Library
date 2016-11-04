package com.tenkiv.tekdaqc.communication.executors;

import com.tenkiv.tekdaqc.communication.ascii.executors.ThrowableExecutor;
import com.tenkiv.tekdaqc.communication.ascii.message.parsing.ASCIIMessageUtils;
import com.tenkiv.tekdaqc.hardware.ATekdaqc;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Encapsulation of Tekdaqc stream parsing. This class allows for safe, threaded parsing of
 * raw data sent by the Tekdaqc and will break this data into discrete message units to be submitted
 * for more specific parsing.
 *
 * @author Tenkiv (software@tenkiv.com)
 * @since v1.0.0.0
 */
public class ReadExecutor implements ITekdaqcExecutor {

    private static final String TAG = "ReadExecutor";
    private static final String POLLING_THREAD_NAME = "TEKDAQC_POLLING_THREAD";
    private static final int POLLING_THREAD_PRIORITY = 4; // Equivilant to Android's Process.THREAD_PRIORITY_BACKGROUND

    /**
     * The communication session this executor is linked to.
     */
    private final AParsingExecutor.IParsingListener mCallback;
    private final ATekdaqc mTekdaqc;
    private final ExecutorService mExecutor;
    private final Future<Void> mTaskFuture;

    /**
     * Constructor.
     *
     * @param tekdaqc The Tekdaqc this read executor represents.
     *
     * @param callback The callback for detected messages.
     */
    public ReadExecutor(ATekdaqc tekdaqc, AParsingExecutor.IParsingListener callback) {
        if (callback == null) throw new IllegalArgumentException("Callback set cannot be null.");
        mTekdaqc = tekdaqc;
        mCallback = callback;
        mExecutor = new ThrowableExecutor(1);
        // Submit the task immediately
        mTaskFuture = mExecutor.submit(new Task());
    }

    @Override
    public void shutdown() {
        mTaskFuture.cancel(true);
        mExecutor.shutdownNow();
    }

    /**
     * Method to search for messages on {@link InputStream} of a {@link ATekdaqc}.
     *
     * @throws IOException Exception thrown in the case of an unexpected break in communication with the board.
     */
    public void detectMessages() throws IOException {
        final Scanner scan = new Scanner(mTekdaqc.getInputStream()).useDelimiter(ASCIIMessageUtils.RECORD_SEPARATOR_PATTERN);
        while (scan.hasNext()) {
            final String message = scan.next();
            if (message != null && !message.isEmpty()) {
                mCallback.onMessageDetetced(message);
            } else {
                System.err.println("Null or empty message was detected.");
            }
        }
    }

    /**
     * Polling task to be submitted to the executor. This task in theory will run for the entire existence of the
     * {@link ATekdaqc}.
     *
     * @author Jared Woolston (jwoolston@tenkiv.com)
     * @since v1.0.0.0
     */
    private final class Task implements Callable<Void> {

        @Override
        public Void call() throws Exception {
            // This is ok because it will only be called once per thread
            Thread.currentThread().setName(POLLING_THREAD_NAME);
            Thread.currentThread().setPriority(POLLING_THREAD_PRIORITY);
            detectMessages();
            return null;
        }
    }
}

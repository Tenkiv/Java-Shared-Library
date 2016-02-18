package com.tenkiv.tekdaqc.communication.ascii.executors;

import com.tenkiv.tekdaqc.communication.ascii.message.parsing.AASCIIMessage;
import com.tenkiv.tekdaqc.communication.ascii.message.parsing.ASCIIMessageUtils;
import com.tenkiv.tekdaqc.communication.executors.AParsingExecutor;

import java.util.concurrent.Callable;

/**
 * Encapsulation of Tekdaqc message parsing. This class allows for safe, threaded parsing of
 * all messages sent by the Tekdaqc. This class expects messages to have already been divided
 * into discrete units.
 *
 * @author Tenkiv (software@tenkiv.com)
 * @since v1.0.0.0
 */
public class ASCIIParsingExecutor extends AParsingExecutor {

    /**
     * Logging tag
     */
    @SuppressWarnings("unused")
    private static final String TAG = "ASCIIParsingExecutor";

    /**
     * Constructor.
     *
     * @param numThreads int The number of threads to use in the parsing pool.
     */
    public ASCIIParsingExecutor(final int numThreads) {
        super(numThreads);
    }

    /**
     * Submit a message for parsing. When parsing is complete, the specified callback will be called with the result.
     *
     * @param messageData {@link String} The raw message data to parse.
     * @param callback    The callback to be called when parsing is complete.
     */
    public void parseMessage(final String messageData, final IParsingListener callback) {
        mExecutor.submit(new ParsingTask(messageData, callback));
    }

    @Override
    public void parseMessage(final byte[] messageData, final IParsingListener callback) {
        parseMessage(new String(messageData), callback);
    }

    /**
     * Parsing task to be submitted to the executor for ASCII coded messages. For each message which is received,
     * a new {@link ParsingTask} is generated and submitted for execution.
     *
     * @author Tenkiv (software@tenkiv.com)
     * @since v1.0.0.0
     */
    private static final class ParsingTask extends AParsingTask implements Callable<Void> {

        /**
         * The raw message data
         */
        private final String mMessageData;

        /**
         * Constructor.
         *
         * @param messageData {@link String} The raw message data.
         * @param callback    The callback for parsed messages.
         */
        public ParsingTask(final String messageData, final IParsingListener callback) {
            super(callback);
            mMessageData = messageData;
        }

        @Override
        public Void call() throws Exception {
            try {
                final AASCIIMessage message = ASCIIMessageUtils.parseMessage(mMessageData);
                if (message != null) mCallback.onParsingComplete(message);
            } catch (final Exception e) {
                System.out.println("ASCIIParsingExecutor absorbing Exception: ");
                e.printStackTrace();
            }
            return null;
        }
    }
}

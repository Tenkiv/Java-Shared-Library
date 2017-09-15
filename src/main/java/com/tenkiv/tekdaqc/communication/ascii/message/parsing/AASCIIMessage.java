package com.tenkiv.tekdaqc.communication.ascii.message.parsing;

import com.tenkiv.tekdaqc.communication.message.ABoardMessage;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Encapsulates the basics of a ASCII message sent by a Tekdaqc.
 *
 * @author Tenkiv (software@tenkiv.com)
 * @since 1.0.0.0
 */
public abstract class AASCIIMessage extends ABoardMessage {

    protected void setData(final String raw) {
        parse(raw);
    }

    protected void reset() {
        mTimestamp = 0;
    }

    /**
     * Internal method to parse the raw data.
     *
     * @param raw Raw message to be parsed.
     */
    protected abstract void parse(final String raw);

    @Override
    protected void readIn(final ObjectInput input) throws IOException,
            ClassNotFoundException {
        // Do nothing
    }

    @Override
    protected void writeOut(final ObjectOutput output) throws IOException {
        // Do nothing
    }

    @Override
    protected void finalize() throws Throwable {
        // Provided to ensure the message is returned to the pool if use code
        // fails to return it. This is primarily
        // necessary in Android due to its extremely aggressive garbage
        // collection.
        super.finalize();
        /*ASCIIMessageUtils.returnMessage(this);*/
    }
}

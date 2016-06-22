package com.tenkiv.tekdaqc.communication.message;

import com.tenkiv.tekdaqc.communication.ascii.message.parsing.ASCIIMessageUtils.MESSAGE_TYPE;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Represents the basic common features of a message sent from a Tekdaqc (Binary or ASCII).
 *
 * @author Tenkiv (software@tenkiv.com)
 * @since 1.0.0.0
 */
public abstract class ABoardMessage implements Externalizable {


    /**
     * The time the message was parsed
     */
    protected long mTimestamp;

    /**
     * Constructor.
     */
    public ABoardMessage() {
        mTimestamp = System.currentTimeMillis();
    }

    @Override
    public void readExternal(final ObjectInput input) throws IOException, ClassNotFoundException {
        mTimestamp = input.readLong();
        readIn(input);
    }

    @Override
    public void writeExternal(final ObjectOutput output) throws IOException {
        output.writeLong(mTimestamp);
        writeOut(output);
    }

    /**
     * Retrieves the message type.
     *
     * @return {@link MESSAGE_TYPE} The type of message.
     */
    public abstract MESSAGE_TYPE getType();

    /**
     * Called during the de-serialization process to allow subclasses to recover their data from the serialization.
     *
     * @param input {@link ObjectInput} The input stream.
     * @throws IOException            IoException.
     * @throws ClassNotFoundException Class not found to parse.
     */
    protected abstract void readIn(final ObjectInput input) throws IOException, ClassNotFoundException;

    /**
     * Called during the serialization process to allow subclasses to add their data to the serialization.
     *
     * @param output {@link ObjectOutput} The output stream.
     * @throws IOException IoException.
     */
    protected abstract void writeOut(final ObjectOutput output) throws IOException;
}

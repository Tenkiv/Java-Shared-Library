package com.tenkiv.tekdaqc.communication.ascii.message.parsing;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Encapsulates an ASCII formatted debug message from a Tekdaqc.
 *
 * @author Tenkiv (software@tenkiv.com)
 * @since 1.0.0.0
 */
public class ASCIIDebugMessage extends ASCIIReadableMessage {

    /**
     * Constructor.
     */
    public ASCIIDebugMessage() {
        super();
    }

    /**
     * Constructor.
     *
     * @param raw {@link String} The raw message string.
     */
    public ASCIIDebugMessage(final String raw) {
        super();
        parse(raw);
    }


    @Override
    public ASCIIMessageUtils.MESSAGE_TYPE getType() {
        return ASCIIMessageUtils.MESSAGE_TYPE.DEBUG;
    }

    @Override
    public String toString() {
        return "DEBUG MESSAGE (" + mTimestamp + "): " + mMessageString;
    }

    @Override
    protected void readIn(final ObjectInput input) throws IOException, ClassNotFoundException {
        super.readIn(input);
    }

    @Override
    protected void writeOut(final ObjectOutput output) throws IOException {
        super.writeOut(output);
    }
}

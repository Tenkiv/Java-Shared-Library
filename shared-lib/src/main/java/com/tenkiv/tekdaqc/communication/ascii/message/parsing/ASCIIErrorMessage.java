package com.tenkiv.tekdaqc.communication.ascii.message.parsing;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Encapsulates an ASCII formatted error message from a Tekdaqc.
 *
 * @author Tenkiv (software@tenkiv.com)
 * @since 1.0.0.0
 */
public class ASCIIErrorMessage extends ASCIIReadableMessage {

    /**
     * Constructor.
     */
    public ASCIIErrorMessage() {
        super();
    }

    /**
     * Constructor.
     *
     * @param raw {@link String} The raw message string.
     */
    public ASCIIErrorMessage(final String raw) {
        super();
        parse(raw);
    }

    @Override
    public ASCIIMessageUtils.MESSAGE_TYPE getType() {
        return ASCIIMessageUtils.MESSAGE_TYPE.ERROR;
    }

    @Override
    public String toString() {
        return "ERROR MESSAGE (" + mTimestamp + "): " + mMessageString;
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

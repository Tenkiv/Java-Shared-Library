package com.tenkiv.tekdaqc.communication.ascii.message.parsing;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Encapsulates an ASCII coded command data message.
 *
 * @author Tenkiv (software@tenkiv.com)
 * @since v1.0.0.0
 */
public class ASCIICommandMessage extends ASCIIReadableMessage {

    /**
     * Constructor.
     */
    public ASCIICommandMessage() {
        super();
    }

    /**
     * Constructor.
     *
     * @param raw {@link String} The raw message string.
     */
    public ASCIICommandMessage(final String raw) {
        super();
        parse(raw);
    }

    @Override
    public ASCIIMessageUtils.MESSAGE_TYPE getType() {
        return ASCIIMessageUtils.MESSAGE_TYPE.COMMAND_DATA;
    }

    @Override
    public String toString() {
        return "COMMAND MESSAGE (" + mTimestamp + "): " + mMessageString;
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

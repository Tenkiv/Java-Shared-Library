package com.tenkiv.tekdaqc.communication.ascii.message.parsing;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Abstract class defining the values of a Tekdaqc Message which is deigned to be read by the end user.
 *
 * @author Tenkiv (software@tenkiv.com)
 * @since v2.0.0.0
 */
public abstract class ASCIIReadableMessage extends AASCIIMessage {

    /**
     * The message.
     */
    protected String mMessageString;

    @Override
    protected void reset() {
        super.reset();
        mMessageString = null;
    }

    @Override
    protected void parse(final String raw) {
        int start = raw.indexOf(ASCIIMessageUtils.MESSAGE_TAG); // Determine where the message tag
        if (start >= 0) { // If the tag exists
            start += ASCIIMessageUtils.MESSAGE_TAG.length(); // Add the length of the message tag
            // Find the position of the end of the message body
            final int end = raw.indexOf(ASCIIMessageUtils.NEW_LINE_CHAR, start);
            // Extract the message body
            mMessageString = (end >= 0) ? raw.substring(start, end) : raw
                    .substring(start);
        } else {
            // The tag does not exist, return null.
            mMessageString = null;
        }
    }

    @Override
    public void readExternal(final ObjectInput input) throws IOException, ClassNotFoundException {
        super.readExternal(input);
        mMessageString = (String) input.readObject();
    }

    @Override
    public void writeExternal(final ObjectOutput output) throws IOException {
        super.writeExternal(output);
        output.writeObject(mMessageString);
    }
}

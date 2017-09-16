package com.tenkiv.tekdaqc.communication.ascii.message.parsing;

import com.tenkiv.tekdaqc.utility.DigitalOutputUtilities;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Encapsulates an ASCII coded digital output data message. These messages contain a arbitrary length list of data.
 *
 * @author Tenkiv (software@tenkiv.com)
 * @since v1.0.0.0
 */
public class ASCIIDigitalOutputDataMessage extends AASCIIMessage {

    protected boolean[] mDigitalOutputArray;

    /**
     * Constructor.
     */
    public ASCIIDigitalOutputDataMessage() {
        super();
    }

    /**
     * Constructor.
     *
     * @param raw {@link String} The raw message string.
     */
    public ASCIIDigitalOutputDataMessage(final String raw) {
        super();
        parse(raw);
    }

    public ASCIIDigitalOutputDataMessage(final boolean[] digitalOutputState) {
        super();
        mDigitalOutputArray = digitalOutputState;
    }

    public boolean[] getDigitalOutputArray() {
        return mDigitalOutputArray;
    }

    @Override
    protected void reset() {
        mDigitalOutputArray = null;
    }

    @Override
    protected void parse(final String raw) throws NumberFormatException {
        int start = raw.indexOf(ASCIIMessageUtils.V1_DIGITAL_OUTPUT_HEADER); // Determine where the message tag is
        if (start < 0) {
            // The tag does not exist
            System.err.println("Digital output header not found. Returning null.");
            return;
        }
        // Extract the name
        start = raw.indexOf(ASCIIMessageUtils.VALUE_TAG, start);
        start += ASCIIMessageUtils.VALUE_TAG.length(); // Add the length of the message tag
        // Find the position of the end of the message text
        int end = raw.indexOf(ASCIIMessageUtils.NEW_LINE_CHAR, start);

        // Extract the message text
        String hex = raw.substring(start, end);
        String binAddr = DigitalOutputUtilities.hexToBinary(hex);
        mDigitalOutputArray = new boolean[16];
        for (int i = 0; i < binAddr.length(); i++) {
            mDigitalOutputArray[i] = binAddr.charAt(i) == '1';
        }
    }

    @Override
    public String toString() {
        return "DIGITAL OUTPUT DATA MESSAGE (" + mTimestamp + "): " + DigitalOutputUtilities.boolArrayConversion(mDigitalOutputArray);
    }

    @Override
    public ASCIIMessageUtils.MESSAGE_TYPE getType() {
        return ASCIIMessageUtils.MESSAGE_TYPE.DIGITAL_OUTPUT_DATA;
    }

    @Override
    protected void readIn(final ObjectInput input) throws IOException, ClassNotFoundException {
        super.readIn(input);
        mDigitalOutputArray = (boolean[]) input.readObject();
    }

    @Override
    protected void writeOut(final ObjectOutput output) throws IOException {
        super.writeOut(output);
        output.writeObject(mDigitalOutputArray);
    }
}

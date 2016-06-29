package com.tenkiv.tekdaqc.communication.ascii.message.parsing;

import com.tenkiv.tekdaqc.communication.data_points.DataPoint;
import com.tenkiv.tekdaqc.communication.data_points.DigitalInputData;
import com.tenkiv.tekdaqc.communication.data_points.IDataPointFactory;
import com.tenkiv.tekdaqc.utility.DigitalState;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Encapsulates an ASCII coded digital input data message. These messages contain a arbitrary length list of data.
 *
 * @author Tenkiv (software@tenkiv.com)
 * @since v1.0.0.0
 */
public class ASCIIDigitalInputDataMessage extends AASCIIMessage implements IDataPointFactory {

    protected String mName;

    protected int mNumber;

    protected String mTimestamps;

    protected DigitalState mReadings;

    /**
     * Constructor.
     */
    public ASCIIDigitalInputDataMessage() {
        super();
    }

    /**
     * Constructor.
     *
     * @param raw {@link String} The raw message string.
     */
    public ASCIIDigitalInputDataMessage(final String raw) {
        super();
        parse(raw);
    }

    @Override
    protected void reset() {
        super.reset();
        mName = null;
        mNumber = 0;
        mTimestamps = null;
        mReadings = null;
    }

    @Override
    protected void parse(final String raw) throws NumberFormatException {

        if (raw != null && raw.contains(ASCIIMessageUtils.V2_DIGITAL_INPUT_HEADER)) {
            parseMessage(raw);
        } else {
            try {
                throw new Exception("Please Update Your Tekdaqc Firmware or use an Older Version of the Tekdaqc Java Library");
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void parseMessage(final String raw) {
        int start = raw.indexOf(ASCIIMessageUtils.V2_DIGITAL_INPUT_HEADER); // Determine where the message tag is
        if (start < 0) {
            // The tag does not exist
            System.err.println("Analog input header not found. Bad Raw is: \n" + raw);
            return;
        }

        start += ASCIIMessageUtils.V2_DIGITAL_INPUT_HEADER.length();
        int end = raw.indexOf(ASCIIMessageUtils.NEW_LINE_CHAR, start) - 1;

        mNumber = Integer.parseInt(raw.substring(start, end));
        start = raw.indexOf(ASCIIMessageUtils.NEW_LINE_CHAR, end) + 1;
        end = raw.indexOf(",", start);

        mTimestamps = raw.substring(start, end);
        start = raw.indexOf(",", end) + 1;
        end = raw.length();

        final String state = (raw.substring(start, end));
        if (state.contains(ASCIIMessageUtils.HIGH_MARKER)) {
            mReadings = DigitalState.LOGIC_HIGH;
        } else {
            mReadings = DigitalState.LOGIC_LOW;
        }
    }

    @Override
    public String toString() {
        return "DIGITAL DATA MESSAGE (" + mTimestamp + "): Physical Input:" + mNumber + " Count:" + mReadings;
    }

    @Override
    public ASCIIMessageUtils.MESSAGE_TYPE getType() {
        return ASCIIMessageUtils.MESSAGE_TYPE.DIGITAL_INPUT_DATA;
    }

    @Override
    public DataPoint toDataPoints() {
        return new DigitalInputData(mNumber, mName, Long.parseLong(mTimestamps.replaceAll("\\s", "")), mReadings);
    }

    @Override
    protected void readIn(final ObjectInput input) throws IOException, ClassNotFoundException {
        super.readIn(input);
        mName = (String) input.readObject();
        mNumber = input.readInt();
        mTimestamps = (String) input.readObject();
        mReadings = (DigitalState) input.readObject();
    }

    @Override
    protected void writeOut(final ObjectOutput output) throws IOException {
        super.writeOut(output);
        output.writeObject(mName);
        output.writeInt(mNumber);
        output.writeObject(mTimestamps);
        output.writeObject(mReadings);

    }
}

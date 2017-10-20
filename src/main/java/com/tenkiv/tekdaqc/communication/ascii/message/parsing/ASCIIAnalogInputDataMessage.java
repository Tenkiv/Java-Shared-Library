package com.tenkiv.tekdaqc.communication.ascii.message.parsing;

import com.tenkiv.tekdaqc.communication.data_points.AnalogInputCountData;
import com.tenkiv.tekdaqc.communication.data_points.DataPoint;
import com.tenkiv.tekdaqc.communication.data_points.IDataPointFactory;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Encapsulates an ASCII coded analog input data message. These messages contain a arbitrary length list of data.
 *
 * @author Tenkiv (software@tenkiv.com)
 * @since v1.0.0.0
 */
public class ASCIIAnalogInputDataMessage extends AASCIIMessage implements IDataPointFactory {

    protected String mName;

    protected int mNumber;

    protected String mTimestamps;

    protected int mReadings;

    /**
     * Constructor.
     */
    public ASCIIAnalogInputDataMessage() {
        super();
    }

    /**
     * Constructor.
     *
     * @param raw {@link String} The raw message string.
     */
    public ASCIIAnalogInputDataMessage(final String raw) {
        super();
        parse(raw);
    }

    @Override
    protected void reset() {
        super.reset();
        mNumber = 0;
        mTimestamps = null;
        mReadings = 0;
    }

    @Override
    protected void parse(final String raw) throws NumberFormatException {

        if (raw != null && raw.contains(ASCIIMessageUtils.V2_ANALOG_INPUT_HEADER)) {
            parseMessage(raw);
        } else if(raw != null && raw.contains(ASCIIMessageUtils.V1_ANALOG_INPUT_HEADER)){
            throw new IllegalArgumentException("Please Update Your Tekdaqc Firmware or use an " +
                    "Older Version of the Tekdaqc Java Library");
        } else {
            throw new IllegalArgumentException("String does not contain required header for parsing");
        }
    }


    private void parseMessage(final String raw) {
        int start = raw.indexOf(ASCIIMessageUtils.V2_ANALOG_INPUT_HEADER); // Determine where the message tag is
        if (start < 0) {
            // The tag does not exist
            System.err.println("Analog input header not found. Bad Raw is: \n" + raw);
            return;
        }

        start += ASCIIMessageUtils.V2_ANALOG_INPUT_HEADER.length();
        int end = raw.indexOf(ASCIIMessageUtils.NEW_LINE_CHAR, start) - 1;

        mNumber = Integer.parseInt(raw.substring(start, end));
        start = raw.indexOf(ASCIIMessageUtils.NEW_LINE_CHAR, end) + 1;
        end = raw.indexOf(",", start);

        mTimestamps = raw.substring(start, end);
        start = raw.indexOf(",", end) + 1;
        end = raw.length();

        mReadings = Integer.parseInt(raw.substring(start, end));


    }

    @Override
    public String toString() {
        return "ANALOG DATA MESSAGE (" + mTimestamp + "): Physical Input:" + mNumber + " Count:" + mReadings;
    }

    @Override
    public ASCIIMessageUtils.MESSAGE_TYPE getType() {
        return ASCIIMessageUtils.MESSAGE_TYPE.ANALOG_INPUT_DATA;
    }

    @Override
    public DataPoint toDataPoints() {
        return new AnalogInputCountData(mNumber, mName, Long.parseLong(mTimestamps.replaceAll("\\s", "")), mReadings);
    }

    @Override
    protected void readIn(final ObjectInput input) throws IOException, ClassNotFoundException {
        super.readIn(input);
        mNumber = input.readInt();
        mTimestamps = (String) input.readObject();
        mReadings = input.readInt();
    }

    @Override
    protected void writeOut(final ObjectOutput output) throws IOException {
        super.writeOut(output);
        output.writeInt(mNumber);
        output.writeObject(mTimestamps);
        output.writeInt(mReadings);
    }
}

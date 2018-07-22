package com.tenkiv.tekdaqc.communication.ascii.message.parsing;

import com.tenkiv.tekdaqc.communication.data_points.DataPoint;
import com.tenkiv.tekdaqc.communication.data_points.PWMInputData;
import com.tenkiv.tekdaqc.communication.data_points.IDataPointFactory;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Created by tenkiv on 5/5/17.
 */
public class ASCIIPWMInputDataMessage extends AASCIIMessage implements IDataPointFactory {

    protected String mName;

    protected int mNumber;

    protected int mTotalTransitions;

    protected double mOnPercentage;

    protected String mTimestamp;

    /**
     * Constructor.
     */
    public ASCIIPWMInputDataMessage() {
        super();
    }

    /**
     * Constructor.
     *
     * @param raw {@link String} The raw message string.
     */
    public ASCIIPWMInputDataMessage(final String raw) {
        super();
        parse(raw);
    }

    @Override
    protected void reset() {
        super.reset();
        mName = null;
        mNumber = 0;
        mTimestamp = null;
    }

    @Override
    protected void parse(final String raw) throws NumberFormatException {
        if (raw != null && raw.contains(ASCIIMessageUtils.DIGITAL_PWM_INPUT_HEADER)) {
            parseMessage(raw);
        }
    }

    private void parseMessage(final String raw) {
        int start = raw.indexOf(ASCIIMessageUtils.DIGITAL_PWM_INPUT_HEADER); // Determine where the message tag is
        if (start < 0) {
            // The tag does not exist
            System.err.println("Analog input header not found. Bad Raw is: \n" + raw);
            return;
        }
        start += ASCIIMessageUtils.DIGITAL_PWM_INPUT_HEADER.length();
        int end = raw.indexOf("\r", start);
        mNumber = Integer.parseInt(raw.substring(start, end));

        start = raw.indexOf(ASCIIMessageUtils.NEW_LINE_CHAR, end) + 1;
        end = raw.indexOf(",", start);
        mTotalTransitions = Integer.parseInt(raw.substring(start, end));

        start = raw.indexOf(",", end) + 1;
        end = raw.indexOf(ASCIIMessageUtils.NEW_LINE_CHAR, start);
        mOnPercentage = Double.parseDouble(raw.substring(start,end));

        start = raw.indexOf(ASCIIMessageUtils.NEW_LINE_CHAR, end) + 1;
        end = raw.length();
        mTimestamp = raw.substring(start, end);
    }

    @Override
    public String toString() {
        return "DIGITAL PWM DATA MESSAGE (" + mTimestamp + "):" +
                " Physical Input:" + mNumber + " Percentage:" + mOnPercentage;
    }

    @Override
    public ASCIIMessageUtils.MESSAGE_TYPE getType() {
        return ASCIIMessageUtils.MESSAGE_TYPE.PWM_INPUT_DATA;
    }

    @Override
    public DataPoint toDataPoints() {
        return new PWMInputData(mNumber,
                mName,
                Long.parseLong(mTimestamp.replaceAll("\\s", "")),
                mOnPercentage,
                mTotalTransitions);
    }

    @Override
    protected void readIn(final ObjectInput input) throws IOException, ClassNotFoundException {
        super.readIn(input);
        mName = (String) input.readObject();
        mNumber = input.readInt();
        mTimestamp = (String) input.readObject();
        mOnPercentage = input.readDouble();
        mTotalTransitions = input.readInt();
    }

    @Override
    protected void writeOut(final ObjectOutput output) throws IOException {
        super.writeOut(output);
        output.writeObject(mName);
        output.writeInt(mNumber);
        output.writeObject(mTimestamp);
        output.writeDouble(mOnPercentage);
        output.writeInt(mTotalTransitions);
    }
}

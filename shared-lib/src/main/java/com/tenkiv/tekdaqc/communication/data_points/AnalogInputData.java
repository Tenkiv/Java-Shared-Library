package com.tenkiv.tekdaqc.communication.data_points;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.math.BigInteger;

/**
 * Encapsulation of an Analog Input data point.
 *
 * @author Tenkiv (software@tenkiv.com)
 * @since v1.0.0.0
 */
public class AnalogInputData extends DataPoint {

    protected int mData;

    /**
     * Provided for externalization. User code should not use this constructor.
     */
    public AnalogInputData() {
        // Do nothing
    }

    /**
     * Constructs an {@link AnalogInputData} point from the provided parameters.
     *
     * @param channel   int The physical channel number.
     * @param name      {@link String} The channel name. Can be null.
     * @param timestamp {@link BigInteger} The timestamp of the sample.
     * @param data      int The sample data, in ADC counts.
     */
    public AnalogInputData(final int channel, final String name, final long timestamp, final int data) {
        super(name, channel, timestamp);
        mData = data;
    }

    /**
     * Retrieve the data for this data point.
     *
     * @return int The data value.
     */
    public int getData() {
        return mData;
    }

    @Override
    public DATA_TYPE getType() {
        return DATA_TYPE.ANALOG_INPUT;
    }

    @Override
    public String toString() {
        return ("Analog Input -\n\r\tPhysical Input: "
                + getPhysicalInput()
                + "\n\r\tName: "
                + getName()
                + "\n\r\tData: " + mData);
    }

    @Override
    protected void readIn(final ObjectInput input) throws IOException, ClassNotFoundException {
        mData = input.readInt();
    }

    @Override
    protected void writeOut(final ObjectOutput output) throws IOException {
        output.writeInt(mData);
    }
}
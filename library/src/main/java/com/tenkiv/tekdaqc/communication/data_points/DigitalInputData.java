package com.tenkiv.tekdaqc.communication.data_points;

import com.tenkiv.tekdaqc.utility.DigitalState;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.math.BigInteger;

/**
 * Encapsulation of a Digital Input data point.
 *
 * @author Tenkiv (software@tenkiv.com)
 * @since v1.0.0.0
 */
public class DigitalInputData extends DataPoint {

    /**
     * The {@link DigitalState} of this data point.
     */
    protected DigitalState mState;

    /**
     * Provided for externalization. User code should not use this constructor.
     */
    public DigitalInputData() {
        // Do nothing
    }

    /**
     * Constructs an {@link DigitalInputData} point from the provided parameters.
     *
     * @param channel   int The physical channel number.
     * @param name      {@link String} The channel name. Can be null.
     * @param timestamp {@link BigInteger} The timestamp of the sample.
     * @param state     {@link DigitalState} The state of this data point.
     */
    public DigitalInputData(final int channel, final String name, final long timestamp, final DigitalState state) {
        super(name, channel, timestamp);
        mState = state;
    }

    /**
     * Retrieve the data for this data point.
     *
     * @return {@link DigitalState} The data value.
     */
    public DigitalState getState() {
        return mState;
    }

    @Override
    public DATA_TYPE getType() {
        return DATA_TYPE.DIGITAL_INPUT;
    }


    @Override
    protected void readIn(final ObjectInput input) throws IOException, ClassNotFoundException {
        mState = (DigitalState) input.readObject();
    }

    @Override
    protected void writeOut(final ObjectOutput output) throws IOException {
        output.writeObject(mState);
    }
}

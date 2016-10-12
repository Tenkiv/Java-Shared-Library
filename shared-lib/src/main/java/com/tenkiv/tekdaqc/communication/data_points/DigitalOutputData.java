package com.tenkiv.tekdaqc.communication.data_points;

import com.tenkiv.tekdaqc.communication.ascii.message.parsing.ASCIIMessageUtils;
import com.tenkiv.tekdaqc.communication.message.ABoardMessage;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.math.BigInteger;

/**
 * Encapsulation of a Digital Output data point.
 *
 * @author Tenkiv (software@tenkiv.com)
 * @since v1.0.0.0
 */
public class DigitalOutputData extends ABoardMessage {

    /**
     * The {@link boolean} of this data point.
     */
    private boolean[] mDigitalState;

    /**
     * Provided for externalization. User code should not use this constructor.
     */
    public DigitalOutputData() {
        // Do nothing
    }

    /**
     * Constructs an {@link DigitalOutputData} point from the provided parameters.
     *
     * @param timestamp {@link BigInteger} The timestamp of the sample.
     * @param state     {@link boolean} The state of this data point.
     */
    public DigitalOutputData(final long timestamp, final boolean[] state) {
        mDigitalState = state;
    }

    public boolean[] getDigitalState() {
        return mDigitalState;
    }

    /**
     * Retrieve the data for this data point.
     *
     * @return {@link boolean} The data value.
     */
    public boolean[] getState() {
        return mDigitalState;
    }

    @Override
    public ASCIIMessageUtils.MESSAGE_TYPE getType() {
        return ASCIIMessageUtils.MESSAGE_TYPE.DIGITAL_OUTPUT_DATA;
    }

    @Override
    protected void readIn(final ObjectInput input) throws IOException, ClassNotFoundException {
        mDigitalState = (boolean[]) input.readObject();
    }

    @Override
    protected void writeOut(final ObjectOutput output) throws IOException {
        output.writeObject(mDigitalState);
    }
}

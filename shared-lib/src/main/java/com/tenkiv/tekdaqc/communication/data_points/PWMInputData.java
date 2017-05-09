package com.tenkiv.tekdaqc.communication.data_points;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Encapsulation of a PWM Digital Input data point.
 *
 * @author Tenkiv (software@tenkiv.com)
 * @since v1.0.0.0
 */
public class PWMInputData extends DataPoint {


    protected int mTotalTransitions;

    protected double mPercetageOn;

    /**
     * Provided for externalization. User code should not use this constructor.
     */
    public PWMInputData() {
        // Do nothing
    }


    public PWMInputData(final int channel, final String name, final long timestamp, final double percetageOn, final int totalCount) {
        super(name, channel, timestamp);
        mPercetageOn = percetageOn;
        mTotalTransitions = totalCount;
    }

    /**
     * Gets the total number of transitions between High and Low state
     *
     * @return Number of transitions
     */
    public int getTotalTransitions() {
        return mTotalTransitions;
    }

    /**
     * Gets the total number of transitions between High and Low state
     *
     * @return Number of transitions
     */
    public double getPercetageOn() {
        return mPercetageOn;
    }

    @Override
    public DATA_TYPE getType() {
        return DATA_TYPE.PWM_INPUT;
    }


    @Override
    protected void readIn(final ObjectInput input) throws IOException, ClassNotFoundException {
        mPercetageOn =  input.readDouble();
        mTotalTransitions = input.readInt();
    }

    @Override
    protected void writeOut(final ObjectOutput output) throws IOException {
        output.writeObject(mPercetageOn);
        output.writeInt(mTotalTransitions);
    }
}

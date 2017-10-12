package com.tenkiv.tekdaqc.communication.data_points;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.math.BigInteger;

/**
 * Base abstract class for all data points.
 *
 * @author Tenkiv (software@tenkiv.com)
 * @since v1.0.0.0
 */
public abstract class DataPoint implements Externalizable {

    /**
     * The name of the channel this data came from. Can be null.
     */
    protected String mName;
    /**
     * The physical channel number this data came from.
     */
    protected int mPhysicalChannel;
    /**
     * The timestamp of this data point.
     */
    protected long mTimeStamp;

    /**
     * Provided for externalization. User code should not use this constructor.
     */
    public DataPoint() {
        // Do nothing
    }

    /**
     * Constructs a new base data point. Subclasses should be sure to call this to
     * ensure proper construction of the data point.
     *
     * @param name      {@link String} The name of the channel for this data. Can be null.
     * @param channel   int The physical channel for this data.
     * @param timestamp {@link BigInteger} The timestamp of this data point. Can be null.
     */
    protected DataPoint(final String name, int channel, final long timestamp) {
        mName = name;
        mPhysicalChannel = channel;
        mTimeStamp = timestamp;
    }

    /**
     * Retrieve the channel name for this data point.
     *
     * @return {@link String} The name value.
     */
    public String getName() {
        return mName;
    }

    /**
     * Retrieve the physical channel for this data point.
     *
     * @return int The channel index value.
     */
    public int getPhysicalInput() {
        return mPhysicalChannel;
    }

    /**
     * Retrieve the timestamp for this data point.
     *
     * @return {@link BigInteger} The timestamp value.
     */
    public long getTimestamp() {
        return mTimeStamp;
    }

    @Override
    public void readExternal(final ObjectInput input) throws IOException, ClassNotFoundException {
        mName = (String) input.readObject();
        mPhysicalChannel = input.readInt();
        mTimeStamp = input.readLong();
        readIn(input);
    }

    @Override
    public void writeExternal(final ObjectOutput output) throws IOException {
        output.writeObject(mName);
        output.writeInt(mPhysicalChannel);
        output.writeLong(mTimeStamp);
        writeOut(output);
    }

    /**
     * Retrieve the type of data point.
     *
     * @return {@link DATA_TYPE} The type of data this data point corresponds to.
     */
    public abstract DATA_TYPE getType();

    /**
     * Provided to allow subclasses to de-serialize information.
     *
     * @param input {@link ObjectInput} The input stream for de-serializing the object.
     * @throws IOException            IoException.
     * @throws ClassNotFoundException Class not found to parse.
     */
    protected abstract void readIn(final ObjectInput input) throws IOException, ClassNotFoundException;

    /**
     * Provided to allow subclases to serialize information.
     *
     * @param output {@link ObjectOutput} The output stream for serializing the object.
     * @throws IOException IoException.
     */
    protected abstract void writeOut(final ObjectOutput output) throws IOException;

    /**
     * Set of possible data point types.
     *
     * @author Tenkiv (software@tenkiv.com)
     * @since v1.0.0.0
     */
    public enum DATA_TYPE {
        ANALOG_INPUT, ANALOG_OUTPUT, DIGITAL_INPUT, DIGITAL_OUTPUT, PWM_INPUT
    }
}

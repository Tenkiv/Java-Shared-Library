package com.tenkiv.tekdaqc.hardware;

import com.tenkiv.tekdaqc.communication.message.ICountListener;
import com.tenkiv.tekdaqc.communication.message.IVoltageListener;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Abstract container class for all data/settings of an analog input on the Tekdaqc.
 *
 * @author Tenkiv (software@tenkiv.com)
 * @since 1.0.0.0
 */
public abstract class AAnalogInput extends IInputOutputHardware {

    private static final long serialVersionUID = 1L;

    /**
     * The name of the input channel. Must be 24 characters or less.
     */
    protected volatile String mName = null;

    /**
     * Channel sampling parameters.
     * Subclasses should define defaults for these members
     */
    protected volatile Gain mGain = Gain.X4;
    protected volatile Rate mRate = Rate.SPS_10;
    protected volatile SensorCurrent mCurrent;

    /**
     * Constructor
     */
    AAnalogInput(final ATekdaqc tekdaqc, final int inputNumber) {
        super(tekdaqc, inputNumber);
    }

    @Override
    protected void queueStatusChange() {
        if (getTekdaqc().isConnected() && isActivated) {
            getTekdaqc().queueCommand(CommandBuilder.removeAnalogInputByNumber(mChannelNumber));
            getTekdaqc().queueCommand(CommandBuilder.addAnalogInput(this));
        }
    }

    /**
     * Retrieves the currently set input name value.
     *
     * @return {@link String} The current input name value.
     */
    public String getName() {
        return mName;
    }

    /**
     * Sets the current input name for this input.
     *
     * @param name {@link String} The input name to set.
     * @return {@link AAnalogInput} This input to facilitate chaining.
     * @throws IllegalArgumentException Name must not exceed maximum character length..
     */
    public AAnalogInput setName(final String name) throws IllegalArgumentException {
        if (name.length() >= ATekdaqc.MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("The maximum length of a name is " + ATekdaqc.MAX_NAME_LENGTH + " characters.");
        } else {
            mName = name;
        }
        return this;
    }

    /**
     * Retrieves the currently set input gain setting.
     *
     * @return {@link Gain} The current input gain setting.
     */
    public Gain getGain() {
        return mGain;
    }

    /**
     * Sets the current gain setting for this input.
     *
     * @param gain {@link Gain} The gain setting to set.
     * @return {@link AAnalogInput} This input to facilitate chaining.
     * @throws IllegalArgumentException Gain must be valid for analog input type.
     */
    public AAnalogInput setGain(final Gain gain) throws IllegalArgumentException {
        if (!isValidGain(gain)) {
            throw new IllegalArgumentException("The specified gain: " + gain.name()
                    + " is not valid for inputs of type " + this.getClass().getCanonicalName() + ".");
        }
        mGain = gain;
        queueStatusChange();
        return this;
    }

    /**
     * Retrieves the currently set input sample rate setting.
     *
     * @return {@link Rate} The current input rate setting.
     */
    public Rate getRate() {
        return mRate;
    }

    /**
     * Sets the current rate setting for this input.
     *
     * @param rate {@link Rate} The rate setting to set.
     * @return {@link AAnalogInput} This input to facilitate chaining.
     * @throws IllegalArgumentException Rate must be valid for analog input type.
     */
    public AAnalogInput setRate(final Rate rate) throws IllegalArgumentException {
        if (!isValidRate(rate)) {
            throw new IllegalArgumentException("The specified rate: " + rate.toString()
                    + " is not valid for inputs of type " + this.getClass().getCanonicalName() + ".");
        }
        mRate = rate;
        queueStatusChange();
        return this;
    }

    /**
     * Retrieves the currently set input sensor current setting.
     *
     * @return {@link SensorCurrent} The current input sensor current setting.
     */
    public SensorCurrent getSensorCurrent() {
        return mCurrent;
    }

    /**
     * Sets the current sensor current setting for this input.
     *
     * @param current {@link SensorCurrent} The sensor current setting to set.
     * @return {@link AAnalogInput} This input to facilitate chaining.
     * @throws IllegalArgumentException Sensor Current name must be valid for analog input type.
     */
    public AAnalogInput setSensorCurrent(final SensorCurrent current) throws IllegalArgumentException {
        if (!isValidSensorCurrent(current)) {
            throw new IllegalArgumentException("The specified sensor drive current: " + current.name()
                    + " is not valid for inputs of type " + this.getClass().getCanonicalName() + ".");
        }
        return this;
    }

    @Override
    public void activate() {
        if (getTekdaqc().isConnected()) {
            isActivated = true;
            getTekdaqc().queueCommand(CommandBuilder.addAnalogInput(this));
        } else {
            throw new IllegalStateException(TEKDAQC_NOT_CONNECTED_EXCEPTION_TEXT);
        }
    }

    @Override
    public void deactivate() {
        if (getTekdaqc().isConnected()) {
            isActivated = false;
            getTekdaqc().queueCommand(CommandBuilder.removeAnalogInput(this));
        } else {
            throw new IllegalStateException(TEKDAQC_NOT_CONNECTED_EXCEPTION_TEXT);
        }
    }

    /**
     * Method to add a {@link ICountListener} to listen for data on only this channel.
     *
     * @param listener The {@link ICountListener} to add for callbacks.
     */
    public void addCountListener(ICountListener listener) {
        getTekdaqc().addAnalogCountListener(listener,this);
    }

    /**
     * Method to add a {@link IVoltageListener} to listen for data on only this channel.
     *
     * @param listener The {@link IVoltageListener} to add for callbacks.
     */
    public void addVoltageListener(IVoltageListener listener) {
        getTekdaqc().addAnalogVoltageListener(listener,this);
    }

    /**
     * Subclasses must implement to serialize their additional data.
     *
     * @param output {@link ObjectOutput} The output stream for serialization.
     * @throws IOException IoException.
     */
    protected abstract void writeOut(final ObjectOutput output) throws IOException;

    /**
     * Subclasses must implement to de-serialize their additional data.
     *
     * @param input {@link ObjectInput} The input stream for serialization.
     * @throws IOException            IoException.
     * @throws ClassNotFoundException Class not found to parse.
     */
    protected abstract void readIn(final ObjectInput input) throws IOException, ClassNotFoundException;

    /**
     * Checks if the provided {@link Gain} is valid for this input.
     *
     * @param gain {@link Gain} The gain setting to check.
     * @return {@code boolean} True if the setting is valid.
     */
    protected abstract boolean isValidGain(final Gain gain);

    /**
     * Checks if the provided {@link Rate} is valid for this input.
     *
     * @param rate {@link Rate} The rate setting to check.
     * @return {@code boolean} True if the setting is valid.
     */
    protected abstract boolean isValidRate(final Rate rate);

    /**
     * Checks if the provided {@link SensorCurrent} is valid for this input.
     *
     * @param current {@link SensorCurrent} The sensor current setting to check.
     * @return {@code boolean} True if the setting is valid.
     */
    protected abstract boolean isValidSensorCurrent(final SensorCurrent current);

    /**
     * Set of possible input gain settings.
     *
     * @author Jared Woolston (jwoolston@tenkiv.com)
     * @since v1.0.0.0
     */
    public enum Gain {
        X1(1), X2(2), X4(4), X8(8), X16(16), X32(32), X64(64);

        private static Gain[] mValueArray = Gain.values();
        public final int gain;

        Gain(final int gain){this.gain = gain;}

        Gain(final String gain) {
            this.gain = Integer.valueOf(gain);
        }

        public static Gain getValueFromOrdinal(final byte ordinal) {
            return mValueArray[ordinal];
        }

        public static Gain fromString(final String gain) {
            for (Gain g : values()) {

                if (g.toString().equals(gain)) {
                    return g;
                }
            }
            return null;
        }

        public static Gain fromInt(final int gain) {
            for (Gain g : values()) {
                if (g.gain == gain) {
                    return g;
                }
            }
            return null;
        }

        @Override
        public String toString() {
            return String.valueOf(gain);
        }
    }

    /**
     * Set of possible input sample rate settings.
     *
     * @author Jared Woolston (jwoolston@tenkiv.com)
     * @since v1.0.0.0
     */
    public enum Rate {
        SPS_30000("30000"), SPS_15000("15000"), SPS_7500("7500"), SPS_3750("3750"), SPS_2000("2000"), SPS_1000("1000"), SPS_500(
                "500"), SPS_100("100"), SPS_60("60"), SPS_50("50"), SPS_30("30"), SPS_25("25"), SPS_15("15"), SPS_10(
                "10"), SPS_5("5"), SPS_2_5("2.5");

        private static Rate[] mValueArray = Rate.values();
        public final String rate;

        Rate(final String rate) {
            this.rate = rate;
        }

        public static Rate getValueFromOrdinal(final byte ordinal) {
            return mValueArray[ordinal];
        }

        public static Rate fromString(final String rate) {
            for (final Rate r : values()) {
                if (r.rate.equals(rate)) {
                    return r;
                }
            }
            return null;
        }

        @Override
        public String toString() {
            return rate;
        }
    }

    /**
     * Set of possible input drive current settings.
     *
     * @author Jared Woolston (jwoolston@tenkiv.com)
     * @since v1.0.0.0
     */
    public enum SensorCurrent {
        _10uA("10"), _2uA("2"), _0_5uA("0.5"), OFF("0");

        private static SensorCurrent[] mValueArray = SensorCurrent.values();
        public final String mCurrent;

        SensorCurrent(final String current) {
            mCurrent = current;
        }

        public static SensorCurrent getValueFromOrdinal(final byte ordinal) {
            return mValueArray[ordinal];
        }

        @Override
        public String toString() {
            return mCurrent;
        }
    }
}

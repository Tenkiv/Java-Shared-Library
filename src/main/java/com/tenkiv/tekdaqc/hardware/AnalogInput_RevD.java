package com.tenkiv.tekdaqc.hardware;

import com.tenkiv.tekdaqc.utility.ChannelType;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Container class for all data/settings of an analog input on the Tekdaqc using the ADS1256 ADC.
 *
 * @author Tenkiv (software@tenkiv.com)
 * @since 1.0.0.0
 */
public class AnalogInput_RevD extends AAnalogInput {

    private static final long serialVersionUID = 1L;
    /**
     * The input buffer setting.
     */
    private BufferState mBuffer = BufferState.ENABLED;

    @Override
    public ChannelType getChannelType() {
        return ChannelType.ANALOG_INPUT;
    }

    /**
     * Constructor. Sets default settings.
     */
    AnalogInput_RevD(final ATekdaqc tekdaqc, final int inputNumber) {
        super(tekdaqc, inputNumber);

        mGain = Gain.X1;
        mRate = Rate.SPS_2_5;
        mCurrent = SensorCurrent.OFF;
        mBuffer = BufferState.ENABLED;
    }

    /**
     * Retrieves the currently set {@link BufferState} value.
     *
     * @return {@link BufferState} The current buffer state.
     */
    public BufferState getBufferState() {
        return mBuffer;
    }

    /**
     * Sets the input buffer setting.
     *
     * @param state {@link BufferState} The buffer setting.
     * @return {@link AnalogInput_RevD} This input to facilitate chaining.
     */
    public AnalogInput_RevD setBufferState(final BufferState state) {
        mBuffer = state;
        return this;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.tenkiv.AAnalogInput#isValidGain(com.tenkiv.AAnalogInput
     * .Gain)
     */
    @Override
    protected boolean isValidGain(final Gain gain) {
        switch (gain) {
            case X1:
            case X2:
            case X4:
            case X8:
            case X16:
            case X32:
            case X64:
                return true;
            default:
                return false;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.tenkiv.AAnalogInput#isValidRate(com.tenkiv.AAnalogInput
     * .Rate)
     */
    @Override
    protected boolean isValidRate(final Rate rate) {
        switch (rate) {
            case SPS_2_5:
            case SPS_5:
            case SPS_10:
            case SPS_15:
            case SPS_25:
            case SPS_30:
            case SPS_50:
            case SPS_60:
            case SPS_100:
            case SPS_500:
            case SPS_1000:
            case SPS_2000:
            case SPS_3750:
            case SPS_7500:
                return true;
            default:
                return false;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.tenkiv.AAnalogInput#isValidSensorCurrent(com.tenkiv.tekdaqc.peripherals.analog
     * .AAnalogInput.SensorCurrent)
     */
    @Override
    protected boolean isValidSensorCurrent(final SensorCurrent current) {
        switch (current) {
            case _0_5uA:
            case _10uA:
            case _2uA:
            case OFF:
                return true;
            default:
                return false;
        }
    }

    @Override
    protected void writeOut(ObjectOutput output) throws IOException {
        output.writeObject(mBuffer);
    }

    @Override
    protected void readIn(ObjectInput input) throws IOException, ClassNotFoundException {
        mBuffer = (BufferState) input.readObject();
    }

    /**
     * Set of possible input buffer states.
     *
     * @author Jared Woolston (jwoolston@tenkiv.com)
     * @since v1.0.0.0
     */
    public enum BufferState {
        ENABLED, DISABLED, INVALID;

        private static final BufferState[] mValueArray = BufferState.values();

        public static BufferState getValueFromOrdinal(final byte ordinal) {
            return mValueArray[ordinal];
        }

        public static BufferState fromString(final String buffer) {
            for (final BufferState b : values()) {
                if (b.name().equals(buffer)) {
                    return b;
                }
            }
            return INVALID;
        }
    }
}

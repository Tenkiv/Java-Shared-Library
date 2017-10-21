package com.tenkiv.tekdaqc.communication.command.queue;

import com.tenkiv.tekdaqc.hardware.ATekdaqc;

/**
 * Enum class handling all parameters of {@link Commands} sent to the {@link ATekdaqc}.
 *
 * @author Tenkiv (software@tenkiv.com)
 * @since v2.0.0.0
 */
public enum Params {

    /**
     * Constant for defining which input to modify.
     */
    INPUT,

    /**
     * Constant for defining the rate of analog inputs.
     */
    RATE,

    /**
     * Constant for defining the gain of analog inputs.
     */
    GAIN,

    /**
     * Constant for defining the buffer status of analog inputs.
     */
    BUFFER,

    /**
     * Constant for defining the number of samples to take for sample commands.
     */
    NUMBER,

    /**
     * Constant for defining the name of inputs and outputs. Currently not implemented.
     */
    NAME,

    /**
     * Constant for defining the output state of digital outputs.
     */
    OUTPUT,

    /**
     * Constant for defining the state. Currently not implemented.
     */
    STATE,

    /**
     * Constant for defining the value of certain variables sent to the Tekdaqc, such as user defined time for RTC.
     */
    VALUE,

    /**
     * Constant for defining the analog scale.
     */
    SCALE,

    /**
     * Constant for defining the temperature of a calibration point.
     */
    TEMPERATURE,

    /**
     * Constant for defining index of a calibration constant.
     */
    INDEX,

    /**
     * Constant for defining all. Not currently implemented.
     */
    ALL,

    /**
     * The duty cycle of the digital output's pulse width modulation.
     */
    DUTYCYCLE;


    /**
     * Array of all values in the enum. This is done because calling Params.values() is a relatively intensive operation.
     */
    private static Params[] mValueArray = Params.values();

    /**
     * {@link Byte} representing the ordinal value of the given {@link Params}.
     */
    private byte commandType;

    /**
     * Constructor instantiating the given {@link Params}.
     */
    Params() {
        this.commandType = (byte) ordinal();
    }

    /**
     * Gets a {@link Params} given its ordinal position.
     *
     * @param ordinal The command's ordinal position.
     * @return {@link Params} of the ordinal positions value.
     */
    public static Params getValueFromOrdinal(final byte ordinal) {
        return mValueArray[ordinal];
    }

    /**
     * Gets a {@link Byte} representing this {@link Params} ordinal position.
     *
     * @return Ordinal position of the {@link Params} as {@link Byte}.
     */
    public byte getOrdinalCommandType() {
        return commandType;
    }
}

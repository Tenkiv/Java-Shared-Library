package com.tenkiv.tekdaqc.communication.command.queue;

import com.tenkiv.tekdaqc.hardware.ATekdaqc;

/**
 * Enum class handling all commands sent to the {@link ATekdaqc}.
 *
 * @author Tenkiv (software@tenkiv.com)
 * @since v2.0.0.0
 */
public enum Commands {

    /**
     * Disconnect from Tekdaqc.
     */
    DISCONNECT,

    /**
     * Upgrade the Tekdaqc's firmware.
     */
    UPGRADE,

    /**
     * Identify basic info for the Tekdaqc.
     */
    IDENTIFY,

    /**
     * Sample all added inputs for both Analog and Digital.
     */
    SAMPLE,

    /**
     * Halt all operations.
     */
    HALT,

    /**
     * Sets real-time cLock.
     */
    SET_RTC,

    /**
     * Sets user MAC address.
     */
    SET_USER_MAC,

    /**
     * Sets a static IP address.
     */
    SET_STATIC_IP,

    /**
     * A command which does nothing. To verify that commands are working.
     */
    NONE,

    /**
     * Lists all analog inputs.
     */
    LIST_ANALOG_INPUTS,

    /**
     * Reads the registers of the analog-to-digital converters.
     */
    READ_ADC_REGISTERS,

    /**
     * Reads specified analog inputs. Analog inputs must be added to be read.
     */
    READ_ANALOG_INPUT,

    /**
     * Adds an analog input with specified values. Attempting to add a preexisting analog input will throw an error.
     */
    ADD_ANALOG_INPUT,

    /**
     * Removes specified analog input. Removing a non-existent input will NOT throw an error.
     */
    REMOVE_ANALOG_INPUT,

    /**
     * Checks specified analog input.
     */
    CHECK_ANALOG_INPUT,

    /**
     * Sets Tekdaqc's analog input scale.
     */
    SET_ANALOG_INPUT_SCALE,

    /**
     * Gets Tekdaqc's analog input scale.
     */
    GET_ANALOG_INPUT_SCALE,

    /**
     * Lists all added digital inputs.
     */
    LIST_DIGITAL_INPUTS,

    /**
     * Reads specified digital inputs. Digital inputs mst be added to be read.
     */
    READ_DIGITAL_INPUT,

    /**
     * Add a digital input with specified value. Attempting to add a preexisting digital input will throw an error.
     */
    ADD_DIGITAL_INPUT,

    /**
     * Removes specified digital input. Removing a non-existent input will NOT throw an error.
     */
    REMOVE_DIGITAL_INPUT,

    /**
     * DEPRECATED.
     */
    LIST_DIGITAL_OUTPUTS,

    /**
     * Sets digital outputs to specific hex values.
     */
    SET_DIGITAL_OUTPUT,

    /**
     * Sets the pulse width modulation to digital outputs specific hex values.
     */
    SET_PWM_OUTPUT,

    SET_PWM_OUTPUT_TIMER,

    /**
     * Reads current state of the digital outputs.
     */
    READ_DIGITAL_OUTPUT,

    /**
     * DEPRECATED.
     */
    ADD_DIGITAL_OUTPUT,

    /**
     * DEPRECATED
     */
    REMOVE_DIGITAL_OUTPUT,

    /**
     * Clear error in the digital outputs.
     */
    CLEAR_DIGITAL_OUTPUT_FAULT,

    /**
     * WARNING: THE FOLLOWING COMMANDS ARE USED IN CALIBRATION. THEY ARE NOT DESIGNED FOR AVERAGE USERS AND SHOULD ONLY BE RUN BY
     * THOSE WITH BOTH THE PREREQUISITE KNOWLEDGE AND EQUIPMENT. IMPROPER OR INCOMPLETE CALIBRATION CAN RESULT IN UNRELIABLE,
     * INACCURATE, OR EMPTY ANALOG INPUT VALUES. UNDERTAKE RECALIBRATION AT YOUR OWN RISK.
     */
    SYSTEM_GCAL,
    SYSTEM_CAL,
    READ_SYSTEM_GCAL,
    GET_CALIBRATION_STATUS,
    ENTER_CALIBRATION_MODE,
    WRITE_GAIN_CALIBRATION_VALUE,
    WRITE_CALIBRATION_TEMP,
    WRITE_CALIBRATION_VALID,
    EXIT_CALIBRATION_MODE,
    SET_BOARD_SERIAL_NUM,
    SET_FACTORY_MAC_ADDR,
    READ_SELF_GCAL,

    ADD_PWM_INPUT,
    REMOVE_PWM_INPUT,
    READ_PWM_INPUT,
    LIST_PWM_INPUTS;

    /**
     * Array of all values in the enum. This is done because calling Commands.values() is a relatively intensive operation.
     */
    private static final Commands[] mValueArray = Commands.values();

    /**
     * {@link Byte} representing the ordinal value of the given {@link Commands}.
     */
    private byte commandType;

    /**
     * Constructor instantiating the given {@link Commands}.
     */
    Commands() {
        this.commandType = (byte) ordinal();
    }

    /**
     * Gets a {@link Commands} given its ordinal position.
     *
     * @param ordinal The command's ordinal position.
     * @return {@link Commands} of the ordinal positions value.
     */
    public static Commands getValueFromOrdinal(final byte ordinal) {
        return mValueArray[ordinal];
    }

    /**
     * Gets {@link Commands} from a {@link String} of the same name.
     *
     * @param command {@link String} of the name.
     * @return {@link Commands} with the name of {@link String}.
     */
    public static Commands toCommand(final String command) {
        try {
            return valueOf(command);
        } catch (IllegalArgumentException e) {
            return NONE;
        }
    }

    /**
     * Gets a {@link Byte} representing this {@link Commands} ordinal position.
     *
     * @return Ordinal position of the {@link Commands} as {@link Byte}.
     */
    public byte getOrdinalCommandType() {
        return commandType;
    }
}

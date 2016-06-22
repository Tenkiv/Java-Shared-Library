package com.tenkiv.tekdaqc.hardware;

import com.tenkiv.tekdaqc.communication.command.queue.Commands;
import com.tenkiv.tekdaqc.communication.command.queue.Params;
import com.tenkiv.tekdaqc.communication.command.queue.Task;
import com.tenkiv.tekdaqc.communication.command.queue.values.*;
import com.tenkiv.tekdaqc.utility.DigitalOutputUtilities;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Utility class which handles the creation of {@link ABaseQueueVal} for the purpose of adding them individually to a {@link Task} or
 * directly to the command queue via the {@link ATekdaqc}.
 *
 * @author Tenkiv (software@tenkiv.com)
 * @since v2.0.0.0
 */
public final class CommandBuilder {

    /**
     * Method to generate the "READ_ANALOG_INPUT" command with the given parameters.
     *
     * @param input  The input to be read.
     * @param number The number of times input should be read. Note: "0" will sample indefinitely.
     * @return The {@link ABaseQueueVal} of the command.
     */
    public static ABaseQueueVal readAnalogInput(final int input, final int number) {
        final ParamQueueValue queueValue = new ParamQueueValue(Commands.READ_ANALOG_INPUT);
        queueValue.addParamValue(Params.INPUT, (byte) input);
        queueValue.addParamValue(Params.NUMBER, (byte) number);
        return queueValue;
    }

    /**
     * Method to generate the "READ_ANALOG_INPUT" command with a range of inputs.
     *
     * @param start  The start of range of inputs to be read.
     * @param end    The inclusive end of the range of input to be read.
     * @param number The number of times the inputs should be read. Note: "0" will sample indefinitely.
     * @return The {@link ABaseQueueVal} of the command.
     */
    public static ABaseQueueVal readAnalogInputRange(final int start, final int end, final int number) {
        final StringQueueValue queueValue = new StringQueueValue(Commands.READ_ANALOG_INPUT, Params.INPUT, start + "-" + end);
        queueValue.addParamValue(Params.NUMBER, (byte) number);
        return queueValue;
    }

    /**
     * Method to generate the "READ_ANALOG_INPUT" command with a set if inputs.
     *
     * @param inputs The {@link Set} of inputs to be read.
     * @param number The number of times the inputs should be sampled. Note: "0" will sample indefinitely.
     * @return The {@link ABaseQueueVal} of the command.
     */
    public static ABaseQueueVal readAnalogInputSet(final Set<Integer> inputs, final int number) {

        final StringBuilder inputBuilder = new StringBuilder();
        for (final Integer input : inputs) {
            inputBuilder.append(input).append(',');
        }
        inputBuilder.deleteCharAt(inputBuilder.length());

        final StringQueueValue queueValue = new StringQueueValue(Commands.READ_ANALOG_INPUT, Params.INPUT, inputBuilder.toString());
        queueValue.addParamValue(Params.NUMBER, (byte) number);

        return queueValue;

    }

    /**
     * Method to generate the "READ_ANALOG_INPUT" command with the "ALL" parameter.
     *
     * @param number The number of times to sample all inputs.
     * @return The {@link ABaseQueueVal} of the command.
     */
    public static ABaseQueueVal readAllAnalogInput(final int number) {
        final StringQueueValue queueValue = new StringQueueValue(Commands.READ_ANALOG_INPUT, Params.INPUT, "ALL");
        queueValue.addParamValue(Params.NUMBER, (byte) number);
        return queueValue;
    }

    /**
     * Method to generate the "READ_ANALOG_INPUT" command with the given parameters.
     *
     * @param input  The input to be read.
     * @param number The number of times input should be read. Note: "0" will sample indefinitely.
     * @return The {@link ABaseQueueVal} of the command.
     */
    public static ABaseQueueVal readDigitalInput(final int input, final int number) {
        final ParamQueueValue queueValue = new ParamQueueValue(Commands.READ_DIGITAL_INPUT);
        queueValue.addParamValue(Params.INPUT, (byte) input);
        queueValue.addParamValue(Params.NUMBER, (byte) number);
        return queueValue;
    }

    /**
     * Method to generate the "READ_ANALOG_INPUT" command with a range of inputs.
     *
     * @param start  The start of range of inputs to be read.
     * @param end    The inclusive end of the range of input to be read.
     * @param number The number of times the inputs should be read. Note: "0" will sample indefinitely.
     * @return The {@link ABaseQueueVal} of the command.
     */
    public static ABaseQueueVal readDigitalInputRange(final int start, final int end, final int number) {
        final StringQueueValue queueValue = new StringQueueValue(Commands.READ_DIGITAL_INPUT, Params.INPUT, start + "-" + end);
        queueValue.addParamValue(Params.NUMBER, (byte) number);
        return queueValue;
    }

    /**
     * Method to generate the "READ_ANALOG_INPUT" command with a set if inputs.
     *
     * @param inputs The {@link Set} of inputs to be read.
     * @param number The number of times the inputs should be sampled. Note: "0" will sample indefinitely.
     * @return The {@link ABaseQueueVal} of the command.
     */
    public static ABaseQueueVal readDigitalInputSet(final Set<Integer> inputs, final int number) {

        final StringBuilder inputBuilder = new StringBuilder();
        for (Integer input : inputs) {
            inputBuilder.append(input).append(',');
        }
        inputBuilder.deleteCharAt(inputBuilder.length());

        final StringQueueValue queueValue = new StringQueueValue(Commands.READ_DIGITAL_INPUT, Params.INPUT, inputBuilder.toString());
        queueValue.addParamValue(Params.NUMBER, (byte) number);

        return queueValue;

    }

    /**
     * Method to generate the "READ_ANALOG_INPUT" command with the "ALL" parameter.
     *
     * @param number The number of times to sample all inputs.
     * @return The {@link ABaseQueueVal} of the command.
     */
    public static ABaseQueueVal readAllDigitalInput(final int number) {
        final StringQueueValue queueValue = new StringQueueValue(Commands.READ_DIGITAL_INPUT, Params.INPUT, "ALL");
        queueValue.addParamValue(Params.NUMBER, (byte) number);
        return queueValue;
    }

    /**
     * Method to generate the "ADD_ANALOG_INPUT" command with the given parameters.
     *
     * @param input The {@link AAnalogInput} to be added.
     * @return The {@link ABaseQueueVal} of the command.
     * @throws IllegalArgumentException Thrown if the {@link AAnalogInput} contains incorrect values.
     * @throws IllegalStateException    Thrown if the {@link AAnalogInput} is of the improper type.
     */
    static ABaseQueueVal addAnalogInput(final AAnalogInput input) throws IllegalArgumentException, IllegalStateException {
        final AnalogInput_RevD Input = (AnalogInput_RevD) input;
        final StringQueueValue queueValue = new StringQueueValue(Commands.ADD_ANALOG_INPUT, Params.NAME, Input.getName());
        queueValue.addParamValue(Params.INPUT, (byte) Input.getChannelNumber());
        queueValue.addParamValue(Params.GAIN, (byte) Input.getGain().ordinal());
        queueValue.addParamValue(Params.RATE, (byte) Input.getRate().ordinal());
        queueValue.addParamValue(Params.BUFFER, (byte) Input.getBufferState().ordinal());
        return queueValue;
    }

    /**
     * Method to generate the "ADD_DIGITAL_INPUT" command with the given parameters.
     *
     * @param input The {@link DigitalInput} to be added.
     * @return The {@link ABaseQueueVal} of the command.
     * @throws IllegalArgumentException Thrown if the {@link DigitalInput} contains incorrect values.
     * @throws IllegalStateException    Thrown if the {@link DigitalInput} is of the improper type.
     */
    static ABaseQueueVal addDigitalInput(final DigitalInput input) throws IllegalArgumentException, IllegalStateException {
        final StringQueueValue queueValue = new StringQueueValue(Commands.ADD_DIGITAL_INPUT, Params.NAME, input.getName());
        queueValue.addParamValue(Params.INPUT, (byte) input.getChannelNumber());
        return queueValue;
    }

    /**
     * Method to generate the "SET_DIGITAL_OUTPUTS" command with a {@link String} representing its binary state. Ie. "001100110011"
     *
     * @param binaryString The {@link String} representing the desired values for the digital outputs as a binary string.
     * @return The {@link ABaseQueueVal} of the command.
     */
    public static ABaseQueueVal setDigitalOutputByBinaryString(final String binaryString) {
        final String hexOutput = DigitalOutputUtilities.hexConversion(binaryString);
        return new StringQueueValue(Commands.SET_DIGITAL_OUTPUT, Params.OUTPUT, hexOutput);
    }

    /**
     * Method to generate the "SET_DIGITAL_OUTPUTS" command with a {@link String} representing its binary state as a hex value. Ie. "FFFF"
     *
     * @param hex The {@link String} representing the desired values for the digital outputs as a hexadecimal code.
     * @return The {@link ABaseQueueVal} of the command.
     */
    public static ABaseQueueVal setDigitalOutputByHex(final String hex) {
        return new StringQueueValue(Commands.SET_DIGITAL_OUTPUT, Params.OUTPUT, hex);
    }

    /**
     * Method to generate the "SET_DIGITAL_OUTPUTS" command with an array of booleans representing its desired state.
     *
     * @param digitalOutputState The {@link Array} of {@link Boolean} representing the desired digital output state.
     * @return The {@link ABaseQueueVal} of the command.
     */
    public static ABaseQueueVal setDigitalOutput(final boolean[] digitalOutputState) {
        final String hexOutput = DigitalOutputUtilities.boolArrayConversion(digitalOutputState);
        return new StringQueueValue(Commands.SET_DIGITAL_OUTPUT, Params.OUTPUT, hexOutput);
    }

    /**
     * Method to generate the "REMOVE_ANALOG_INPUT" command from the given {@link AAnalogInput}.
     *
     * @param input The {@link AAnalogInput} to remove.
     * @return The {@link ABaseQueueVal} of the command.
     */
    static ABaseQueueVal removeAnalogInput(final AAnalogInput input) {
        final ParamQueueValue queueValue = new ParamQueueValue(Commands.REMOVE_ANALOG_INPUT);
        queueValue.addParamValue(Params.INPUT, (byte) input.getChannelNumber());
        return queueValue;
    }

    /**
     * Method to generate the "REMOVE_ANALOG_INPUT" command from input number.
     *
     * @param input The physical input of the number to be removed.
     * @return The {@link ABaseQueueVal} of the command.
     */
    static ABaseQueueVal removeAnalogInputByNumber(final int input) {
        final ParamQueueValue queueValue = new ParamQueueValue(Commands.REMOVE_ANALOG_INPUT);
        queueValue.addParamValue(Params.INPUT, (byte) input);
        return queueValue;
    }

    /**
     * Method to generate a {@link List} of {@link ABaseQueueVal} representing the commands to remove all analog inputs on the given {@link Map}.
     *
     * @param inputs The {@link Map} of the inputs to be removed.
     * @return The {@link List} of {@link ABaseQueueVal} representing the commands.
     */
    static List<ABaseQueueVal> removeMappedAnalogInputs(final Map<Integer, AAnalogInput> inputs) {
        ArrayList<ABaseQueueVal> queueVals = new ArrayList<ABaseQueueVal>();
        final Set<Integer> keys = inputs.keySet();
        for (final Integer i : keys) {
            final AAnalogInput input = inputs.get(i);
            final ParamQueueValue queueValue = new ParamQueueValue(Commands.REMOVE_ANALOG_INPUT);
            queueValue.addParamValue(Params.INPUT, (byte) input.getChannelNumber());
            queueVals.add(queueValue);
        }
        return queueVals;
    }

    /**
     * Method to generate a {@link ABaseQueueVal} representing the "REMOVE_DIGITAL_INPUT" command.
     *
     * @param input The {@link DigitalInput} to be removed.
     * @return The {@link ABaseQueueVal} of the command.
     */
    static ABaseQueueVal removeDigitalInput(final DigitalInput input) {
        final ParamQueueValue queueValue = new ParamQueueValue(Commands.REMOVE_DIGITAL_INPUT);
        queueValue.addParamValue(Params.INPUT, (byte) input.getChannelNumber());
        return queueValue;
    }

    /**
     * Method to generate a {@link ABaseQueueVal} representing the "REMOVE_DIGITAL_INPUT" command.
     *
     * @param input The input to be removed.
     * @return The {@link ABaseQueueVal} of the command.
     */
    static ABaseQueueVal removeDigitalInputByNumber(final int input) {
        final ParamQueueValue queueValue = new ParamQueueValue(Commands.REMOVE_DIGITAL_INPUT);
        queueValue.addParamValue(Params.INPUT, (byte) input);
        return queueValue;
    }

    /**
     * Method to generate a {@link List} of {@link ABaseQueueVal} representing the commands to remove all digital inputs on the given {@link Map}.
     *
     * @param inputs The {@link Map} of the inputs to be removed.
     * @return The {@link List} of {@link ABaseQueueVal} representing the commands.
     */
    static List<ABaseQueueVal> removeMappedDigitalInputs(final Map<Integer, DigitalInput> inputs) {
        final ArrayList<ABaseQueueVal> queueVals = new ArrayList<ABaseQueueVal>();
        final Set<Integer> keys = inputs.keySet();
        for (final Integer i : keys) {
            final DigitalInput input = inputs.get(i);
            final ParamQueueValue queueValue = new ParamQueueValue(Commands.REMOVE_DIGITAL_INPUT);
            queueValue.addParamValue(Params.INPUT, (byte) input.getChannelNumber());
            queueVals.add(queueValue);
        }
        return queueVals;
    }

    /**
     * Method to generate a {@link List} of {@link ABaseQueueVal} representing commands to remove all known added digital inputs.
     * Note: The library only knows which inputs it has added on this session and not which were added to the tekdaqc before connecting.
     *
     * @return The {@link List} of {@link ABaseQueueVal} representing the commands.
     */
    public static List<ABaseQueueVal> deactivateAllDigitalInputs() {
        final ArrayList<ABaseQueueVal> queueVals = new ArrayList<ABaseQueueVal>();
        for (int count = 0; count < Tekdaqc_RevD.DIGITAL_INPUT_COUNT; count++) {
            ParamQueueValue queueValue = new ParamQueueValue(Commands.REMOVE_DIGITAL_INPUT);
            queueValue.addParamValue(Params.INPUT, (byte) count);
            queueVals.add(queueValue);
        }
        return queueVals;
    }

    /**
     * Method to generate a {@link List} of {@link ABaseQueueVal} representing commands to remove all known added analog inputs.
     * Note: The library only knows which inputs it has added on this session and not which were added to the tekdaqc before connecting.
     *
     * @return The {@link List} of {@link ABaseQueueVal} representing the commands.
     */
    public static List<ABaseQueueVal> deactivateAllAnalogInputs() {
        final ArrayList<ABaseQueueVal> queueVals = new ArrayList<ABaseQueueVal>();
        for (int count = 0; count < Tekdaqc_RevD.ANALOG_INPUT_COUNT; count++) {
            final ParamQueueValue queueValue = new ParamQueueValue(Commands.REMOVE_ANALOG_INPUT);
            queueValue.addParamValue(Params.INPUT, (byte) count);
            queueVals.add(queueValue);
        }
        return queueVals;
    }

    /**
     * Method to generate a {@link ABaseQueueVal} representing the command "SET_ANALOG_SCALE" with the given parameters.
     *
     * @param scale The desired {@link ATekdaqc.AnalogScale} to be set.
     * @return The {@link ABaseQueueVal} of the command.
     */
    public static ABaseQueueVal setAnalogInputScale(final ATekdaqc.AnalogScale scale) {
        final ParamQueueValue queueValue = new ParamQueueValue(Commands.SET_ANALOG_INPUT_SCALE);
        queueValue.addParamValue(Params.SCALE, (byte) scale.ordinal());
        return queueValue;
    }

    /**
     * Method to generate a {@link ABaseQueueVal} representing the command "GET_ANALOG_SCALE".
     *
     * @return The {@link ABaseQueueVal} of the command.
     */
    public static ABaseQueueVal getAnalogInputScale() {
        return new BlankQueueValue(Commands.GET_ANALOG_INPUT_SCALE);
    }

    /**
     * Method to generate a {@link ABaseQueueVal} representing the command "SYSTEM_GCAL" with the given parameters.
     *
     * WARNING: THIS COMMAND IS USED IN CALIBRATION. IT IS NOT DESIGNED FOR AVERAGE USERS AND SHOULD ONLY BE RUN BY
     * THOSE WITH BOTH THE PREREQUISITE KNOWLEDGE AND EQUIPMENT. IMPROPER OR INCOMPLETE CALIBRATION CAN RESULT IN UNRELIABLE,
     * INACCURATE, OR EMPTY ANALOG INPUT VALUES. UNDERTAKE RECALIBRATION AT YOUR OWN RISK.
     *
     * @param input Inputs which are being calibrated
     * @return The {@link ABaseQueueVal} of the command.
     */
    public static ABaseQueueVal systemGainCalibrate(final int input) {
        final ParamQueueValue queueValue = new ParamQueueValue(Commands.SYSTEM_GCAL);
        queueValue.addParamValue(Params.INPUT, (byte) input);
        return queueValue;
    }

    /**
     * Method to generate a {@link ABaseQueueVal} representing the command "READ_SYSTEM_GCAL".
     *
     * WARNING: THIS COMMAND IS USED IN CALIBRATION. IT IS NOT DESIGNED FOR AVERAGE USERS AND SHOULD ONLY BE RUN BY
     * THOSE WITH BOTH THE PREREQUISITE KNOWLEDGE AND EQUIPMENT. IMPROPER OR INCOMPLETE CALIBRATION CAN RESULT IN UNRELIABLE,
     * INACCURATE, OR EMPTY ANALOG INPUT VALUES. UNDERTAKE RECALIBRATION AT YOUR OWN RISK.
     *
     * @return The {@link ABaseQueueVal} of the command.
     */
    public static ABaseQueueVal readSystemGainCalibration() {
        return new BlankQueueValue(Commands.READ_SYSTEM_GCAL);
    }

    /**
     * Method to generate a {@link ABaseQueueVal} representing the command "READ_SELF_GCAL" with the given parameters.
     *
     * WARNING: THIS COMMAND IS USED IN CALIBRATION. IT IS NOT DESIGNED FOR AVERAGE USERS AND SHOULD ONLY BE RUN BY
     * THOSE WITH BOTH THE PREREQUISITE KNOWLEDGE AND EQUIPMENT. IMPROPER OR INCOMPLETE CALIBRATION CAN RESULT IN UNRELIABLE,
     * INACCURATE, OR EMPTY ANALOG INPUT VALUES. UNDERTAKE RECALIBRATION AT YOUR OWN RISK.
     *
     * @param gain   Gain of the cal to read.
     * @param rate   Rate of the cal to read.
     * @param buffer Buffer of the cal to read.
     * @return The {@link ABaseQueueVal} of the command.
     */
    public static ABaseQueueVal readSelfGainCalibration(final AAnalogInput.Gain gain
            , final AAnalogInput.Rate rate
            , final AnalogInput_RevD.BufferState buffer) {
        ParamQueueValue queueValue = new ParamQueueValue(Commands.READ_SELF_GCAL);
        queueValue.addParamValue(Params.GAIN, (byte) gain.ordinal());
        queueValue.addParamValue(Params.RATE, (byte) rate.ordinal());
        queueValue.addParamValue(Params.BUFFER, (byte) buffer.ordinal());
        return queueValue;
    }

    /**
     * Method to generate a {@link ABaseQueueVal} representing the command "UPGRADE".
     *
     * @return The {@link ABaseQueueVal} of the command.
     */
    public static ABaseQueueVal upgrade() {
        return new BlankQueueValue(Commands.UPGRADE);
    }

    /**
     * Method to generate a {@link ABaseQueueVal} representing the command "IDENTIFY".
     *
     * @return The {@link ABaseQueueVal} of the command.
     */
    public static ABaseQueueVal identify() {
        return new BlankQueueValue(Commands.IDENTIFY);
    }

    /**
     * Method to generate a {@link ABaseQueueVal} representing the command "SAMPLE" with the given parameters.
     *
     * @param number The number of times to sample. Note: "0" will sample indefinitely.
     * @return The {@link ABaseQueueVal} of the command.
     */
    public static ABaseQueueVal sample(final int number) {
        ParamQueueValue queueValue = new ParamQueueValue(Commands.SAMPLE);
        queueValue.addParamValue(Params.NUMBER, (byte) number);
        return queueValue;
    }

    /**
     * Method to generate a {@link ABaseQueueVal} representing the command "HALT".
     *
     * @return The {@link ABaseQueueVal} of the command.
     */
    public static ABaseQueueVal halt() {
        return new BlankQueueValue(Commands.HALT);
    }

    /**
     * Method to generate a {@link ABaseQueueVal} representing the command "SET_RTC" with the given parameters.
     *
     * @return The {@link ABaseQueueVal} of the command.
     */
    public static ABaseQueueVal setRTC(final long timestamp) {
        return new StringQueueValue(Commands.WRITE_CALIBRATION_TEMP, Params.VALUE, String.valueOf(timestamp));
    }

    /**
     * Method to generate a {@link ABaseQueueVal} representing the command "READ_ADC_REGISTERS".
     *
     * @return The {@link ABaseQueueVal} of the command.
     */
    public static ABaseQueueVal readADCRegisters() {
        return new BlankQueueValue(Commands.READ_ADC_REGISTERS);
    }

    /**
     * Method to generate a {@link ABaseQueueVal} representing the command "GET_CAL_STATUS".
     *
     * WARNING: THIS COMMAND IS USED IN CALIBRATION. IT IS NOT DESIGNED FOR AVERAGE USERS AND SHOULD ONLY BE RUN BY
     * THOSE WITH BOTH THE PREREQUISITE KNOWLEDGE AND EQUIPMENT. IMPROPER OR INCOMPLETE CALIBRATION CAN RESULT IN UNRELIABLE,
     * INACCURATE, OR EMPTY ANALOG INPUT VALUES. UNDERTAKE RECALIBRATION AT YOUR OWN RISK.
     *
     * @return The {@link ABaseQueueVal} of the command.
     */
    public static ABaseQueueVal getCalibrationStatus() {
        return new BlankQueueValue(Commands.GET_CALIBRATION_STATUS);
    }

    /**
     * Method to generate a {@link ABaseQueueVal} representing the command "WRITE_CAL_TEMP" with the given parameters.
     *
     * WARNING: THIS COMMAND IS USED IN CALIBRATION. IT IS NOT DESIGNED FOR AVERAGE USERS AND SHOULD ONLY BE RUN BY
     * THOSE WITH BOTH THE PREREQUISITE KNOWLEDGE AND EQUIPMENT. IMPROPER OR INCOMPLETE CALIBRATION CAN RESULT IN UNRELIABLE,
     * INACCURATE, OR EMPTY ANALOG INPUT VALUES. UNDERTAKE RECALIBRATION AT YOUR OWN RISK.
     *
     * @param temp  Temperature of the index.
     * @param index Number of the index.
     * @return he {@link ABaseQueueVal} of the command.
     */
    public static ABaseQueueVal writeCalibrationTemperature(final double temp, final int index) {
        final StringQueueValue queueValue = new StringQueueValue(Commands.WRITE_CALIBRATION_TEMP, Params.TEMPERATURE, String.valueOf(temp));
        queueValue.addParamValue(Params.INDEX, (byte) index);
        return queueValue;
    }

    /**
     * Method to generate a {@link ABaseQueueVal} representing the command "WRITE_SYSTEM_GAIN_CAL" with the given parameters.
     *
     * WARNING: THIS COMMAND IS USED IN CALIBRATION. IT IS NOT DESIGNED FOR AVERAGE USERS AND SHOULD ONLY BE RUN BY
     * THOSE WITH BOTH THE PREREQUISITE KNOWLEDGE AND EQUIPMENT. IMPROPER OR INCOMPLETE CALIBRATION CAN RESULT IN UNRELIABLE,
     * INACCURATE, OR EMPTY ANALOG INPUT VALUES. UNDERTAKE RECALIBRATION AT YOUR OWN RISK.
     *
     * @param value  correction value.
     * @param gain   gain of value.
     * @param rate   rate of value.
     * @param buffer buffer of value.
     * @param scale  scale of value.
     * @param temp   temp index of value.
     * @return he {@link ABaseQueueVal} of the command.
     */
    public static ABaseQueueVal writeGainCalibrationValue(final float value
            , final AAnalogInput.Gain gain
            , final AAnalogInput.Rate rate
            , final AnalogInput_RevD.BufferState buffer
            , final ATekdaqc.AnalogScale scale
            , final int temp) {

        return new CalibrationQueueValue(
                (byte) Commands.WRITE_GAIN_CALIBRATION_VALUE.ordinal(),
                temp,
                value,
                (byte) buffer.ordinal(),
                (byte) rate.ordinal(),
                (byte) gain.ordinal(),
                (byte) scale.ordinal());
    }

    /**
     * Method to generate a {@link ABaseQueueVal} representing the command "WRITE_CAL_VALID".
     *
     * WARNING: THIS COMMAND IS USED IN CALIBRATION. IT IS NOT DESIGNED FOR AVERAGE USERS AND SHOULD ONLY BE RUN BY
     * THOSE WITH BOTH THE PREREQUISITE KNOWLEDGE AND EQUIPMENT. IMPROPER OR INCOMPLETE CALIBRATION CAN RESULT IN UNRELIABLE,
     * INACCURATE, OR EMPTY ANALOG INPUT VALUES. UNDERTAKE RECALIBRATION AT YOUR OWN RISK.
     *
     * @return The {@link ABaseQueueVal} of the command.
     */
    public static ABaseQueueVal writeCalibrationValid() {
        return new BlankQueueValue(Commands.WRITE_CALIBRATION_VALID);
    }

    /**
     * Method to generate a {@link ABaseQueueVal} representing the command "DISCONNECT".
     *
     * @return The {@link ABaseQueueVal} of the command.
     */
    public static ABaseQueueVal disconnect() {
        return new BlankQueueValue(Commands.DISCONNECT);
    }

    /**
     * Method to generate a {@link ABaseQueueVal} representing the command "READ_DIGITAL_OUTPUTS".
     *
     * @return The {@link ABaseQueueVal} of the command.
     */
    public static ABaseQueueVal readDigitalOutput() {
        return new BlankQueueValue(Commands.READ_DIGITAL_OUTPUT);
    }

    /**
     * Method to generate a {@link ABaseQueueVal} representing the command "ENTER_CALIBRATION_MODE".
     *
     * WARNING: THIS COMMAND IS USED IN CALIBRATION. IT IS NOT DESIGNED FOR AVERAGE USERS AND SHOULD ONLY BE RUN BY
     * THOSE WITH BOTH THE PREREQUISITE KNOWLEDGE AND EQUIPMENT. IMPROPER OR INCOMPLETE CALIBRATION CAN RESULT IN UNRELIABLE,
     * INACCURATE, OR EMPTY ANALOG INPUT VALUES. UNDERTAKE RECALIBRATION AT YOUR OWN RISK.
     *
     * @return The {@link ABaseQueueVal} of the command.
     */
    public static ABaseQueueVal enterCalibrationMode() {
        return new BlankQueueValue(Commands.ENTER_CALIBRATION_MODE);
    }

    /**
     * Method to generate a {@link ABaseQueueVal} representing the command "EXIT_CALIBRATION_MODE".
     *
     * WARNING: THIS COMMAND IS USED IN CALIBRATION. IT IS NOT DESIGNED FOR AVERAGE USERS AND SHOULD ONLY BE RUN BY
     * THOSE WITH BOTH THE PREREQUISITE KNOWLEDGE AND EQUIPMENT. IMPROPER OR INCOMPLETE CALIBRATION CAN RESULT IN UNRELIABLE,
     * INACCURATE, OR EMPTY ANALOG INPUT VALUES. UNDERTAKE RECALIBRATION AT YOUR OWN RISK.
     *
     * @return The {@link ABaseQueueVal} of the command.
     */
    public static ABaseQueueVal exitCalibrationMode() {
        return new BlankQueueValue(Commands.EXIT_CALIBRATION_MODE);
    }

    /**
     * Method to generate a {@link ABaseQueueVal} representing the command "WRITE_SERIAL_NUMBER" with the given parameters.
     *
     * WARNING: THIS COMMAND IS USED IN CALIBRATION. IT IS NOT DESIGNED FOR AVERAGE USERS AND SHOULD ONLY BE RUN BY
     * THOSE WITH BOTH THE PREREQUISITE KNOWLEDGE AND EQUIPMENT. IMPROPER OR INCOMPLETE CALIBRATION CAN RESULT IN UNRELIABLE,
     * INACCURATE, OR EMPTY ANALOG INPUT VALUES. UNDERTAKE RECALIBRATION AT YOUR OWN RISK.
     *
     * @param serial The serial number to write.
     * @return The {@link ABaseQueueVal} of the command.
     */
    public static ABaseQueueVal writeSerialNumber(final String serial) {
        return new StringQueueValue(Commands.READ_DIGITAL_OUTPUT, Params.VALUE, serial);
    }

    /**
     * Method to generate a {@link ABaseQueueVal} representing the command "WRITE_MAC_ADDRESS" with the given parameters.
     *
     * WARNING: THIS COMMAND IS USED IN CALIBRATION. IT IS NOT DESIGNED FOR AVERAGE USERS AND SHOULD ONLY BE RUN BY
     * THOSE WITH BOTH THE PREREQUISITE KNOWLEDGE AND EQUIPMENT. IMPROPER OR INCOMPLETE CALIBRATION CAN RESULT IN UNRELIABLE,
     * INACCURATE, OR EMPTY ANALOG INPUT VALUES. UNDERTAKE RECALIBRATION AT YOUR OWN RISK.
     *
     * @param mac The MAC address to write.
     * @return The {@link ABaseQueueVal} of the command.
     */
    public static ABaseQueueVal writeFactoryMacAddress(final long mac) {
        return new StringQueueValue(Commands.SET_FACTORY_MAC_ADDR, Params.VALUE, mac + "");
    }

    /**
     * Method to generate a {@link ABaseQueueVal} representing the command "NONE".
     *
     * @return The {@link ABaseQueueVal} of the command.
     */
    public static ABaseQueueVal none() {
        return new BlankQueueValue(Commands.NONE);
    }

    /**
     * Method to generate a {@link ABaseQueueVal} representing the command "SYSTEM_CAL".
     *
     * WARNING: THIS COMMAND IS USED IN CALIBRATION. IT IS NOT DESIGNED FOR AVERAGE USERS AND SHOULD ONLY BE RUN BY
     * THOSE WITH BOTH THE PREREQUISITE KNOWLEDGE AND EQUIPMENT. IMPROPER OR INCOMPLETE CALIBRATION CAN RESULT IN UNRELIABLE,
     * INACCURATE, OR EMPTY ANALOG INPUT VALUES. UNDERTAKE RECALIBRATION AT YOUR OWN RISK.
     *
     * @return The {@link ABaseQueueVal} of the command.
     */
    public static ABaseQueueVal systemCalibrate() {
        return new BlankQueueValue(Commands.SYSTEM_CAL);
    }

    /**
     * Method to generate a {@link ABaseQueueVal} representing the command "LIST_ANALOG_INPUTS".
     *
     * @return The {@link ABaseQueueVal} of the command.
     */
    public static ABaseQueueVal listAnalogInputs() {
        return new BlankQueueValue(Commands.LIST_ANALOG_INPUTS);
    }
}

package com.tenkiv.tekdaqc.hardware;

import com.tenkiv.tekdaqc.communication.ascii.message.parsing.ASCIIAnalogInputDataMessage;
import com.tenkiv.tekdaqc.communication.ascii.message.parsing.ASCIIDigitalInputDataMessage;
import com.tenkiv.tekdaqc.communication.command.queue.Commands;
import com.tenkiv.tekdaqc.communication.command.queue.values.ABaseQueueVal;
import com.tenkiv.tekdaqc.communication.command.queue.values.BlankQueueValue;
import com.tenkiv.tekdaqc.communication.data_points.AnalogInputData;
import com.tenkiv.tekdaqc.communication.data_points.DataPoint;
import com.tenkiv.tekdaqc.communication.data_points.DigitalInputData;
import com.tenkiv.tekdaqc.communication.message.ABoardMessage;
import com.tenkiv.tekdaqc.hardware.AAnalogInput.Gain;
import com.tenkiv.tekdaqc.hardware.AAnalogInput.Rate;
import com.tenkiv.tekdaqc.hardware.AnalogInput_RevD.BufferState;
import com.tenkiv.tekdaqc.locator.LocatorResponse;
import com.tenkiv.tekdaqc.utility.DigitalOutputUtilities;
import com.tenkiv.tekdaqc.utility.DigitalState;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Implementation of {@link ATekdaqc} for revision D and E boards.
 *
 * @author Tenkiv (software@tenkiv.com)
 * @since v1.0.0.0
 */
public class Tekdaqc_RevD extends ATekdaqc {

    /**
     * The number of analog inputs present on the board.
     */
    public static final int ANALOG_INPUT_COUNT = 36;
    /**
     * The number of digital inputs present on the board.
     */
    public static final int DIGITAL_INPUT_COUNT = 24;
    /**
     * The number of digital outputs present on the board.
     */
    public static final int DIGITAL_OUTPUT_COUNT = 16;
    private static final long serialVersionUID = 1L;
    /**
     * The value of the reference voltage on the Tekdaqc
     */
    private static final double REFERENCE_VOLTAGE = 2.5;
    /**
     * The input number for the onboard cold junction sensor.
     */
    private static final int COLD_JUNCTION_PHYSICAL_INPUT = 36;

    /**
     * List of valid configuration settings for the {@link AAnalogInput}s of
     * this board.
     */
    private static final List<Gain> VALID_GAINS = Arrays.asList(
            Gain.X1,
            Gain.X2,
            Gain.X4,
            Gain.X8,
            Gain.X16,
            Gain.X32,
            Gain.X64);

    private static final List<Rate> VALID_RATES = Arrays.asList(
            Rate.SPS_2_5,
            Rate.SPS_5,
            Rate.SPS_10,
            Rate.SPS_15,
            Rate.SPS_25,
            Rate.SPS_30,
            Rate.SPS_50,
            Rate.SPS_60,
            Rate.SPS_100,
            Rate.SPS_500,
            Rate.SPS_1000,
            Rate.SPS_2000,
            Rate.SPS_3750,
            Rate.SPS_7500,
            Rate.SPS_15000,
            Rate.SPS_30000);

    private static final List<BufferState> VALID_BUFFER_STATEs = Arrays.asList(
            BufferState.ENABLED,
            BufferState.DISABLED);

    private static final List<AnalogScale> VALID_ANALOG_SCALEs = Arrays.asList(
            AnalogScale.ANALOG_SCALE_5V,
            AnalogScale.ANALOG_SCALE_400V);

    /**
     * Provided only to support serialization. User code should not use this
     * method.
     */
    public Tekdaqc_RevD() {
        super();
    }

    /**
     * Creates a Tekdaqc board object from the information provided by the
     * {@link LocatorResponse}.
     *
     * @param response {@link LocatorResponse} Response provided by a Tekdaqc locator request.
     */
    public Tekdaqc_RevD(final LocatorResponse response) {
        super(response);
    }

    @Override
    public void connect(CONNECTION_METHOD method) throws IOException {
        super.connect(method);
    }

    @Override
    protected void initializeBoardStatusLists() {

        for (int i = 0; i < ANALOG_INPUT_COUNT; i++) {
            mAnalogInputs.put(i, new AnalogInput_RevD(this, i));
        }

        for (int i = 0; i < DIGITAL_INPUT_COUNT; i++) {
            mDigitalInputs.put(i, new DigitalInput(this, i));
        }

        for (int i = 0; i < DIGITAL_OUTPUT_COUNT; i++) {
            mDigitalOutputs.put(i, new DigitalOutput(this, i));
        }
    }

    @Override
    public void readAnalogInput(int input, int number) {
        mCommandQueue.queueCommand(CommandBuilder.readAnalogInput(input, number));

    }

    @Override
    public void readAnalogInputRange(int start, int end, int number) {
        mCommandQueue.queueCommand(CommandBuilder.readAnalogInputRange(start, end, number));
    }

    @Override
    public void readAnalogInputSet(Set<Integer> inputs, int number) {
        mCommandQueue.queueCommand(CommandBuilder.readAnalogInputSet(inputs, number));
    }

    @Override
    public void readAllAnalogInput(int number) {
        mCommandQueue.queueCommand(CommandBuilder.readAllAnalogInput(number));
    }

    @Override
    public void readDigitalInput(int input, int number) throws IllegalArgumentException {
        mDigitalInputSampleTimer.cancel();
        mDigitalInputSampleTimer.purge();

        mCommandQueue.queueCommand(CommandBuilder.readDigitalInput(input, number));
    }

    @Override
    public void readDigitalInputRange(int start, int end, int number) throws IllegalArgumentException {
        mDigitalInputSampleTimer.cancel();
        mDigitalInputSampleTimer.purge();

        mCommandQueue.queueCommand(CommandBuilder.readDigitalInputRange(start, end, number));
    }

    @Override
    public void readDigitalInputSet(Set<Integer> inputs, int number) throws IllegalArgumentException {
        mDigitalInputSampleTimer.cancel();
        mDigitalInputSampleTimer.purge();

        mCommandQueue.queueCommand(CommandBuilder.readDigitalInputSet(inputs, number));
    }

    @Override
    public void readAllDigitalInput(int number) {
        mDigitalInputSampleTimer.cancel();
        mDigitalInputSampleTimer.purge();

        mCommandQueue.queueCommand(CommandBuilder.readAllDigitalInput(number));
    }

    @Override
    protected void addAnalogInput(AAnalogInput input) throws IllegalArgumentException, IllegalStateException {
        mCommandQueue.queueCommand(CommandBuilder.addAnalogInput(input));
    }

    @Override
    public AAnalogInput activateAnalogInput(int inputNumber) throws IllegalArgumentException, IllegalStateException {
        AAnalogInput input = getAnalogInput(inputNumber);
        input.activate();
        return input;
    }

    @Override
    public DigitalInput activateDigitalInput(int inputNumber) throws IllegalArgumentException, IllegalStateException {
        DigitalInput input = getDigitalInput(inputNumber);
        input.activate();
        return input;
    }

    @Override
    public DigitalOutput toggleDigitalOutput(int outputNumber, boolean status) {
        DigitalOutput output = getDigitalOutput(outputNumber);
        if (status) {
            output.activate();
        } else {
            output.deactivate();
        }
        return output;
    }

    @Override
    public DigitalOutput toggleDigitalOutput(int outputNumber, DigitalState status) {
        DigitalOutput output = getDigitalOutput(outputNumber);
        if (status == DigitalState.LOGIC_HIGH) {
            output.activate();
        } else {
            output.deactivate();
        }
        return output;
    }

    @Override
    public void setDigitalOutput(String binaryString) {
        for (DigitalOutput output : mDigitalOutputs.values()) {
            if (binaryString.charAt(output.getChannelNumber()) == '1') {
                output.setCurrentState(DigitalState.LOGIC_HIGH);
            } else {
                output.setCurrentState(DigitalState.LOGIC_LOW);
            }
        }
        mCommandQueue.queueCommand(CommandBuilder.setDigitalOutputByBinaryString(binaryString));
    }

    @Override
    public void setDigitalOutputByHex(String hex) {
        for (DigitalOutput output : mDigitalOutputs.values()) {
            if (DigitalOutputUtilities.hex_to_binary(hex)
                    .charAt(output.getChannelNumber()) == '1') {
                output.setCurrentState(DigitalState.LOGIC_HIGH);
            } else {
                output.setCurrentState(DigitalState.LOGIC_LOW);
            }
        }
        mCommandQueue.queueCommand(CommandBuilder.setDigitalOutputByHex(hex));
    }

    @Override
    public void setDigitalOutput(boolean[] digitalOutputState) {
        for (DigitalOutput output : mDigitalOutputs.values()) {
            if (digitalOutputState[output.getChannelNumber()]) {
                output.setCurrentState(DigitalState.LOGIC_HIGH);
            } else {
                output.setCurrentState(DigitalState.LOGIC_LOW);
            }
        }
        mCommandQueue.queueCommand(CommandBuilder.setDigitalOutput(digitalOutputState));
    }

    @Override
    public void removeAnalogInput(AAnalogInput input) {
        mCommandQueue.queueCommand(CommandBuilder.removeAnalogInput(input));
    }

    @Override
    public void deactivateAnalogInput(int input) {
        mCommandQueue.queueCommand(CommandBuilder.removeAnalogInputByNumber(input));
    }

    @Override
    public void deactivateAllAddedAnalogInputs() {
        for (ABaseQueueVal queueValue : CommandBuilder.removeMappedAnalogInputs(mAnalogInputs)) {
            mCommandQueue.queueCommand(queueValue);
        }
    }

    @Override
    public void deactivateAllAnalogInputs() {
        for (ABaseQueueVal queueValue : CommandBuilder.deactivateAllAnalogInputs()) {
            mCommandQueue.queueCommand(queueValue);
        }
    }

    @Override
    public void deactivateDigitalInput(DigitalInput input) {
        mCommandQueue.queueCommand(CommandBuilder.removeDigitalInput(input));
    }

    @Override
    public void deactivateDigitalInput(int input) {
        mCommandQueue.queueCommand(CommandBuilder.removeDigitalInputByNumber(input));
    }

    @Override
    public void deactivateAllAddedDigitalInputs() {
        for (ABaseQueueVal queueValue : CommandBuilder.removeMappedDigitalInputs(mDigitalInputs)) {
            mCommandQueue.queueCommand(queueValue);
        }
    }

    @Override
    public void deactivateAllDigitalInputs() {
        for (ABaseQueueVal queueValue : CommandBuilder.deactivateAllDigitalInputs()) {
            mCommandQueue.queueCommand(queueValue);
        }
    }

    @Override
    public void setAnalogInputScale(AnalogScale scale) {
        mCommandQueue.queueCommand(CommandBuilder.setAnalogInputScale(scale));
    }

    @Override
    public void getAnalogInputScale() {
        mCommandQueue.queueCommand(CommandBuilder.getAnalogInputScale());
    }

    @Override
    protected void addDigitalInput(DigitalInput input) throws IllegalArgumentException, IllegalStateException {
        mCommandQueue.queueCommand(CommandBuilder.addDigitalInput(input));
    }

    public void systemGainCalibrate(int input) {
        mCommandQueue.queueCommand(CommandBuilder.systemGainCalibrate(input));
    }

    /**
     * Instructs the Tekdaqc to return the current gain calibration value stored
     * in the ADC.
     */
    @Override
    public void readSystemGainCalibration() {
        mCommandQueue.queueCommand(CommandBuilder.readSystemGainCalibration());
    }

    /**
     * Instructs the Tekdaqc to return the saved base gain calibration value for
     * the specified sampling parameters.
     *
     * @param gain   {@link Gain} The gain to retrieve the calibration for.
     * @param rate   {@link Rate} The rate to retrieve the calibration for.
     * @param buffer {@link BufferState} The buffer state to retrieve the
     *               calibration for.
     */
    public void readSelfGainCalibration(Gain gain, Rate rate, BufferState buffer) {
        mCommandQueue.queueCommand(CommandBuilder.readSelfGainCalibration(gain, rate, buffer));
    }

    @Override
    public void upgrade() {
        mCommandQueue.queueCommand(CommandBuilder.upgrade());
    }

    @Override
    public void identify() {
        mCommandQueue.queueCommand(CommandBuilder.identify());
    }

    @Override
    public void sample(int number) {
        mCommandQueue.queueCommand(CommandBuilder.sample(number));
    }

    @Override
    public void halt() {
        mCommandQueue.queueCommand(new BlankQueueValue(Commands.HALT));
    }

    @Override
    public void setRTC(long timestamp) {
        mCommandQueue.queueCommand(CommandBuilder.setRTC(timestamp));
    }

    @Override
    public void readADCRegisters() {
        mCommandQueue.queueCommand(CommandBuilder.readADCRegisters());
    }

    @Override
    public void getCalibrationStatus() {
        mCommandQueue.queueCommand(CommandBuilder.getCalibrationStatus());
    }

    @Override
    public void writeCalibrationTemperature(double temp, int index) {
        mCommandQueue.queueCommand(CommandBuilder.writeCalibrationTemperature(temp, index));
    }

    @Override
    public void writeGainCalibrationValue(float value, Gain gain, Rate rate, BufferState buffer, AnalogScale scale, int temp) {
        mCommandQueue.queueCommand(CommandBuilder.writeGainCalibrationValue(value, gain, rate, buffer, scale, temp));
    }

    @Override
    public void writeCalibrationValid() {
        mCommandQueue.queueCommand(new BlankQueueValue(Commands.WRITE_CALIBRATION_VALID));
    }

    @Override
    public AAnalogInput getAnalogInput(int input) {
        if (input >= 0 && input < ANALOG_INPUT_COUNT) {
            return mAnalogInputs.get(input);
        } else {
            throw new IllegalArgumentException("The requested physical analog input is out of range: " + input);
        }
    }

    @Override
    public double convertAnalogInputDataToVoltage(AnalogInputData data, AnalogScale scale) {
        AAnalogInput analogInput = getAnalogInput(data.getPhysicalInput());
        double ratio = (data.getData() / 8388607.0);
        double gainDivisor = 1.0 / Integer.valueOf(analogInput.getGain().toString());
        double multiplier = 2.0 * REFERENCE_VOLTAGE;
        double scaleMultiplier = getAnalogScaleMultiplier(scale);
        return (multiplier * ratio * gainDivisor * scaleMultiplier);
    }

    @Override
    public double convertAnalogInputDataToTemperature(AnalogInputData data) {
        final double voltage = convertAnalogInputDataToVoltage(data, AnalogScale.ANALOG_SCALE_5V);
        return (voltage / 0.010); // LM35 output is 10mV/Deg C
    }

    @Override
    public int getColdJunctionInputNumber() {
        return COLD_JUNCTION_PHYSICAL_INPUT;
    }

    @Override
    public double getAnalogScaleMultiplier(AnalogScale scale) {
        switch (scale) {
            case ANALOG_SCALE_5V:
                return 1.0;
            case ANALOG_SCALE_400V:
                return 80.0;
            default:
                throw new IllegalArgumentException("Unrecognized analog input scale.");
        }
    }

    /**
     * Method to get a {@link List of all the valid {@link Gain} for this board revision.}
     *
     * @return A {@link List} of {@link Gain}.
     */
    public List<Gain> getValidGains() {
        return VALID_GAINS;
    }

    /**
     * Method to get a {@link List of all the valid {@link Rate} for this board revision.}
     *
     * @return A {@link List} of {@link Rate}.
     */
    public List<Rate> getValidRates() {
        return VALID_RATES;
    }

    /**
     * Method to get a {@link List of all the valid {@link BufferState} for this board revision.}
     *
     * @return A {@link List} of {@link BufferState}.
     */
    public List<BufferState> getValidBufferStates() {
        return VALID_BUFFER_STATEs;
    }

    /**
     * Method to get a {@link List of all the valid {@link com.tenkiv.tekdaqc.hardware.ATekdaqc.AnalogScale} for this board revision.}
     *
     * @return A {@link List} of {@link com.tenkiv.tekdaqc.hardware.ATekdaqc.AnalogScale}.
     */
    public List<AnalogScale> getValidAnalogScales() {
        return VALID_ANALOG_SCALEs;
    }

    @Override
    protected void readIn(ObjectInput input) throws IOException, ClassNotFoundException {

    }

    @Override
    protected void writeOut(ObjectOutput output) throws IOException {

    }

    @Override
    public void onParsingComplete(ABoardMessage message) {
        switch (message.getType()) {
            case DEBUG: // Fall through for all message types
            case STATUS:
            case ERROR:
            case COMMAND_DATA:
            case DIGITAL_OUTPUT_DATA:
                messageBroadcaster.broadcastMessage(this, message);
                break;
            case ANALOG_INPUT_DATA:
                final DataPoint analogInputData = ((ASCIIAnalogInputDataMessage) message).toDataPoints();
                messageBroadcaster.broadcastAnalogInputDataPoint(this, (AnalogInputData) analogInputData);
                    /*ASCIIMessageUtils.returnMessage((AASCIIMessage) message);*/
                break;
            case DIGITAL_INPUT_DATA:
                final DataPoint digitalInputData = ((ASCIIDigitalInputDataMessage) message).toDataPoints();
                messageBroadcaster.broadcastDigitalInputDataPoint(this, (DigitalInputData) digitalInputData);
                    /*ASCIIMessageUtils.returnMessage((AASCIIMessage) message);*/
                break;
        }
    }

    @Override
    public void onMessageDetetced(String message) {
        super.onMessageDetetced(message);
        mParsingExecutor.parseMessage(message, this);
    }
}

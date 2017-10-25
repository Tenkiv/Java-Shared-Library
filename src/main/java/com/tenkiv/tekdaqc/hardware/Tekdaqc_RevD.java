package com.tenkiv.tekdaqc.hardware;

import com.tenkiv.tekdaqc.communication.ascii.message.parsing.ASCIIAnalogInputDataMessage;
import com.tenkiv.tekdaqc.communication.ascii.message.parsing.ASCIIDigitalInputDataMessage;
import com.tenkiv.tekdaqc.communication.ascii.message.parsing.ASCIIErrorMessage;
import com.tenkiv.tekdaqc.communication.ascii.message.parsing.ASCIIPWMInputDataMessage;
import com.tenkiv.tekdaqc.communication.command.queue.Commands;
import com.tenkiv.tekdaqc.communication.command.queue.values.QueueValue;
import com.tenkiv.tekdaqc.communication.data_points.AnalogInputCountData;
import com.tenkiv.tekdaqc.communication.data_points.DataPoint;
import com.tenkiv.tekdaqc.communication.data_points.DigitalInputData;
import com.tenkiv.tekdaqc.communication.data_points.PWMInputData;
import com.tenkiv.tekdaqc.communication.message.ABoardMessage;
import com.tenkiv.tekdaqc.hardware.AAnalogInput.Gain;
import com.tenkiv.tekdaqc.hardware.AAnalogInput.Rate;
import com.tenkiv.tekdaqc.hardware.AnalogInput_RevD.BufferState;
import com.tenkiv.tekdaqc.locator.LocatorResponse;
import com.tenkiv.tekdaqc.utility.DigitalOutputUtilities;
import org.jetbrains.annotations.NotNull;

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
    public static final int ANALOG_INPUT_COUNT = 32;

    /**
     * The channel number of the analog input temperature sensor.
     */
    public static final int ANALOG_INPUT_TEMP_SENSOR = 36;

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
     * The analog multiplier for 5V scale.
     */
    private static final double ANALOG_SCALE_MULTIPLIER_5V = 1.0;

    /**
     * The analog multiplier for 400V scale.
     */
    private static final double ANALOG_SCALE_MULTIPLIER_400V = 80.0;

    /**
     * The input number for the onboard cold junction sensor.
     */
    private static final int COLD_JUNCTION_PHYSICAL_INPUT = 36;

    /**
     * List of valid configuration settings for the {@link Gain}s of the {@link AAnalogInput}s of
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

    /**
     * List of valid configuration settings for the {@link Rate}s of the {@link AAnalogInput}s of
     * this board.
     */
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
            Rate.SPS_7500);

    /**
     * List of valid configuration settings for the {@link BufferState}s of the {@link AAnalogInput}s of
     * this board.
     */
    private static final List<BufferState> VALID_BUFFER_STATEs = Arrays.asList(
            BufferState.ENABLED,
            BufferState.DISABLED);

    /**
     * List of valid configuration settings for the {@link AnalogScale}s of the {@link AAnalogInput}s of
     * this board.
     */
    private static final List<AnalogScale> VALID_ANALOG_SCALEs = Arrays.asList(
            AnalogScale.ANALOG_SCALE_5V,
            AnalogScale.ANALOG_SCALE_400V);

    /**
     * The temperature reference for the Tekdaqc.
     */
    private TemperatureReference_RevD temperatureReference = new TemperatureReference_RevD(this);

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

    @NotNull
    @Override
    public TemperatureReference_RevD getTemperatureReference() {
        return temperatureReference;
    }

    @Override
    public void connect(AnalogScale analogScale, CONNECTION_METHOD method) throws IOException {
        super.connect(analogScale, method);
    }

    @Override
    protected void initializeBoardStatusLists() {

        for (int i = 0; i < ANALOG_INPUT_COUNT; i++) {
            getAnalogInputs().put(i, new AnalogInput_RevD(this, i));
        }

        for (int i = 0; i < DIGITAL_INPUT_COUNT; i++) {
            getDigitalInputs().put(i, new DigitalInput(this, i));
        }

        for (int i = 0; i < DIGITAL_OUTPUT_COUNT; i++) {
            getDigitalOutputs().put(i, new DigitalOutput(this, i));
        }
    }

    @Override
    public void readAnalogInput(final int input, final int number) {
        queueCommand(CommandBuilder.INSTANCE.readAnalogInput(input, number));

    }

    @Override
    public void readAnalogInputRange(final int start, final int end, final int number) {
        queueCommand(CommandBuilder.INSTANCE.readAnalogInputRange(start, end, number));
    }

    @Override
    public void readAnalogInputSet(final Set<Integer> inputs, final int number) {
        queueCommand(CommandBuilder.INSTANCE.readAnalogInputSet(inputs, number));
    }

    @Override
    public void readAllAnalogInput(final int number) {
        queueCommand(CommandBuilder.INSTANCE.readAllAnalogInput(number));
    }

    @Override
    public void readDigitalInput(final int input, final int number) throws IllegalArgumentException {
        haltThrottedDigitalInputReading();
        queueCommand(CommandBuilder.INSTANCE.readDigitalInput(input, number));
    }

    @Override
    public void readDigitalInputRange(final int start, final int end, final int number) throws IllegalArgumentException {
        haltThrottedDigitalInputReading();
        queueCommand(CommandBuilder.INSTANCE.readDigitalInputRange(start, end, number));
    }

    @Override
    public void readDigitalInputSet(final Set<Integer> inputs, final int number) throws IllegalArgumentException {
        haltThrottedDigitalInputReading();
        queueCommand(CommandBuilder.INSTANCE.readDigitalInputSet(inputs, number));
    }

    @Override
    public void readAllDigitalInput(final int number) {
        haltThrottedDigitalInputReading();
        queueCommand(CommandBuilder.INSTANCE.readAllDigitalInput(number));
    }

    @Override
    protected void addAnalogInput(final AAnalogInput input) throws IllegalArgumentException, IllegalStateException {
        input.activate();
    }

    @Override
    public AAnalogInput activateAnalogInput(final int inputNumber) throws IllegalArgumentException, IllegalStateException {
        final AAnalogInput input = getAnalogInput(inputNumber);
        input.activate();
        return input;
    }

    @Override
    public DigitalInput activateDigitalInput(final int inputNumber) throws IllegalArgumentException, IllegalStateException {
        final DigitalInput input = getDigitalInput(inputNumber);
        input.activate();
        return input;
    }

    @Override
    public DigitalOutput toggleDigitalOutput(final int outputNumber, final boolean isOn) {
        final DigitalOutput output = getDigitalOutput(outputNumber);
        if (isOn) {
            output.activate();
        } else {
            output.deactivate();
        }
        return output;
    }

    @Override
    public void setDigitalOutput(final String binaryString) {
        for (final DigitalOutput output : getDigitalOutputs().values()) {
            if (binaryString.charAt(output.getChannelNumber()) == '1') {
                output.setIsActive(true);
            } else {
                output.setIsActive(false);
            }
        }
        queueCommand(CommandBuilder.INSTANCE.setDigitalOutputByBinaryString(binaryString));
    }

    @Override
    public void setDigitalOutputByHex(final String hex) {
        for (final DigitalOutput output : getDigitalOutputs().values()) {
            if (DigitalOutputUtilities.hexToBinary(hex)
                    .charAt(output.getChannelNumber()) == '1') {
                output.setIsActive(true);
            } else {
                output.setIsActive(false);
            }
        }
        queueCommand(CommandBuilder.INSTANCE.setDigitalOutputByHex(hex));
    }

    @Override
    public void setDigitalOutput(final boolean[] digitalOutputState) {
        for (final DigitalOutput output : getDigitalOutputs().values()) {
            if (digitalOutputState[output.getChannelNumber()]) {
                output.setIsActive(true);
            } else {
                output.setIsActive(false);
            }
        }
        queueCommand(CommandBuilder.INSTANCE.setDigitalOutput(digitalOutputState));
    }

    @Override
    public void removeAnalogInput(final AAnalogInput input) {
        input.deactivate();
    }

    @Override
    public void removeDigitalInput(final DigitalInput input) {
        input.deactivate();
    }

    @Override
    public void deactivateAnalogInput(final int input) {
        getAnalogInput(input).deactivate();
    }

    @Override
    public void deactivateAllAddedAnalogInputs() {
        getAnalogInputs().values().stream().filter(it -> it.isActivated).forEach(AAnalogInput::deactivate);
    }

    @Override
    public void deactivateAllAnalogInputs() {
        getAnalogInputs().values().forEach(AAnalogInput::deactivate);
    }

    @Override
    public void deactivateDigitalInput(final DigitalInput input) {
        input.deactivate();
    }

    @Override
    public void deactivateDigitalInput(final int input) {
        getDigitalInput(input).deactivate();
    }

    @Override
    public void deactivateAllAddedDigitalInputs() {
        getDigitalInputs().values().stream().filter(it -> it.isActivated).forEach(DigitalInput::deactivate);
    }

    @Override
    public void deactivateAllDigitalInputs() {
        getDigitalInputs().values().forEach(DigitalInput::deactivate);
    }

    @Override
    protected void addDigitalInput(final DigitalInput input) throws IllegalArgumentException, IllegalStateException {
        input.activate();
    }

    public void systemGainCalibrate(final int input) {
        queueCommand(CommandBuilder.INSTANCE.systemGainCalibrate(input));
    }

    /**
     * Instructs the Tekdaqc to return the current gain calibration value stored
     * in the ADC.
     */
    @Override
    public void readSystemGainCalibration() {
        queueCommand(CommandBuilder.INSTANCE.readSystemGainCalibration());
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
        queueCommand(CommandBuilder.INSTANCE.readSelfGainCalibration(gain, rate, buffer));
    }

    @Override
    public void upgrade() {
        queueCommand(CommandBuilder.INSTANCE.upgrade());
    }

    @Override
    public void identify() {
        queueCommand(CommandBuilder.INSTANCE.identify());
    }

    @Override
    public void sample(int number) {
        queueCommand(CommandBuilder.INSTANCE.sample(number));
    }

    @Override
    public void halt() {
        queueCommand(new QueueValue(Commands.HALT.getOrdinalCommandType()));
    }

    @Override
    public void setRTC(long timestamp) {
        queueCommand(CommandBuilder.INSTANCE.setRTC(timestamp));
    }

    @Override
    public void readADCRegisters() {
        queueCommand(CommandBuilder.INSTANCE.readADCRegisters());
    }

    @Override
    public void getCalibrationStatus() {
        queueCommand(CommandBuilder.INSTANCE.getCalibrationStatus());
    }

    @Override
    public void writeCalibrationTemperature(final double temp, final int index) {
        queueCommand(CommandBuilder.INSTANCE.writeCalibrationTemperature(temp, index));
    }

    @Override
    public void writeGainCalibrationValue(final float value, final Gain gain, final Rate rate,
                                          final BufferState buffer, final AnalogScale scale, final int temp) {
        queueCommand(CommandBuilder.INSTANCE.writeGainCalibrationValue(value, gain, rate, buffer, scale, temp));
    }

    @Override
    public void writeCalibrationValid() {
        queueCommand(new QueueValue(Commands.WRITE_CALIBRATION_VALID.getOrdinalCommandType()));
    }

    @Override
    public AAnalogInput getAnalogInput(final int input) {
        if ((input >= 0 && input < ANALOG_INPUT_COUNT)) {
            return getAnalogInputs().get(input);
        } else if (input == ANALOG_INPUT_TEMP_SENSOR) {
            return temperatureReference;
        } else {
            throw new IllegalArgumentException("The requested physical analog input is out of range: " + input);
        }
    }

    @Override
    public double convertAnalogInputDataToVoltage(final AnalogInputCountData data, final AnalogScale scale) {
        final AAnalogInput analogInput = getAnalogInput(data.getPhysicalInput());
        final double ratio = (data.getData() / 8388607.0);
        final double gainDivisor = 1.0 / analogInput.getGain().gain;
        final double multiplier = 2.0 * REFERENCE_VOLTAGE;
        return (multiplier * ratio * gainDivisor * getAnalogScaleMultiplier(scale));
    }

    @Override
    public double convertAnalogInputDataToTemperature(final AnalogInputCountData data) {
        final double voltage = convertAnalogInputDataToVoltage(data, AnalogScale.ANALOG_SCALE_5V);
        return (voltage / 0.010); // LM35 outpuSAMPLE --NUMBER=0t is 10mV/Deg C
    }

    @Override
    public int getColdJunctionInputNumber() {
        return COLD_JUNCTION_PHYSICAL_INPUT;
    }

    @Override
    public double getAnalogScaleMultiplier(final AnalogScale scale) {
        switch (scale) {
            case ANALOG_SCALE_5V:
                return ANALOG_SCALE_MULTIPLIER_5V;
            case ANALOG_SCALE_400V:
                return ANALOG_SCALE_MULTIPLIER_400V;
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

    /**
     * Not overridden in this hardware revision.
     *
     * @param input The ObjectInput
     * @throws IOException            IOException
     * @throws ClassNotFoundException Failed to cast class.
     */
    @Override
    protected void readIn(final ObjectInput input) throws IOException, ClassNotFoundException {
    }

    /**
     * Not overridden in this hardware revision.
     *
     * @param output The ObjectInput
     * @throws IOException IOException
     */
    @Override
    protected void writeOut(final ObjectOutput output) throws IOException {
    }

    @Override
    public void onParsingComplete(final ABoardMessage message) {
        switch (message.getType()) {
            case DEBUG: // Fall through for all message types
            case STATUS:
            case COMMAND_DATA:
            case DIGITAL_OUTPUT_DATA:
                getMessageBroadcaster().broadcastMessage(this, message);
                break;
            case ERROR:
                if (((ASCIIErrorMessage) message).isNetworkError) {
                    getMessageBroadcaster().broadcastNetworkError(this, message);
                }
                getMessageBroadcaster().broadcastMessage(this, message);
                break;
            case ANALOG_INPUT_DATA:
                final DataPoint analogInputData = ((ASCIIAnalogInputDataMessage) message).toDataPoints();
                getMessageBroadcaster().broadcastAnalogInputDataPoint(this, (AnalogInputCountData) analogInputData);
                break;
            case DIGITAL_INPUT_DATA:
                final DataPoint digitalInputData = ((ASCIIDigitalInputDataMessage) message).toDataPoints();
                getMessageBroadcaster().broadcastDigitalInputDataPoint(this, (DigitalInputData) digitalInputData);
                break;
            case PWM_INPUT_DATA:
                final DataPoint pwmInputData = ((ASCIIPWMInputDataMessage) message).toDataPoints();
                getMessageBroadcaster().broadcastPWMInputDataPoint(this, (PWMInputData) pwmInputData);
                break;
            default:
                break;
        }
    }

    @Override
    public void onMessageDetected(final String message) {
        super.onMessageDetected(message);
        getParsingExecutor().parseMessage(message, this);
    }

    @Override
    public int getAnalogInputCount() {
        return ANALOG_INPUT_COUNT;
    }

    @Override
    public int getDigitalInputCount() {
        return DIGITAL_INPUT_COUNT;
    }

    @Override
    public int getDigitalOutputCount() {
        return DIGITAL_OUTPUT_COUNT;
    }

    @Override
    public int getAnalogTemperatureReferenceChannel() {
        return ANALOG_INPUT_TEMP_SENSOR;
    }
}

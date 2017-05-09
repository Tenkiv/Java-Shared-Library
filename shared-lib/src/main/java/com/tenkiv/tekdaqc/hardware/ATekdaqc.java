package com.tenkiv.tekdaqc.hardware;

import com.tenkiv.tekdaqc.communication.ascii.executors.ASCIIParsingExecutor;
import com.tenkiv.tekdaqc.communication.command.queue.*;
import com.tenkiv.tekdaqc.communication.command.queue.values.ABaseQueueVal;
import com.tenkiv.tekdaqc.communication.command.queue.values.IQueueObject;
import com.tenkiv.tekdaqc.communication.data_points.AnalogInputCountData;
import com.tenkiv.tekdaqc.communication.executors.AParsingExecutor.IParsingListener;
import com.tenkiv.tekdaqc.communication.executors.ReadExecutor;
import com.tenkiv.tekdaqc.communication.message.*;
import com.tenkiv.tekdaqc.communication.tasks.ITaskComplete;
import com.tenkiv.tekdaqc.hardware.AAnalogInput.Gain;
import com.tenkiv.tekdaqc.hardware.AAnalogInput.Rate;
import com.tenkiv.tekdaqc.hardware.AnalogInput_RevD.BufferState;
import com.tenkiv.tekdaqc.locator.Locator;
import com.tenkiv.tekdaqc.locator.LocatorResponse;
import com.tenkiv.tekdaqc.telnet.client.EthernetTelnetConnection;
import com.tenkiv.tekdaqc.telnet.client.ITekdaqcTelnetConnection;
import com.tenkiv.tekdaqc.telnet.client.SerialTelnetConnection;
import com.tenkiv.tekdaqc.telnet.client.USBTelnetConnection;
import com.tenkiv.tekdaqc.utility.CriticalErrorListener;
import com.tenkiv.tekdaqc.utility.TekdaqcCriticalError;
import com.tenkiv.tekdaqc.utility.UtilityKt;
import tec.uom.se.unit.Units;

import javax.measure.Quantity;
import javax.measure.quantity.Dimensionless;
import java.io.*;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Class which contains information about a specific Tekdaqc and provides
 * methods for interacting with it.
 *
 * @author Tenkiv (software@tenkiv.com)
 * @since 1.0.0.0
 */
public abstract class ATekdaqc implements Externalizable, IParsingListener {

    /**
     * Maximum length of a channel name.
     */
    public static final int MAX_NAME_LENGTH = 24;

    /**
     * The singleton instance.
     */
    protected final static MessageBroadcaster messageBroadcaster = new MessageBroadcaster();

    /**
     * Required for serialization
     */
    private static final long serialVersionUID = 1L;

    /**
     * Maps of inputs/outputs
     */
    protected final Map<Integer, AAnalogInput> mAnalogInputs;
    protected final Map<Integer, DigitalInput> mDigitalInputs;
    protected final Map<Integer, DigitalOutput> mDigitalOutputs;

    /**
     * The executor responsible for reading the input stream of the Tekdaqc
     */
    protected ReadExecutor mReadExecutor;

    /**
     * The executor responsible for parsing split messages
     */
    protected ASCIIParsingExecutor mParsingExecutor;

    /**
     * The digital input rate for throttled sampling.
     */
    protected transient int mDigitalInputRate = 1000;

    /**
     * The current {@link AnalogScale}
     */
    protected transient AnalogScale mAnalogScale;

    /**
     * The current {@link COMMUNICATION_ENCODING} used by the {@link ATekdaqc}.
     * Default is {@link COMMUNICATION_ENCODING#ASCII}.
     */
    protected COMMUNICATION_ENCODING mCurrentEncoding = COMMUNICATION_ENCODING.ASCII;

    /**
     * The {@link LocatorResponse} used to generate this {@link ATekdaqc}.
     */
    protected LocatorResponse mResponse;

    /**
     * The {@link ICommandManager} which controls who commands to be executed are handled.
     */
    protected transient ICommandManager mCommandQueue;

    /**
     * The Telnet connection.
     */
    protected transient ITekdaqcTelnetConnection mConnection;

    /**
     * The read stream for the telnet connection.
     */
    protected transient InputStream mReadStream;

    /**
     * The write stream for the telnet connection.
     */
    protected transient OutputStream mWriteStream;

    /**
     * The {@link Timer} used when using throttled sampling.
     */
    protected Timer mDigitalInputSampleTimer;

    /**
     * The list of {@link CriticalErrorListener}s.
     */
    private List<CriticalErrorListener> mCriticalErrorListener = new ArrayList<>();

    /**
     * The number of samples currently taken by throttled sampling.
     */
    protected volatile int mThrottledSamples = -1;

    /**
     * Interval of the watchdog timer.
     */
    protected static final int HEARTBEAT_TIMER_INTERVAL = 5000;

    /**
     * Boolean for if the tekdaqc is connected in the current tick.
     */
    protected boolean tentativeIsConnected = false;

    /**
     * If the keep alive packet was sent.
     */
    protected boolean keepAlivePacketSent = false;

    /**
     * If the tekdaqc is connected.
     */
    protected boolean isConnected = false;

    /**
     * Method to get the total number of analog inputs, not including the board's temperature sensor.
     *
     * @return The number of analog inputs.
     */
    abstract int getAnalogInputCount();

    /**
     * Method to gte the total number of digital inputs.
     *
     * @return The number of digital inputs.
     */
    abstract int getDigitalInputCount();

    /**
     * Method to gte the number of digital outputs.
     *
     * @return The number of digital outputs.
     */
    abstract int getDigitalOutputCount();

    /**
     * Method to return the channel number of the tekdaqc's temperature sensor.
     *
     * @return The channel number of the tekdaqc's temperature sensor.
     */
    abstract int getAnalogTemperatureSensorChannel();

    /**
     * A {@link TimerTask} to be executed when attempting to use throttled sampling.
     */
    protected TimerTask mDigitalInputActivationTask = new TimerTask() {

        @Override
        public void run() {
            if (mConnection.isConnected()) {
                mCommandQueue.queueCommand(CommandBuilderKt.readAllDigitalInput(1));

                if (mThrottledSamples > 0) {
                    mThrottledSamples--;

                } else if (mThrottledSamples == 0) {
                    mDigitalInputSampleTimer = UtilityKt.reprepare(mDigitalInputSampleTimer);
                }
            }
        }
    };

    /**
     * Heartbeat timer to check for disconnection from the tekdaqc.
     */
    protected Timer mHeartbeatTimer = new Timer("Tekdaqc Heartbeat Timer",true);

    /**
     * A {@link TimerTask} to be executed for checking to see if the Tekdaqc connection is active.
     */
    protected TimerTask mHeartbeatTimerTask = new TimerTask() {
        @Override
        public void run() {
            if (keepAlivePacketSent && !tentativeIsConnected) {
                isConnected = false;
                criticalErrorNotification(TekdaqcCriticalError.TERMINAL_CONNECTION_DISRUPTION);
            } else if (!keepAlivePacketSent && !tentativeIsConnected) {
                keepAlivePacketSent = true;
                queueCommand(CommandBuilderKt.none());
            } else if (tentativeIsConnected) {
                isConnected = true;
                tentativeIsConnected = false;
            }
        }
    };


    /**
     * This constructor provided solely to support serialization. User code
     * should never utilize it.
     */
    public ATekdaqc() {
        mAnalogInputs = new HashMap<Integer, AAnalogInput>();
        mDigitalInputs = new HashMap<Integer, DigitalInput>();
        mDigitalOutputs = new HashMap<Integer, DigitalOutput>();

        initializeBoardStatusLists();

        mCommandQueue = getCommandManager();

        mParsingExecutor = getParsingExecutor();

        mDigitalInputSampleTimer = new Timer("Digital Input Sample Timer",true);
    }

    /**
     * Adds a new {@link CriticalErrorListener} to be notified of any catastrophic events.
     *
     * @param listener The listener to be added.
     */
    public void addCriticalFailureListener(CriticalErrorListener listener){
        mCriticalErrorListener.add(listener);
    }

    /**
     * Removes a {@link CriticalErrorListener} from being notified of events.
     *
     * @param listener The listener to be removed.
     */
    public void removeCriticalFailureListener(CriticalErrorListener listener){
        mCriticalErrorListener.remove(listener);
    }

    /**
     * Notifies all {@link CriticalErrorListener}s of something awful happening.
     *
     * @param error The {@link TekdaqcCriticalError} which has caused such a problem.
     */
    public void criticalErrorNotification(TekdaqcCriticalError error){
        for(CriticalErrorListener listener: mCriticalErrorListener){
            listener.onTekdaqcCriticalError(error);
        }
    }

    /**
     * Creates a Tekdaqc board object from the information provided by the
     * {@link LocatorResponse}.
     *
     * @param response {@link LocatorResponse} Response provided by a Tekdaqc locator request.
     */
    public ATekdaqc(final LocatorResponse response) {
        this();
        mResponse = response;
    }

    /**
     * Method to set {@link MessageBroadcaster}'s callback {@link Executor} to a new value. This should only be done if
     * no messages are queued to be sent. It is recommended that this method only be called after the {@link ATekdaqc#halt()}
     * command. This method is not for general use cases and can result in severe issues if used improperly.
     *
     * @param callbackExecutor The new {@link Executor} to be used for callbacks.
     */
    public void setMessageBroadcasterCallbackExecutor(Executor callbackExecutor){
        messageBroadcaster.setCallbackExecutor(callbackExecutor);
    }

    @Override
    public void onMessageDetetced(final String message) {
        if (keepAlivePacketSent) {
            keepAlivePacketSent = false;
        }
        tentativeIsConnected = true;
    }

    /**
     * Gets the {@link ICommandManager} for this {@link ATekdaqc}.
     *
     * @return The {@link ICommandManager} for the {@link ATekdaqc}.
     */
    protected ICommandManager getCommandManager(){
        return new CommandQueueManager(this);
    }

    /**
     * Generates a binary string of all {@link DigitalOutput}s this board controls.
     *
     * @return A Binary {@link String} representing the state of the {@link DigitalOutput}s.
     */
    protected String generateBinaryStringFromOutput() {
        StringBuilder builder = new StringBuilder();

        for(int i=0; i< getDigitalOutputCount(); i++){
            builder.append(0);
        }

        for (int outputNumber : mDigitalOutputs.keySet()) {
            if (mDigitalOutputs.get(outputNumber).getIsActivated()) {
                builder.replace(outputNumber,outputNumber+1, "1");
            }
        }
        return builder.toString();
    }

    /**
     * Method to return the {@link ATekdaqc}'s parsing executor.
     *
     * @return The {@link ASCIIParsingExecutor} the tekdaqc owns.
     */
    protected ASCIIParsingExecutor getParsingExecutor() {
        return new ASCIIParsingExecutor(1);
    }

    /**
     * Method reads the digital inputs at a reduced rate such that slower devices can process the output.
     * Throttled sampling must be halted by calling haltThrottledDigitalInputReading().
     *
     * @param rateMillis Rate at which to read the digital inputs in samples/millisecond.
     * @throws IllegalArgumentException Exception thrown if specified rate is less them 0
     */
    public void readThrottledDigitalInput(int rateMillis) throws IllegalArgumentException {
        if (rateMillis > 0) {
            mDigitalInputRate = rateMillis;
            mDigitalInputSampleTimer = UtilityKt.reprepare(mDigitalInputSampleTimer);
            mDigitalInputSampleTimer.schedule(mDigitalInputActivationTask, mDigitalInputRate);

        } else {
            throw new IllegalArgumentException("Specified rate must be greater then 0.");
        }
    }

    /**
     * Attempts disconnect and reconnect to the Tekdaqc in the case of a critical failure. Can attempt to restore
     * the state of all {@link AAnalogInput}s and {@link DigitalInput}s. Throws {@link CriticalErrorListener}s if it is
     * unable to successfully do so.
     *
     * @param millisTimeout Time to wait for reconnection in milliseconds,
     * @param reactivateChannels If channels should be reactivated after reconnection.
     */
    public void restoreTekdaqc(final long millisTimeout, final boolean reactivateChannels){
        try {
            mCommandQueue.purge(false);

            mConnection.disconnect();

            mReadStream = null;
            mWriteStream = null;

            Locator.Companion.getInstance()
                    .blockingSearchForSpecificTekdaqcs(
                            millisTimeout,
                            new ReentrantLock(),
                            false,
                            null,
                            getSerialNumber())
                    .get(0).connect(mAnalogScale,CONNECTION_METHOD.ETHERNET);


            List<ABaseQueueVal> commands = new ArrayList<ABaseQueueVal>();

            commands.addAll(CommandBuilderKt.deactivateAllAnalogInputs());

            commands.addAll(CommandBuilderKt.deactivateAllDigitalInputs());

            if(reactivateChannels) {

                for(AAnalogInput input: mAnalogInputs.values()){
                    if (input.isActivated) {
                        commands.add(CommandBuilderKt.addAnalogInput(input));
                    }
                }

                for(DigitalInput input: mDigitalInputs.values()){
                    if (input.isActivated) {
                        commands.add(CommandBuilderKt.addDigitalInput(input));
                    }
                }
            }

            queueTask(new Task(new ITaskComplete() {
                @Override
                public void onTaskSuccess(ATekdaqc tekdaqc) {

                }

                @Override
                public void onTaskFailed(ATekdaqc tekdaqc) {
                    criticalErrorNotification(TekdaqcCriticalError.FAILED_TO_REINITIALIZE);
                }
            },commands));


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method reads the digital inputs at a reduced rate such that slower devices can process the output.
     * Throttled sampling must be halted by calling haltThrottledDigitalInputReading().
     *
     * @param rateMillis Rate at which to read the digital inputs in samples/millisecond.
     * @param samples    Number of samples to take.
     * @throws IllegalArgumentException Rate must be greater then 0.
     */
    public void readThrottledDigitalInput(int rateMillis, int samples) throws IllegalArgumentException {
        mThrottledSamples = samples;
        readThrottledDigitalInput(rateMillis);
    }

    /**
     * Method to halt the throttled sampling of the digital inputs.
     */
    public void haltThrottedDigitalInputReading() {
        mDigitalInputSampleTimer = UtilityKt.reprepare(mDigitalInputSampleTimer);
    }

    /**
     * Method returning the current throttled digital input rate in samples/millisecond..
     *
     * @return An {@link Integer} of the throttled rate in samples/millisecond.
     */
    public int getThrottledDigitalInputSampleRate() {
        return mDigitalInputRate;
    }

    /**
     * Retrieve the board's Firmware version string.
     *
     * @return {@link String} The firmware version.
     */
    public LocatorResponse getLocatorResponse() {
        return mResponse;
    }

    /**
     * Retrieve the board's Firmware version string.
     *
     * @return {@link String} The firmware version.
     */
    public String getFirmwareVersion() {
        return mResponse.getFirwareVersion();
    }

    /**
     * Retrieve the board's revision code.
     *
     * @return char The boards revision code.
     */
    public char getRevisionType() {
        return (char) mResponse.getType();
    }

    /**
     * Retrieve the board's IP address.
     *
     * @return {@link String} The board's IP address.
     */
    public String getHostIP() {
        return mResponse.getHostIP();
    }

    /**
     * Retrieve the boards MAC address.
     *
     * @return {@link String} The board's MAC address.
     */
    public String getMACAddress() {
        return mResponse.getMacAddress();
    }

    /**
     * Retrieve the serial number of this {@link ATekdaqc}.
     *
     * @return {@link String} The serial number.
     */
    public String getSerialNumber() {
        return mResponse.getSerial();
    }

    /**
     * Retrieve the board's application title.
     *
     * @return {@link String} The application title.
     */
    public String getTitle() {
        return mResponse.getTitle();
    }

    /**
     * Retrieves the current encoding mode of this Tekdaqc.
     *
     * @return {@link COMMUNICATION_ENCODING} The current encoding.
     */
    public COMMUNICATION_ENCODING getCurrentEncoding() {
        return mCurrentEncoding;
    }

    /**
     * Fetches an analog input from this Tekdaqc.
     *
     * @param input int The input to fetch from the board.
     * @return The {@link AAnalogInput} for the specified physical input channel.
     */
    public AAnalogInput getAnalogInput(int input) {
        return mAnalogInputs.get(input);
    }

    /**
     * Fetches a digital input from this Tekdaqc.
     *
     * @param input int The input to fetch from the board.
     * @return The {@link DigitalInput} for the specified physical input channel.
     */
    public DigitalInput getDigitalInput(int input) {
        return mDigitalInputs.get(input);
    }

    /**
     * Fetches a digital output from this Tekdaqc.
     *
     * @param output int The input to fetch from the board.
     * @return The {@link DigitalOutput} for the specified physical output channel.
     */
    public DigitalOutput getDigitalOutput(int output) {
        return mDigitalOutputs.get(output);
    }

    /**
     * Activates pulse width modulation on a digital output; allowing the user to set the percentage of the time
     * the digital output will be active.
     *
     * @param output Output to set.
     * @param dutyCycle A float value 0 and 1 to set as the uptime percentage.
     */
    public void setPulseWidthModulation(int output, int dutyCycle){
        mDigitalOutputs.get(output).setPulseWidthModulation(dutyCycle);
    }

    /**
     * Activates pulse width modulation on a digital output; allowing the user to set the percentage of the time
     * the digital output will be active.
     *
     * @param output Output to set.
     * @param dutyCycle A {@link Quantity} that should contain a value in {@link Units#PERCENT}.
     */
    public void setPulseWidthModulation(int output, Quantity<Dimensionless> dutyCycle){
        mDigitalOutputs.get(output).setPulseWidthModulation(dutyCycle);
    }

    /**
     * Fetches a shallow copy of the internal {@link Map} of {@link AAnalogInput}s.
     *
     * @return {@link Map} of {@link AAnalogInput} containing all inputs
     */
    public Map<Integer, AAnalogInput> getAnalogInputs() {
        final Map<Integer, AAnalogInput> inputs = new HashMap<Integer, AAnalogInput>(mAnalogInputs.size());
        inputs.putAll(mAnalogInputs);
        return inputs;
    }

    /**
     * Fetches a shallow copy of the internal {@link Map} of {@link DigitalInput}s.
     *
     * @return {@link Map} of {@link DigitalInput} containing all inputs.
     */
    public Map<Integer, DigitalInput> getDigitalInputs() {
        final Map<Integer, DigitalInput> inputs = new HashMap<Integer, DigitalInput>(mDigitalInputs.size());
        inputs.putAll(mDigitalInputs);
        return inputs;
    }

    /**
     * Fetches a shallow copy of the internal {@link Map} of {@link DigitalOutput}s.
     *
     * @return {@link Map} of {@link DigitalOutput} containing all currently added inputs.
     */
    public Map<Integer, DigitalOutput> getDigitalOutputs() {
        final Map<Integer, DigitalOutput> inputs = new HashMap<Integer, DigitalOutput>(mDigitalOutputs.size());
        inputs.putAll(mDigitalOutputs);
        return inputs;
    }

    /**
     * Checks the state of the connection with this board.
     *
     * @return boolean True if we have an active connection.
     */
    public boolean isConnected() {
        return isConnected;
    }

    /**
     * Instruct the Tekdaqc to return a list of all it's added analog inputs.
     */
    public void listAnalogInputs() {
        mCommandQueue.queueCommand(CommandBuilderKt.listAnalogInputs());
    }

    /**
     * Instruct the Tekdaqc to initiate a system calibration.
     */
    public void systemCalibrate() {
        mCommandQueue.queueCommand(CommandBuilderKt.systemCalibrate());
    }

    /**
     * Retrieve the command string to instruct the Tekdaqc to do nothing.
     */
    public void none() {
        mCommandQueue.queueCommand(CommandBuilderKt.none());
    }

    /**
     * Queues a {@link IQueueObject} on the {@link ATekdaqc}'s internal {@link ICommandManager}.
     *
     * @param object The command or callback to be executed.
     */
    public void queueCommand(IQueueObject object) {
        mCommandQueue.queueCommand(object);
    }

    /**
     * Queues a {@link Task} on the {@link ATekdaqc}'s internal {@link ICommandManager}.
     *
     * @param task The {@link Task} to be executed.
     */
    public void queueTask(Task task) {
        mCommandQueue.queueTask(task);
    }

    /**
     * Convenience method for adding a listener to a tekdaqc.
     *
     * @param listener Listener to be registered.
     */
    public void addListener(IMessageListener listener) {
        messageBroadcaster.addMessageListener(this, listener);
    }

    /**
     * Convenience method for adding a network listener to a tekdaqc.
     *
     * @param listener Network listener to be registered.
     */
    public void addNetworkListener(INetworkListener listener) {
        messageBroadcaster.addNetworkListener(this, listener);
    }

    /**
     * Convenience method for adding a listener to a particular channel.
     *
     * @param listener {@link ICountListener} Listener to be registered.
     * @param input    {@link AAnalogInput} Input to register to.
     */
    public void addAnalogCountListener(ICountListener listener, AAnalogInput input) {
        messageBroadcaster.addAnalogChannelListener(this, input, listener);
    }

    /**
     * Convenience method for adding a listener to a particular channel.
     *
     * @param listener {@link ICountListener} Listener to be registered.
     * @param input    {@link AAnalogInput} Input to register to.
     */
    public void addAnalogVoltageListener(IVoltageListener listener, AAnalogInput input) {
        messageBroadcaster.addAnalogVoltageListener(this, input, listener);
    }

    /**
     * Convenience method for adding a listener to a particular channel.
     *
     * @param listener {@link IDigitalChannelListener} Listener to be registered.
     * @param input    {@link DigitalInput} Input to register to.
     */
    public void addDigitalChannelListener(IDigitalChannelListener listener, DigitalInput input) {
        messageBroadcaster.addDigitalChannelListener(this, input, listener);
    }

    /**
     * Convenience method for adding a listener to a particular channel.
     *
     * @param listener {@link IPWMChannelListener} Listener to be registered.
     * @param input    {@link DigitalInput} Input to register to.
     */
    public void addPWMChannelListener(IPWMChannelListener listener, DigitalInput input) {
        messageBroadcaster.addPWMChannelListener(this, input, listener);
    }

    /**
     * Convenience method for removing a listener from a tekdaqc.
     *
     * @param listener {@link IPWMChannelListener} Listener to be unregistered.
     */
    public void removePWMChannelListener(IPWMChannelListener listener, DigitalInput input) {
        messageBroadcaster.removePWMChannelListener(this, input, listener);
    }

    /**
     * Convenience method for removing a listener from a tekdaqc.
     *
     * @param listener {@link IMessageListener} Listener to be unregistered.
     */
    public void removeListener(IMessageListener listener) {
        messageBroadcaster.removeListener(this, listener);
    }

    /**
     * Convenience method for removing a listener from a channel on a tekdaqc.
     *
     * @param listener {@link ICountListener} The listener to be unregistered.
     * @param input    {@link AAnalogInput} The input to unregister from.
     */
    public void removeAnalogCountListener(AAnalogInput input, ICountListener listener) {
        messageBroadcaster.removeAnalogCountListener(this, input, listener);
    }

    /**
     * Convenience method for removing a listener from a channel on a tekdaqc.
     *
     * @param listener {@link ICountListener} The listener to be unregistered.
     * @param input    {@link AAnalogInput} The input to unregister from.
     */
    public void removeAnalogVoltageListener(AAnalogInput input, IVoltageListener listener) {
        messageBroadcaster.removeAnalogVoltageListener(this, input, listener);
    }

    /**
     * Convenience method for removing a listener from a channel on a tekdaqc.
     *
     * @param listener {@link IDigitalChannelListener} The listener to be unregistered.
     * @param input    {@link DataInput} The input to unregister from.
     */
    public void removeDigitalChannelListener(DigitalInput input, IDigitalChannelListener listener) {
        messageBroadcaster.removeDigitalChannelListener(this, input, listener);
    }

    /**
     * Connect to this Tekdaqc via Telnet using the specified connection method.
     *
     * @param method {@link CONNECTION_METHOD} The connection method to use.
     * @param currentAnalogScale The current {@link ATekdaqc.AnalogScale} the board is set to.
     *                     This should match the physical jumpers on the board.
     * @throws IOException Thrown if the underlying Telnet client fails
     *                     to connect.
     */
    public void connect(AnalogScale currentAnalogScale, CONNECTION_METHOD method) throws IOException {
        if(isConnected()){
            throw new IOException("Tekdaqc Already Connected");
        }
        switch (method) {
            case ETHERNET:
                mConnection = new EthernetTelnetConnection(mResponse.getHostIP(), EthernetTelnetConnection.TEKDAQC_TELNET_PORT);
                break;
            case SERIAL:
                mConnection = new SerialTelnetConnection();
                break;
            case USB:
                mConnection = new USBTelnetConnection();
                break;
        }
        mReadStream = mConnection.getInputStream();
        mWriteStream = mConnection.getOutputStream();

        mReadExecutor = new ReadExecutor(this, this);

        mCommandQueue.tryCommand();

        mCommandQueue.queueCommand(CommandBuilderKt.setAnalogInputScale(currentAnalogScale));

        mAnalogScale = currentAnalogScale;

        isConnected = true;
        mHeartbeatTimer.schedule(mHeartbeatTimerTask, HEARTBEAT_TIMER_INTERVAL, HEARTBEAT_TIMER_INTERVAL);
    }

    /**
     * Disconnect from this Tekdaqc's Telnet server. This is an unclean
     * disconnect (no command).
     *
     * @throws IOException Thrown if the underlying Telnet client fails to disconnect
     *                     properly.
     */
    public void disconnect() throws IOException {

        mReadExecutor.shutdown();
        mParsingExecutor.shutdown();

        mCommandQueue.purge(true);

        mConnection.disconnect();

        isConnected = false;

        mHeartbeatTimer = UtilityKt.reprepare(mHeartbeatTimer);
    }

    /**
     * Disconnect from this Tekdaqc's Telnet server cleanly (issue the command).
     * It is the responsibility of the calling application to ensure that the
     * connection's stream resources are properly cleaned up.
     */
    public void disconnectCleanly() {

        mCommandQueue.queueCommand(CommandBuilderKt.disconnect());

        mCommandQueue.queueCommand(new QueueCallback(new ITaskComplete() {
            @Override
            public void onTaskSuccess(ATekdaqc tekdaqc) {
                try {
                    disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onTaskFailed(ATekdaqc tekdaqc) {
                criticalErrorNotification(TekdaqcCriticalError.IMPARTIAL_DISCONNECTION);
            }
        }));

        mHeartbeatTimer = UtilityKt.reprepare(mHeartbeatTimer);
    }

    /**
     * Instructs the Tekdaqc to read the current state of the digital outputs.
     */
    public void readDigitalOutput() {
        mCommandQueue.queueCommand(CommandBuilderKt.readDigitalOutput());
    }

    /**
     * Instructs the Tekdaqc to enter calibration mode. WARNING: This command
     * must be used with caution. When executed, the Tekdaqc will erase the
     * entire contents of the calibration table in preparation for writing new
     * data.
     */
    public void enterCalibrationMode() {
        mCommandQueue.queueCommand(CommandBuilderKt.enterCalibrationMode());
    }

    /**
     * Instructs the Tekdaqc to exit calibration mode. WARNING: This command
     * must be used with caution. When executed, the Tekdaqc will lock the
     * calibration table, requiring a complete erasure prior to being able to
     * write any new data.
     */
    public void exitCalibrationMode() {
        mCommandQueue.queueCommand(CommandBuilderKt.exitCalibrationMode());
    }

    /**
     * Instruct the Tekdaqc to write its serial number.
     *
     * @param serial {@link String} The serial number value.
     */
    public void writeSerialNumber(String serial) {
        mCommandQueue.queueCommand(CommandBuilderKt.writeSerialNumber(serial));
    }

    /**
     * Instruct the Tekdaqc to write its factory MAC address.
     *
     * @param mac {@code long} The factory MAC address value.
     */
    public void writeFactoryMacAddress(long mac) {
        mCommandQueue.queueCommand(CommandBuilderKt.writeFactoryMacAddress(mac));
    }

    /**
     * Gets the {@link InputStream} for this {@link ATekdaqc}.
     *
     * @return {@link InputStream} The reader for the data stream from the
     * Tekdaqc.
     */
    public InputStream getInputStream() {
        return mReadStream;
    }

    /**
     * Gets the {@link OutputStream} for this {@link ATekdaqc}.
     *
     * @return {@link OutputStream} The writer for the output data stream to the
     * Tekdaqc.
     */
    public OutputStream getOutputStream() {
        return mWriteStream;
    }

    @Override
    public String toString() {
        return mResponse.toString();
    }

    @Override
    public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException {
        mResponse = (LocatorResponse) input.readObject();
        readIn(input);
    }

    @Override
    public void writeExternal(ObjectOutput output) throws IOException {
        output.writeObject(mResponse);
        writeOut(output);
    }

    /**
     * Internal method used for initializing the correct number of
     */
    protected abstract void initializeBoardStatusLists();

    /**
     * Reads a specified analog input the specified number of times, with the
     * parameters specified in the input.
     *
     * @param input  {@code int} The input to sample.
     * @param number {@code int} The number of samples to take. Any number less then or equal to 0 is interpreted
     *               as continuously.
     * @throws IllegalArgumentException if the provided input is of the wrong type.
     */
    public abstract void readAnalogInput(int input, int number) throws IllegalArgumentException;

    /**
     * Reads a specified range of analog inputs the specified number of times,
     * with the default parameters in the Tekdaqc. Note that if the specified
     * inputs correspond to ones which were added to the Tekdaqc with sampling
     * parameters, those will override the board defaults.
     *
     * @param start  {@code int} The physical channel to start at. Must be less than end.
     * @param end    {@code int} The physical channel to end at. Must be greater than
     *               start.
     * @param number {@code int} The number of samples to take. Any number less then or equal to 0 is
     *               interpreted as continuously.
     * @throws IllegalArgumentException if the provided inputs do not
     *                                  exist on the board.
     */
    public abstract void readAnalogInputRange(int start, int end, int number) throws IllegalArgumentException;

    /**
     * Reads the specified set of analog inputs the specified number of times,
     * with the default parameters in the Tekdaqc. Note that if the specified
     * inputs correspond to ones which were added to the Tekdaqc with sampling
     * parameters, those will override the board defaults.
     *
     * @param inputs {@link Set} of {@link Integer} input numbers to read. If null
     *               a null command is returned.
     * @param number {@code int} The number of samples to take. Any number less then or equal to 0 is
     *               interpreted as continuously.
     * @throws IllegalArgumentException if the input set contains inputs
     *                                  which do not exist on the board.
     */
    public abstract void readAnalogInputSet(Set<Integer> inputs, int number) throws IllegalArgumentException;

    /**
     * Reads all of the analog inputs the specified number of times, with the
     * default parameters in the Tekdaqc. Note that if any of the inputs
     * correspond to ones which were added to the Tekdaqc with sampling
     * parameters, those will override the board defaults.
     *
     * @param number {@code int} The number of samples to take. Any number less then or equal to 0 is
     *               interpreted as continuously.
     */
    public abstract void readAllAnalogInput(int number);

    /**
     * Reads a specified digital input the specified number of times, with the
     * parameters specified in the input.
     *
     * @param input  {@code int} The input to sample.
     * @param number {@code int} The number of samples to take. Any number less then or equal to 0 is interpreted
     *               as continuously.
     * @throws IllegalArgumentException if the provided input is of the wrong type.
     */
    public abstract void readDigitalInput(int input, int number) throws IllegalArgumentException;

    /**
     * Reads a specified range of digital inputs the specified number of times,
     * with the default parameters in the Tekdaqc. Note that if the specified
     * inputs correspond to ones which were added to the Tekdaqc with sampling
     * parameters, those will override the board defaults.
     *
     * @param start  {@code int} The physical channel to start at. Must be less than end.
     * @param end    {@code int} The physical channel to end at. Must be greater than
     *               start.
     * @param number {@code int} The number of samples to take. Any number less then or equal to 0 is
     *               interpreted as continuously.
     * @throws IllegalArgumentException if the provided inputs do not
     *                                  exist on the board.
     */
    public abstract void readDigitalInputRange(int start, int end, int number) throws IllegalArgumentException;

    /**
     * Reads the specified set of digital inputs the specified number of times,
     * with the default parameters in the Tekdaqc. Note that if the specified
     * inputs correspond to ones which were added to the Tekdaqc with sampling
     * parameters, those will override the board defaults.
     *
     * @param inputs {@link Set} of {@link Integer} input numbers to read. If null
     *               a null command is returned.
     * @param number {@code int} The number of samples to take. Any number less then or equal to 0 is
     *               interpreted as continuously.
     * @throws IllegalArgumentException if the input set contains inputs
     *                                  which do not exist on the board.
     */
    public abstract void readDigitalInputSet(Set<Integer> inputs, int number) throws IllegalArgumentException;

    /**
     * Reads all of the digital inputs the specified number of times, with the
     * default parameters in the Tekdaqc. Note that if any of the inputs
     * correspond to ones which were added to the Tekdaqc with sampling
     * parameters, those will override the board defaults.
     *
     * @param number {@code int} The number of samples to take. Any number less then or equal to 0 is
     *               interpreted as continuously.
     */
    public abstract void readAllDigitalInput(int number);

    /**
     * Retrieve the command string to add a specified {@link AAnalogInput} to
     * the Tekdaqc.
     *
     * @param input {@link AAnalogInput} The input to build the command for.
     * @throws IllegalArgumentException Thrown if the provided input is
     *                                  of the wrong type or for a non-existent physical channel.
     * @throws IllegalStateException    Thrown if no connection exists with
     *                                  the Tekdaqc or if the added input replaced an existing one.
     */
    protected abstract void addAnalogInput(AAnalogInput input) throws IllegalArgumentException, IllegalStateException;

    /**
     * Activates an {@link AAnalogInput} so that it will be sampled when the "SAMPLE" or "READ_ANALOG_INPUT" commands are called.
     *
     * @param inputNumber The physical input number of the {@link AAnalogInput} to be activated.
     * @return The {@link AAnalogInput} which has been activated.
     * @throws IllegalArgumentException Thrown if the provided input is
     *                                  of the wrong type or for a non-existent physical channel.
     * @throws IllegalStateException    Thrown if no connection exists with
     *                                  the Tekdaqc or if the added input replaced an existing one.
     */
    public abstract AAnalogInput activateAnalogInput(int inputNumber) throws IllegalArgumentException, IllegalStateException;

    /**
     * Activates an {@link DigitalInput} so that it will be sampled when the "SAMPLE" or "READ_DIGITAL_INPUT" commands are called.
     *
     * @param inputNumber The physical input number of the {@link DigitalInput} to be activated.
     * @return The {@link DigitalInput} which has been activated.
     * @throws IllegalArgumentException Thrown if the provided input is
     *                                  of the wrong type or for a non-existent physical channel.
     * @throws IllegalStateException    Thrown if no connection exists with
     *                                  the Tekdaqc or if the added input replaced an existing one.
     */
    public abstract DigitalInput activateDigitalInput(int inputNumber) throws IllegalArgumentException, IllegalStateException;

    /**
     * Toggles the {@link DigitalOutput} to the desired status.
     *
     * @param outputNumber The physical output number of the {@link DigitalOutput} to be changed.
     * @param status       The status of the output. True representing ON and False representing OFF.
     * @return The toggled {@link DigitalOutput}.
     */
    public abstract DigitalOutput toggleDigitalOutput(int outputNumber, boolean status);

    /**
     * Removes an analog input from this Tekdaqc.
     *
     * @param input {@link AAnalogInput} The input to remove from the board.
     */
    protected abstract void removeAnalogInput(AAnalogInput input);

    /**
     * Removes an analog input from this Tekdaqc.
     *
     * @param input {@code int} The input to remove from the board.
     */
    public abstract void deactivateAnalogInput(int input);

    /**
     * Attempts to remove all known added {@link AAnalogInput}.
     */
    public abstract void deactivateAllAddedAnalogInputs();

    /**
     * Attempts to remove all analog inputs.
     */
    public abstract void deactivateAllAnalogInputs();

    /**
     * Sets the analog input voltage scale on the Tekdaqc.
     *
     * @param scale {@link AnalogScale} The analog input scale to switch to.
     */
    public abstract void setAnalogInputScale(AnalogScale scale);

    /**
     * Gets the analog input voltage scale on the Tekdaqc.
     *
     * @return The {@link AnalogScale} the software has been most recently notified about
     */
    public AnalogScale getAnalogInputScale(){
        return mAnalogScale;
    }

    /**
     * Adds a specified {@link DigitalInput} to the Tekdaqc.
     *
     * @param input {@link DigitalInput} The input to build the command for.
     * @throws IllegalArgumentException Thrown if the provided input is
     *                                  of the wrong type or for a non-existent physical channel.
     * @throws IllegalStateException    Thrown if no connection exists with
     *                                  the Tekdaqc or if the added input replaced an existing one.
     */
    protected abstract void addDigitalInput(DigitalInput input) throws IllegalArgumentException, IllegalStateException;

    /**
     * Removes a digital input from this Tekdaqc.
     *
     * @param input {@link DigitalInput} The input to remove from the board.
     */
    public abstract void deactivateDigitalInput(DigitalInput input);

    /**
     * Removes a digital input from this Tekdaqc.
     *
     * @param input {@code int} The input to remove from the board.
     */
    public abstract void deactivateDigitalInput(int input);

    /**
     * Attempts to remove all known added digital inputs.
     */
    public abstract void deactivateAllAddedDigitalInputs();


    /**
     * Attempts to remove all digital inputs.
     */
    public abstract void deactivateAllDigitalInputs();

    /**
     * The command to set the digital outputs to their desired state.
     *
     * @param binaryString The string representing the desired state of all digital outputs. Example: Only digital output 5
     *                     on would be represented as "0000010000000000".
     */
    public abstract void setDigitalOutput(String binaryString);

    /**
     * The command to set the digital outputs to their desired state.
     *
     * @param hex The string representing the desired state of all digital outputs in hexadecimal form. Example: All Digital Outputs on
     *            would be represented by "FFFF".
     */
    public abstract void setDigitalOutputByHex(String hex);

    /**
     * The command to set the digital outputs to their desired state.
     *
     * @param digitalOutputArray The array of booleans representing the desired state of all digital outputs with each index representing
     *                           the corresponding Digital Output.
     */
    public abstract void setDigitalOutput(boolean[] digitalOutputArray);

    /**
     * Retrieve the command string to instruct the board to reboot into upgrade
     * mode.
     */
    public abstract void upgrade();

    /**
     * Retrieve the command string to instruct the Tekdaqc to identify itself.
     */
    public abstract void identify();

    /**
     * Retrieve the command string to instruct the Tekdaqc to sample all
     * channels a number of times.
     *
     * @param number {@code int} The number of samples to take.
     */
    public abstract void sample(int number);

    /**
     * Retrieve the command string to instruct the Tekdaqc to halt any ongoing
     * operations.
     */
    public abstract void halt();

    /**
     * Retrieve the command string to instruct the Tekdaqc to set its real time
     * calendar.
     *
     * @param timestamp {@code long} The unix epoch timestamp.
     */
    public abstract void setRTC(long timestamp);

    /**
     * Retrieve the command string to read the analog input ADC's internal
     * registers.
     */
    public abstract void readADCRegisters();

    /**
     * Instruct the Tekdaqc to initiate a system gain calibration.
     *
     * @param input {@code int} The input to use for this calibration. This input must
     *              have a valid calibration signal applied.
     */
    public abstract void systemGainCalibrate(int input);

    /**
     * Instruct the Tekdaqc to read out and return its current system gain
     * calibration value.
     */
    public abstract void readSystemGainCalibration();

    /**
     * Instruct the Tekdaqc to read out and return its current calibration
     * status.
     */
    public abstract void getCalibrationStatus();

    /**
     * Instruct the Tekdaqc to write a temperature value into its calibration
     * table at the specified index.
     *
     * @param temperature {@code double} The temperature value to write.
     * @param index       {@code int} The temperature table index to write the value at.
     */
    public abstract void writeCalibrationTemperature(double temperature, int index);

    /**
     * Instruct the Tekdaqc to write a calibration value into its table for the
     * provided parameters.
     *
     * @param value       {@code float} The calibration value.
     * @param gain        {@link Gain} The gain this value is valid for.
     * @param rate        {@link Rate} The rate this value is valid for.
     * @param buffer      {@link BufferState} The buffer state this value is valid for.
     * @param scale       {@link AnalogScale} The analog input scale this value is valid for.
     * @param temperature {@code int} The temperature index this value is valid for.
     */
    public abstract void writeGainCalibrationValue(float value, Gain gain, Rate rate, BufferState buffer, AnalogScale scale,
                                                   int temperature);

    /**
     * Instruct the Tekdaqc to write into its calibration table that the table
     * is valid.
     */
    public abstract void writeCalibrationValid();

    /**
     * Converts the provided {@link AnalogInputCountData} point into a voltage using
     * the parameters of the data point and the specific Tekdaqc board.
     *
     * @param data         {@link AnalogInputCountData} The data point to convert.
     * @param currentScale {@link AnalogScale} The current analog scale to use in the conversion.
     * @return double The reference voltage value.
     */
    public abstract double convertAnalogInputDataToVoltage(AnalogInputCountData data, AnalogScale currentScale);

    /**
     * Converts the provided {@link AnalogInputCountData} point into a temperature
     * using the parameters of the data point and the specific Tekdaqc board.
     * This will assume that the data point came from the input specified by
     * {@link #getColdJunctionInputNumber()}, but does not enforce it.
     *
     * @param data {@link AnalogInputCountData} The data point to convert.
     * @return double The reference voltage value.
     */
    public abstract double convertAnalogInputDataToTemperature(AnalogInputCountData data);

    /**
     * Retrieves the physical input number for the cold junction channel this
     * Tekdaqc.
     *
     * @return {@code int} The physical channel of the cold junction input.
     */
    public abstract int getColdJunctionInputNumber();

    /**
     * Retrieves the analog scale multiplier for the provided scale.
     *
     * @param scale {@link AnalogScale} The scale to retrieve the multiplier for.
     * @return double The multiplier.
     */
    public abstract double getAnalogScaleMultiplier(AnalogScale scale);

    /**
     * Retrieves the list of valid {@link Gain} settings for this
     * {@link ATekdaqc}.
     *
     * @return {@link List} of {@link Gain} values.
     */
    public abstract List<Gain> getValidGains();

    /**
     * Retrieves the list of valid {@link Rate} settings for this
     * {@link ATekdaqc}.
     *
     * @return {@link List} of {@link Rate} values.
     */
    public abstract List<Rate> getValidRates();

    /**
     * Retrieves the list of valid {@link BufferState} settings for this
     * {@link ATekdaqc}.
     *
     * @return {@link List} of {@link BufferState} values.
     */
    public abstract List<BufferState> getValidBufferStates();

    /**
     * Retrieves the list of valid {@link AnalogScale} settings for this
     * {@link ATekdaqc}.
     *
     * @return {@link List} of {@link AnalogScale} values.
     */
    public abstract List<AnalogScale> getValidAnalogScales();

    /**
     * Called during the de-serialization process to allow subclasses to recover
     * their data from the serialization.
     *
     * @param input {@link ObjectInput} The input stream.
     * @throws IOException            IoException.
     * @throws ClassNotFoundException Called if class is not found to parse.
     */
    protected abstract void readIn(ObjectInput input) throws IOException, ClassNotFoundException;

    /**
     * Called during the serialization process to allow subclasses to add their
     * data to the serialization.
     *
     * @param output {@link ObjectOutput} The output stream.
     * @throws IOException IoException.
     */
    protected abstract void writeOut(ObjectOutput output) throws IOException;

    /**
     * Enumeration of the available connection methods.
     *
     * @author Jared Woolston (jwoolston@tenkiv.com)
     * @since v1.0.0.0
     */
    public static enum CONNECTION_METHOD {
        ETHERNET, SERIAL, USB;
    }

    /**
     * Enumeration of the available communication methods.
     *
     * @author Jared Woolston (jwoolston@tenkiv.com)
     * @since v1.0.0.0
     */
    public static enum COMMUNICATION_ENCODING {
        ASCII, BINARY;
    }

    /**
     * Enumeration of the available analog input voltage scales.
     *
     * @author Jared Woolston (jwoolston@tenkiv.com)
     * @since v1.0.0.0
     */
    public static enum AnalogScale {
        ANALOG_SCALE_5V("ANALOG_SCALE_5V"), ANALOG_SCALE_400V("ANALOG_SCALE_400V");

        private static final AnalogScale[] mValueArray = AnalogScale.values();
        public final String scale;

        AnalogScale(String scale) {
            this.scale = scale;
        }

        public static AnalogScale getValueFromOrdinal(byte ordinal) {
            return mValueArray[ordinal];
        }

        public static AnalogScale fromString(String scale) {
            for (final AnalogScale s : values()) {
                if (s.scale.equals(scale)) {
                    return s;
                }
            }
            return null;
        }

        @Override
        public String toString() {
            return scale;
        }
    }

}

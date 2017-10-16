package com.tenkiv.tekdaqc.hardware

import com.tenkiv.tekdaqc.communication.ascii.executors.ASCIIParsingExecutor
import com.tenkiv.tekdaqc.communication.command.queue.*
import com.tenkiv.tekdaqc.communication.command.queue.values.ABaseQueueVal
import com.tenkiv.tekdaqc.communication.command.queue.values.IQueueObject
import com.tenkiv.tekdaqc.communication.data_points.AnalogInputCountData
import com.tenkiv.tekdaqc.communication.executors.AParsingExecutor.IParsingListener
import com.tenkiv.tekdaqc.communication.executors.ReadExecutor
import com.tenkiv.tekdaqc.communication.message.*
import com.tenkiv.tekdaqc.communication.tasks.ITaskComplete
import com.tenkiv.tekdaqc.hardware.AAnalogInput.Gain
import com.tenkiv.tekdaqc.hardware.AAnalogInput.Rate
import com.tenkiv.tekdaqc.hardware.AnalogInput_RevD.BufferState
import com.tenkiv.tekdaqc.locator.Locator
import com.tenkiv.tekdaqc.locator.LocatorResponse
import com.tenkiv.tekdaqc.telnet.client.EthernetTelnetConnection
import com.tenkiv.tekdaqc.telnet.client.ITekdaqcTelnetConnection
import com.tenkiv.tekdaqc.telnet.client.SerialTelnetConnection
import com.tenkiv.tekdaqc.telnet.client.USBTelnetConnection
import com.tenkiv.tekdaqc.utility.CriticalErrorListener
import com.tenkiv.tekdaqc.utility.TekdaqcCriticalError
import com.tenkiv.tekdaqc.utility.*
import tec.uom.se.unit.Units

import javax.measure.Quantity
import javax.measure.quantity.Dimensionless
import java.io.*
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.locks.ReentrantLock

/**
 * Class which contains information about a specific Tekdaqc and provides
 * methods for interacting with it.

 * @author Tenkiv (software@tenkiv.com)
 * *
 * @since 1.0.0.0
 */
abstract class ATekdaqc protected constructor(): Externalizable, IParsingListener {

    private var locatorResponse: LocatorResponse? = null

    protected constructor(locatorResponse: LocatorResponse): this(){
        this.locatorResponse = locatorResponse
    }
    /**
     * Maximum length of a channel name.
     */
    companion object {
        val MAX_NAME_LENGTH = 24
    }

    /**
     * The singleton instance.
     */
    internal val messageBroadcaster = MessageBroadcaster()

    protected fun getMessageBroadcaster(): MessageBroadcaster = messageBroadcaster

    /**
     * Required for serialization
     */
    private val serialVersionUID = 1L

    /**
     * Interval of the watchdog timer.
     */
    protected val HEARTBEAT_TIMER_INTERVAL = 5000

    /**
     * Maps of inputs/outputs
     */
    val analogInputs: Map<Int, AAnalogInput> = HashMap<Int, AAnalogInput>()
    val digitalInputs: Map<Int, DigitalInput> = HashMap<Int, DigitalInput>()
    val digitalOutputs: Map<Int, DigitalOutput> = HashMap<Int, DigitalOutput>()

    abstract val temperatureReference: AAnalogInput

    /**
     * The executor responsible for reading the input stream of the Tekdaqc
     */
    protected var readExecutor: ReadExecutor? = null

    /**
     * The executor responsible for parsing split messages
     */
    protected var parsingExecutor: ASCIIParsingExecutor = ASCIIParsingExecutor(5)

    /**
     * Method returning the current throttled digital input rate in samples/millisecond..

     * @return An [Integer] of the throttled rate in samples/millisecond.
     */
    @Transient var throttledDigitalInputSampleRate = 1000

    private var _analogScale: AnalogScale = AnalogScale.ANALOG_SCALE_5V

    /**
     * The current [AnalogScale]
     */
    var analogScale: AnalogScale
        get() = _analogScale
        set(value) {
            _analogScale = value
            mCommandQueue.queueCommand(CommandBuilder.setAnalogInputScale(value))
        }

    /**
     * The current [COMMUNICATION_ENCODING] used by the [ATekdaqc].
     * Default is [COMMUNICATION_ENCODING.ASCII].
     */
    /**
     * Retrieves the current encoding mode of this Tekdaqc.

     * @return [COMMUNICATION_ENCODING] The current encoding.
     */
    var currentEncoding = COMMUNICATION_ENCODING.ASCII
        protected set

    /**
     * The [ICommandManager] which controls who commands to be executed are handled.
     */
    @Transient var mCommandQueue: ICommandManager = CommandQueueManager(this)

    /**
     * The Telnet connection.
     */
    @Transient protected var mConnection: ITekdaqcTelnetConnection? = null

    /**
     * Gets the [InputStream] for this [ATekdaqc].

     * @return [InputStream] The reader for the data stream from the
     * * Tekdaqc.
     */
    @Transient var inputStream: InputStream? = null
        protected set

    /**
     * Gets the [OutputStream] for this [ATekdaqc].

     * @return [OutputStream] The writer for the output data stream to the
     * * Tekdaqc.
     */
    @Transient var outputStream: OutputStream? = null
        protected set

    /**
     * The [Timer] used when using throttled sampling.
     */
    protected var mDigitalInputSampleTimer: Timer = Timer("Digital Input Sample Timer", true)

    /**
     * The list of [CriticalErrorListener]s.
     */
    private val mCriticalErrorListener = ArrayList<CriticalErrorListener>()

    /**
     * The number of samples currently taken by throttled sampling.
     */
    @Volatile protected var mThrottledSamples = -1

    /**
     * Boolean for if the tekdaqc is connected in the current tick.
     */
    protected var tentativeIsConnected = false

    /**
     * If the keep alive packet was sent.
     */
    protected var keepAlivePacketSent = false

    /**
     * Checks the state of the connection with this board.

     * @return boolean True if we have an active connection.
     */
    var isConnected = false
        protected set

    /**
     * Method to get the total number of analog inputs, not including the board's temperature sensor.

     * @return The number of analog inputs.
     */
    abstract fun getAnalogInputCount(): Int

    /**
     * Method to gte the total number of digital inputs.

     * @return The number of digital inputs.
     */
    abstract fun getDigitalInputCount(): Int

    /**
     * Method to gte the number of digital outputs.

     * @return The number of digital outputs.
     */
    abstract fun getDigitalOutputCount(): Int

    /**
     * Method to return the channel number of the tekdaqc's temperature sensor.

     * @return The channel number of the tekdaqc's temperature sensor.
     */
    abstract fun getAnalogTemperatureReferenceChannel(): Int

    /**
     * A [TimerTask] to be executed when attempting to use throttled sampling.
     */
    protected var mDigitalInputActivationTask: TimerTask = object : TimerTask() {

        override fun run() {
            if (mConnection?.isConnected ?: throw NullPointerException()) {
                mCommandQueue.queueCommand(CommandBuilder.readAllDigitalInput(1))

                if (mThrottledSamples > 0) {
                    mThrottledSamples--

                } else if (mThrottledSamples == 0) {
                    mDigitalInputSampleTimer = mDigitalInputSampleTimer.reprepare()
                }
            }
        }
    }

    /**
     * Heartbeat timer to check for disconnection from the tekdaqc.
     */
    protected var mHeartbeatTimer = Timer("Tekdaqc Heartbeat Timer", true)

    /**
     * A [TimerTask] to be executed for checking to see if the Tekdaqc connection is active.
     */
    protected var mHeartbeatTimerTask: TimerTask = object : TimerTask() {
        override fun run() {
            if (keepAlivePacketSent && !tentativeIsConnected) {
                isConnected = false
                criticalErrorNotification(TekdaqcCriticalError.TERMINAL_CONNECTION_DISRUPTION)
            } else if (!keepAlivePacketSent && !tentativeIsConnected) {
                keepAlivePacketSent = true
                queueCommand(CommandBuilder.none())
            } else if (tentativeIsConnected) {
                isConnected = true
                tentativeIsConnected = false
            }
        }
    }


    init {
        initializeBoardStatusLists()
    }

    /**
     * Adds a new [CriticalErrorListener] to be notified of any catastrophic events.

     * @param listener The listener to be added.
     */
    fun addCriticalFailureListener(listener: CriticalErrorListener) {
        mCriticalErrorListener.add(listener)
    }

    /**
     * Removes a [CriticalErrorListener] from being notified of events.

     * @param listener The listener to be removed.
     */
    fun removeCriticalFailureListener(listener: CriticalErrorListener) {
        mCriticalErrorListener.remove(listener)
    }

    /**
     * Notifies all [CriticalErrorListener]s of something awful happening.

     * @param error The [TekdaqcCriticalError] which has caused such a problem.
     */
    fun criticalErrorNotification(error: TekdaqcCriticalError) {
        for (listener in mCriticalErrorListener) {
            listener.onTekdaqcCriticalError(error)
        }
    }

    /**
     * Creates a Tekdaqc board object from the information provided by the
     * [LocatorResponse].

     * @param response [LocatorResponse] Response provided by a Tekdaqc locator request.
     */


    /**
     * Method to set [MessageBroadcaster]'s callback [Executor] to a new value. This should only be done if
     * no messages are queued to be sent. It is recommended that this method only be called after the [ATekdaqc.halt]
     * command. This method is not for general use cases and can result in severe issues if used improperly.

     * @param callbackExecutor The new [Executor] to be used for callbacks.
     */
    fun setMessageBroadcasterCallbackExecutor(callbackExecutor: Executor) {
        messageBroadcaster.setCallbackExecutor(callbackExecutor)
    }

    override fun onMessageDetetced(message: String) {
        if (keepAlivePacketSent) {
            keepAlivePacketSent = false
        }
        tentativeIsConnected = true
    }

    /**
     * Generates a binary string of all [DigitalOutput]s this board controls.

     * @return A Binary [String] representing the state of the [DigitalOutput]s.
     */
    fun generateBinaryStringFromOutput(): String {
        val builder = StringBuilder()

        for (i in 0 until getDigitalOutputCount()) {
            builder.append(0)
        }

        for (outputNumber in digitalOutputs.keys) {
            if (digitalOutputs[outputNumber]?.getIsActivated()?: throw IndexOutOfBoundsException("Digital Output Out of Range,")) {
                builder.replace(outputNumber, outputNumber + 1, "1")
            }
        }
        return builder.toString()
    }

    /**
     * Method reads the digital inputs at a reduced rate such that slower devices can process the output.
     * Throttled sampling must be halted by calling haltThrottledDigitalInputReading().

     * @param rateMillis Rate at which to read the digital inputs in samples/millisecond.
     * *
     * @throws IllegalArgumentException Exception thrown if specified rate is less them 0
     */
    @Throws(IllegalArgumentException::class)
    fun readThrottledDigitalInput(rateMillis: Int) {
        if (rateMillis > 0) {
            throttledDigitalInputSampleRate = rateMillis
            mDigitalInputSampleTimer = mDigitalInputSampleTimer.reprepare()
            mDigitalInputSampleTimer.schedule(mDigitalInputActivationTask, throttledDigitalInputSampleRate.toLong())

        } else {
            throw IllegalArgumentException("Specified rate must be greater then 0.")
        }
    }

    /**
     * Attempts disconnect and reconnect to the Tekdaqc in the case of a critical failure. Can attempt to restore
     * the state of all [AAnalogInput]s and [DigitalInput]s. Throws [CriticalErrorListener]s if it is
     * unable to successfully do so.

     * @param millisTimeout Time to wait for reconnection in milliseconds,
     * *
     * @param reactivateChannels If channels should be reactivated after reconnection.
     */
    fun restoreTekdaqc(millisTimeout: Long, reactivateChannels: Boolean) {
        try {
            mCommandQueue.purge(false)

            mConnection?.disconnect()

            inputStream = null
            outputStream = null

            Locator.instance
                    .blockingSearchForSpecificTekdaqcs(
                            millisTimeout,
                            ReentrantLock(),
                            false, null,
                            serialNumber)[0].connect(analogScale, CONNECTION_METHOD.ETHERNET)


            val commands = ArrayList<ABaseQueueVal>()

            commands.addAll(CommandBuilder.deactivateAllAnalogInputs())

            commands.addAll(CommandBuilder.deactivateAllDigitalInputs())

            if (reactivateChannels) {

                for (input in analogInputs.values) {
                    if (input.isActivated) {
                        commands.add(CommandBuilder.addAnalogInput(input))
                    }
                }

                for (input in digitalInputs.values) {
                    if (input.isActivated) {
                        commands.add(CommandBuilder.addDigitalInput(input))
                    }
                }
            }

            queueTask(Task(object : ITaskComplete {
                override fun onTaskSuccess(tekdaqc: ATekdaqc) {

                }

                override fun onTaskFailed(tekdaqc: ATekdaqc) {
                    criticalErrorNotification(TekdaqcCriticalError.FAILED_TO_REINITIALIZE)
                }
            }, commands))


        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    /**
     * Method reads the digital inputs at a reduced rate such that slower devices can process the output.
     * Throttled sampling must be halted by calling haltThrottledDigitalInputReading().

     * @param rateMillis Rate at which to read the digital inputs in samples/millisecond.
     * *
     * @param samples    Number of samples to take.
     * *
     * @throws IllegalArgumentException Rate must be greater then 0.
     */
    @Throws(IllegalArgumentException::class)
    fun readThrottledDigitalInput(rateMillis: Int, samples: Int) {
        mThrottledSamples = samples
        readThrottledDigitalInput(rateMillis)
    }

    /**
     * Method to halt the throttled sampling of the digital inputs.
     */
    fun haltThrottedDigitalInputReading() {
        mDigitalInputSampleTimer = mDigitalInputSampleTimer.reprepare()
    }

    /**
     * Retrieve the board's Firmware version string.

     * @return [String] The firmware version.
     */
    val firmwareVersion: String
        get() = locatorResponse?.firwareVersion ?: throw NullPointerException()

    /**
     * Retrieve the board's revision code.

     * @return char The boards revision code.
     */
    val revisionType: Char
        get() = locatorResponse?.type?.toChar() ?: throw NullPointerException()

    /**
     * Retrieve the board's IP address.

     * @return [String] The board's IP address.
     */
    val hostIP: String
        get() = locatorResponse?.hostIP ?: throw NullPointerException()

    /**
     * Retrieve the boards MAC address.

     * @return [String] The board's MAC address.
     */
    val macAddress: String
        get() = locatorResponse?.macAddress ?: throw NullPointerException()

    /**
     * Retrieve the serial number of this [ATekdaqc].

     * @return [String] The serial number.
     */
    val serialNumber: String
        get() = locatorResponse?.serial ?: throw NullPointerException()

    /**
     * Retrieve the board's application title.

     * @return [String] The application title.
     */
    val title: String
        get() = locatorResponse?.title ?: throw NullPointerException()

    /**
     * Fetches an analog input from this Tekdaqc.

     * @param input int The input to fetch from the board.
     * *
     * @return The [AAnalogInput] for the specified physical input channel.
     */
    open fun getAnalogInput(input: Int): AAnalogInput {
        return analogInputs[input] ?: throw IndexOutOfBoundsException()
    }

    /**
     * Fetches a digital input from this Tekdaqc.

     * @param input int The input to fetch from the board.
     * *
     * @return The [DigitalInput] for the specified physical input channel.
     */
    fun getDigitalInput(input: Int): DigitalInput {
        return digitalInputs[input] ?: throw IndexOutOfBoundsException()
    }

    /**
     * Fetches a digital output from this Tekdaqc.

     * @param output int The input to fetch from the board.
     * *
     * @return The [DigitalOutput] for the specified physical output channel.
     */
    fun getDigitalOutput(output: Int): DigitalOutput {
        return digitalOutputs[output] ?: throw IndexOutOfBoundsException()
    }

    /**
     * Activates pulse width modulation on a digital output; allowing the user to set the percentage of the time
     * the digital output will be active.

     * @param output Output to set.
     * *
     * @param dutyCycle A float value 0 and 1 to set as the uptime percentage.
     */
    fun setPulseWidthModulation(output: Int, dutyCycle: Int) {
        digitalOutputs[output]?.setPulseWidthModulation(dutyCycle) ?: IndexOutOfBoundsException()
    }

    /**
     * Activates pulse width modulation on a digital output; allowing the user to set the percentage of the time
     * the digital output will be active.

     * @param output Output to set.
     * *
     * @param dutyCycle A [Quantity] that should contain a value in [Units.PERCENT].
     */
    fun setPulseWidthModulation(output: Int, dutyCycle: Quantity<Dimensionless>) {
        digitalOutputs[output]?.setPulseWidthModulation(dutyCycle) ?: throw IndexOutOfBoundsException()
    }

    /**
     * Instruct the Tekdaqc to return a list of all it's added analog inputs.
     */
    fun listAnalogInputs() {
        mCommandQueue.queueCommand(CommandBuilder.listAnalogInputs())
    }

    /**
     * Instruct the Tekdaqc to initiate a system calibration.
     */
    fun systemCalibrate() {
        mCommandQueue.queueCommand(CommandBuilder.systemCalibrate())
    }

    /**
     * Retrieve the command string to instruct the Tekdaqc to do nothing.
     */
    fun none() {
        mCommandQueue.queueCommand(CommandBuilder.none())
    }

    /**
     * Queues a [IQueueObject] on the [ATekdaqc]'s internal [ICommandManager].

     * @param object The command or callback to be executed.
     */
    fun queueCommand(command: IQueueObject) {
        mCommandQueue.queueCommand(command)
    }

    /**
     * Queues a [Task] on the [ATekdaqc]'s internal [ICommandManager].

     * @param task The [Task] to be executed.
     */
    fun queueTask(task: Task) {
        mCommandQueue.queueTask(task)
    }

    /**
     * Convenience method for adding a listener to a tekdaqc.

     * @param listener Listener to be registered.
     */
    fun addListener(listener: IMessageListener) {
        messageBroadcaster.addMessageListener(this, listener)
    }

    /**
     * Convenience method for adding a network listener to a tekdaqc.

     * @param listener Network listener to be registered.
     */
    fun addNetworkListener(listener: INetworkListener) {
        messageBroadcaster.addNetworkListener(this, listener)
    }

    /**
     * Convenience method for adding a listener to a particular channel.

     * @param listener [ICountListener] Listener to be registered.
     * *
     * @param input    [AAnalogInput] Input to register to.
     */
    fun addAnalogCountListener(listener: ICountListener, input: AAnalogInput) {
        messageBroadcaster.addAnalogChannelListener(this, input, listener)
    }

    /**
     * Convenience method for adding a listener to a particular channel.

     * @param listener [ICountListener] Listener to be registered.
     * *
     * @param input    [AAnalogInput] Input to register to.
     */
    fun addAnalogVoltageListener(listener: IVoltageListener, input: AAnalogInput) {
        messageBroadcaster.addAnalogVoltageListener(this, input, listener)
    }

    /**
     * Convenience method for adding a listener to a particular channel.

     * @param listener [IDigitalChannelListener] Listener to be registered.
     * *
     * @param input    [DigitalInput] Input to register to.
     */
    fun addDigitalChannelListener(listener: IDigitalChannelListener, input: DigitalInput) {
        messageBroadcaster.addDigitalChannelListener(this, input, listener)
    }

    /**
     * Convenience method for adding a listener to a particular channel.

     * @param listener [IPWMChannelListener] Listener to be registered.
     * *
     * @param input    [DigitalInput] Input to register to.
     */
    fun addPWMChannelListener(listener: IPWMChannelListener, input: DigitalInput) {
        messageBroadcaster.addPWMChannelListener(this, input, listener)
    }

    /**
     * Convenience method for removing a listener from a tekdaqc.

     * @param listener [IPWMChannelListener] Listener to be unregistered.
     */
    fun removePWMChannelListener(listener: IPWMChannelListener, input: DigitalInput) {
        messageBroadcaster.removePWMChannelListener(this, input, listener)
    }

    /**
     * Convenience method for removing a listener from a tekdaqc.

     * @param listener [IMessageListener] Listener to be unregistered.
     */
    fun removeListener(listener: IMessageListener) {
        messageBroadcaster.removeListener(this, listener)
    }

    /**
     * Convenience method for removing a listener from a channel on a tekdaqc.

     * @param listener [ICountListener] The listener to be unregistered.
     * *
     * @param input    [AAnalogInput] The input to unregister from.
     */
    fun removeAnalogCountListener(input: AAnalogInput, listener: ICountListener) {
        messageBroadcaster.removeAnalogCountListener(this, input, listener)
    }

    /**
     * Convenience method for removing a listener from a channel on a tekdaqc.

     * @param listener [ICountListener] The listener to be unregistered.
     * *
     * @param input    [AAnalogInput] The input to unregister from.
     */
    fun removeAnalogVoltageListener(input: AAnalogInput, listener: IVoltageListener) {
        messageBroadcaster.removeAnalogVoltageListener(this, input, listener)
    }

    /**
     * Convenience method for removing a listener from a channel on a tekdaqc.

     * @param listener [IDigitalChannelListener] The listener to be unregistered.
     * *
     * @param input    [DataInput] The input to unregister from.
     */
    fun removeDigitalChannelListener(input: DigitalInput, listener: IDigitalChannelListener) {
        messageBroadcaster.removeDigitalChannelListener(this, input, listener)
    }

    /**
     * Connect to this Tekdaqc via Telnet using the specified connection method.

     * @param method [CONNECTION_METHOD] The connection method to use.
     * *
     * @param currentAnalogScale The current [ATekdaqc.AnalogScale] the board is set to.
     * *                     This should match the physical jumpers on the board.
     * *
     * @throws IOException Thrown if the underlying Telnet client fails
     * *                     to connect.
     */
    @Throws(IOException::class)
    open fun connect(currentAnalogScale: AnalogScale, method: CONNECTION_METHOD) {
        if (isConnected) {
            throw IOException("Tekdaqc Already Connected")
        }
        when (method) {
            ATekdaqc.CONNECTION_METHOD.ETHERNET -> mConnection = EthernetTelnetConnection(hostIP, EthernetTelnetConnection.TEKDAQC_TELNET_PORT)
            ATekdaqc.CONNECTION_METHOD.SERIAL -> mConnection = SerialTelnetConnection()
            ATekdaqc.CONNECTION_METHOD.USB -> mConnection = USBTelnetConnection()
        }
        inputStream = mConnection?.inputStream
        outputStream = mConnection?.outputStream

        readExecutor = ReadExecutor(this, this)

        mCommandQueue.tryCommand()

        analogScale = currentAnalogScale

        isConnected = true
        mHeartbeatTimer.schedule(mHeartbeatTimerTask, HEARTBEAT_TIMER_INTERVAL.toLong(), HEARTBEAT_TIMER_INTERVAL.toLong())
    }

    /**
     * Disconnect from this Tekdaqc's Telnet server. This is an unclean
     * disconnect (no command).

     * @throws IOException Thrown if the underlying Telnet client fails to disconnect
     * *                     properly.
     */
    @Throws(IOException::class)
    fun disconnect() {

        readExecutor?.shutdown()
        parsingExecutor.shutdown()

        mCommandQueue.purge(true)

        mConnection?.disconnect()

        isConnected = false

        mHeartbeatTimer = mHeartbeatTimer.reprepare()
    }

    /**
     * Disconnect from this Tekdaqc's Telnet server cleanly (issue the command).
     * It is the responsibility of the calling application to ensure that the
     * connection's stream resources are properly cleaned up.
     */
    fun disconnectCleanly() {

        mCommandQueue.queueCommand(CommandBuilder.disconnect())

        mCommandQueue.queueCommand(QueueCallback(object : ITaskComplete {
            override fun onTaskSuccess(tekdaqc: ATekdaqc) {
                try {
                    disconnect()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }

            override fun onTaskFailed(tekdaqc: ATekdaqc) {
                criticalErrorNotification(TekdaqcCriticalError.PARTIAL_DISCONNECTION)
            }
        }))

        mHeartbeatTimer = mHeartbeatTimer.reprepare()
    }

    /**
     * Instructs the Tekdaqc to read the current state of the digital outputs.
     */
    fun readDigitalOutput() {

        mCommandQueue.queueCommand(CommandBuilder.readDigitalOutput())
    }

    /**
     * Instructs the Tekdaqc to enter calibration mode. WARNING: This command
     * must be used with caution. When executed, the Tekdaqc will erase the
     * entire contents of the calibration table in preparation for writing new
     * data.
     */
    fun enterCalibrationMode() {
        mCommandQueue.queueCommand(CommandBuilder.enterCalibrationMode())
    }

    /**
     * Instructs the Tekdaqc to exit calibration mode. WARNING: This command
     * must be used with caution. When executed, the Tekdaqc will lock the
     * calibration table, requiring a complete erasure prior to being able to
     * write any new data.
     */
    fun exitCalibrationMode() {
        mCommandQueue.queueCommand(CommandBuilder.exitCalibrationMode())
    }

    /**
     * Instruct the Tekdaqc to write its serial number.

     * @param serial [String] The serial number value.
     */
    fun writeSerialNumber(serial: String) {
        mCommandQueue.queueCommand(CommandBuilder.writeSerialNumber(serial))
    }

    /**
     * Instruct the Tekdaqc to write its factory MAC address.

     * @param mac `long` The factory MAC address value.
     */
    fun writeFactoryMacAddress(mac: Long) {
        mCommandQueue.queueCommand(CommandBuilder.writeFactoryMacAddress(mac))
    }

    override fun toString(): String {
        return locatorResponse.toString()
    }

    @Throws(IOException::class, ClassNotFoundException::class)
    override fun readExternal(input: ObjectInput) {
        locatorResponse = input.readObject() as LocatorResponse
        readIn(input)
    }

    @Throws(IOException::class)
    override fun writeExternal(output: ObjectOutput) {
        output.writeObject(locatorResponse)
        writeOut(output)
    }

    /**
     * Internal method used for initializing the correct number of
     */
    protected abstract fun initializeBoardStatusLists()

    /**
     * Reads a specified analog input the specified number of times, with the
     * parameters specified in the input.

     * @param input  `int` The input to sample.
     * *
     * @param number `int` The number of samples to take. Any number less then or equal to 0 is interpreted
     * *               as continuously.
     * *
     * @throws IllegalArgumentException if the provided input is of the wrong type.
     */
    @Throws(IllegalArgumentException::class)
    abstract fun readAnalogInput(input: Int, number: Int)

    /**
     * Reads a specified range of analog inputs the specified number of times,
     * with the default parameters in the Tekdaqc. Note that if the specified
     * inputs correspond to ones which were added to the Tekdaqc with sampling
     * parameters, those will override the board defaults.

     * @param start  `int` The physical channel to start at. Must be less than end.
     * *
     * @param end    `int` The physical channel to end at. Must be greater than
     * *               start.
     * *
     * @param number `int` The number of samples to take. Any number less then or equal to 0 is
     * *               interpreted as continuously.
     * *
     * @throws IllegalArgumentException if the provided inputs do not
     * *                                  exist on the board.
     */
    @Throws(IllegalArgumentException::class)
    abstract fun readAnalogInputRange(start: Int, end: Int, number: Int)

    /**
     * Reads the specified set of analog inputs the specified number of times,
     * with the default parameters in the Tekdaqc. Note that if the specified
     * inputs correspond to ones which were added to the Tekdaqc with sampling
     * parameters, those will override the board defaults.

     * @param inputs [Set] of [Integer] input numbers to read. If null
     * *               a null command is returned.
     * *
     * @param number `int` The number of samples to take. Any number less then or equal to 0 is
     * *               interpreted as continuously.
     * *
     * @throws IllegalArgumentException if the input set contains inputs
     * *                                  which do not exist on the board.
     */
    @Throws(IllegalArgumentException::class)
    abstract fun readAnalogInputSet(inputs: Set<Int>, number: Int)

    /**
     * Reads all of the analog inputs the specified number of times, with the
     * default parameters in the Tekdaqc. Note that if any of the inputs
     * correspond to ones which were added to the Tekdaqc with sampling
     * parameters, those will override the board defaults.

     * @param number `int` The number of samples to take. Any number less then or equal to 0 is
     * *               interpreted as continuously.
     */
    abstract fun readAllAnalogInput(number: Int)

    /**
     * Reads a specified digital input the specified number of times, with the
     * parameters specified in the input.

     * @param input  `int` The input to sample.
     * *
     * @param number `int` The number of samples to take. Any number less then or equal to 0 is interpreted
     * *               as continuously.
     * *
     * @throws IllegalArgumentException if the provided input is of the wrong type.
     */
    @Throws(IllegalArgumentException::class)
    abstract fun readDigitalInput(input: Int, number: Int)

    /**
     * Reads a specified range of digital inputs the specified number of times,
     * with the default parameters in the Tekdaqc. Note that if the specified
     * inputs correspond to ones which were added to the Tekdaqc with sampling
     * parameters, those will override the board defaults.

     * @param start  `int` The physical channel to start at. Must be less than end.
     * *
     * @param end    `int` The physical channel to end at. Must be greater than
     * *               start.
     * *
     * @param number `int` The number of samples to take. Any number less then or equal to 0 is
     * *               interpreted as continuously.
     * *
     * @throws IllegalArgumentException if the provided inputs do not
     * *                                  exist on the board.
     */
    @Throws(IllegalArgumentException::class)
    abstract fun readDigitalInputRange(start: Int, end: Int, number: Int)

    /**
     * Reads the specified set of digital inputs the specified number of times,
     * with the default parameters in the Tekdaqc. Note that if the specified
     * inputs correspond to ones which were added to the Tekdaqc with sampling
     * parameters, those will override the board defaults.

     * @param inputs [Set] of [Integer] input numbers to read. If null
     * *               a null command is returned.
     * *
     * @param number `int` The number of samples to take. Any number less then or equal to 0 is
     * *               interpreted as continuously.
     * *
     * @throws IllegalArgumentException if the input set contains inputs
     * *                                  which do not exist on the board.
     */
    @Throws(IllegalArgumentException::class)
    abstract fun readDigitalInputSet(inputs: Set<Int>, number: Int)

    /**
     * Reads all of the digital inputs the specified number of times, with the
     * default parameters in the Tekdaqc. Note that if any of the inputs
     * correspond to ones which were added to the Tekdaqc with sampling
     * parameters, those will override the board defaults.

     * @param number `int` The number of samples to take. Any number less then or equal to 0 is
     * *               interpreted as continuously.
     */
    abstract fun readAllDigitalInput(number: Int)

    /**
     * Retrieve the command string to add a specified [AAnalogInput] to
     * the Tekdaqc.

     * @param input [AAnalogInput] The input to build the command for.
     * *
     * @throws IllegalArgumentException Thrown if the provided input is
     * *                                  of the wrong type or for a non-existent physical channel.
     * *
     * @throws IllegalStateException    Thrown if no connection exists with
     * *                                  the Tekdaqc or if the added input replaced an existing one.
     */
    @Throws(IllegalArgumentException::class, IllegalStateException::class)
    protected abstract fun addAnalogInput(input: AAnalogInput)

    /**
     * Activates an [AAnalogInput] so that it will be sampled when the "SAMPLE" or "READ_ANALOG_INPUT" commands are called.

     * @param inputNumber The physical input number of the [AAnalogInput] to be activated.
     * *
     * @return The [AAnalogInput] which has been activated.
     * *
     * @throws IllegalArgumentException Thrown if the provided input is
     * *                                  of the wrong type or for a non-existent physical channel.
     * *
     * @throws IllegalStateException    Thrown if no connection exists with
     * *                                  the Tekdaqc or if the added input replaced an existing one.
     */
    @Throws(IllegalArgumentException::class, IllegalStateException::class)
    abstract fun activateAnalogInput(inputNumber: Int): AAnalogInput

    /**
     * Activates an [DigitalInput] so that it will be sampled when the "SAMPLE" or "READ_DIGITAL_INPUT" commands are called.

     * @param inputNumber The physical input number of the [DigitalInput] to be activated.
     * *
     * @return The [DigitalInput] which has been activated.
     * *
     * @throws IllegalArgumentException Thrown if the provided input is
     * *                                  of the wrong type or for a non-existent physical channel.
     * *
     * @throws IllegalStateException    Thrown if no connection exists with
     * *                                  the Tekdaqc or if the added input replaced an existing one.
     */
    @Throws(IllegalArgumentException::class, IllegalStateException::class)
    abstract fun activateDigitalInput(inputNumber: Int): DigitalInput

    /**
     * Toggles the [DigitalOutput] to the desired status.

     * @param outputNumber The physical output number of the [DigitalOutput] to be changed.
     * *
     * @param status       The status of the output. True representing ON and False representing OFF.
     * *
     * @return The toggled [DigitalOutput].
     */
    abstract fun toggleDigitalOutput(outputNumber: Int, status: Boolean): DigitalOutput

    /**
     * Removes an analog input from this Tekdaqc.

     * @param input [AAnalogInput] The input to remove from the board.
     */
    protected abstract fun removeAnalogInput(input: AAnalogInput)

    /**
     * Removes an analog input from this Tekdaqc.

     * @param input `int` The input to remove from the board.
     */
    abstract fun deactivateAnalogInput(input: Int)

    /**
     * Attempts to remove all known added [AAnalogInput].
     */
    abstract fun deactivateAllAddedAnalogInputs()

    /**
     * Attempts to remove all analog inputs.
     */
    abstract fun deactivateAllAnalogInputs()

    /**
     * Adds a specified [DigitalInput] to the Tekdaqc.

     * @param input [DigitalInput] The input to build the command for.
     * *
     * @throws IllegalArgumentException Thrown if the provided input is
     * *                                  of the wrong type or for a non-existent physical channel.
     * *
     * @throws IllegalStateException    Thrown if no connection exists with
     * *                                  the Tekdaqc or if the added input replaced an existing one.
     */
    @Throws(IllegalArgumentException::class, IllegalStateException::class)
    protected abstract fun addDigitalInput(input: DigitalInput)

    /**
     * Removes a digital input from this Tekdaqc.

     * @param input [DigitalInput] The input to remove from the board.
     */
    abstract fun deactivateDigitalInput(input: DigitalInput)

    /**
     * Removes a digital input from this Tekdaqc.

     * @param input `int` The input to remove from the board.
     */
    abstract fun deactivateDigitalInput(input: Int)

    /**
     * Attempts to remove all known added digital inputs.
     */
    abstract fun deactivateAllAddedDigitalInputs()


    /**
     * Attempts to remove all digital inputs.
     */
    abstract fun deactivateAllDigitalInputs()

    /**
     * The command to set the digital outputs to their desired state.

     * @param binaryString The string representing the desired state of all digital outputs. Example: Only digital output 5
     * *                     on would be represented as "0000010000000000".
     */
    abstract fun setDigitalOutput(binaryString: String)

    /**
     * The command to set the digital outputs to their desired state.

     * @param hex The string representing the desired state of all digital outputs in hexadecimal form. Example: All Digital Outputs on
     * *            would be represented by "FFFF".
     */
    abstract fun setDigitalOutputByHex(hex: String)

    /**
     * The command to set the digital outputs to their desired state.

     * @param digitalOutputArray The array of booleans representing the desired state of all digital outputs with each index representing
     * *                           the corresponding Digital Output.
     */
    abstract fun setDigitalOutput(digitalOutputArray: BooleanArray)

    /**
     * Retrieve the command string to instruct the board to reboot into upgrade
     * mode.
     */
    abstract fun upgrade()

    /**
     * Retrieve the command string to instruct the Tekdaqc to identify itself.
     */
    abstract fun identify()

    /**
     * Retrieve the command string to instruct the Tekdaqc to sample all
     * channels a number of times.

     * @param number `int` The number of samples to take.
     */
    abstract fun sample(number: Int)

    /**
     * Retrieve the command string to instruct the Tekdaqc to halt any ongoing
     * operations.
     */
    abstract fun halt()

    /**
     * Retrieve the command string to instruct the Tekdaqc to set its real time
     * calendar.

     * @param timestamp `long` The unix epoch timestamp.
     */
    abstract fun setRTC(timestamp: Long)

    /**
     * Retrieve the command string to read the analog input ADC's internal
     * registers.
     */
    abstract fun readADCRegisters()

    /**
     * Instruct the Tekdaqc to initiate a system gain calibration.

     * @param input `int` The input to use for this calibration. This input must
     * *              have a valid calibration signal applied.
     */
    abstract fun systemGainCalibrate(input: Int)

    /**
     * Instruct the Tekdaqc to read out and return its current system gain
     * calibration value.
     */
    abstract fun readSystemGainCalibration()

    /**
     * Instruct the Tekdaqc to read out and return its current calibration
     * status.
     */
    abstract fun getCalibrationStatus()

    /**
     * Instruct the Tekdaqc to write a temperature value into its calibration
     * table at the specified index.

     * @param temperature `double` The temperature value to write.
     * *
     * @param index       `int` The temperature table index to write the value at.
     */
    abstract fun writeCalibrationTemperature(temperature: Double, index: Int)

    /**
     * Instruct the Tekdaqc to write a calibration value into its table for the
     * provided parameters.

     * @param value       `float` The calibration value.
     * *
     * @param gain        [Gain] The gain this value is valid for.
     * *
     * @param rate        [Rate] The rate this value is valid for.
     * *
     * @param buffer      [BufferState] The buffer state this value is valid for.
     * *
     * @param scale       [AnalogScale] The analog input scale this value is valid for.
     * *
     * @param temperature `int` The temperature index this value is valid for.
     */
    abstract fun writeGainCalibrationValue(value: Float, gain: Gain, rate: Rate, buffer: BufferState, scale: AnalogScale,
                                           temperature: Int)

    /**
     * Instruct the Tekdaqc to write into its calibration table that the table
     * is valid.
     */
    abstract fun writeCalibrationValid()

    /**
     * Converts the provided [AnalogInputCountData] point into a voltage using
     * the parameters of the data point and the specific Tekdaqc board.

     * @param data         [AnalogInputCountData] The data point to convert.
     * *
     * @param currentScale [AnalogScale] The current analog scale to use in the conversion.
     * *
     * @return double The reference voltage value.
     */
    abstract fun convertAnalogInputDataToVoltage(data: AnalogInputCountData, currentScale: AnalogScale): Double

    /**
     * Converts the provided [AnalogInputCountData] point into a temperature
     * using the parameters of the data point and the specific Tekdaqc board.
     * This will assume that the data point came from the input specified by
     * [.getColdJunctionInputNumber], but does not enforce it.

     * @param data [AnalogInputCountData] The data point to convert.
     * *
     * @return double The reference voltage value.
     */
    abstract fun convertAnalogInputDataToTemperature(data: AnalogInputCountData): Double

    /**
     * Retrieves the physical input number for the cold junction channel this
     * Tekdaqc.

     * @return `int` The physical channel of the cold junction input.
     */
    abstract val coldJunctionInputNumber: Int

    /**
     * Retrieves the analog scale multiplier for the provided scale.

     * @param scale [AnalogScale] The scale to retrieve the multiplier for.
     * *
     * @return double The multiplier.
     */
    abstract fun getAnalogScaleMultiplier(scale: AnalogScale): Double

    /**
     * Retrieves the list of valid [Gain] settings for this
     * [ATekdaqc].

     * @return [List] of [Gain] values.
     */
    abstract val validGains: List<Gain>

    /**
     * Retrieves the list of valid [Rate] settings for this
     * [ATekdaqc].

     * @return [List] of [Rate] values.
     */
    abstract val validRates: List<Rate>

    /**
     * Retrieves the list of valid [BufferState] settings for this
     * [ATekdaqc].

     * @return [List] of [BufferState] values.
     */
    abstract val validBufferStates: List<BufferState>

    /**
     * Retrieves the list of valid [AnalogScale] settings for this
     * [ATekdaqc].

     * @return [List] of [AnalogScale] values.
     */
    abstract val validAnalogScales: List<AnalogScale>

    /**
     * Called during the de-serialization process to allow subclasses to recover
     * their data from the serialization.

     * @param input [ObjectInput] The input stream.
     * *
     * @throws IOException            IoException.
     * *
     * @throws ClassNotFoundException Called if class is not found to parse.
     */
    @Throws(IOException::class, ClassNotFoundException::class)
    protected abstract fun readIn(input: ObjectInput)

    /**
     * Called during the serialization process to allow subclasses to add their
     * data to the serialization.

     * @param output [ObjectOutput] The output stream.
     * *
     * @throws IOException IoException.
     */
    @Throws(IOException::class)
    protected abstract fun writeOut(output: ObjectOutput)

    /**
     * Enumeration of the available connection methods.

     * @author Tenkiv (software@tenkiv.com)
     * *
     * @since v1.0.0.0
     */
    enum class CONNECTION_METHOD {
        ETHERNET, SERIAL, USB
    }

    /**
     * Enumeration of the available communication methods.

     * @author Tenkiv (software@tenkiv.com)
     * *
     * @since v1.0.0.0
     */
    enum class COMMUNICATION_ENCODING {
        ASCII, BINARY
    }

    /**
     * Enumeration of the available analog input voltage scales.

     * @author Tenkiv (software@tenkiv.com)
     * *
     * @since v1.0.0.0
     */
    enum class AnalogScale private constructor(val scale: String) {
        ANALOG_SCALE_5V("ANALOG_SCALE_5V"), ANALOG_SCALE_400V("ANALOG_SCALE_400V");

        override fun toString(): String {
            return scale
        }

        companion object {

            private val mValueArray = AnalogScale.values()

            fun getValueFromOrdinal(ordinal: Byte): AnalogScale {
                return mValueArray[ordinal.toInt()]
            }

            fun fromString(scale: String): AnalogScale? {
                for (s in values()) {
                    if (s.scale == scale) {
                        return s
                    }
                }
                return null
            }
        }
    }

}

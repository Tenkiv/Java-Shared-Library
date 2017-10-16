package com.tenkiv.tekdaqc.communication.message

import com.tenkiv.tekdaqc.communication.ascii.message.parsing.ASCIIDigitalOutputDataMessage
import com.tenkiv.tekdaqc.communication.ascii.message.parsing.ASCIIMessageUtils
import com.tenkiv.tekdaqc.communication.data_points.AnalogInputCountData
import com.tenkiv.tekdaqc.communication.data_points.DigitalInputData
import com.tenkiv.tekdaqc.communication.data_points.PWMInputData
import com.tenkiv.tekdaqc.hardware.AAnalogInput
import com.tenkiv.tekdaqc.hardware.ATekdaqc
import com.tenkiv.tekdaqc.hardware.DigitalInput
import com.tenkiv.tekdaqc.hardware.IInputOutputHardware
import org.tenkiv.coral.ValueInstant
import tec.uom.se.ComparableQuantity
import tec.uom.se.quantity.Quantities
import tec.uom.se.unit.Units

import javax.measure.Quantity
import javax.measure.quantity.ElectricPotential
import java.time.Instant
import java.util.ArrayList
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * Class responsible for broadcasting messages received from Tekdaqcs.
 * <br></br>**This class is thread safe.**

 * @author Tenkiv (software@tenkiv.com)
 * *
 * @since v1.0.0.0
 */
class MessageBroadcaster {

    /**
     * Map of all registered all-channel listeners.
     */
    private val mFullListeners = ConcurrentHashMap<ATekdaqc, MutableList<IMessageListener>>()

    /**
     * Map of all registered network listeners.
     */
    private val mNetworkListeners = ConcurrentHashMap<ATekdaqc, MutableList<INetworkListener>>()

    /**
     * Map of all registered count listeners.
     */
    private val mAnalogCountListeners = ConcurrentHashMap<ATekdaqc, MutableMap<Int, MutableList<ICountListener>>>()

    /**
     * Map of all registered voltage listeners.
     */
    private val mAnalogVoltageListeners = ConcurrentHashMap<ATekdaqc, MutableMap<Int, MutableList<IVoltageListener>>>()

    /**
     * Map of all registered digital listeners.
     */
    private val mDigitalChannelListeners = ConcurrentHashMap<ATekdaqc, MutableMap<Int, MutableList<IDigitalChannelListener>>>()

    /**
     * Map of all registered PWM Input listeners.
     */
    private val mPWMChannelListeners = ConcurrentHashMap<ATekdaqc, MutableMap<Int, MutableList<IPWMChannelListener>>>()

    /**
     * Map of prioritized listeners.
     */
    private val mQueueListeners = ConcurrentHashMap<ATekdaqc, IMessageListener>()

    /**
     * Executor for handling callbacks to listeners.
     */
    private var mCallbackThreadpool: Executor = Executors.newCachedThreadPool()

    /**
     * Sets the [Executor] that manages callbacks to [IMessageListener]s, [ICountListener]s,
     * and [IDigitalChannelListener]s.

     * @param callbackExecutor The new [Executor].
     */
    fun setCallbackExecutor(callbackExecutor: Executor) {
        mCallbackThreadpool = callbackExecutor
    }

    internal fun commandQueueAddListener(tekdaqc: ATekdaqc, listener: IMessageListener) {
        mQueueListeners.put(tekdaqc, listener)
    }

    internal fun commandQueueRemoveListener(tekdaqc: ATekdaqc) {
        mQueueListeners.remove(tekdaqc)
    }

    /**
     * Register an object for message broadcasts for a particular Tekdaqc.

     * @param tekdaqc  [ATekdaqc] The Tekdaqc to register for.
     * *
     * @param listener [IMessageListener] Listener instance to receive the broadcasts.
     */
    fun addMessageListener(tekdaqc: ATekdaqc, listener: IMessageListener) {
        val listeners: MutableList<IMessageListener>
                = mFullListeners.computeIfAbsent(tekdaqc, { ArrayList() })

        synchronized(listeners) {
            if (!listeners.contains(listener)) {
                listeners.add(listener)
            }
        }
    }

    /**
     * Register an object for message broadcasts for a particular Tekdaqc.

     * @param tekdaqc  [ATekdaqc] The Tekdaqc to register for.
     * *
     * @param listener [IMessageListener] Listener instance to receive the broadcasts.
     */
    fun addNetworkListener(tekdaqc: ATekdaqc, listener: INetworkListener) {
        val listeners: MutableList<INetworkListener>
                = mNetworkListeners.computeIfAbsent(tekdaqc, { ArrayList() })

        synchronized(listeners) {
            if (!listeners.contains(listener)) {
                listeners.add(listener)
            }
        }
    }

    /**
     * Register an object for PWM broadcasts for a specific channel on a particular Tekdaqc.

     * @param tekdaqc  [ATekdaqc] The Tekdaqc to register for.
     * *
     * @param input    [DigitalInput] Physical number of the channel to listen for.
     * *
     * @param listener [IPWMChannelListener] Listener instance to receive the broadcasts.
     */
    fun addPWMChannelListener(tekdaqc: ATekdaqc, input: DigitalInput, listener: IPWMChannelListener) {
        val listeners: MutableMap<Int, MutableList<IPWMChannelListener>>
                = mPWMChannelListeners.computeIfAbsent(tekdaqc,
                { ConcurrentHashMap() })

        synchronized(listeners) {
            val listenerList: MutableList<IPWMChannelListener>
                    = listeners.getOrPut(input.channelNumber, { ArrayList<IPWMChannelListener>() })

            if (!listenerList.contains(listener)) {
                listenerList.add(listener)
            }
        }
    }

    /**
     * Un-register an object from PWM broadcasts for a particular Tekdaqc.

     * @param tekdaqc  [ATekdaqc] The Tekdaqc to un-register for.
     * *
     * @param input    [DigitalInput] The input to unregister from
     * *
     * @param listener [IPWMChannelListener] Listener instance to remove from broadcasts.
     */
    fun removePWMChannelListener(tekdaqc: ATekdaqc, input: DigitalInput, listener: IPWMChannelListener) {
        unregisterInputListener(tekdaqc, input, listener, mPWMChannelListeners)
    }

    /**
     * Register an object for message broadcasts for a specific channel on a particular Tekdaqc.

     * @param tekdaqc  [ATekdaqc] The Tekdaqc to register for.
     * *
     * @param input    [AAnalogInput] Physical number of the channel to listen for.
     * *
     * @param listener [ICountListener] Listener instance to receive the broadcasts.
     */
    fun addAnalogChannelListener(tekdaqc: ATekdaqc, input: AAnalogInput, listener: ICountListener) {
        val listeners: MutableMap<Int, MutableList<ICountListener>>
                = mAnalogCountListeners.computeIfAbsent(tekdaqc,
                { ConcurrentHashMap() })

        synchronized(listeners) {
            val listenerList: MutableList<ICountListener>
                    = listeners.getOrPut(input.channelNumber, { ArrayList() })

            if (!listenerList.contains(listener)) {
                listenerList.add(listener)
            }
        }
    }

    /**
     * Register an object for message broadcasts for a specific channel on a particular Tekdaqc.

     * @param tekdaqc  [ATekdaqc] The Tekdaqc to register for.
     * *
     * @param input    [AAnalogInput] Physical number of the channel to listen for.
     * *
     * @param listener [IVoltageListener] Listener instance to receive the broadcasts.
     */
    fun addAnalogVoltageListener(tekdaqc: ATekdaqc, input: AAnalogInput, listener: IVoltageListener) {
        val listeners: MutableMap<Int, MutableList<IVoltageListener>>
                = mAnalogVoltageListeners.computeIfAbsent(tekdaqc,
                { ConcurrentHashMap() })

        synchronized(listeners) {
            val listenerList: MutableList<IVoltageListener>
                    = listeners.getOrPut(input.channelNumber, { ArrayList() })

            if (!listenerList.contains(listener)) {
                listenerList.add(listener)
            }
        }
    }

    /**
     * Register an object for message broadcasts for a specific channel on a particular Tekdaqc.

     * @param tekdaqc  [ATekdaqc] The Tekdaqc to register for.
     * *
     * @param input    [DigitalInput] Physical number of the channel to listen for.
     * *
     * @param listener [IDigitalChannelListener] Listener instance to receive the broadcasts.
     */
    fun addDigitalChannelListener(tekdaqc: ATekdaqc, input: DigitalInput, listener: IDigitalChannelListener) {
        val listeners: MutableMap<Int, MutableList<IDigitalChannelListener>>
                = mDigitalChannelListeners.computeIfAbsent(tekdaqc,
                { ConcurrentHashMap() })

        synchronized(listeners) {
            val listenerList: MutableList<IDigitalChannelListener>
                    = listeners.getOrPut(input.channelNumber, { ArrayList() })

            if (!listenerList.contains(listener)) {
                listenerList.add(listener)
            }
        }
    }

    /**
     * Un-register an object from message broadcasts for a particular Tekdaqc.

     * @param tekdaqc  [ATekdaqc] The Tekdaqc to un-register for.
     * *
     * @param listener [IMessageListener] Listener instance to remove from broadcasts.
     */
    fun removeListener(tekdaqc: ATekdaqc, listener: IMessageListener) {
        val listeners = mFullListeners[tekdaqc]
        if (listeners != null) {
            synchronized(listeners) {
                listeners.remove(listener)
                if (listeners.size == 0) {
                    mFullListeners.remove(tekdaqc)
                }
            }
        }
    }

    /**
     * Un-register an object from message broadcasts for a particular Tekdaqc.

     * @param tekdaqc  [ATekdaqc] The Tekdaqc to un-register for.
     * *
     * @param input    [AAnalogInput] The input to unregister from
     * *
     * @param listener [ICountListener] Listener instance to remove from broadcasts.
     */
    fun removeAnalogCountListener(tekdaqc: ATekdaqc, input: AAnalogInput, listener: ICountListener) {
        unregisterInputListener(tekdaqc, input, listener, mAnalogCountListeners)
    }

    /**
     * Un-register an object from message broadcasts for a particular Tekdaqc.

     * @param tekdaqc  [ATekdaqc] The Tekdaqc to un-register for.
     * *
     * @param input    [AAnalogInput] The input to unregister from
     * *
     * @param listener [IVoltageListener] Listener instance to remove from broadcasts.
     */
    fun removeAnalogVoltageListener(tekdaqc: ATekdaqc, input: AAnalogInput, listener: IVoltageListener) {
        unregisterInputListener(tekdaqc, input, listener, mAnalogVoltageListeners)
    }

    /**
     * Un-register an object from message broadcasts for a particular Tekdaqc.

     * @param tekdaqc  [ATekdaqc] The Tekdaqc to un-register for.
     * *
     * @param input    [DigitalInput] The input to unregister from
     * *
     * @param listener [IDigitalChannelListener] Listener instance to remove from broadcasts.
     */
    fun removeDigitalChannelListener(tekdaqc: ATekdaqc, input: DigitalInput, listener: IDigitalChannelListener) {
        unregisterInputListener(tekdaqc, input, listener, mDigitalChannelListeners)
    }

    private fun <IT : IInputOutputHardware, LT> unregisterInputListener(tekdaqc: ATekdaqc, input: IT, listener: LT, listenerMap: MutableMap<ATekdaqc, MutableMap<Int, MutableList<LT>>>) {
        val listeners = listenerMap[tekdaqc]?.get(input.channelNumber)

        if (listeners != null) {
            synchronized(listeners) {
                listeners.remove(listener)
                if (listeners.size == 0) {
                    listenerMap[tekdaqc]?.remove(input.channelNumber)
                }
            }
        }
    }


    /**
     * Broadcast a [ABoardMessage] to all registered listeners for the specified Tekdaqc.

     * @param tekdaqc [ATekdaqc] The serial number string of the Tekdaqc to broadcast for.
     * *
     * @param message [ABoardMessage] The message to broadcast.
     */
    fun broadcastMessage(tekdaqc: ATekdaqc, message: ABoardMessage) {
        mCallbackThreadpool.execute(BroadcastRunnable(tekdaqc, message))
    }

    /**
     * Broadcast a [ABoardMessage] to registered network listeners for the specified Tekdaqc.

     * @param tekdaqc [ATekdaqc] The serial number string of the Tekdaqc to broadcast for.
     * *
     * @param message [ABoardMessage] The message to broadcast.
     */
    fun broadcastNetworkError(tekdaqc: ATekdaqc, message: ABoardMessage) {
        mCallbackThreadpool.execute(NetworkBroadcastRunnable(tekdaqc, message))
    }

    /**
     * Broadcast a single [AnalogInputCountData] point to all registered listeners for the specified Tekdaqc.

     * @param tekdaqc [ATekdaqc] The serial number string of the Tekdaqc to broadcast for.
     * *
     * @param data    [AnalogInputCountData] The data point to broadcast.
     */
    fun broadcastAnalogInputDataPoint(tekdaqc: ATekdaqc, data: AnalogInputCountData) {
            val listeners = mFullListeners[tekdaqc]
            if (listeners != null) {
                synchronized(listeners) {
                    listeners.forEach { listener ->
                        listener.onAnalogInputDataReceived(tekdaqc, data)
                    }
                }
            }

            if (mAnalogCountListeners.containsKey(tekdaqc)) {
                if (mAnalogCountListeners[tekdaqc]?.containsKey(data.physicalInput) == true) {
                    val channelListeners = mAnalogCountListeners[tekdaqc]?.get(data.physicalInput)
                    channelListeners?.let {
                        synchronized(it) {
                            channelListeners.forEach { listener -> listener.onAnalogDataReceived(tekdaqc.getAnalogInput(data.physicalInput), data.data) }
                        }
                    }
                }
            }

            if (mAnalogVoltageListeners.containsKey(tekdaqc)) {
                if (mAnalogVoltageListeners[tekdaqc]?.containsKey(data.physicalInput) == true) {
                    val channelListeners = mAnalogVoltageListeners[tekdaqc]?.get(data.physicalInput)
                    channelListeners?.let {
                        synchronized(it) {

                                val quant = Quantities.getQuantity(
                                        tekdaqc.convertAnalogInputDataToVoltage(
                                                data,
                                                tekdaqc.analogScale), Units.VOLT)
                                channelListeners.forEach { listener ->
                                    listener.onVoltageDataReceived(
                                            tekdaqc.getAnalogInput(data.physicalInput),
                                            ValueInstant<ComparableQuantity<ElectricPotential>>(
                                                    quant,
                                                    Instant.ofEpochMilli(data.timestamp)))
                                }

                        }
                    }
                }
            }
    }

    /**
     * Broadcast a single [DigitalInputData] point to all registered listeners for the specified Tekdaqc.

     * @param tekdaqc [ATekdaqc] The serial number string of the Tekdaqc to broadcast for.
     * *
     * @param data    [DigitalInputData] The data point to broadcast.
     */
    fun broadcastDigitalInputDataPoint(tekdaqc: ATekdaqc, data: DigitalInputData) {
        val listeners = mFullListeners[tekdaqc]
        listeners?.let {
            synchronized(it) {
                for (listener in listeners) {
                    listener.onDigitalInputDataReceived(tekdaqc, data)
                }
            }
        }

        if (mDigitalChannelListeners.containsKey(tekdaqc)) {
            if (mDigitalChannelListeners[tekdaqc]?.containsKey(data.physicalInput) == true) {
                val channelListeners = mDigitalChannelListeners[tekdaqc]?.get(data.physicalInput)
                channelListeners?.let {
                    synchronized(it) {
                        channelListeners.forEach { listener -> listener.onDigitalDataReceived(tekdaqc.getDigitalInput(data.physicalInput), data) }
                    }
                }
            }
        }
    }

    /**
     * Broadcast a single [DigitalInputData] point to all registered listeners for the specified Tekdaqc.

     * @param tekdaqc [ATekdaqc] The serial number string of the Tekdaqc to broadcast for.
     * *
     * @param data    [DigitalInputData] The data point to broadcast.
     */
    fun broadcastPWMInputDataPoint(tekdaqc: ATekdaqc, data: PWMInputData) {
        if (mPWMChannelListeners.containsKey(tekdaqc)) {
            if (mPWMChannelListeners[tekdaqc]?.containsKey(data.physicalInput) == true) {
                val channelListeners = mPWMChannelListeners[tekdaqc]?.get(data.physicalInput)
                channelListeners?.let {
                    synchronized(it) {
                        channelListeners.forEach { listener -> listener.onPWMDataReceived(tekdaqc.getDigitalInput(data.physicalInput), data) }
                    }
                }
            }
        }
    }

    /**
     * Class that wraps callbacks from the [com.tenkiv.tekdaqc.communication.ascii.executors.ASCIIParsingExecutor]
     * so that they are called back in a different thread.
     */
    private inner class BroadcastRunnable(internal val mTekdaqc: ATekdaqc, internal val mMessage: ABoardMessage) : Runnable {

        override fun run() {

            if(mMessage.type == ASCIIMessageUtils.MESSAGE_TYPE.STATUS) {
                mQueueListeners[mTekdaqc]?.onStatusMessageReceived(mTekdaqc, mMessage)
            }else if(mMessage.type == ASCIIMessageUtils.MESSAGE_TYPE.ERROR){
                mQueueListeners[mTekdaqc]?.onStatusMessageReceived(mTekdaqc, mMessage)
            }

            val listeners = mFullListeners[mTekdaqc]
            if (listeners != null) {
                synchronized(listeners) {
                    for (listener in listeners) {
                        when (mMessage.type) {
                            ASCIIMessageUtils.MESSAGE_TYPE.DEBUG -> listener.onDebugMessageReceived(mTekdaqc, mMessage)
                            ASCIIMessageUtils.MESSAGE_TYPE.STATUS -> listener.onStatusMessageReceived(mTekdaqc, mMessage)
                            ASCIIMessageUtils.MESSAGE_TYPE.ERROR -> listener.onErrorMessageReceived(mTekdaqc, mMessage)
                            ASCIIMessageUtils.MESSAGE_TYPE.COMMAND_DATA -> listener.onCommandDataMessageReceived(mTekdaqc, mMessage)
                            ASCIIMessageUtils.MESSAGE_TYPE.DIGITAL_OUTPUT_DATA -> {
                                listener.onDigitalOutputDataReceived(mTekdaqc, (mMessage as ASCIIDigitalOutputDataMessage)
                                        .digitalOutputArray)
                                System.err.println("Unknown message type with serial: " + mTekdaqc.serialNumber)
                            }
                            else -> System.err.println("Unknown message type with serial: " + mTekdaqc.serialNumber)
                        }
                    }
                }
            }

        }
    }

    private inner class NetworkBroadcastRunnable(internal val mTekdaqc: ATekdaqc, internal val mMessage: ABoardMessage) : Runnable {

        override fun run() {
            val listeners = mNetworkListeners[mTekdaqc]
            listeners?.forEach { listener -> listener.onNetworkConditionDetected(mTekdaqc, mMessage) }
        }
    }
}

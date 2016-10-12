package com.tenkiv.tekdaqc.communication.message;

import com.tenkiv.tekdaqc.communication.ascii.message.parsing.ASCIIDigitalOutputDataMessage;
import com.tenkiv.tekdaqc.communication.data_points.ProtectedAnalogInputData;
import com.tenkiv.tekdaqc.communication.data_points.DigitalInputData;
import com.tenkiv.tekdaqc.hardware.AAnalogInput;
import com.tenkiv.tekdaqc.hardware.ATekdaqc;
import com.tenkiv.tekdaqc.hardware.DigitalInput;
import com.tenkiv.tekdaqc.hardware.IInputOutputHardware;
import tec.uom.se.quantity.Quantities;
import tec.uom.se.spi.Measurement;
import tec.uom.se.unit.Units;

import javax.measure.Quantity;
import javax.measure.quantity.ElectricPotential;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Class responsible for broadcasting messages received from Tekdaqcs.
 * <br><b>This class is thread safe.</b>
 *
 * @author Tenkiv (software@tenkiv.com)
 * @since v1.0.0.0
 */
public final class MessageBroadcaster {

    /**
     * Map of all registered all-channel listeners.
     */
    private final Map<ATekdaqc, List<IMessageListener>> mFullListeners = new ConcurrentHashMap<ATekdaqc, List<IMessageListener>>();

    /**
     * Map of all registered count listeners.
     */
    private final Map<ATekdaqc, Map<Integer, List<ICountListener>>> mAnalogCountListeners = new ConcurrentHashMap<ATekdaqc, Map<Integer, List<ICountListener>>>();

    /**
     * Map of all registered voltage listeners.
     */
    private final Map<ATekdaqc, Map<Integer, List<IVoltageListener>>> mAnalogVoltageListeners = new ConcurrentHashMap<ATekdaqc, Map<Integer, List<IVoltageListener>>>();

    /**
     * Map of all registered digital listeners.
     */
    private final Map<ATekdaqc, Map<Integer, List<IDigitalChannelListener>>> mDigitalChannelListeners = new ConcurrentHashMap<ATekdaqc, Map<Integer, List<IDigitalChannelListener>>>();

    /**
     * Executor for handling callbacks to listeners.
     */
    private Executor mCallbackThreadpool = Executors.newFixedThreadPool(1);

    /**
     * Sets the {@link Executor} that manages callbacks to {@link IMessageListener}s, {@link ICountListener}s,
     * and {@link IDigitalChannelListener}s.
     *
     * @param callbackExecutor The new {@link Executor}.
     */
    public void setCallbackExecutor(Executor callbackExecutor){
        mCallbackThreadpool = callbackExecutor;
    }

    /**
     * Register an object for message broadcasts for a particular Tekdaqc.
     *
     * @param tekdaqc  {@link ATekdaqc} The Tekdaqc to register for.
     * @param listener {@link IMessageListener} Listener instance to receive the broadcasts.
     */
    public void addMessageListener(final ATekdaqc tekdaqc, final IMessageListener listener) {
        final List<IMessageListener> listeners;
        if (mFullListeners.get(tekdaqc) != null) {
            listeners = mFullListeners.get(tekdaqc);
        } else {
            listeners = new ArrayList<IMessageListener>();
            mFullListeners.put(tekdaqc, listeners);
        }

        synchronized (listeners) {
            if (!listeners.contains(listener)) {
                listeners.add(listener);
            } else {
                System.err.println("Listener " + listener
                        + " has already been registered for serial: " + tekdaqc.getSerialNumber());
            }
        }
    }

    /**
     * Register an object for message broadcasts for a specific channel on a particular Tekdaqc.
     *
     * @param tekdaqc  {@link ATekdaqc} The Tekdaqc to register for.
     * @param input    {@link AAnalogInput} Physical number of the channel to listen for.
     * @param listener {@link ICountListener} Listener instance to receive the broadcasts.
     */
    public void addAnalogChannelListener(final ATekdaqc tekdaqc
            , final AAnalogInput input
            , final ICountListener listener) {
        final Map<Integer, List<ICountListener>> listeners;
        if (mAnalogCountListeners.get(tekdaqc) != null) {
            listeners = mAnalogCountListeners.get(tekdaqc);
        } else {
            listeners = new ConcurrentHashMap<Integer, List<ICountListener>>();
            mAnalogCountListeners.put(tekdaqc, listeners);
        }

        synchronized (listeners) {
            if (!listeners.containsKey(input.getChannelNumber())) {
                listeners.put(input.getChannelNumber(), new ArrayList<ICountListener>());
            }
            ArrayList<ICountListener> listenerList = (ArrayList<ICountListener>) listeners
                    .get(input.getChannelNumber());
            if (!listenerList.contains(listener)) {
                listenerList.add(listener);
            } else {
                System.err.println("Listener " + listener + " has already been registered for serial: "
                        + tekdaqc.getSerialNumber());
            }
        }
    }

    /**
     * Register an object for message broadcasts for a specific channel on a particular Tekdaqc.
     *
     * @param tekdaqc  {@link ATekdaqc} The Tekdaqc to register for.
     * @param input    {@link AAnalogInput} Physical number of the channel to listen for.
     * @param listener {@link IVoltageListener} Listener instance to receive the broadcasts.
     */
    public void addAnalogVoltageListener(final ATekdaqc tekdaqc
            , final AAnalogInput input
            , final IVoltageListener listener) {
        final Map<Integer, List<IVoltageListener>> listeners;
        if (mAnalogVoltageListeners.get(tekdaqc) != null) {
            listeners = mAnalogVoltageListeners.get(tekdaqc);
        } else {
            listeners = new ConcurrentHashMap<Integer, List<IVoltageListener>>();
            mAnalogVoltageListeners.put(tekdaqc, listeners);
        }

        synchronized (listeners) {
            if (!listeners.containsKey(input.getChannelNumber())) {
                listeners.put(input.getChannelNumber(), new ArrayList<IVoltageListener>());
            }
            ArrayList<IVoltageListener> listenerList = (ArrayList<IVoltageListener>) listeners
                    .get(input.getChannelNumber());
            if (!listenerList.contains(listener)) {
                listenerList.add(listener);
            } else {
                System.err.println("Listener " + listener + " has already been registered for serial: "
                        + tekdaqc.getSerialNumber());
            }
        }
    }

    /**
     * Register an object for message broadcasts for a specific channel on a particular Tekdaqc.
     *
     * @param tekdaqc  {@link ATekdaqc} The Tekdaqc to register for.
     * @param input    {@link DigitalInput} Physical number of the channel to listen for.
     * @param listener {@link IDigitalChannelListener} Listener instance to receive the broadcasts.
     */
    public void addDigitalChannelListener(final ATekdaqc tekdaqc
            , final DigitalInput input
            , final IDigitalChannelListener listener) {
        final Map<Integer, List<IDigitalChannelListener>> listeners;
        if (mDigitalChannelListeners.get(tekdaqc) != null) {
            listeners = mDigitalChannelListeners.get(tekdaqc);
        } else {
            listeners = new ConcurrentHashMap<Integer, List<IDigitalChannelListener>>();
            mDigitalChannelListeners.put(tekdaqc, listeners);
        }
        synchronized (listeners) {
            if (!listeners.containsKey(input.getChannelNumber())) {
                listeners.put(input.getChannelNumber(), new ArrayList<IDigitalChannelListener>());
            }
            ArrayList<IDigitalChannelListener> listenerList = (ArrayList<IDigitalChannelListener>) listeners
                    .get(input.getChannelNumber());
            if (!listenerList.contains(listener)) {
                listenerList.add(listener);
            } else {
                System.err.println("Listener " + listener + " has already been registered for serial: "
                        + tekdaqc.getSerialNumber());
            }
        }
    }

    /**
     * Un-register an object from message broadcasts for a particular Tekdaqc.
     *
     * @param tekdaqc  {@link ATekdaqc} The Tekdaqc to un-register for.
     * @param listener {@link IMessageListener} Listener instance to remove from broadcasts.
     */
    public void removeListener(final ATekdaqc tekdaqc, final IMessageListener listener) {
        final List<IMessageListener> listeners = mFullListeners.get(tekdaqc);
        if (listeners != null) {
            synchronized (listeners) {
                listeners.remove(listener);
                if (listeners.size() == 0) {
                    mFullListeners.remove(tekdaqc);
                }
            }
        }
    }

    /**
     * Un-register an object from message broadcasts for a particular Tekdaqc.
     *
     * @param tekdaqc  {@link ATekdaqc} The Tekdaqc to un-register for.
     * @param input    {@link AAnalogInput} The input to unregister from
     * @param listener {@link ICountListener} Listener instance to remove from broadcasts.
     */
    public void removeAnalogCountListener(final ATekdaqc tekdaqc
            , final AAnalogInput input
            , final ICountListener listener) {
        unregisterInputListener(tekdaqc, input, listener, mAnalogCountListeners);
    }

    /**
     * Un-register an object from message broadcasts for a particular Tekdaqc.
     *
     * @param tekdaqc  {@link ATekdaqc} The Tekdaqc to un-register for.
     * @param input    {@link AAnalogInput} The input to unregister from
     * @param listener {@link IVoltageListener} Listener instance to remove from broadcasts.
     */
    public void removeAnalogVoltageListener(final ATekdaqc tekdaqc
            , final AAnalogInput input
            , final IVoltageListener listener) {
        unregisterInputListener(tekdaqc, input, listener, mAnalogVoltageListeners);
    }

    /**
     * Un-register an object from message broadcasts for a particular Tekdaqc.
     *
     * @param tekdaqc  {@link ATekdaqc} The Tekdaqc to un-register for.
     * @param input    {@link DigitalInput} The input to unregister from
     * @param listener {@link IDigitalChannelListener} Listener instance to remove from broadcasts.
     */
    public void removeDigitalChannelListener(final ATekdaqc tekdaqc
            , final DigitalInput input
            , final IDigitalChannelListener listener) {
        unregisterInputListener(tekdaqc, input, listener, mDigitalChannelListeners);
    }

    private <IT extends IInputOutputHardware, LT> void unregisterInputListener(final ATekdaqc tekdaqc
            , final IT input
            , final LT listener
            , final Map<ATekdaqc, Map<Integer, List<LT>>> listenerMap) {
        final List<LT> listeners = listenerMap.get(tekdaqc).get(input.getChannelNumber());

        if (listeners != null) {
            synchronized (listeners) {
                listeners.remove(listener);
                if (listeners.size() == 0) {
                    listenerMap.get(tekdaqc).remove(input.getChannelNumber());
                }
            }
        }
    }


    /**
     * Broadcast a {@link ABoardMessage} to all registered listeners for the specified Tekdaqc.
     *
     * @param tekdaqc {@link ATekdaqc} The serial number string of the Tekdaqc to broadcast for.
     * @param message {@link ABoardMessage} The message to broadcast.
     */
    public void broadcastMessage(final ATekdaqc tekdaqc, final ABoardMessage message) {

        mCallbackThreadpool.execute(new BroadcastRunnable(tekdaqc,message));

    }

    /**
     * Broadcast a single {@link ProtectedAnalogInputData} point to all registered listeners for the specified Tekdaqc.
     *
     * @param tekdaqc {@link ATekdaqc} The serial number string of the Tekdaqc to broadcast for.
     * @param data    {@link ProtectedAnalogInputData} The data point to broadcast.
     */
    public void broadcastAnalogInputDataPoint(final ATekdaqc tekdaqc, final ProtectedAnalogInputData data) {
        final List<IMessageListener> listeners = mFullListeners.get(tekdaqc);
        if (listeners != null) {
            synchronized (listeners) {
                for (final IMessageListener listener : listeners) {
                    listener.onAnalogInputDataReceived(tekdaqc.getAnalogInput(data.getPhysicalInput()), data.getData());
                }
            }
        } else {
            System.out.println("No listeners for board: " + tekdaqc.getSerialNumber());
        }

        if (mAnalogCountListeners.containsKey(tekdaqc)) {
            if (mAnalogCountListeners.get(tekdaqc).containsKey(data.getPhysicalInput())) {
                final List<ICountListener> channelListeners = mAnalogCountListeners
                        .get(tekdaqc).get(data.getPhysicalInput());
                synchronized (channelListeners) {
                    for (final ICountListener listener : channelListeners) {
                        listener.onAnalogDataReceived(tekdaqc.getAnalogInput(data.getPhysicalInput()), data.getData());
                    }
                }
            }
        }

        if (mAnalogVoltageListeners.containsKey(tekdaqc)) {
            if (mAnalogVoltageListeners.get(tekdaqc).containsKey(data.getPhysicalInput())) {
                final List<IVoltageListener> channelListeners = mAnalogVoltageListeners
                        .get(tekdaqc).get(data.getPhysicalInput());
                synchronized (channelListeners) {

                    Quantity<ElectricPotential> quant = Quantities.getQuantity(tekdaqc.convertAnalogInputDataToVoltage(data,tekdaqc.getAnalogInputScale()), Units.VOLT);

                    for (final IVoltageListener listener : channelListeners) {
                        listener.onVoltageDataReceived(tekdaqc.getAnalogInput(data.getPhysicalInput()), Measurement.of(quant, Instant.ofEpochSecond(data.getTimestamp())));
                    }
                }
            }
        }
    }

    /**
     * Broadcast a single {@link DigitalInputData} point to all registered listeners for the specified Tekdaqc.
     *
     * @param tekdaqc {@link ATekdaqc} The serial number string of the Tekdaqc to broadcast for.
     * @param data    {@link DigitalInputData} The data point to broadcast.
     */
    public void broadcastDigitalInputDataPoint(final ATekdaqc tekdaqc, final DigitalInputData data) {
        final List<IMessageListener> listeners = mFullListeners.get(tekdaqc);
        synchronized (listeners) {
            for (final IMessageListener listener : listeners) {
                listener.onDigitalInputDataReceived(tekdaqc, data);
            }
        }

        if (mDigitalChannelListeners.containsKey(tekdaqc)) {
            if (mDigitalChannelListeners.get(tekdaqc).containsKey(data.getPhysicalInput())) {
                final List<IDigitalChannelListener> channelListeners = mDigitalChannelListeners
                        .get(tekdaqc).get(data.getPhysicalInput());
                synchronized (channelListeners) {
                    for (final IDigitalChannelListener listener : channelListeners) {
                        listener.onDigitalDataReceived(tekdaqc, data);
                    }
                }
            }
        }
    }

    /**
     * Class that wraps callbacks from the {@link com.tenkiv.tekdaqc.communication.ascii.executors.ASCIIParsingExecutor}
     * so that they are called back in a different thread.
     */
    private class BroadcastRunnable implements Runnable{

        final ATekdaqc mTekdaqc;
        final ABoardMessage mMessage;

        protected BroadcastRunnable(final ATekdaqc tekdaqc, final ABoardMessage message){
            mTekdaqc = tekdaqc;
            mMessage = message;
        }

        @Override
        public void run() {

            final List<IMessageListener> listeners = mFullListeners.get(mTekdaqc);
            if (listeners != null) {
                synchronized (listeners) {
                    for (final IMessageListener listener : listeners) {
                        switch (mMessage.getType()) {
                            case DEBUG:
                                listener.onDebugMessageReceived(mTekdaqc, mMessage);
                                break;
                            case STATUS:
                                listener.onStatusMessageReceived(mTekdaqc, mMessage);
                                break;
                            case ERROR:
                                listener.onErrorMessageReceived(mTekdaqc, mMessage);
                                break;
                            case COMMAND_DATA:
                                listener.onCommandDataMessageReceived(mTekdaqc, mMessage);
                                break;
                            case DIGITAL_OUTPUT_DATA:
                                listener.onDigitalOutputDataReceived(mTekdaqc, (((ASCIIDigitalOutputDataMessage) mMessage)
                                        .getDigitalOutputArray()));
                            default:
                                System.err.println("Unknown message type with serial: " + mTekdaqc.getSerialNumber());
                                break;
                        }
                    }
                }
            }

        }
    }
}

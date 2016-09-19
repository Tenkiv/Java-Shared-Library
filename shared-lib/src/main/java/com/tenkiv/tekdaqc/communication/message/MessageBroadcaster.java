package com.tenkiv.tekdaqc.communication.message;

import com.tenkiv.tekdaqc.communication.ascii.message.parsing.ASCIIDigitalOutputDataMessage;
import com.tenkiv.tekdaqc.communication.data_points.AnalogInputData;
import com.tenkiv.tekdaqc.communication.data_points.DigitalInputData;
import com.tenkiv.tekdaqc.hardware.AAnalogInput;
import com.tenkiv.tekdaqc.hardware.ATekdaqc;
import com.tenkiv.tekdaqc.hardware.DigitalInput;
import com.tenkiv.tekdaqc.hardware.IInputOutputHardware;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
    private final Map<ATekdaqc, List<IMessageListener>> mFullListeners;

    /**
     * Map of all registered listeners.
     */
    private final Map<ATekdaqc, Map<Integer, List<IAnalogChannelListener>>> mAnalogChannelListeners;

    /**
     * Map of all registered listeners.
     */
    private final Map<ATekdaqc, Map<Integer, List<IDigitalChannelListener>>> mDigitalChannelListeners;

    /**
     * Default constructor.
     */
    public MessageBroadcaster() {
        mFullListeners = new ConcurrentHashMap<ATekdaqc, List<IMessageListener>>();
        mAnalogChannelListeners = new ConcurrentHashMap<ATekdaqc, Map<Integer, List<IAnalogChannelListener>>>();
        mDigitalChannelListeners = new ConcurrentHashMap<ATekdaqc, Map<Integer, List<IDigitalChannelListener>>>();
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
     * @param listener {@link IAnalogChannelListener} Listener instance to receive the broadcasts.
     */
    public void addAnalogChannelListener(final ATekdaqc tekdaqc
            , final AAnalogInput input
            , final IAnalogChannelListener listener) {
        final Map<Integer, List<IAnalogChannelListener>> listeners;
        if (mAnalogChannelListeners.get(tekdaqc) != null) {
            listeners = mAnalogChannelListeners.get(tekdaqc);
        } else {
            listeners = new ConcurrentHashMap<Integer, List<IAnalogChannelListener>>();
            mAnalogChannelListeners.put(tekdaqc, listeners);
        }

        synchronized (listeners) {
            if (!listeners.containsKey(input.getChannelNumber())) {
                listeners.put(input.getChannelNumber(), new ArrayList<IAnalogChannelListener>());
            }
            ArrayList<IAnalogChannelListener> listenerList = (ArrayList<IAnalogChannelListener>) listeners
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
     * @param listener {@link IAnalogChannelListener} Listener instance to remove from broadcasts.
     */
    public void removeAnalogChannelListener(final ATekdaqc tekdaqc
            , final AAnalogInput input
            , final IAnalogChannelListener listener) {
        unregisterInputListener(tekdaqc, input, listener, mAnalogChannelListeners);
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
        final List<IMessageListener> listeners = mFullListeners.get(tekdaqc);
        if (listeners != null) {
            synchronized (listeners) {
                for (final IMessageListener listener : listeners) {
                    switch (message.getType()) {
                        case DEBUG:
                            listener.onDebugMessageReceived(tekdaqc, message);
                            break;
                        case STATUS:
                            listener.onStatusMessageReceived(tekdaqc, message);
                            break;
                        case ERROR:
                            listener.onErrorMessageReceived(tekdaqc, message);
                            break;
                        case COMMAND_DATA:
                            listener.onCommandDataMessageReceived(tekdaqc, message);
                            break;
                        case DIGITAL_OUTPUT_DATA:
                            listener.onDigitalOutputDataReceived(tekdaqc, (((ASCIIDigitalOutputDataMessage) message)
                                    .getDigitalOutputArray()));
                        default:
                            System.err.println("Unknown message type with serial: " + tekdaqc.getSerialNumber());
                            break;
                    }
                }
            }
        }
    }

    /**
     * Broadcast a single {@link AnalogInputData} point to all registered listeners for the specified Tekdaqc.
     *
     * @param tekdaqc {@link ATekdaqc} The serial number string of the Tekdaqc to broadcast for.
     * @param data    {@link AnalogInputData} The data point to broadcast.
     */
    public void broadcastAnalogInputDataPoint(final ATekdaqc tekdaqc, final AnalogInputData data) {
        final List<IMessageListener> listeners = mFullListeners.get(tekdaqc);
        if (listeners != null) {
            synchronized (listeners) {
                for (final IMessageListener listener : listeners) {
                    listener.onAnalogInputDataReceived(tekdaqc, data);
                }
            }
        } else {
            System.out.println("No listeners for board: " + tekdaqc.getSerialNumber());
        }

        if (mAnalogChannelListeners.containsKey(tekdaqc)) {
            if (mAnalogChannelListeners.get(tekdaqc).containsKey(data.getPhysicalInput())) {
                final List<IAnalogChannelListener> channelListeners = mAnalogChannelListeners
                        .get(tekdaqc).get(data.getPhysicalInput());
                synchronized (channelListeners) {
                    for (final IAnalogChannelListener listener : channelListeners) {
                        listener.onAnalogDataReceived(tekdaqc, data);
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
}

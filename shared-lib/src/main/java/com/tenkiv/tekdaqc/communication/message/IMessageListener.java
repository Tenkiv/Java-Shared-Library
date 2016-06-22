package com.tenkiv.tekdaqc.communication.message;

import com.tenkiv.tekdaqc.communication.data_points.AnalogInputData;
import com.tenkiv.tekdaqc.communication.data_points.DigitalInputData;
import com.tenkiv.tekdaqc.hardware.ATekdaqc;

import java.util.Arrays;

/**
 * Interface defining methods for receiving broadcasts of data or messages as they are received.
 *
 * @author Tenkiv (software@tenkiv.com)
 * @since v1.0.0.0
 */
public interface IMessageListener {

    /**
     * Called when an error message is received.
     *
     * @param tekdaqc {@link ATekdaqc} The board this message was sent from.
     * @param message {@link ABoardMessage} The received message.
     */
    void onErrorMessageReceived(ATekdaqc tekdaqc, ABoardMessage message);

    /**
     * Called when a status message is received.
     *
     * @param tekdaqc {@link ATekdaqc} The board this message was sent from.
     * @param message {@link ABoardMessage} The received message.
     */
    void onStatusMessageReceived(ATekdaqc tekdaqc, ABoardMessage message);

    /**
     * Called when an debug message is received.
     *
     * @param tekdaqc {@link ATekdaqc} The board this message was sent from.
     * @param message {@link ABoardMessage} The received message.
     */
    void onDebugMessageReceived(ATekdaqc tekdaqc, ABoardMessage message);

    /**
     * Called when a command data message is received.
     *
     * @param tekdaqc {@link ATekdaqc} The board this message was sent from.
     * @param message {@link ABoardMessage} The received message.
     */
    void onCommandDataMessageReceived(ATekdaqc tekdaqc, ABoardMessage message);

    /**
     * Called when an analog input data point is received.
     *
     * @param tekdaqc {@link ATekdaqc} The board this message was sent from.
     * @param data    {@link AnalogInputData} The received message.
     */
    void onAnalogInputDataReceived(ATekdaqc tekdaqc, AnalogInputData data);

    /**
     * Called when a digital input data point is received.
     *
     * @param tekdaqc {@link ATekdaqc} The board this message was sent from.
     * @param data    {@link DigitalInputData} The received message.
     */
    void onDigitalInputDataReceived(ATekdaqc tekdaqc, DigitalInputData data);

    /**
     * Called when a digital output data point is received.
     *
     * @param tekdaqc {@link ATekdaqc} The board this message was sent from.
     * @param data    {@link Arrays} The state of the digital outputs in the form of a boolean array corresponding to their indices.
     */
    void onDigitalOutputDataReceived(ATekdaqc tekdaqc, boolean[] data);
}

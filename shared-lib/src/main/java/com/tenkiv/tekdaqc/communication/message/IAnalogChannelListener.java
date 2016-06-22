package com.tenkiv.tekdaqc.communication.message;

import com.tenkiv.tekdaqc.communication.data_points.AnalogInputData;
import com.tenkiv.tekdaqc.hardware.ATekdaqc;

/**
 * Interface defining methods for receiving broadcasts of a single analog input channel.
 * Its should be noted adding a large number of single channel listeners can effect performance on slower computers.
 *
 * @author Tenkiv (software@tenkiv.com)
 * @since v2.0.0.0
 */
public interface IAnalogChannelListener {

    /**
     * Called when {@link AnalogInputData} is received for the given input channel.
     *
     * @param tekdaqc The {@link ATekdaqc} which has received the data.
     * @param data    The {@link AnalogInputData} which has been parsed.
     */
    void onAnalogDataReceived(ATekdaqc tekdaqc, AnalogInputData data);
}

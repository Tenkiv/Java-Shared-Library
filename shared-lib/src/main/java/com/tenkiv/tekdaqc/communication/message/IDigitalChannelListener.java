package com.tenkiv.tekdaqc.communication.message;

import com.tenkiv.tekdaqc.communication.data_points.DigitalInputData;
import com.tenkiv.tekdaqc.hardware.ATekdaqc;
import com.tenkiv.tekdaqc.hardware.DigitalInput;

/**
 * Interface defining methods for receiving broadcasts of a single digital input channel.
 * Its should be noted adding a large number of single channel listeners can effect performance on slower computers.
 *
 * @author Tenkiv (software@tenkiv.com)
 * @since v2.0.0.0
 */
public interface IDigitalChannelListener {

    /**
     * Called when {@link DigitalInputData} is received for the given input channel.
     *
     * @param input The {@link DigitalInput} which has received the data.
     * @param data    The {@link DigitalInputData} which has been parsed.
     */
    void onDigitalDataReceived(DigitalInput input, DigitalInputData data);
}

package com.tenkiv.tekdaqc.communication.message;

import com.tenkiv.tekdaqc.communication.data_points.ProtectedAnalogInputData;
import com.tenkiv.tekdaqc.hardware.AAnalogInput;
import com.tenkiv.tekdaqc.hardware.ATekdaqc;

/**
 * Interface defining methods for receiving broadcasts of a single analog input channel.
 * Its should be noted adding a large number of single channel listeners can effect performance on slower computers.
 *
 * @author Tenkiv (software@tenkiv.com)
 * @since v2.0.0.0
 */
public interface ICountListener {

    /**
     * Called when a count is received for the given input channel.
     *
     * @param input The {@link AAnalogInput} which has received the data.
     * @param count    The {@link int} of the count received by the ADC.
     */
    void onAnalogDataReceived(AAnalogInput input, int count);
}

package com.tenkiv.tekdaqc.communication.message

import com.tenkiv.tekdaqc.communication.data_points.PWMInputData
import com.tenkiv.tekdaqc.hardware.DigitalInput

/**
 * Interface which returns data bout the state of PWM digital input data.
 */
interface IPWMChannelListener {

    /**
     * Callback for received PWM input data.
     *
     * @param input The input which is receiving the data.
     * @param data The PWM data received.
     */
    fun onPWMDataReceived(input: DigitalInput, data: PWMInputData)
}
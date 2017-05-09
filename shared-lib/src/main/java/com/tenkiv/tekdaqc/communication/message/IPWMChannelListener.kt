package com.tenkiv.tekdaqc.communication.message

import com.tenkiv.tekdaqc.communication.data_points.DigitalInputData
import com.tenkiv.tekdaqc.communication.data_points.PWMInputData
import com.tenkiv.tekdaqc.hardware.DigitalInput

/**
 * Created by tenkiv on 5/5/17.
 */
interface IPWMChannelListener {
    fun onPWMDataReceived(input: DigitalInput, data: PWMInputData)
}
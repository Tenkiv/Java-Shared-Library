package com.tenkiv.tekdaqc.communication.message

import com.tenkiv.tekdaqc.hardware.ATekdaqc

/**
 * Created by tenkiv on 2/6/17.
 */
interface INetworkListener {
    fun onNetworkConditionDetected(tekdaqc: ATekdaqc, message: ABoardMessage)
}
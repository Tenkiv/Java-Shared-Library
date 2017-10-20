package com.tenkiv.tekdaqc.communication.message

import com.tenkiv.tekdaqc.hardware.ATekdaqc

/**
 * Class which gives callbacks for changes to network conditions
 */
interface INetworkListener {
    /**
     * Returns changes to the status of network conditions that may result in throttling of sampled data.
     *
     * @param tekdaqc The Tekdaqc whose network condition ahs changed.
     * @param message The message about the network condition.
     */
    fun onNetworkConditionDetected(tekdaqc: ATekdaqc, message: ABoardMessage)
}
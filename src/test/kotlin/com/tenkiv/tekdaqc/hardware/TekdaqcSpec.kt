package com.tenkiv.tekdaqc.hardware

import com.tenkiv.tekdaqc.locator.LocatorParams
import com.tenkiv.tekdaqc.locator.LocatorResponse
import com.tenkiv.tekdaqc.locator.getSimulatedLocatorResponse
import io.kotlintest.matchers.shouldEqual
import io.kotlintest.specs.ShouldSpec

class TekdaqcSpec: ShouldSpec({
    "TekdaqcSpec"{
        val tekdaqc = Tekdaqc_RevD(getSimulatedLocatorResponse())

        tekdaqc.analogInputs.values.forEach {
            it.apply {
                rate = AAnalogInput.Rate.SPS_100
                gain = AAnalogInput.Gain.X64
            }
        }

        should("Have new values"){
            tekdaqc.analogInputs.values.forEach {
                it.rate shouldEqual AAnalogInput.Rate.SPS_100
                it.gain shouldEqual AAnalogInput.Gain.X64
            }
        }
    }
})

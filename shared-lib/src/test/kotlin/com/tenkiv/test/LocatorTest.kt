package com.tenkiv.test

import com.tenkiv.tekdaqc.communication.data_points.DigitalInputData
import com.tenkiv.tekdaqc.communication.data_points.PWMInputData
import com.tenkiv.tekdaqc.communication.message.IDigitalChannelListener
import com.tenkiv.tekdaqc.communication.message.IPWMChannelListener
import com.tenkiv.tekdaqc.hardware.ATekdaqc
import com.tenkiv.tekdaqc.hardware.DigitalInput
import com.tenkiv.tekdaqc.locator.Locator
import com.tenkiv.tekdaqc.locator.OnTargetTekdaqcFound
import com.tenkiv.tekdaqc.locator.OnTekdaqcDiscovered
import io.kotlintest.specs.StringSpec

/**
 * Created by tenkiv on 5/5/17.
 */
class LocatorTest: StringSpec() {
    init{
        "Finding Tekdaqc"{

            var assert = false

            Locator.instance.addLocatorListener(object: OnTekdaqcDiscovered{
                override fun onTekdaqcResponse(board: ATekdaqc) {
                    println("Found ${board.serialNumber}")
                }

                override fun onTekdaqcFirstLocated(board: ATekdaqc) {
                }

                override fun onTekdaqcNoLongerLocated(board: ATekdaqc) {
                }

            })

            Locator.instance.searchForSpecificTekdaqcs(object: OnTargetTekdaqcFound{
                override fun onTargetFound(tekdaqc: ATekdaqc) {
                    println(tekdaqc.serialNumber)
                    tekdaqc.addDigitalChannelListener(IDigitalChannelListener { input, data -> assert = true }, tekdaqc.getDigitalInput(0))

                    tekdaqc.getDigitalInput(0).activate()

                    tekdaqc.sample(20)

                    println("Added n Sampling")
                }

                override fun onTargetFailure(serial: String, flag: OnTargetTekdaqcFound.FailureFlag) {
                }

                override fun onAllTargetsFound(tekdaqcs: Set<ATekdaqc>) {
                }

            },10000,true,ATekdaqc.AnalogScale.ANALOG_SCALE_5V,"00000000000000000000000000000012")

            Thread.sleep(20000)

            assert(assert)
        }
    }
}
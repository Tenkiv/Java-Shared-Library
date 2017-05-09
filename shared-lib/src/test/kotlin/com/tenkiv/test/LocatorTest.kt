package com.tenkiv.test

import com.tenkiv.tekdaqc.communication.data_points.PWMInputData
import com.tenkiv.tekdaqc.communication.message.IPWMChannelListener
import com.tenkiv.tekdaqc.hardware.ATekdaqc
import com.tenkiv.tekdaqc.hardware.DigitalInput
import com.tenkiv.tekdaqc.locator.Locator
import com.tenkiv.tekdaqc.locator.OnTargetTekdaqcFound
import io.kotlintest.specs.StringSpec

/**
 * Created by tenkiv on 5/5/17.
 */
class LocatorTest: StringSpec() {
    init{
        "Finding Tekdaqc Thing"{

            var assert = false

            Locator.instance.searchForSpecificTekdaqcs(object: OnTargetTekdaqcFound{
                override fun onTargetFound(tekdaqc: ATekdaqc) {
                    println(tekdaqc.serialNumber)
                    tekdaqc.addPWMChannelListener(object: IPWMChannelListener{
                        override fun onPWMDataReceived(input: DigitalInput, data: PWMInputData) {
                            println("PWM: ${data.percetageOn} ${data.totalTransitions}")
                            assert = true
                        }

                    }, tekdaqc.getDigitalInput(0))

                    tekdaqc.getDigitalInput(0).activatePWM()

                    tekdaqc.sample(20)

                    println("Added n Sampling")
                }

                override fun onTargetFailure(serial: String, flag: OnTargetTekdaqcFound.FailureFlag) {
                }

                override fun onAllTargetsFound(tekdaqcs: Set<ATekdaqc>) {
                }

            },10000,true,ATekdaqc.AnalogScale.ANALOG_SCALE_5V,"00000000000000000000000000000017")

            Thread.sleep(20000)

            assert(assert)
        }
    }
}
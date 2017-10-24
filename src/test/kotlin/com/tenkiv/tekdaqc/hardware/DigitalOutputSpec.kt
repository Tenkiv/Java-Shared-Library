package com.tenkiv.tekdaqc.hardware

import com.tenkiv.tekdaqc.locator.getSimulatedLocatorResponse
import com.tenkiv.tekdaqc.utility.ChannelType
import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldThrow
import io.kotlintest.specs.ShouldSpec

/**
 * Class to test Digital Output functionality
 */
class DigitalOutputSpec: ShouldSpec({
    "Digital Output Spec"{
        val digitalOutput = Tekdaqc_RevD(getSimulatedLocatorResponse()).getDigitalOutput(DIGITAL_OUTPUT_NUMBER)

        should("Have correctly initialized values"){
            digitalOutput.isActivated shouldBe false

            digitalOutput.name shouldBe null

            digitalOutput.mChannelNumber shouldBe DIGITAL_OUTPUT_NUMBER

            digitalOutput.channelType shouldBe ChannelType.DIGITAL_OUTPUT

            digitalOutput.pulseWidthModulationDutyCycle shouldBe PWM_OFF
        }

        should("Set values correctly"){
            digitalOutput.name = OUTPUT_NAME

            digitalOutput.name shouldBe OUTPUT_NAME
        }

        should("Throw Correct exceptions"){
            // Unconnected Tekdaqc's should throw IllegalStateException
            shouldThrow<IllegalStateException> {
                digitalOutput.activate()
            }

            // Unconnected Tekdaqc's should throw IllegalStateException
            shouldThrow<IllegalStateException> {
                digitalOutput.deactivate()
            }

            // Unconnected Tekdaqc's should throw IllegalStateException
            shouldThrow<IllegalStateException> {
                digitalOutput.queueStatusChange()
            }

            shouldThrow<IllegalStateException> {
                digitalOutput.setPulseWidthModulation(PWM_ZERO)
            }

            shouldThrow<IllegalArgumentException> {
                digitalOutput.setName(ILLEGAL_NAME)
            }

            shouldThrow<IllegalArgumentException> {
                digitalOutput.setPulseWidthModulation(ILLEGAL_PWM_HIGH)
            }

            shouldThrow<IllegalArgumentException> {
                digitalOutput.setPulseWidthModulation(ILLEGAL_PWM_LOW)
            }
        }

    }
}){
    companion object {
        const val DIGITAL_OUTPUT_NUMBER = 0
        const val OUTPUT_NAME = "Some Name"
        const val ILLEGAL_NAME = "ASDFGHJKL:LKJHGFDSASDFGHJKL:LKJHGFDSA"
        const val PWM_OFF = -1
        const val ILLEGAL_PWM_HIGH = 999
        const val ILLEGAL_PWM_LOW = -55
        const val PWM_ZERO = 0
    }
}

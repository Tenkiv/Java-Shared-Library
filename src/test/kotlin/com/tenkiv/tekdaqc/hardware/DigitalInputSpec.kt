package com.tenkiv.tekdaqc.hardware

import com.tenkiv.tekdaqc.locator.getSimulatedLocatorResponse
import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldThrow
import io.kotlintest.specs.ShouldSpec

/**
 * Class to test functionality of Digital Inputs
 */
class DigitalInputSpec: ShouldSpec({
    "Digital Input Spec"{
        val simulatedTekdaqc = Tekdaqc_RevD(getSimulatedLocatorResponse())
        val digitalInput = simulatedTekdaqc.getDigitalInput(INPUT_NUMBER)

        should("Set values"){

            digitalInput.channelNumber shouldBe INPUT_NUMBER

            digitalInput.name = INPUT_NAME

            shouldThrow<IllegalArgumentException>({
                digitalInput.setName(INVALID_STRING)
            })

            shouldThrow<IllegalStateException>({
                digitalInput.activate()
            })

            shouldThrow<IllegalStateException>({
                digitalInput.deactivate()
            })

            shouldThrow<IllegalStateException>({
                digitalInput.activatePWM()
            })

            shouldThrow<IllegalStateException>({
                digitalInput.deactivatePWM()
            })
        }
    }
}){
    companion object {
        private const val INPUT_NUMBER = 0
        private const val INPUT_NAME = "SOMENAME"
        private const val INVALID_STRING: String = "asdfghjklkjhgfdsfghjklkjhgfdsfghjklkjhgfdghjkljhgfhjkl"
    }
}
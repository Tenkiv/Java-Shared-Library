package com.tenkiv.tekdaqc.communication.ascii.message.parsing

import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.StringSpec

/**
 * Class to test DataMessage generation.
 */
class DataMessageSpec: StringSpec({
    "Analog Input Data Message Spec"{
        val message = getAnalogTestMessage()
        message.mNumber shouldBe 0
        message.mTimestamps shouldBe "967711311300"
        message.mReadings shouldBe -512
    }

    "Digital Input Data Message Spec"{
        val message = getDigitalTestMessage()
        message.mNumber shouldBe 0
        message.mTimestamps shouldBe "967711311300"
        message.mReadings shouldBe false
    }
})

/**
 * Function to get fake AnalogMessage
 *
 * @return A fake message
 */
fun getAnalogTestMessage(): ASCIIAnalogInputDataMessage = ASCIIAnalogInputDataMessage("?A0\r\n967711311300,-512")

/**
 * Function to get fake DigitalMessage
 *
 * @return A fake message
 */
fun getDigitalTestMessage(): ASCIIDigitalInputDataMessage = ASCIIDigitalInputDataMessage("?D0\r\n967711311300,L")
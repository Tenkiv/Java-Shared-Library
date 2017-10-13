package com.tenkiv.tekdaqc.communication.ascii.message.parsing

import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.StringSpec

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

fun getAnalogTestMessage(): ASCIIAnalogInputDataMessage = ASCIIAnalogInputDataMessage("?A0\r\n967711311300,-512")

fun getDigitalTestMessage(): ASCIIDigitalInputDataMessage = ASCIIDigitalInputDataMessage("?D0\r\n967711311300,L")
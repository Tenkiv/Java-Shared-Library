package com.tenkiv.tekdaqc.communication.ascii.message.parsing

import io.kotlintest.specs.StringSpec

class DataMessageSpec: StringSpec({
    "Analog Input Data Message Spec"{
        val testAnalogMessage = "?A0\r\n"+
                "967711311300,-512"

        val message = ASCIIAnalogInputDataMessage(testAnalogMessage)
        assert(message.mNumber == 0)
        assert(message.mTimestamps == "967711311300")
        assert(message.mReadings == -512)
    }

    "Digital Input Data Message Spec"{
        val testAnalogMessage = "?D0\r\n"+
                "967711311300,L"

        val message = ASCIIDigitalInputDataMessage(testAnalogMessage)
        assert(message.mNumber == 0)
        assert(message.mTimestamps == "967711311300")
        assert(!message.mReadings)
    }
})
package com.tenkiv.tekdaqc.communication.ascii.message.parsing

import com.tenkiv.tekdaqc.*
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.ShouldSpec

class ASCIIMessageUtilsSpec: ShouldSpec({
    "ASCII Message Utils Spec"{
        should("Parse correctly"){

            (ASCIIMessageUtils.parseMessage(TEST_ANALOG_INPUT_DATA) is ASCIIAnalogInputDataMessage) shouldBe true

            (ASCIIMessageUtils.parseMessage(TEST_DIGITAL_INPUT_DATA) is ASCIIDigitalInputDataMessage) shouldBe true

            (ASCIIMessageUtils.parseMessage(TEST_ERROR_MESSAGE_DATA) is ASCIIErrorMessage) shouldBe true

            (ASCIIMessageUtils.parseMessage(TEST_DEBUG_MESSAGE_DATA) is ASCIIDebugMessage) shouldBe true

            (ASCIIMessageUtils.parseMessage(TEST_COMMAND_MESSAGE_DATA) is ASCIICommandMessage) shouldBe true

            (ASCIIMessageUtils.parseMessage(TEST_STATUS_MESSAGE_DATA) is ASCIIStatusMessage) shouldBe true

            (ASCIIMessageUtils.parseMessage("TH1SisSOMEGIBBERISH")) shouldBe null

            (ASCIIMessageUtils.parseMessage(null)) shouldBe null
        }
    }
})
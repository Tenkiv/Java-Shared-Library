package com.tenkiv.tekdaqc.communication.ascii.message.parsing

import com.tenkiv.tekdaqc.serializeToAny
import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldNotBe
import io.kotlintest.matchers.shouldThrow
import io.kotlintest.specs.ShouldSpec

/**
 * Class to test DataMessage generation.
 */
class DataMessageSpec : ShouldSpec({

    "Analog Input Data Message Spec"{

        val message = getAnalogTestMessage()

        should("Generate values correctly") {
            message.mNumber shouldBe 0
            message.mTimestamps shouldBe "967711311300"
            message.mReadings shouldBe -512
        }

        should("Serialize correctly") {
            val obj = serializeToAny(message)
            (obj is ASCIIAnalogInputDataMessage) shouldBe true
            (obj as ASCIIAnalogInputDataMessage).mNumber shouldBe 0
            (obj).mTimestamps shouldBe "967711311300"
            (obj).mReadings shouldBe -512
        }

        should("Reset value") {
            message.reset()
            message.mName shouldBe null
            message.mNumber shouldBe 0
            message.mReadings shouldBe 0
            message.mTimestamps shouldBe null
        }

        should("Fail in parsing") {
            shouldThrow<IllegalArgumentException> { ASCIIAnalogInputDataMessage("RANDOMDATA") }
            shouldThrow<IllegalArgumentException> { ASCIIAnalogInputDataMessage("Analog Input ASDFSD") }
        }
    }

    "Digital Input Data Message Spec"{

        val message = getDigitalTestMessage()

        should("Generate values correctly") {
            message.mNumber shouldBe 0
            message.mTimestamps shouldBe "967711311300"
            message.mReadings shouldBe false
        }

        should("Serialize correctly") {
            val obj = serializeToAny(message)
            (obj is ASCIIDigitalInputDataMessage) shouldBe true
            (obj as ASCIIDigitalInputDataMessage).mNumber shouldBe 0
            (obj).mTimestamps shouldBe "967711311300"
            (obj).mReadings shouldBe false
        }

        should("Reset value") {
            message.reset()
            message.mName shouldBe null
            message.mNumber shouldBe 0
            message.mReadings shouldBe false
            message.mTimestamps shouldBe null
        }

        should("Fail in parsing") {
            shouldThrow<IllegalArgumentException> { ASCIIDigitalInputDataMessage("RANDOMDATA") }
            shouldThrow<IllegalArgumentException> { ASCIIDigitalInputDataMessage("Digital Input ASDFSD") }
        }
    }

    "Command Message Spec"{

        val message = getCommandTestMessage()

        should("Generate values correctly") {
            message.mMessageString shouldNotBe null
            message.type shouldBe ASCIIMessageUtils.MESSAGE_TYPE.COMMAND_DATA
        }

        should("Serialize correctly") {
            val obj = serializeToAny(message)
            (obj is ASCIICommandMessage) shouldBe true
            (obj as ASCIICommandMessage).mMessageString shouldNotBe null
        }

        should("Reset value") {
            message.reset()
            message.mMessageString shouldBe null
        }
    }

    "Debug Message Spec"{

        val message = getDebugTestMessage()

        should("Generate values correctly") {
            message.mMessageString shouldNotBe null
            message.type shouldBe ASCIIMessageUtils.MESSAGE_TYPE.DEBUG
        }

        should("Serialize correctly") {
            val obj = serializeToAny(message)
            (obj is ASCIIDebugMessage) shouldBe true
            (obj as ASCIIDebugMessage).mMessageString shouldNotBe null
        }

        should("Reset value") {
            message.reset()
            message.mMessageString shouldBe null
        }
    }

    "Error Message Spec"{

        val message = getErrorTestMessage()

        should("Generate values correctly") {
            message.mMessageString shouldNotBe null
            message.type shouldBe ASCIIMessageUtils.MESSAGE_TYPE.ERROR
        }

        should("Serialize correctly") {
            val obj = serializeToAny(message)
            (obj is ASCIIErrorMessage) shouldBe true
            (obj as ASCIIErrorMessage).mMessageString shouldNotBe null
        }

        should("Reset value") {
            message.reset()
            message.mMessageString shouldBe null
        }
    }

    "Status Message Spec"{

        val message = getStatusTestMessage()

        should("Generate values correctly") {
            message.mMessageString shouldNotBe null
        }

        should("Serialize correctly") {
            val obj = serializeToAny(message)
            (obj is ASCIIStatusMessage) shouldBe true
            (obj as ASCIIStatusMessage).mMessageString shouldNotBe null
        }

        should("Reset value") {
            message.reset()
            message.mMessageString shouldBe null
        }
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

/**
 * Function to get fake CommandMessage
 *
 * @return A fake message
 */
fun getCommandTestMessage(): ASCIICommandMessage = ASCIICommandMessage(
        "Command Data Message\r\nMessage: SomeMessage")

/**
 * Function to get fake DebugMessage
 *
 * @return A fake message
 */
fun getDebugTestMessage(): ASCIIDebugMessage = ASCIIDebugMessage(
        "Debug Message\r\nMessage: SomeMessage")

/**
 * Function to get fake ErrorMessage
 *
 * @return A fake message
 */
fun getErrorTestMessage(): ASCIIErrorMessage = ASCIIErrorMessage(
        "Error Message\r\nMessage: SomeMessage")

/**
 * Function to get fake StatusMessage
 *
 * @return A fake message
 */
fun getStatusTestMessage(): ASCIIStatusMessage = ASCIIStatusMessage(
        "Status Message\r\nMessage: SomeMessage")
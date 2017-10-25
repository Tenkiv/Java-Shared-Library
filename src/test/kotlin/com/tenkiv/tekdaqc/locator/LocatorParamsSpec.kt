package com.tenkiv.tekdaqc.locator

import com.tenkiv.tekdaqc.*
import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldEqual
import io.kotlintest.specs.ShouldSpec
import java.net.InetAddress

/**
 * Class to test locator param building.
 */
class LocatorParamsSpec: ShouldSpec({
    "Locator Params Spec"{
        val paramBuilder = LocatorParams.Builder()
        paramBuilder.setFirmware(FIRMWARE)
        paramBuilder.setIpAddress(InetAddress.getLocalHost().hostAddress)
        paramBuilder.setMessage(MSG)
        paramBuilder.setPort(PORT)
        paramBuilder.setSerial(SERIAL)
        paramBuilder.setTimeout(TIMEOUT)
        paramBuilder.setTitle(TITLE)
        paramBuilder.setType(TYPE)

        val locatorParams = paramBuilder.build()

        should("Equal locator values"){
            locatorParams.firmware shouldEqual FIRMWARE
            locatorParams.ipAddress shouldEqual InetAddress.getLocalHost().hostAddress
            locatorParams.message shouldEqual MSG
            locatorParams.port shouldEqual PORT
            locatorParams.serial shouldEqual SERIAL
            locatorParams.timeout shouldEqual TIMEOUT
            locatorParams.title shouldEqual TITLE
            locatorParams.type shouldEqual TYPE
        }

        should("Serialize correctly"){

            val obj = serializeToAny(locatorParams)

            (obj is LocatorParams) shouldBe true

            (obj as? LocatorParams)?.firmware shouldEqual FIRMWARE

        }
    }
})
package com.tenkiv.tekdaqc.locator

import io.kotlintest.matchers.shouldEqual
import io.kotlintest.specs.ShouldSpec

private const val FIRMWARE = "1.0.0.0"

private const val IPADDR = "127.0.0.1"

private const val MSG = "arbitrary"

private const val PORT = 8000

private const val SERIAL = "0000000000000016"

private const val TIMEOUT = 5000

private const val TITLE = "My Tekdaqc"

private const val TYPE = 'D'

/**
 * Class to test locator param building.
 */
class LocatorParamsSpec: ShouldSpec({
    "Locator Params Spec"{
        val paramBuilder = LocatorParams.Builder()
        paramBuilder.setFirmware(FIRMWARE)
        paramBuilder.setIpAddress(IPADDR)
        paramBuilder.setMessage(MSG)
        paramBuilder.setPort(PORT)
        paramBuilder.setSerial(SERIAL)
        paramBuilder.setTimeout(TIMEOUT)
        paramBuilder.setTitle(TITLE)
        paramBuilder.setType(TYPE)

        val locatorParams = paramBuilder.build()

        should("Equal Locator Values"){
            locatorParams.firmware shouldEqual FIRMWARE
            locatorParams.ipAddress shouldEqual IPADDR
            locatorParams.message shouldEqual MSG
            locatorParams.port shouldEqual PORT
            locatorParams.serial shouldEqual SERIAL
            locatorParams.timeout shouldEqual TIMEOUT
            locatorParams.title shouldEqual TITLE
            locatorParams.type shouldEqual TYPE
        }
    }
})
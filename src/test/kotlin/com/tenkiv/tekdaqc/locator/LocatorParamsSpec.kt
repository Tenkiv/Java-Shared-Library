package com.tenkiv.tekdaqc.locator

import io.kotlintest.matchers.shouldEqual
import io.kotlintest.specs.ShouldSpec

/**
 * Copyright 2017 TENKIV, INC.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this list of conditions and the
following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
disclaimer in the documentation and/or other materials provided with the distribution.

3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote
products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

private const val FIRMWARE = "1.0.0.0"
private const val IPADDR = "127.0.0.1"
private const val MSG = "arbitrary"
private const val PORT = 8000
private const val SERIAL = "0000000000000016"
private const val TIMEOUT = 5000
private const val TITLE = "My Tekdaqc"
private const val TYPE = 'D'

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
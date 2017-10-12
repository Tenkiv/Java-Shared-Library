package com.tenkiv.tekdaqc.locator

import com.tenkiv.tekdaqc.communication.message.IDigitalChannelListener
import com.tenkiv.tekdaqc.hardware.ATekdaqc
import io.kotlintest.Duration
import io.kotlintest.specs.ShouldSpec
import io.kotlintest.eventually
import java.util.concurrent.TimeUnit

class LocatorSpec: ShouldSpec({
    "Locator Spec"{
        should("Find a tekdaqc on network"){
            eventually(Duration(5,TimeUnit.SECONDS)){
                Locator.instance.addLocatorListener(object : OnTekdaqcDiscovered {
                    override fun onTekdaqcResponse(board: ATekdaqc) {
                        println("Found ${board.serialNumber}")
                        assert(true)
                    }

                    override fun onTekdaqcFirstLocated(board: ATekdaqc) {
                    }

                    override fun onTekdaqcNoLongerLocated(board: ATekdaqc) {
                    }

                })
            }
        }.config(enabled = false)

        Locator.instance.searchForTekdaqcs()
    }
})
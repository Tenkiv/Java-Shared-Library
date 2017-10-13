package com.tenkiv.tekdaqc.locator

import com.tenkiv.tekdaqc.hardware.ATekdaqc
import io.kotlintest.specs.ShouldSpec
import java.lang.Thread.sleep
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import kotlin.concurrent.thread

@Volatile
var onResponseCalled = false
@Volatile
var onFirstLocatedCalled = false
@Volatile
var onNotLocatedCalled = false

class LocatorSpec : ShouldSpec({

    "Locator Spec" {
        thread(start = true) {
            Locator.instance.addLocatorListener(object : OnTekdaqcDiscovered {
                override fun onTekdaqcResponse(board: ATekdaqc) {
                    onResponseCalled = true
                    println("Found ${board.serialNumber}")
                }

                override fun onTekdaqcFirstLocated(board: ATekdaqc) {
                    onFirstLocatedCalled = true
                }

                override fun onTekdaqcNoLongerLocated(board: ATekdaqc) {
                    println("Lost ${board.serialNumber}")
                    onNotLocatedCalled = true
                }

            })
        }

        Locator.instance.searchForTekdaqcs()

        val clientSocket = DatagramSocket()
        clientSocket.broadcast = true

        clientSocket.send(DatagramPacket(
                spoofedLocatorResponse,
                spoofedLocatorResponse.size, InetAddress.getLoopbackAddress(), 9800))

        sleep(5000)

        assert(onResponseCalled && onFirstLocatedCalled && onNotLocatedCalled)
    }
})
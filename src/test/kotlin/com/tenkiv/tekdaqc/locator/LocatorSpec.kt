package com.tenkiv.tekdaqc.locator

import com.tenkiv.tekdaqc.hardware.ATekdaqc
import io.kotlintest.specs.ShouldSpec
import java.lang.Thread.sleep
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.NetworkInterface
import kotlin.concurrent.thread

@Volatile
var onResponseCalled = false

@Volatile
var onFirstLocatedCalled = false

@Volatile
var onNotLocatedCalled = false

/**
 * Class to test Locator class.
 */
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

        Locator.instance.enableLoopbackBroadcast = true

        val clientSocket = DatagramSocket()
        clientSocket.broadcast = true

        clientSocket.send(DatagramPacket(
                spoofedLocatorResponse,
                spoofedLocatorResponse.size, InetAddress.getByName("127.255.255.255"), 9800))

        val interfaces = NetworkInterface.getNetworkInterfaces()
        while (interfaces.hasMoreElements()) {
            val iAddrs = interfaces.nextElement().interfaceAddresses
            iAddrs.forEach { addr ->
                println("iA:${iAddrs} \naddrB:${addr.broadcast}")
                if (addr.broadcast != null) {
                    clientSocket.send(DatagramPacket(
                            spoofedLocatorResponse,
                            spoofedLocatorResponse.size, addr.broadcast, 9800))
                }
            }
        }

        sleep(5000)

        assert(onResponseCalled && onFirstLocatedCalled && onNotLocatedCalled)
    }
})
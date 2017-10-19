package com.tenkiv.tekdaqc.locator

import com.tenkiv.tekdaqc.hardware.ATekdaqc
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.ShouldSpec
import java.lang.Thread.sleep
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.*
import kotlin.concurrent.thread
import kotlin.concurrent.timerTask

@Volatile
private var onResponseCalled = false

@Volatile
private var onFirstLocatedCalled = false

@Volatile
private var onNotLocatedCalled = false

/**
 * Class to test Locator class.
 */
class LocatorSpec : ShouldSpec({

    "Basic Locator Spec" {
        thread(start = true) {

            Locator.instance.addLocatorListener(object : OnTekdaqcDiscovered {
                override fun onTekdaqcResponse(board: ATekdaqc) {
                    onResponseCalled = true
                }

                override fun onTekdaqcFirstLocated(board: ATekdaqc) {
                    onFirstLocatedCalled = true
                }

                override fun onTekdaqcNoLongerLocated(board: ATekdaqc) {
                    onNotLocatedCalled = true
                }

            })
        }

        Locator.instance.searchForTekdaqcsForDuration(3000)

        Locator.instance.enableLoopbackBroadcast = true

        sendFakeTekdaqcUPD()

        sleep(3500)

        should("have called all locator responses"){
            onResponseCalled shouldBe true

            onFirstLocatedCalled shouldBe true

            onNotLocatedCalled shouldBe true
        }
    }

    "Search for Specific"{

        var specificWasLocated = false

        Locator.instance.searchForSpecificTekdaqcs(object: OnTargetTekdaqcFound{
            override fun onTargetFound(tekdaqc: ATekdaqc) {
                specificWasLocated = true
            }

            override fun onTargetFailure(serial: String, flag: OnTargetTekdaqcFound.FailureFlag) {
            }

            override fun onAllTargetsFound(tekdaqcs: Set<ATekdaqc>) {
            }
        },5000,serials = "00000000000000000000000000000012")

        sendFakeTekdaqcUPD()

        sleep(3000)

        should("have found specified tekdaqc"){
            specificWasLocated shouldBe true
        }
    }

    "Blocking Search for Specific"{

        val timer = Timer()

        var count = 5

        timer.schedule(timerTask {
            if(count > 0){
                sendFakeTekdaqcUPD()
                count--
            }else{
                this.cancel()
            }
        },1000,1000)

        val tekdaqcs = Locator.instance.blockingSearchForSpecificTekdaqcs(
                timeoutMillis = 5000,serials = "00000000000000000000000000000012")

        should("not be empty"){
            tekdaqcs.isNotEmpty() shouldBe true
        }
    }
})

private fun sendFakeTekdaqcUPD(){
    val clientSocket = DatagramSocket()
    clientSocket.broadcast = true

    clientSocket.send(DatagramPacket(
            spoofedLocatorResponse,
            spoofedLocatorResponse.size, InetAddress.getByName("127.255.255.255"), 9800))

    val interfaces = NetworkInterface.getNetworkInterfaces()
    while (interfaces.hasMoreElements()) {
        val iAddrs = interfaces.nextElement().interfaceAddresses
        iAddrs.forEach { addr ->
            if (addr.broadcast != null) {
                clientSocket.send(DatagramPacket(
                        spoofedLocatorResponse,
                        spoofedLocatorResponse.size, addr.broadcast, 9800))
            }
        }
    }
}
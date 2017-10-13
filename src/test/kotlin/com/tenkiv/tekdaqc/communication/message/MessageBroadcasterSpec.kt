package com.tenkiv.tekdaqc.communication.message

import com.tenkiv.tekdaqc.communication.ascii.message.parsing.getAnalogTestMessage
import com.tenkiv.tekdaqc.communication.ascii.message.parsing.getDigitalTestMessage
import com.tenkiv.tekdaqc.communication.data_points.AnalogInputCountData
import com.tenkiv.tekdaqc.communication.data_points.DigitalInputData
import com.tenkiv.tekdaqc.hardware.ATekdaqc
import com.tenkiv.tekdaqc.hardware.Tekdaqc_RevD
import com.tenkiv.tekdaqc.locator.getSimulatedLocatorResponse
import io.kotlintest.eventually
import io.kotlintest.seconds
import io.kotlintest.specs.ShouldSpec

class MessageBroadcasterSpec: ShouldSpec({
    "Message Broadcaster Spec"{
        val simulatedTekdaqc = Tekdaqc_RevD(getSimulatedLocatorResponse())
        val messageBroadcaster = simulatedTekdaqc.messageBroadcaster

        should("Receive Analog Count Data"){
            eventually(2.seconds){
                messageBroadcaster.addAnalogChannelListener(
                        simulatedTekdaqc,
                        simulatedTekdaqc.analogInputs[0]!!,
                        ICountListener { input, count ->
                            assert(true)
                        })
            }
        }

        should("Receive Voltage Count Data"){
            eventually(2.seconds){
                messageBroadcaster.addAnalogVoltageListener(
                        simulatedTekdaqc,
                        simulatedTekdaqc.analogInputs[0]!!,
                        IVoltageListener { input, value ->
                            assert(true)
                        })
            }
        }

        should("Receive Digital Data"){
            eventually(2.seconds){
                messageBroadcaster.addDigitalChannelListener(
                        simulatedTekdaqc,
                        simulatedTekdaqc.digitalInputs[0]!!,
                        IDigitalChannelListener { input, data ->
                            assert(true)
                        })
            }
        }

        should("Receive Any Data"){
            eventually(2.seconds){
                messageBroadcaster.addMessageListener(
                        simulatedTekdaqc,
                        object: IMessageListener{
                            override fun onStatusMessageReceived(tekdaqc: ATekdaqc?, message: ABoardMessage?) {
                            }

                            override fun onDebugMessageReceived(tekdaqc: ATekdaqc?, message: ABoardMessage?) {
                            }

                            override fun onCommandDataMessageReceived(tekdaqc: ATekdaqc?, message: ABoardMessage?) {
                            }

                            override fun onAnalogInputDataReceived(tekdaqc: ATekdaqc?, data: AnalogInputCountData?) {
                                assert(true)
                            }

                            override fun onDigitalInputDataReceived(tekdaqc: ATekdaqc?, data: DigitalInputData?) {
                                assert(true)
                            }

                            override fun onDigitalOutputDataReceived(tekdaqc: ATekdaqc?, data: BooleanArray?) {
                            }

                            override fun onErrorMessageReceived(tekdaqc: ATekdaqc?, message: ABoardMessage?) {
                            }
                        })
            }
        }

        simulatedTekdaqc.onParsingComplete(getAnalogTestMessage())

        simulatedTekdaqc.onParsingComplete(getDigitalTestMessage())
    }
})
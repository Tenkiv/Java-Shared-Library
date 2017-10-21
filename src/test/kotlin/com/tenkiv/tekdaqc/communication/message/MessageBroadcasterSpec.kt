package com.tenkiv.tekdaqc.communication.message

import com.tenkiv.tekdaqc.TEST_ERROR_MESSAGE_DATA
import com.tenkiv.tekdaqc.communication.ascii.message.parsing.ASCIIErrorMessage
import com.tenkiv.tekdaqc.communication.data_points.AnalogInputCountData
import com.tenkiv.tekdaqc.communication.data_points.DigitalInputData
import com.tenkiv.tekdaqc.communication.data_points.PWMInputData
import com.tenkiv.tekdaqc.hardware.ATekdaqc
import com.tenkiv.tekdaqc.hardware.DigitalInput
import com.tenkiv.tekdaqc.hardware.Tekdaqc_RevD
import com.tenkiv.tekdaqc.locator.getSimulatedLocatorResponse
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.ShouldSpec
import java.lang.Thread.sleep

/**
 * Class to test sending commands through MessageBroadcaster
 */
class MessageBroadcasterSpec : ShouldSpec({
    "Message Broadcaster Spec"{
        val simulatedTekdaqc = Tekdaqc_RevD(getSimulatedLocatorResponse())
        val messageBroadcaster = simulatedTekdaqc.messageBroadcaster

        var analogCountTrigger = false
        val analogCountListener = ICountListener { _, _ ->
            analogCountTrigger = true
        }

        var analogVoltageTrigger = false
        val analogVolatgeListener = IVoltageListener { _, _ ->
            analogVoltageTrigger = true
        }

        var digitalTrigger = false
        val digitalListener = IDigitalChannelListener { _, _ ->
            digitalTrigger = true
        }

        var digitalPwmTrigger = false
        val digitalPwmListener = object : IPWMChannelListener {
            override fun onPWMDataReceived(input: DigitalInput, data: PWMInputData) {
                digitalPwmTrigger = true
            }
        }

        var networkTrigger = false
        val networkListener = object : INetworkListener {
            override fun onNetworkConditionDetected(tekdaqc: ATekdaqc, message: ABoardMessage) {
                networkTrigger = true
            }
        }

        var fullMessageAnalogTrigger = false
        var fullMessageDigitalTrigger = false
        val fullListener = object : IMessageListener {
            override fun onErrorMessageReceived(tekdaqc: ATekdaqc?, message: ABoardMessage?) {
            }

            override fun onStatusMessageReceived(tekdaqc: ATekdaqc?, message: ABoardMessage?) {
            }

            override fun onDebugMessageReceived(tekdaqc: ATekdaqc?, message: ABoardMessage?) {
            }

            override fun onCommandDataMessageReceived(tekdaqc: ATekdaqc?, message: ABoardMessage?) {
            }

            override fun onAnalogInputDataReceived(tekdaqc: ATekdaqc?, data: AnalogInputCountData?) {
                fullMessageAnalogTrigger = true
            }

            override fun onDigitalInputDataReceived(tekdaqc: ATekdaqc?, data: DigitalInputData?) {
                fullMessageDigitalTrigger = true
            }

            override fun onDigitalOutputDataReceived(tekdaqc: ATekdaqc?, data: BooleanArray?) {
            }
        }

        messageBroadcaster.addMessageListener(
                simulatedTekdaqc,
                fullListener
        )

        messageBroadcaster.addNetworkListener(
                simulatedTekdaqc,
                networkListener
        )

        messageBroadcaster.addAnalogChannelListener(
                simulatedTekdaqc,
                simulatedTekdaqc.getAnalogInput(0),
                analogCountListener)

        messageBroadcaster.addAnalogVoltageListener(
                simulatedTekdaqc,
                simulatedTekdaqc.getAnalogInput(0),
                analogVolatgeListener)

        messageBroadcaster.addDigitalChannelListener(
                simulatedTekdaqc,
                simulatedTekdaqc.getDigitalInput(0),
                digitalListener)

        messageBroadcaster.addPWMChannelListener(simulatedTekdaqc,
                simulatedTekdaqc.getDigitalInput(0),
                digitalPwmListener)

        should("Receive updates") {

            messageBroadcaster.broadcastAnalogInputDataPoint(
                    simulatedTekdaqc,
                    AnalogInputCountData(0, "", 1000L, 1000))
            messageBroadcaster.broadcastDigitalInputDataPoint(
                    simulatedTekdaqc,
                    DigitalInputData(0, "", 1000L, false))
            messageBroadcaster.broadcastPWMInputDataPoint(
                    simulatedTekdaqc,
                    PWMInputData(0, "", 1000L, 0.0, 100)
            )
            messageBroadcaster.broadcastNetworkError(simulatedTekdaqc,ASCIIErrorMessage(TEST_ERROR_MESSAGE_DATA))
            messageBroadcaster.broadcastMessage(simulatedTekdaqc,ASCIIErrorMessage(TEST_ERROR_MESSAGE_DATA))

            sleep(2000)

            fullMessageAnalogTrigger shouldBe true

            fullMessageDigitalTrigger shouldBe true

            analogCountTrigger shouldBe true

            analogVoltageTrigger shouldBe true

            digitalTrigger shouldBe true

            digitalPwmTrigger shouldBe true

            networkTrigger shouldBe true

        }

        should("Remove listeners") {
            messageBroadcaster.removeListener(
                    simulatedTekdaqc,
                    fullListener)
            messageBroadcaster.removeNetworkListener(
                    simulatedTekdaqc,
                    networkListener)
            messageBroadcaster.removeAnalogCountListener(
                    simulatedTekdaqc,
                    simulatedTekdaqc.getAnalogInput(0),
                    analogCountListener)
            messageBroadcaster.removeAnalogVoltageListener(
                    simulatedTekdaqc,
                    simulatedTekdaqc.getAnalogInput(0),
                    analogVolatgeListener)
            messageBroadcaster.removeDigitalChannelListener(
                    simulatedTekdaqc,
                    simulatedTekdaqc.getDigitalInput(0),
                    digitalListener)
            messageBroadcaster.removePWMChannelListener(simulatedTekdaqc,
                    simulatedTekdaqc.getDigitalInput(0),
                    digitalPwmListener)
        }
    }
})
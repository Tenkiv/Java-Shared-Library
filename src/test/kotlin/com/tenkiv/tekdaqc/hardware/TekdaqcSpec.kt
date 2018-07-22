package com.tenkiv.tekdaqc.hardware

import com.tenkiv.tekdaqc.*
import com.tenkiv.tekdaqc.communication.data_points.AnalogInputCountData
import com.tenkiv.tekdaqc.locator.getSimulatedLocatorResponse
import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldEqual
import io.kotlintest.matchers.shouldNotBe
import io.kotlintest.specs.ShouldSpec

/**
 * Class to test Tekdaqc functions
 */
class TekdaqcSpec: ShouldSpec({
    "Tekdaqc Spec"{
        val tekdaqc = Tekdaqc_RevD(getSimulatedLocatorResponse())

        should("Temperature reference check"){
            tekdaqc.temperatureReference shouldNotBe null
        }

        should("Have basic properties"){

            tekdaqc.analogInputs.size shouldEqual 32
            tekdaqc.digitalInputs.size shouldEqual 24
            tekdaqc.digitalOutputs.size shouldEqual 16

            tekdaqc.analogScale = ATekdaqc.AnalogScale.ANALOG_SCALE_400V
            tekdaqc.analogScale shouldBe ATekdaqc.AnalogScale.ANALOG_SCALE_400V

            tekdaqc.firmwareVersion shouldBe FIRMWARE
            tekdaqc.hostIP shouldBe IPADDR
            tekdaqc.macAddress shouldBe MACADDR
            tekdaqc.title shouldBe TITLE
            tekdaqc.serialNumber shouldBe SERIAL
        }

        //TODO This needs a more in depth Simulated Tekdaqc
        /*should("Add values to a queue"){

            tekdaqc.apply {
                //Command queue iterates by two. Once for the command and then the completion callback.
                commandQueue.numberQueued shouldBe 0

                readAnalogInput(0,10)
                commandQueue.numberQueued shouldBe 2

                readAnalogInputRange(0,10,10)
                commandQueue.numberQueued shouldBe 4

                readAnalogInputSet(setOf(0,1,2),10)
                commandQueue.numberQueued shouldBe 6

                readAllAnalogInput(10)
                commandQueue.numberQueued shouldBe 8

                readDigitalInput(0,10)
                commandQueue.numberQueued shouldBe 10

                readDigitalInputRange(0,10,10)
                commandQueue.numberQueued shouldBe 12

                readDigitalInputSet(setOf(0,1,2),10)
                commandQueue.numberQueued shouldBe 14

                tekdaqc.readAllDigitalInput(10)
                commandQueue.numberQueued shouldBe 16

                addAnalogInput(tekdaqc.getAnalogInput(0))
                commandQueue.numberQueued shouldBe 18

                setDigitalOutput("0000000000000000")
                commandQueue.numberQueued shouldBe 20

                setDigitalOutputByHex("0000")
                commandQueue.numberQueued shouldBe 22

                setDigitalOutput(BooleanArray(16,{false}))
                commandQueue.numberQueued shouldBe 24

                removeAnalogInput(tekdaqc.getAnalogInput(0))
                commandQueue.numberQueued shouldBe 26

                addDigitalInput(tekdaqc.getDigitalInput(0))
                commandQueue.numberQueued shouldBe 28

                systemGainCalibrate(0)
                commandQueue.numberQueued shouldBe 30

                readSystemGainCalibration()
                commandQueue.numberQueued shouldBe 32

                readSelfGainCalibration(AAnalogInput.Gain.X1,
                        AAnalogInput.Rate.SPS_5,
                        AnalogInput_RevD.BufferState.ENABLED)
                commandQueue.numberQueued shouldBe 34

                upgrade()
                commandQueue.numberQueued shouldBe 36

                identify()
                commandQueue.numberQueued shouldBe 38

                sample(0)
                commandQueue.numberQueued shouldBe 40

                halt()
                commandQueue.numberQueued shouldBe 42

                setRTC(1000)
                commandQueue.numberQueued shouldBe 44

                readADCRegisters()
                commandQueue.numberQueued shouldBe 46

                getCalibrationStatus()
                commandQueue.numberQueued shouldBe 48

                writeCalibrationTemperature(0.0,0)
                commandQueue.numberQueued shouldBe 50

                writeGainCalibrationValue(0f,
                        AAnalogInput.Gain.X1,
                        AAnalogInput.Rate.SPS_5,
                        AnalogInput_RevD.BufferState.ENABLED,
                        ATekdaqc.AnalogScale.ANALOG_SCALE_5V,
                        0)
                commandQueue.numberQueued shouldBe 52

                writeCalibrationValid()
                commandQueue.numberQueued shouldBe 54

                getAnalogInput(0)

                convertAnalogInputDataToVoltage(
                        AnalogInputCountData(0,null,1000,1500),
                        ATekdaqc.AnalogScale.ANALOG_SCALE_5V)

                convertAnalogInputDataToTemperature(AnalogInputCountData(36,null,1000,1500))
            }
        }*/
    }
})

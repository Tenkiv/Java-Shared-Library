package com.tenkiv.tekdaqc.hardware

import com.tenkiv.tekdaqc.communication.data_points.AnalogInputCountData
import com.tenkiv.tekdaqc.locator.getSimulatedLocatorResponse
import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldEqual
import io.kotlintest.matchers.shouldNot
import io.kotlintest.matchers.shouldNotBe
import io.kotlintest.specs.ShouldSpec

class TekdaqcSpec: ShouldSpec({
    "TekdaqcSpec"{
        val tekdaqc = Tekdaqc_RevD(getSimulatedLocatorResponse())

        should("Have certain numbers of i/o"){
            tekdaqc.analogInputs.size shouldEqual 32
            tekdaqc.digitalInputs.size shouldEqual 24
            tekdaqc.digitalOutputs.size shouldEqual 16
        }

        should("Temperature reference check"){
            tekdaqc.temperatureReference shouldNotBe null
        }

        should("Add values to a queue"){

            tekdaqc.apply {
                //Command queue iterates by two. Once for the command and then the completion callback.
                mCommandQueue.numberQueued shouldBe 0
                should("some"){
                    true shouldBe false
                }

                readAnalogInput(0,10)
                mCommandQueue.numberQueued shouldBe 2

                readAnalogInputRange(0,10,10)
                mCommandQueue.numberQueued shouldBe 4

                readAnalogInputSet(setOf(0,1,2),10)
                mCommandQueue.numberQueued shouldBe 6

                readAllAnalogInput(10)
                mCommandQueue.numberQueued shouldBe 8

                readDigitalInput(0,10)
                mCommandQueue.numberQueued shouldBe 10

                readDigitalInputRange(0,10,10)
                mCommandQueue.numberQueued shouldBe 12

                readDigitalInputSet(setOf(0,1,2),10)
                mCommandQueue.numberQueued shouldBe 14

                tekdaqc.readAllDigitalInput(10)
                mCommandQueue.numberQueued shouldBe 16

                addAnalogInput(tekdaqc.analogInputs[0]!!)
                mCommandQueue.numberQueued shouldBe 18

                setDigitalOutput("0000000000000000")
                mCommandQueue.numberQueued shouldBe 20

                setDigitalOutputByHex("0000")
                mCommandQueue.numberQueued shouldBe 22

                setDigitalOutput(BooleanArray(16,{false}))
                mCommandQueue.numberQueued shouldBe 24

                removeAnalogInput(tekdaqc.analogInputs[0]!!)
                mCommandQueue.numberQueued shouldBe 26

                addDigitalInput(tekdaqc.digitalInputs[0]!!)
                mCommandQueue.numberQueued shouldBe 28

                systemGainCalibrate(0)
                mCommandQueue.numberQueued shouldBe 30

                readSystemGainCalibration()
                mCommandQueue.numberQueued shouldBe 32

                readSelfGainCalibration(AAnalogInput.Gain.X1,
                        AAnalogInput.Rate.SPS_5,
                        AnalogInput_RevD.BufferState.ENABLED)
                mCommandQueue.numberQueued shouldBe 34

                upgrade()
                mCommandQueue.numberQueued shouldBe 36

                identify()
                mCommandQueue.numberQueued shouldBe 38

                sample(0)
                mCommandQueue.numberQueued shouldBe 40

                halt()
                mCommandQueue.numberQueued shouldBe 42

                setRTC(1000)
                mCommandQueue.numberQueued shouldBe 44

                readADCRegisters()
                mCommandQueue.numberQueued shouldBe 46

                getCalibrationStatus()
                mCommandQueue.numberQueued shouldBe 48

                writeCalibrationTemperature(0.0,0)
                mCommandQueue.numberQueued shouldBe 50

                writeGainCalibrationValue(0f,
                        AAnalogInput.Gain.X1,
                        AAnalogInput.Rate.SPS_5,
                        AnalogInput_RevD.BufferState.ENABLED,
                        ATekdaqc.AnalogScale.ANALOG_SCALE_5V,
                        0)
                mCommandQueue.numberQueued shouldBe 52

                writeCalibrationValid()
                mCommandQueue.numberQueued shouldBe 54

                getAnalogInput(0)

                convertAnalogInputDataToVoltage(
                        AnalogInputCountData(0,null,1000,1500),
                        ATekdaqc.AnalogScale.ANALOG_SCALE_5V)

                convertAnalogInputDataToTemperature(AnalogInputCountData(36,null,1000,1500))
            }
        }
    }
})

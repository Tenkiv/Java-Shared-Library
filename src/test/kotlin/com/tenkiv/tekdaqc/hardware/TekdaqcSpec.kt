package com.tenkiv.tekdaqc.hardware

import com.tenkiv.tekdaqc.FIRMWARE
import com.tenkiv.tekdaqc.MACADDR
import com.tenkiv.tekdaqc.SERIAL
import com.tenkiv.tekdaqc.TITLE
import com.tenkiv.tekdaqc.communication.data_points.AnalogInputCountData
import com.tenkiv.tekdaqc.locator.getSimulatedLocatorResponse
import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldEqual
import io.kotlintest.matchers.shouldNotBe
import io.kotlintest.specs.ShouldSpec
import tec.uom.se.quantity.Quantities
import tec.uom.se.unit.Units
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Thread.sleep
import java.net.InetAddress
import java.net.ServerSocket
import java.net.SocketException
import kotlin.concurrent.thread

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

            tekdaqc.analogInputs.size shouldEqual Tekdaqc_RevD.ANALOG_INPUT_COUNT
            tekdaqc.digitalInputs.size shouldEqual Tekdaqc_RevD.DIGITAL_INPUT_COUNT
            tekdaqc.digitalOutputs.size shouldEqual Tekdaqc_RevD.DIGITAL_OUTPUT_COUNT

            tekdaqc.analogScale = ATekdaqc.AnalogScale.ANALOG_SCALE_400V
            tekdaqc.analogScale shouldBe ATekdaqc.AnalogScale.ANALOG_SCALE_400V

            tekdaqc.firmwareVersion shouldBe FIRMWARE
            tekdaqc.hostIP shouldBe InetAddress.getLocalHost().hostAddress
            tekdaqc.macAddress shouldBe MACADDR
            tekdaqc.title shouldBe TITLE
            tekdaqc.serialNumber shouldBe SERIAL
        }

        should("Connect to spoofed Tekdaqc"){

            val commandMap = mutableMapOf<String,Boolean>(
                    Pair("SET_ANALOG_INPUT_SCALE --SCALE=ANALOG_SCALE_5V",false),
                    Pair("ADD_ANALOG_INPUT --INPUT=0 --GAIN=1 --RATE=2.5 --BUFFER=ENABLED",false),
                    Pair("ADD_DIGITAL_INPUT --INPUT=0",false),
                    Pair("SET_DIGITAL_OUTPUT --OUTPUT=8000",false),
                    Pair("SET_DIGITAL_OUTPUT --OUTPUT=0000",false),
                    Pair("REMOVE_DIGITAL_INPUT --INPUT=0",false),
                    Pair("REMOVE_ANALOG_INPUT --INPUT=0",false),
                    Pair("SET_DIGITAL_OUTPUT --OUTPUT=0000",false),
                    Pair("SET_DIGITAL_OUTPUT --OUTPUT=0000 --DUTYCYCLE=100",false),
                    Pair("SET_DIGITAL_OUTPUT --OUTPUT=0000 --DUTYCYCLE=0",false),
                    Pair("DISCONNECT",false)
            )


            thread {
                val socket = ServerSocket(9801).accept()

                Thread.sleep(2000)

                val r = BufferedReader(InputStreamReader(socket.getInputStream()))

                while (true) {
                    try {
                        socket.getOutputStream().write(("--------------------\n" +
                                "Status Message\n" +
                                "\tMessage: SUCCESS - COMMAND: OK\n" +
                                "--------------------${0x1E.toChar()}").toByteArray())
                        socket.getOutputStream().flush()

                        val command = r.readLine()

                        if(command != null){
                            commandMap.put(command,true)
                        }

                        sleep(100)
                    }catch (e: SocketException){
                        break
                    }
                }
            }

            tekdaqc.connect(ATekdaqc.AnalogScale.ANALOG_SCALE_5V,ATekdaqc.CONNECTION_METHOD.ETHERNET)

            tekdaqc.activateAnalogInput(HW_NUMBER)

            tekdaqc.activateDigitalInput(HW_NUMBER)

            tekdaqc.toggleDigitalOutput(HW_NUMBER,true)

            tekdaqc.setDigitalOutputByHex("0000")

            tekdaqc.deactivateDigitalInput(HW_NUMBER)

            tekdaqc.deactivateAnalogInput(HW_NUMBER)

            tekdaqc.toggleDigitalOutput(HW_NUMBER,false)

            tekdaqc.setPulseWidthModulation(HW_NUMBER,100)

            tekdaqc.setPulseWidthModulation(HW_NUMBER,Quantities.getQuantity(0, Units.PERCENT))

            tekdaqc.disconnectCleanly()

            commandMap.filterValues { !it }.isEmpty() shouldBe true

        }

        should("Add values to a queue"){

            tekdaqc.apply {
                var commandQueueCount = 0

                //Command queue iterates by two. Once for the command and then the completion callback.
                commandQueue.numberQueued shouldBe commandQueueCount
                commandQueueCount += INCREMENT_NUMBER

                readAnalogInput(HW_NUMBER,SAMPLE_NUMBER)
                commandQueue.numberQueued shouldBe commandQueueCount
                commandQueueCount += INCREMENT_NUMBER

                readAnalogInputRange(HW_NUMBER,HW_END,SAMPLE_NUMBER)
                commandQueue.numberQueued shouldBe commandQueueCount
                commandQueueCount += INCREMENT_NUMBER

                readAnalogInputSet(setOf(0,1,2),SAMPLE_NUMBER)
                commandQueue.numberQueued shouldBe commandQueueCount
                commandQueueCount += INCREMENT_NUMBER

                readAllAnalogInput(SAMPLE_NUMBER)
                commandQueue.numberQueued shouldBe commandQueueCount
                commandQueueCount += INCREMENT_NUMBER

                readDigitalInput(HW_NUMBER,SAMPLE_NUMBER)
                commandQueue.numberQueued shouldBe commandQueueCount
                commandQueueCount += INCREMENT_NUMBER

                readDigitalInputRange(HW_NUMBER,HW_END,SAMPLE_NUMBER)
                commandQueue.numberQueued shouldBe commandQueueCount
                commandQueueCount += INCREMENT_NUMBER

                readDigitalInputSet(setOf(0,1,2),SAMPLE_NUMBER)
                commandQueue.numberQueued shouldBe commandQueueCount
                commandQueueCount += INCREMENT_NUMBER

                tekdaqc.readAllDigitalInput(SAMPLE_NUMBER)
                commandQueue.numberQueued shouldBe commandQueueCount
                commandQueueCount += INCREMENT_NUMBER

                setDigitalOutput("0000000000000000")
                commandQueue.numberQueued shouldBe commandQueueCount
                commandQueueCount += INCREMENT_NUMBER

                setDigitalOutputByHex("0000")
                commandQueue.numberQueued shouldBe commandQueueCount
                commandQueueCount += INCREMENT_NUMBER

                setDigitalOutput(BooleanArray(Tekdaqc_RevD.DIGITAL_OUTPUT_COUNT,{false}))
                commandQueue.numberQueued shouldBe commandQueueCount
                commandQueueCount += INCREMENT_NUMBER

                systemGainCalibrate(HW_NUMBER)
                commandQueue.numberQueued shouldBe commandQueueCount
                commandQueueCount += INCREMENT_NUMBER

                readSystemGainCalibration()
                commandQueue.numberQueued shouldBe commandQueueCount
                commandQueueCount += INCREMENT_NUMBER

                readSelfGainCalibration(AAnalogInput.Gain.X1,
                        AAnalogInput.Rate.SPS_5,
                        AnalogInput_RevD.BufferState.ENABLED)
                commandQueue.numberQueued shouldBe commandQueueCount
                commandQueueCount += INCREMENT_NUMBER

                upgrade()
                commandQueue.numberQueued shouldBe commandQueueCount
                commandQueueCount += INCREMENT_NUMBER

                identify()
                commandQueue.numberQueued shouldBe commandQueueCount
                commandQueueCount += INCREMENT_NUMBER

                sample(HW_NUMBER)
                commandQueue.numberQueued shouldBe commandQueueCount
                commandQueueCount += INCREMENT_NUMBER

                halt()
                commandQueue.numberQueued shouldBe commandQueueCount
                commandQueueCount += INCREMENT_NUMBER

                setRTC(1000)
                commandQueue.numberQueued shouldBe commandQueueCount
                commandQueueCount += INCREMENT_NUMBER

                readADCRegisters()
                commandQueue.numberQueued shouldBe commandQueueCount
                commandQueueCount += INCREMENT_NUMBER

                getCalibrationStatus()
                commandQueue.numberQueued shouldBe commandQueueCount
                commandQueueCount += INCREMENT_NUMBER

                writeCalibrationTemperature(0.0,HW_NUMBER)
                commandQueue.numberQueued shouldBe commandQueueCount
                commandQueueCount += INCREMENT_NUMBER

                writeGainCalibrationValue(0f,
                        AAnalogInput.Gain.X1,
                        AAnalogInput.Rate.SPS_5,
                        AnalogInput_RevD.BufferState.ENABLED,
                        ATekdaqc.AnalogScale.ANALOG_SCALE_5V,
                        HW_NUMBER)
                commandQueue.numberQueued shouldBe commandQueueCount
                commandQueueCount += INCREMENT_NUMBER

                writeCalibrationValid()
                commandQueue.numberQueued shouldBe commandQueueCount
                commandQueueCount += INCREMENT_NUMBER

                getAnalogInput(HW_NUMBER)

                convertAnalogInputDataToVoltage(
                        AnalogInputCountData(HW_NUMBER,null,TIME_STAMP,TEST_DATA),
                        ATekdaqc.AnalogScale.ANALOG_SCALE_5V)

                convertAnalogInputDataToTemperature(
                        AnalogInputCountData(
                                Tekdaqc_RevD.ANALOG_INPUT_TEMP_SENSOR,
                                null,TIME_STAMP,TEST_DATA))
            }
        }
    }
}){
    companion object {
        const val INCREMENT_NUMBER = 2
        const val HW_NUMBER = 0
        const val HW_END = 10
        const val TIME_STAMP = 1000L
        const val TEST_DATA = 1500
        const val SAMPLE_NUMBER = 10
    }
}

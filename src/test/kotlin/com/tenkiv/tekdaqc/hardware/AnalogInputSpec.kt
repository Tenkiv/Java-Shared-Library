package com.tenkiv.tekdaqc.hardware

import com.tenkiv.tekdaqc.TEST_ANALOG_INPUT_DATA
import com.tenkiv.tekdaqc.communication.ascii.message.parsing.ASCIIMessageUtils
import com.tenkiv.tekdaqc.communication.data_points.AnalogInputCountData
import com.tenkiv.tekdaqc.communication.message.ICountListener
import com.tenkiv.tekdaqc.communication.message.IVoltageListener
import com.tenkiv.tekdaqc.locator.getSimulatedLocatorResponse
import com.tenkiv.tekdaqc.hardware.AAnalogInput.SensorCurrent
import com.tenkiv.tekdaqc.hardware.AAnalogInput.Rate
import com.tenkiv.tekdaqc.hardware.AAnalogInput.Gain
import com.tenkiv.tekdaqc.hardware.AnalogInput_RevD.BufferState
import com.tenkiv.tekdaqc.serializeToAny
import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldThrow
import io.kotlintest.specs.ShouldSpec
import java.lang.Thread.sleep

/**
 * Class to test analog input class
 */
class AnalogInputSpec: ShouldSpec({
    "Analog Input Spec"{
        val simulatedTekdaqc = Tekdaqc_RevD(getSimulatedLocatorResponse())
        val analogInput = simulatedTekdaqc.getAnalogInput(INPUT_NUMBER) as? AnalogInput_RevD
                ?: throw IllegalArgumentException()

        should("Set values"){
            analogInput.rate = AAnalogInput.Rate.SPS_5

            analogInput.gain = AAnalogInput.Gain.X1

            analogInput.bufferState = BufferState.ENABLED

            analogInput.sensorCurrent = AAnalogInput.SensorCurrent._10uA

            analogInput.channelNumber shouldBe INPUT_NUMBER

            analogInput.name = INPUT_NAME

            shouldThrow<IllegalArgumentException>({
                analogInput.setName(INVALID_STRING)
            })

            shouldThrow<IllegalStateException>({
                analogInput.activate()
            })

            shouldThrow<IllegalStateException>({
                analogInput.deactivate()
            })
        }

        should("Calculate gain given voltage"){
            simulatedTekdaqc.analogScale = ATekdaqc.AnalogScale.ANALOG_SCALE_5V

            analogInput.setGainByMaxVoltage(VALID_5V_VOLTAGE)

            shouldThrow<IllegalArgumentException>({
                analogInput.setGainByMaxVoltage(INVALID_5V_VOLTAGE)
            })

            simulatedTekdaqc.analogScale = ATekdaqc.AnalogScale.ANALOG_SCALE_400V

            analogInput.setGainByMaxVoltage(VALID_400V_VOLTAGE)

            shouldThrow<IllegalArgumentException>({
                analogInput.setGainByMaxVoltage(INVALID_400V_VOLTAGE)
            })
        }

        should("Test gain"){
            Gain.fromInt(INT_OF_GAIN) shouldBe Gain.X1

            Gain.fromInt(INVALID_INT_OF_GAIN) shouldBe null

            Gain.fromString(STRING_OF_GAIN) shouldBe Gain.X1

            Gain.fromString(INVALID_STRING) shouldBe null

            Gain.valueOf(VALUE_STRING_OF_GAIN) shouldBe Gain.X1

            Gain.getValueFromOrdinal(ORDINAL_FIRST) shouldBe  Gain.X1
        }

        should("Test rate"){
            Rate.fromString(STRING_OF_RATE) shouldBe Rate.SPS_5

            Rate.getValueFromOrdinal(ORDINAL_FIRST) shouldBe  Rate.SPS_30000

            Rate.SPS_5.toString() shouldBe STRING_OF_RATE

            Rate.fromString(INVALID_STRING) shouldBe null
        }

        should("Test current"){
            SensorCurrent.valueOf(SENSOR_CURRENT_NAME) shouldBe SensorCurrent._10uA

            SensorCurrent.getValueFromOrdinal(ORDINAL_FIRST) shouldBe SensorCurrent._10uA

            SensorCurrent._10uA.toString() shouldBe SENSOR_CURRENT_STRING_VALUE
        }

        should("Test buffer"){
            BufferState.valueOf(ENABLED) shouldBe BufferState.ENABLED
            BufferState.fromString(ENABLED) shouldBe BufferState.ENABLED

            BufferState.getValueFromOrdinal(ORDINAL_FIRST) shouldBe BufferState.ENABLED
        }

        should("Add and remove listeners"){
            val analogMessage = ASCIIMessageUtils.parseMessage(TEST_ANALOG_INPUT_DATA)

            var countReceived = false
            var voltageReceived = false

            val countListener = ICountListener { _, _ -> countReceived = true }
            val voltageListener = IVoltageListener { _, _ -> voltageReceived = true }

            analogInput.addCountListener(countListener)
            analogInput.addVoltageListener(voltageListener)

            simulatedTekdaqc.onParsingComplete(analogMessage)

            countReceived shouldBe true

            voltageReceived shouldBe true

            analogInput.removeCountListener(countListener)
            analogInput.removeVoltageListener(voltageListener)
        }
    }
}){
    companion object {
        private const val INPUT_NUMBER = 0
        private const val INPUT_NAME = "SOMENAME"
        private const val INVALID_STRING: String = "asdfghjklkjhgfdsfghjklkjhgfdsfghjklkjhgfdghjkljhgfhjkl"
        private const val VALID_5V_VOLTAGE = 0.1
        private const val INVALID_5V_VOLTAGE = 4000.0
        private const val VALID_400V_VOLTAGE = 30.1
        private const val INVALID_400V_VOLTAGE = 4000.1

        private const val INT_OF_GAIN = 1
        private const val INVALID_INT_OF_GAIN = 92
        private const val ORDINAL_FIRST: Byte = 0

        private const val VALUE_STRING_OF_GAIN = "X1"
        private const val SENSOR_CURRENT_NAME = "_10uA"
        private const val STRING_OF_GAIN = "1"
        private const val STRING_OF_RATE = "5"
        private const val SENSOR_CURRENT_STRING_VALUE = "10"

        private const val ENABLED = "ENABLED"
    }
}
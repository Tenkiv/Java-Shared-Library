package com.tenkiv.tekdaqc.hardware

import com.tenkiv.tekdaqc.locator.getSimulatedLocatorResponse
import com.tenkiv.tekdaqc.serializeToAny
import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldThrow
import io.kotlintest.specs.ShouldSpec

/**
 * Class to test analog input class
 */
class AnalogInputSpec: ShouldSpec({
    "Analog Input Spec"{
        val simulatedTekdaqc = Tekdaqc_RevD(getSimulatedLocatorResponse())
        val analogInput = simulatedTekdaqc.getAnalogInput(INPUT_NUMBER)

        should("Set values"){
            analogInput.rate = AAnalogInput.Rate.SPS_5

            analogInput.gain = AAnalogInput.Gain.X1

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
            AAnalogInput.Gain.fromInt(INT_OF_GAIN) shouldBe AAnalogInput.Gain.X1

            AAnalogInput.Gain.fromInt(INVALID_INT_OF_GAIN) shouldBe null

            AAnalogInput.Gain.fromString(STRING_OF_GAIN) shouldBe AAnalogInput.Gain.X1

            AAnalogInput.Gain.fromString(INVALID_STRING) shouldBe null

            AAnalogInput.Gain.valueOf(VALUE_STRING_OF_GAIN) shouldBe AAnalogInput.Gain.X1

            AAnalogInput.Gain.getValueFromOrdinal(ORDINAL_FIRST) shouldBe  AAnalogInput.Gain.X1
        }

        should("Test rate"){
            AAnalogInput.Rate.fromString(STRING_OF_RATE) shouldBe AAnalogInput.Rate.SPS_5

            AAnalogInput.Rate.getValueFromOrdinal(ORDINAL_FIRST) shouldBe  AAnalogInput.Rate.SPS_30000

            AAnalogInput.Rate.SPS_5.toString() shouldBe STRING_OF_RATE

            AAnalogInput.Rate.fromString(INVALID_STRING) shouldBe null
        }

        should("Test current"){
            AAnalogInput.SensorCurrent.valueOf(SENSOR_CURRENT_NAME) shouldBe AAnalogInput.SensorCurrent._10uA

            AAnalogInput.SensorCurrent.getValueFromOrdinal(ORDINAL_FIRST) shouldBe AAnalogInput.SensorCurrent._10uA

            AAnalogInput.SensorCurrent._10uA.toString() shouldBe SENSOR_CURRENT_STRING_VALUE
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
    }
}
package com.tenkiv.tekdaqc.hardware

import com.tenkiv.tekdaqc.utility.DigitalOutputUtilities
import io.kotlintest.matchers.shouldEqual
import io.kotlintest.specs.ShouldSpec

/**
 * Class to test building commands
 */
class CommandBuilderSpec : ShouldSpec({
    "Generating Values"{

        val WRITE_GAIN_CALIBRATION_VALUE = "WRITE_GAIN_CALIBRATION_VALUE " +
                "--TEMPERATURE=0 --VALUE=0.0 --BUFFER=ENABLED --RATE=5 --GAIN=32 --SCALE=ANALOG_SCALE_5V\r"
        val SET_ANALOG_INPUT_SCALE = "SET_ANALOG_INPUT_SCALE --SCALE=ANALOG_SCALE_5V\r"
        val ADD_ANALOG_INPUT = "ADD_ANALOG_INPUT --INPUT=0 --GAIN=1 --RATE=2.5 --BUFFER=ENABLED\r"
        val analogInput = AnalogInput_RevD(null, 0)

        val DIGITAL_OUTPUT_ON_BIN_STRING = "1111111111111111"
        val SET_DIGITAL_OUTPUT_ON = "SET_DIGITAL_OUTPUT --OUTPUT=ffff\r"

        should("Generate gain calibration value"){
            String(CommandBuilder.
                    writeGainCalibrationValue(
                            0f,
                            AAnalogInput.Gain.X32,
                            AAnalogInput.Rate.SPS_5,
                            AnalogInput_RevD.BufferState.ENABLED,
                            ATekdaqc.AnalogScale.ANALOG_SCALE_5V,
                            0)
                    .generateCommandBytes()).shouldEqual(WRITE_GAIN_CALIBRATION_VALUE)
        }

        should("Generate set analog scale value") {
            String(CommandBuilder.setAnalogInputScale(ATekdaqc.AnalogScale.ANALOG_SCALE_5V)
                    .generateCommandBytes()).shouldEqual(SET_ANALOG_INPUT_SCALE)
        }

        should("Generate add analog input value") {
            String(CommandBuilder.addAnalogInput(analogInput)
                    .generateCommandBytes()).shouldEqual(ADD_ANALOG_INPUT)
        }

        should("Set digital output"){
            String(CommandBuilder.setDigitalOutputByHex(
                    DigitalOutputUtilities.hexConversion(
                            DIGITAL_OUTPUT_ON_BIN_STRING))
                    .generateCommandBytes()).shouldEqual(SET_DIGITAL_OUTPUT_ON)
        }
    }
})



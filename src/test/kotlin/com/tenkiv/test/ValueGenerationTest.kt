package com.tenkiv.test

import com.tenkiv.tekdaqc.hardware.ATekdaqc
import com.tenkiv.tekdaqc.hardware.CommandBuilder
import io.kotlintest.specs.StringSpec

/**
 * Created by tenkiv on 4/29/17.
 */

class ValueGenerationTest : StringSpec() {

    val SET_ANALOG_INPUT_SCALE = "SET_ANALOG_INPUT_SCALE --SCALE=ANALOG_SCALE_5V\r"
    val ADD_ANALOG_INPUT = "ADD_ANALOG_INPUT --INPUT=0 --RATE=5 --GAIN=2 --BUFFER=ON\r"

    init {
        "Generating Values"{

            val aiScale = CommandBuilder.setAnalogInputScale(ATekdaqc.AnalogScale.ANALOG_SCALE_5V)
            String(aiScale.generateCommandBytes()).shouldEqual(SET_ANALOG_INPUT_SCALE)

        }
    }
}



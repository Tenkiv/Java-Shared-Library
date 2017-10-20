package com.tenkiv.tekdaqc.hardware

/**
 * Analog input specifically handling the board's internal temperature reference.
 * Needed for calculating voltage correctly.
 */
class TemperatureReference_RevD(tekdaqc: ATekdaqc): AnalogInput_RevD(tekdaqc, Tekdaqc_RevD.ANALOG_INPUT_TEMP_SENSOR) {
    override fun getGain(): Gain = Gain.X4
    override fun getRate(): Rate = Rate.SPS_3750
    override fun getBufferState(): BufferState = BufferState.ENABLED

    override fun setGain(gain: Gain?): AAnalogInput {
        System.err.println("Cannot Set Gain of Temperature Reference")
        return this
    }

    override fun setRate(rate: Rate?): AAnalogInput {
        System.err.println("Cannot Set Rate of Temperature Reference")
        return this
    }

    override fun setBufferState(state: BufferState?): AnalogInput_RevD {
        System.err.println("Cannot Set Buffer of Temperature Reference")
        return this
    }
}
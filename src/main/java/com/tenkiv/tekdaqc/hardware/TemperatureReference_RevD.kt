package com.tenkiv.tekdaqc.hardware

class TemperatureReference_RevD(tekdaqc: ATekdaqc): AnalogInput_RevD(tekdaqc, 36) {
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
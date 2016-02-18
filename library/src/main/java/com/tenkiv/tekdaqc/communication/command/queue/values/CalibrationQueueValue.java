package com.tenkiv.tekdaqc.communication.command.queue.values;

import com.tenkiv.tekdaqc.communication.command.queue.Commands;
import com.tenkiv.tekdaqc.communication.command.queue.Params;
import com.tenkiv.tekdaqc.communication.command.queue.QueueUtil;
import com.tenkiv.tekdaqc.hardware.AAnalogInput;
import com.tenkiv.tekdaqc.hardware.ATekdaqc;
import com.tenkiv.tekdaqc.hardware.AnalogInput_RevD;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Specialized queue value used for calibrating Tekdaqcs.
 *
 * WARNING!: Calibration should only be done by those with accurate voltage references and temperature controlled environments. Calibration tables
 * cannot be recovered once reset and improper or imprecise calibration can result in inaccurate analog readings or no value readings. Do not attempt
 * a calibration without the prerequisite knowledge or equipment. For questions or detailed explanation on how to use the library to preform a
 * calibration contact (software@tenkiv.com).
 *
 * @author Tenkiv (software@tenkiv.com)
 * @since v2.0.0.0
 */
public class CalibrationQueueValue extends ABaseQueueVal {

    /**
     * The temperature index for the calibration.
     */
    private float mCalTemp;

    /**
     * The value of the calibration.
     */
    private int mCalValue;

    /**
     * The buffer state of the value in {@link Byte} ordinal.
     */
    private byte mBufferVal;

    /**
     * The rate of the value in {@link Byte} ordinal.
     */
    private byte mRateVal;

    /**
     * The gain of the value in {@link Byte} ordinal.
     */
    private byte mGainVal;

    /**
     * The scale of the value in {@link Byte} ordinal.
     */
    private byte mScaleVal;

    /**
     * Constructor for {@link CalibrationQueueValue}.
     *
     * WARNING!: Do not attempt a calibration without the prerequisite knowledge or equipment.
     * For questions or detailed explanation on how to use the library to preform a calibration contact (software@tenkiv.com).
     *
     * @param command   Command type in {@link Byte} ordinal.
     * @param calTemp   Calibration temperature as a {@link Double}.
     * @param value     Value of the correction as an {@link Integer}.
     * @param bufferVal Buffer state of the value in {@link Byte} ordinal.
     * @param rateVal   Rate of the value in {@link Byte} ordinal.
     * @param gainVal   Gain of the value in {@link Byte} ordinal.
     * @param scaleVal  Scale of the value in {@link Byte} ordinal.
     */
    public CalibrationQueueValue(final byte command
            , final float calTemp
            , final int value
            , final byte bufferVal
            , final byte rateVal
            , final byte gainVal
            , final byte scaleVal) {
        super(command);
        mCalTemp = calTemp;
        mCalValue = value;
        mBufferVal = bufferVal;
        mRateVal = rateVal;
        mGainVal = gainVal;
        mScaleVal = scaleVal;
    }


    @Override
    public byte[] generateCommandBytes() {
        return (Commands.getValueFromOrdinal(mCommandType).name() +
                QueueUtil.GENERAL_DELIMETER +
                QueueUtil.PARAMETER_FLAG +
                Params.VALUE.name() +
                QueueUtil.KEY_VALUE_SEPARATOR +
                mCalValue +

                QueueUtil.GENERAL_DELIMETER +
                QueueUtil.PARAMETER_FLAG +
                Params.BUFFER.name() +
                QueueUtil.KEY_VALUE_SEPARATOR +
                AnalogInput_RevD.BufferState.getValueFromOrdinal(mBufferVal) +

                QueueUtil.GENERAL_DELIMETER +
                QueueUtil.PARAMETER_FLAG +
                Params.GAIN.name() +
                QueueUtil.KEY_VALUE_SEPARATOR +
                AAnalogInput.Gain.getValueFromOrdinal(mGainVal) +

                QueueUtil.GENERAL_DELIMETER +
                QueueUtil.PARAMETER_FLAG +
                Params.SCALE.name() +
                QueueUtil.KEY_VALUE_SEPARATOR +
                ATekdaqc.AnalogScale.getValueFromOrdinal(mScaleVal) +

                QueueUtil.GENERAL_DELIMETER +
                QueueUtil.PARAMETER_FLAG +
                Params.TEMPERATURE.name() +
                QueueUtil.KEY_VALUE_SEPARATOR +
                mCalTemp +
                QueueUtil.COMMAND_EOF)
                .getBytes();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeFloat(mCalTemp);
        out.writeInt(mCalValue);
        out.writeByte(mBufferVal);
        out.writeByte(mRateVal);
        out.writeByte(mGainVal);
        out.writeByte(mScaleVal);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        mCalTemp = in.readFloat();
        mCalValue = in.readInt();
        mBufferVal = in.readByte();
        mRateVal = in.readByte();
        mGainVal = in.readByte();
        mScaleVal = in.readByte();
    }
}

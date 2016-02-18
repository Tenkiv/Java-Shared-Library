package com.tenkiv.tekdaqc.communication.command.queue.values;

import com.tenkiv.tekdaqc.communication.command.queue.Commands;
import com.tenkiv.tekdaqc.communication.command.queue.Params;
import com.tenkiv.tekdaqc.communication.command.queue.QueueUtil;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Implementation of {@link ParamQueueValue} which contains a {@link String} to be serialized with the existing {@link Params}.
 * Class used for commands in the queue such as {@link Commands#SET_USER_MAC}, {@link Commands#SET_RTC}, etc.
 *
 * @author Tenkiv (software@tenkiv.com)
 * @since v2.0.0.0
 */
public class StringQueueValue extends ParamQueueValue {

    /**
     * {@link String} to be serialized and sent as {@link Params} value.
     */
    private String mStringVal;

    /**
     * {@link Byte} representing the ordinal position of the {@link Commands} for the {@link String} value.
     */
    private byte mStringParam;

    /**
     * Constructor for {@link StringQueueValue} which takes the {@link Commands} as an ordinal {@link Byte}.
     *
     * @param command     {@link Byte} as an ordinal position of {@link Commands}.
     * @param stringParam {@link Byte} as an ordinal position of {@link Params}.
     * @param stringVal   {@link String} to be serialized as {@link Params} value.
     */
    public StringQueueValue(final byte command, final byte stringParam, final String stringVal) {
        super(command);
        mStringParam = stringParam;
        mStringVal = stringVal;
    }

    /**
     * Constructor for {@link StringQueueValue} which takes {@link Commands}.
     *
     * @param command     {@link Commands} of the command to be sent over Telnet.
     * @param stringParam {@link Params} of the {@link String} value to be sent over Telnet.
     * @param stringVal   {@link String} to be serialized as {@link Params} value.
     */
    public StringQueueValue(final Commands command, final Params stringParam, final String stringVal) {
        super(command.getOrdinalCommandType());
        mStringParam = stringParam.getOrdinalCommandType();
        mStringVal = stringVal;
    }

    /**
     * Empty constructor used for serialization.
     */
    public StringQueueValue() {
        super();
    }

    @Override
    public byte[] generateCommandBytes() {
        return (Commands.getValueFromOrdinal(mCommandType).name() +
                QueueUtil.GENERAL_DELIMETER +
                generateParamsArrayToString() +
                QueueUtil.PARAMETER_FLAG +
                Params.getValueFromOrdinal(mStringParam).name() +
                QueueUtil.KEY_VALUE_SEPARATOR +
                mStringVal +
                QueueUtil.COMMAND_EOF)
                .getBytes();
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        super.writeExternal(out);

        out.writeByte(mStringParam);
        out.writeObject(mStringVal);
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);

        mStringParam = in.readByte();
        mStringVal = (String) in.readObject();
    }
}

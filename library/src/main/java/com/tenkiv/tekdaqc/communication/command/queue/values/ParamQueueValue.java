package com.tenkiv.tekdaqc.communication.command.queue.values;


import com.tenkiv.tekdaqc.communication.command.queue.Commands;
import com.tenkiv.tekdaqc.communication.command.queue.Params;
import com.tenkiv.tekdaqc.communication.command.queue.QueueUtil;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Concrete implementation of {@link ABaseQueueVal} which contains ordinal {@link Params} as well as the
 * ordinal representation of their respective values.
 * Class used for commands in the queue such as {@link Commands#ADD_ANALOG_INPUT}, {@link Commands#SAMPLE}, etc.
 *
 * @author Tenkiv (software@tenkiv.com)
 * @since v2.0.0.0
 */
public class ParamQueueValue extends ABaseQueueVal {

    /**
     * Size of the X-Dimen of the {@link Params} array.
     * Array[0][0-yDimen] contain ordinal {@link Params}.
     * Array[1][0-yDimen] contain ordinal values for the {@link Params}.
     */
    private static final byte xDimen = 2;

    /**
     * Size of the Y-Dimen of the {@link Params} array.
     * Array[0][0-yDimen] contain ordinal {@link Params}.
     * Array[1][0-yDimen] contain ordinal values for the {@link Params}.
     */
    private static final byte yDimen = 4;

    /**
     * Array containing the ordinal values of the {@link Params} as well as the ordinal representation of their values.
     * Array[0][0-yDimen] contain ordinal {@link Params}.
     * Array[1][0-yDimen] contain ordinal values for the {@link Params}.
     */
    private byte[][] mParamArray = new byte[xDimen][yDimen];

    /**
     * Class constructor for building a {@link ParamQueueValue} with a command specified as an ordinal {@link Byte} from the {@link Commands}.
     *
     * @param command Command as an ordinal {@link Byte}.
     */
    public ParamQueueValue(final byte command) {
        super(command);
        initializeParams();
    }

    /**
     * Class constructor for building a {@link ParamQueueValue} with a command specified as {@link Commands}.
     *
     * @param command Command as {@link Commands}.
     */
    public ParamQueueValue(final Commands command) {
        this(command.getOrdinalCommandType());
    }

    /**
     * Empty class constructor without specified {@link Commands}. Default Command is {@link Commands#NONE}.
     */
    public ParamQueueValue() {
        super();
        initializeParams();

    }

    /**
     * Initializes {@link ParamQueueValue#mParamArray}.
     */
    private void initializeParams() {
        for (int x = 0; x < xDimen; x++) {
            for (int y = 0; y < yDimen; y++) {
                mParamArray[x][y] = -1;
            }
        }
    }

    /**
     * Finds first open slot in {@link ParamQueueValue#mParamArray} to store ordinal reference for {@link Params} and its value.
     *
     * @param param {@link Params} to be added to {@link ParamQueueValue#mParamArray}.
     * @param value Ordinal position of the value of {@link Params} to be added to {@link ParamQueueValue#mParamArray}.
     */
    public void addParamValue(final Params param, final byte value) {
        int i = -1;
        while (true) {
            i++;
            if (i == yDimen) {
                break;
            } else if (mParamArray[0][i] == -1) {
                mParamArray[0][i] = param.getOrdinalCommandType();
                mParamArray[1][i] = value;
                break;
            }
        }
    }

    /**
     * Method to generate an ASCII {@link String} from the ordinal values in {@link ParamQueueValue#mParamArray}.
     *
     * @return An ASCII {@link String} of the params to be sent over Telnet.
     */
    protected String generateParamsArrayToString() {
        StringBuilder paramString = new StringBuilder();
        for (int i = 0; i < yDimen; i++) {
            if (mParamArray[0][i] != -1) {
                Params param = Params.getValueFromOrdinal(mParamArray[0][i]);
                paramString.append(QueueUtil.PARAMETER_FLAG);
                paramString.append(param.name());
                paramString.append(QueueUtil.KEY_VALUE_SEPARATOR);
                paramString.append(Params.getParamValStringFromOrdinals(param, mParamArray[1][i]));
                paramString.append(QueueUtil.GENERAL_DELIMETER);
            }
        }
        if (!paramString.toString().isEmpty()) {
            //Delete last Delimiter.
            paramString.deleteCharAt(paramString.length() - 1);
        }
        return paramString.toString();
    }

    @Override
    public byte[] generateCommandBytes() {

        return (Commands.getValueFromOrdinal(mCommandType).name() +
                QueueUtil.GENERAL_DELIMETER +
                generateParamsArrayToString() +
                QueueUtil.COMMAND_EOF)
                .getBytes();
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);

        for (int x = 0; x < xDimen; x++) {
            for (int y = 0; y < yDimen; y++) {
                mParamArray[x][y] = in.readByte();
            }
        }

    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        super.writeExternal(out);

        for (int x = 0; x < xDimen; x++) {
            for (int y = 0; y < yDimen; y++) {
                out.write(mParamArray[x][y]);
            }
        }
    }

}

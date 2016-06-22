package com.tenkiv.tekdaqc.communication.command.queue.values;

import com.tenkiv.tekdaqc.communication.command.queue.Commands;
import com.tenkiv.tekdaqc.communication.command.queue.IQueueObject;
import com.tenkiv.tekdaqc.communication.command.queue.Params;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;

/**
 * Abstract class denoting the basic values for a command in the queue.
 *
 * @author Tenkiv (software@tenkiv.com)
 * @since v2.0.0.0
 */
public abstract class ABaseQueueVal implements IQueueObject {

    /**
     * {@link Commands} to be executed in ordinal {@link Byte} representation.
     */
    protected byte mCommandType;

    /**
     * Constructor specifying {@link Commands} which {@link ABaseQueueVal} represents.
     *
     * @param command
     */
    protected ABaseQueueVal(final byte command) {
        mCommandType = command;
    }

    /**
     * Empty constructor for {@link ABaseQueueVal}. Defaults to NONE {@link Commands}. Required for serialization.
     */
    public ABaseQueueVal() {
        mCommandType = (byte) Commands.NONE.ordinal();
    }

    /**
     * Call to generate the {@link Arrays} of {@link Byte} which {@link Commands} as well as it's {@link Params}.
     *
     * @return {@link Arrays} of {@link Byte} to be sent over Telnet.
     */
    public abstract byte[] generateCommandBytes();

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        out.writeByte(mCommandType);
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        mCommandType = in.readByte();
    }

}

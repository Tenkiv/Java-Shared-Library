package com.tenkiv.tekdaqc.communication.command.queue.values;

import com.tenkiv.tekdaqc.communication.command.queue.Commands;
import com.tenkiv.tekdaqc.communication.command.queue.QueueUtil;

/**
 * Concrete implementation of {@link ABaseQueueVal} which contains no additional parameters or values beyond type.
 * Class used for commands in the queue such as {@link Commands#HALT}, {@link Commands#NONE}, etc.
 *
 * @author Tenkiv (software@tenkiv.com)
 * @since v2.0.0.0
 */
public class BlankQueueValue extends ABaseQueueVal {

    /**
     * Class constructor for building a {@link BlankQueueValue} with a command specified as an ordinal {@link Byte} from the {@link Commands}.
     *
     * @param command Command as an ordinal {@link Byte}.
     */
    public BlankQueueValue(final byte command) {
        super(command);
    }

    /**
     * Class constructor for building a {@link BlankQueueValue} with a command specified as {@link Commands}.
     *
     * @param command Command as {@link Commands}.
     */
    public BlankQueueValue(final Commands command) {
        super(command.getOrdinalCommandType());
    }

    /**
     * Empty class constructor without specified {@link Commands}. Default Command is {@link Commands#NONE}.
     */
    public BlankQueueValue() {
        super();
    }

    @Override
    public byte[] generateCommandBytes() {
        return (Commands.getValueFromOrdinal(mCommandType).name() +
                QueueUtil.COMMAND_EOF)
                .getBytes();
    }
}

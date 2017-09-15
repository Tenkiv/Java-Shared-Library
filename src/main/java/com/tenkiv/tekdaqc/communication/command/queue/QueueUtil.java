package com.tenkiv.tekdaqc.communication.command.queue;

/**
 * Utility class for storing strings commonly used by the Tekdqac Java Library in queue commands and tasks.
 *
 * @author Tenkiv (software@tenkiv.com)
 * @since v2.0.0.0
 */
public class QueueUtil {

    /**
     * ASCII delimiter between parameters.
     */
    public static final String GENERAL_DELIMETER = " ";

    /**
     * Flag denoting the beginning of a valid parameter.
     */
    public static final String PARAMETER_FLAG = "--";

    /**
     * Flag denoting the separation of a valid parameter from its value.
     */
    public static final String KEY_VALUE_SEPARATOR = "=";

    /**
     * Flag which denotes the end of a command.
     */
    public static final char COMMAND_EOF = '\r';

}

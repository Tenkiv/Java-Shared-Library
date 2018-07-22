package com.tenkiv.tekdaqc.utility;

/**
 * Enum of critical errors which may necessitate total reboot of board.
 */
public enum TekdaqcCriticalError{
    /**
     * Error thrown when board doesn't reinitialize from exception
     */
    FAILED_TO_REINITIALIZE,
    /**
     * Error thrown when a major command is unable to be executed
     */
    FAILED_MAJOR_COMMAND,
    /**
     * Error thrown when connection is lost between board and controller
     */
    TERMINAL_CONNECTION_DISRUPTION,
    /**
     * Error thrown when connection is disrupted between board and controller
     */
    PARTIAL_DISCONNECTION
}

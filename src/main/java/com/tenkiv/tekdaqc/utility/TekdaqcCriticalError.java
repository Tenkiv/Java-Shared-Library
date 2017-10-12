package com.tenkiv.tekdaqc.utility;

/**
 * Enum of critical errors which may necessitate total reboot of board.
 */
public enum TekdaqcCriticalError{
    FAILED_TO_REINITIALIZE,
    FAILED_MAJOR_COMMAND,
    TERMINAL_CONNECTION_DISRUPTION,
    PARTIAL_DISCONNECTION
}

package com.tenkiv.tekdaqc.communication.executors;

/**
 * Common methods for the threaded executors for the Tekdaqc.
 *
 * @author Tenkiv (software@tenkiv.com)
 * @since v1.0.0.0
 */
public interface ITekdaqcExecutor {

    /**
     * Instruct the executor to shutdown.
     */
    void shutdown();
}

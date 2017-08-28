package com.tenkiv.tekdaqc.communication.tasks;

import com.tenkiv.tekdaqc.hardware.ATekdaqc;

/**
 * Callback interface for listeners of task completion status.
 *
 * @author Tenkiv (software@tenkiv.com)
 * @since v1.0.0.0
 */
public interface ITaskComplete {

    /**
     * Called when a task has completed successfully.
     *
     * @param tekdaqc Tekdaqc whose task failed.
     */
    void onTaskSuccess(ATekdaqc tekdaqc);

    /**
     * Called when a task has failed to complete successfully.
     *
     * @param tekdaqc Tekdaqc whose task failed.
     */
    void onTaskFailed(ATekdaqc tekdaqc);
}

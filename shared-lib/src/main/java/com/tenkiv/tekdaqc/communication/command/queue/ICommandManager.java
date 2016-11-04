package com.tenkiv.tekdaqc.communication.command.queue;

import java.util.Queue;

/**
 * A general interface for the current system of command management.
 *
 * @author Tenkiv (software@tenkiv.com)
 * @since v2.0.0.0
 */
public interface ICommandManager {

    /**
     * This method should queue an {@link IQueueObject} for execution either locally like {@link CommandQueueManager} or remotely.
     *
     * @param command {@link IQueueObject} to be prepared for execution.
     */
    void queueCommand(IQueueObject command);

    /**
     * This method should queue a {@link Task} for execution either locally like {@link CommandQueueManager} or remotely.
     *
     * @param task {@link Task} to be prepared for execution.
     */
    void queueTask(Task task);

    /**
     * Attempt to poll an {@link IQueueObject} from the {@link Queue} and process it.
     */
    void tryCommand();

    /**
     * Purge all queued {@link IQueueObject}s.
     *
     * @param forShutdown Flag for safely shutting down the command queue after purge.
     */
    void purge(boolean forShutdown);

    /**
     * Returns the current number of queued commands.
     *
     * @return The number of commands.
     */
    int getNumberQueued();
}

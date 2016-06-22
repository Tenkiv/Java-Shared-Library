package com.tenkiv.tekdaqc.communication.command.queue;

import com.tenkiv.tekdaqc.communication.command.queue.values.ABaseQueueVal;
import com.tenkiv.tekdaqc.communication.tasks.ITaskComplete;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Class used for issuing of {@link IQueueObject} in blocks and ensuring that each command's execution is dependant on the success of all prior commands.
 * If a {@link IQueueObject} fails to execute, the command executor will cull until it has cleaned all commands remaining in the {@link Task}.
 */
public class Task {

    /**
     * The{@link List} of all {@link IQueueObject} added as commands.
     */
    protected final List<IQueueObject> mCommands = new ArrayList<>();

    /**
     * The {@link List} of all {@link ITaskComplete} added as callbacks  to this task.
     */
    protected final List<ITaskComplete> mListeners = new ArrayList<>();

    /**
     * Constructor which adds the {@link ITaskComplete} as a callback and all {@link IQueueObject} as commands.
     *
     * @param callback The {@link ITaskComplete} to be added as a callback to this task.
     * @param commands The {@link Collection} of {@link ABaseQueueVal} to be added as commands.
     */
    public Task(final ITaskComplete callback, final Collection<ABaseQueueVal> commands) {
        mListeners.add(callback);
        mCommands.addAll(commands);
    }

    /**
     * Constructor which adds all {@link ITaskComplete} as a callbacks and all {@link IQueueObject} as commands.
     *
     * @param callbacks The {@link Collection} of {@link ITaskComplete} to be added as callbacks  to this task.
     * @param commands  The {@link Collection} of {@link ABaseQueueVal} to be added as commands.
     */
    public Task(final Collection<ITaskComplete> callbacks, final Collection<ABaseQueueVal> commands) {
        mListeners.addAll(callbacks);
        mCommands.addAll(commands);
    }

    /**
     * Constructor which adds all {@link IQueueObject} as commands.
     *
     * @param commands The {@link List} of {@link IQueueObject} to be added as commands.
     */
    public Task(final List<IQueueObject> commands) {
        mCommands.addAll(commands);
    }

    /**
     * Constructor which adds the {@link ITaskComplete} as a callback.
     *
     * @param callback The {@link ITaskComplete} to be added as a callback  to this task.
     */
    public Task(final ITaskComplete callback) {
        mListeners.add(callback);
    }

    /**
     * Constructor which adds all {@link ITaskComplete} as a callbacks.
     *
     * @param callbacks The {@link Collection} of {@link ITaskComplete} to be added as callbacks to this task.
     */
    public Task(final Collection<ITaskComplete> callbacks) {
        mListeners.addAll(callbacks);
    }

    /**
     * Empty constructor.
     */
    public Task() {
    }

    /**
     * Method to add a {@link ABaseQueueVal} as a command to the {@link Task}.
     *
     * @param command The {@link ABaseQueueVal} to add as a command.
     */
    public void addCommand(final ABaseQueueVal command) {
        mCommands.add(command);
    }

    /**
     * Method to add a {@link Collection} of {@link ABaseQueueVal} as commands.
     *
     * @param commands The {@link Collection} of {@link ABaseQueueVal} to add as commands.
     */
    public void addCommands(final Collection<ABaseQueueVal> commands) {
        mCommands.addAll(commands);
    }

    /**
     * Method to remove {@link ABaseQueueVal} from the {@link Task}.
     *
     * @param command The {@link ABaseQueueVal} to remove.
     */
    public void removeCommand(final ABaseQueueVal command) {
        mCommands.remove(command);
    }

    /**
     * Method to add a {@link ITaskComplete} as a callback to this task
     *
     * @param listener The {@link ITaskComplete} to add as a callback to the task.
     */
    public void addListener(final ITaskComplete listener) {
        mListeners.add(listener);
    }

    /**
     * Method to remove a {@link ITaskComplete} from a {@link Task}.
     *
     * @param listener The {@link ITaskComplete} to be removed.
     */
    public void removeListener(final ITaskComplete listener) {
        mListeners.remove(listener);
    }

    /**
     * Method to get the current list of all commands in the task, including the callbacks used for the command queue.
     *
     * @return The {@link List} of {@link IQueueObject} that is currently added to this task.
     */
    public List<IQueueObject> getCommandList() {
        List<IQueueObject> fullList = new ArrayList<>();
        fullList.addAll(mCommands);
        fullList.add(new QueueCallback(mListeners));
        return fullList;
    }

    /**
     * Method to get the current list of all {@link ITaskComplete} added as callbacks to this task.
     *
     * @return The {@link List} of {@link ITaskComplete} currently added to this {@link Task} as callbacks.
     */
    public List<ITaskComplete> getListenersList() {
        return mListeners;
    }
}

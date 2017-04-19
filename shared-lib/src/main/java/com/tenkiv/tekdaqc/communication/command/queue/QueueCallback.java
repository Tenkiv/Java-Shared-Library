package com.tenkiv.tekdaqc.communication.command.queue;

import com.tenkiv.tekdaqc.communication.command.queue.values.IQueueObject;
import com.tenkiv.tekdaqc.communication.tasks.ITaskComplete;
import com.tenkiv.tekdaqc.hardware.ATekdaqc;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;

/**
 * Listener class used for callbacks added to the command queue.
 *
 * @author Tenkiv (software@tenkiv.com)
 * @since v2.0.0.0
 */
public class QueueCallback implements IQueueObject {

    /**
     * List of all callbacks added to this callback.
     */
    private final ArrayList<ITaskComplete> mCallback = new ArrayList<>();

    /**
     *
     */
    private boolean isInternal = false;

    /**
     * The unique identifier for this {@link QueueCallback}. Used for demarcating this callback when serializing.
     */
    private double mUID;

    /**
     * Empty constructor for either serialization or later value declaration.
     */
    public QueueCallback() {}

    /**
     * Constructor which creates a {@link QueueCallback} and adds the specified {@link ITaskComplete} to the list of callbacks.
     *
     * @param callback {@link ITaskComplete} to be added as a callback to the queue.
     */
    public QueueCallback(final ITaskComplete callback) {
        mCallback.add(callback);
    }

    /**
     * Constructor which creates a {@link QueueCallback} and adds the specified {@link List} of {@link ITaskComplete} to the list of callbacks.
     *
     * @param callback {@link List} of {@link ITaskComplete} to be added to the queue as a callback.
     */
    public QueueCallback(final List<ITaskComplete> callback) {
        mCallback.addAll(callback);
    }

    /**
     * Internal constructor used as an internal delimiter for commands. This causes commands added to the queue to be compartmentalized for culling callbacks.
     *
     * @param isInternalDelimiter {@link Boolean} determines if this {@link QueueCallback} is an internal delimiter.
     */
    protected QueueCallback(final boolean isInternalDelimiter) {
        isInternal = isInternalDelimiter;
    }

    /**
     * Internal constructor used as an internal delimiter for commands. This causes commands added to the queue to be compartmentalized for culling callbacks.
     * This constuctor also adds a {@link List} of {@link ITaskComplete} as callbacks for this {@link QueueCallback}.
     *
     * @param callback            {@link List} of {@link ITaskComplete} to be added as callbacks.
     * @param isInternalDelimiter {@link Boolean} determines if this {@link QueueCallback} is an internal delimiter.
     */
    protected QueueCallback(final List<ITaskComplete> callback, final boolean isInternalDelimiter) {
        mCallback.addAll(callback);
        isInternal = isInternalDelimiter;
    }

    /**
     * Internal method for determining if a {@link QueueCallback} is an internal delimiter for the command queue.
     *
     * @return {@link Boolean} of if the {@link QueueCallback} is an internal delimiter.
     */
    protected boolean isInternalDelimiter() {
        return isInternal;
    }

    /**
     * Method to add a {@link ITaskComplete} as a callback to this {@link QueueCallback}.
     *
     * @param listener The {@link ITaskComplete} listener to be added as a callback.
     */
    public void addCallback(final ITaskComplete listener) {
        mCallback.add(listener);
    }

    /**
     * Method to remove a {@link ITaskComplete} as a callback from this {@link QueueCallback}.
     *
     * @param listener The {@link ITaskComplete} listener to be removed as a callback.
     */
    public void removeCallback(final ITaskComplete listener) {
        mCallback.remove(listener);
    }

    /**
     * Method used for calling back to all added listeners about a {@link ATekdaqc} successfully reaching
     * this {@link QueueCallback} on the command queue.
     *
     * @param tekdaqc The {@link ATekdaqc} which reached the {@link QueueCallback} in its command queue.
     */
    public void success(final ATekdaqc tekdaqc) {
        for (final ITaskComplete callback : mCallback) {
            callback.onTaskSuccess(tekdaqc);
        }
    }

    /**
     * Method used for calling back to all added listeners about a {@link ATekdaqc} failing to reach
     * this {@link QueueCallback} on the command queue.
     *
     * @param tekdaqc The {@link ATekdaqc} which failed to reach the {@link QueueCallback} in its command queue.
     */
    public void failure(final ATekdaqc tekdaqc) {
        for (final ITaskComplete callback : mCallback) {
            callback.onTaskFailed(tekdaqc);
        }
    }

    /**
     * Method which returns the unique identifier of this {@link QueueCallback}.
     *
     * @return The {@link Double} which serves as the UID of this {@link QueueCallback}.
     */
    public double getUID() {
        return mUID;
    }

    /**
     * Method to set the unique identifier of this {@link QueueCallback}.
     *
     * @param mUID The {@link Double} which serves as the UID for this {@link QueueCallback}.
     */
    public void setUID(final double mUID) {
        this.mUID = mUID;
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        out.writeDouble(mUID);
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        mUID = in.readDouble();
    }
}

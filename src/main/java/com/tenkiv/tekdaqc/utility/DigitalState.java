package com.tenkiv.tekdaqc.utility;

/**
 * Set of possible digital channel states.
 *
 * @author Tenkiv (software@tenkiv.com)
 * @since v1.0.0.0
 */
public enum DigitalState {
    LOGIC_HIGH("Logic High"), LOGIC_LOW("Logic Low"), UNKNOWN("UNKNOWN");

    /**
     * The state of the digital input.
     */
    public final String state;

    private static DigitalState[] mValueArray = DigitalState.values();

    /**
     * Internal constructor for digital state which converts a {@link String} to the corresponding digital state.
     *
     * @param state The {@link String} of the name of the digital state.
     */
    DigitalState(String state) {
        this.state = state;
    }

    public static DigitalState getValueFromOrdinal(byte ordinal) {
        return mValueArray[ordinal];
    }

    /**
     * Static method which returns the corresponding {@link DigitalState} from a {@link String} of its name. Returns null if {@link String} is invalid.
     *
     * @param state The {@link String} of the name of the {@link DigitalState}.
     * @return The {@link DigitalState} with name of the {@link String}. Returns null if name is invalid.
     */
    public static DigitalState fromString(final String state) {
        for (final DigitalState s : values()) {
            if (s.state.equals(state)) {
                return s;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return state;
    }
}

package com.tenkiv.tekdaqc.communication.ascii.message.parsing;

import java.util.regex.Pattern;

/**
 * Utility class for ASCII Messages.
 *
 * @author Tenkiv (software@tenkiv.com)
 * @since 2.0.0.0
 */
public class ASCIIMessageUtils {

    public static final String V2_ANALOG_INPUT_HEADER = "?A";
    public static final String V2_DIGITAL_INPUT_HEADER = "?D";
    public static final String DIGITAL_PWM_INPUT_HEADER = "?P";
    public static final String V2_DIGITAL_OUTPUT_HEADER = "";

    public static final String V1_ANALOG_INPUT_HEADER = "Analog Input";
    public static final String V1_DIGITAL_INPUT_HEADER = "Digital Input";
    public static final String V1_DIGITAL_OUTPUT_HEADER = "Digital Output";
    public static final String DEBUG_MESSAGE_HEADER = "Debug Message";
    public static final String STATUS_MESSAGE_HEADER = "Status Message";
    public static final String COMMAND_MESSAGE_HEADER = "Command Data Message";
    public static final String ERROR_MESSAGE_HEADER = "Error Message";
    public static final String NETWORK_ERROR_FLAG = "[NETWORK]";
    public static final String MESSAGE_TAG = "Message: ";
    public static final String NAME_TAG = "Name: ";
    public static final String PHYSICAL_INPUT_TAG = "Physical Input: ";
    public static final String PGA_TAG = "PGA: ";
    public static final String RATE_TAG = "Rate: ";
    public static final String BUFFER_STATUS_TAG = "Buffer Status: ";
    public static final String TIMESTAMP_TAG = "Timestamp: ";
    public static final String VALUE_TAG = "Value: ";
    public static final String LEVEL_TAG = "Level: ";
    public static final String LINE_MARKER = "--------------------";
    public static final String HIGH_MARKER = "H";
    public static final String LOW_MARKER = "L";
    public static final int UNIT_SEPARATOR_CHAR = 0x1F;
    public static final int NEW_LINE_CHAR = 0x0A;
    public static final int CARRIAGE_RETURN_CHAR = 0x0D;

    public static final Pattern RECORD_SEPARATOR_PATTERN = Pattern
            .compile("\\x1E");

    private static final String TAG = "AASCIIMessage";
    private static final int MESSAGE_POOL_CAPACITY = 50;
    private static final long serialVersionUID = 1L;

    private static ASCIIAnalogInputDataMessage getAnalogInputDataMessage() {

        return new ASCIIAnalogInputDataMessage();

    }

    private static ASCIIDigitalInputDataMessage getDigitalInputDataMessage() {
        return new ASCIIDigitalInputDataMessage();
    }

    private static ASCIIPWMInputDataMessage getDigitalPWMInputDataMessage() {
        return new ASCIIPWMInputDataMessage();
    }

    private static ASCIIDigitalOutputDataMessage getDigitalOutputDataMessage() {
        return new ASCIIDigitalOutputDataMessage();
    }

    private static ASCIICommandMessage getCommandDataMessage() {
        return new ASCIICommandMessage();
    }

    private static ASCIIErrorMessage getErrorMessage() {
        return new ASCIIErrorMessage();
    }

    private static ASCIIDebugMessage getDebugMessage() {
        return new ASCIIDebugMessage();
    }

    private static ASCIIStatusMessage getStatusMessage() {
        return new ASCIIStatusMessage();
    }

    /**
     * Factory method to produce the appropriate messages from the provided raw
     * message data.
     *
     * @param messageData {@link String} The raw message data.
     * @return {@link AASCIIMessage} The constructed message.
     */
    public static AASCIIMessage parseMessage(final String messageData) {
        final AASCIIMessage message;
        try {
            if (messageData == null)
                return null;

            /*
			 * The order here is important because debug/status/error messages
			 * may contain tags which could register as other message types.
			 */
            if (messageData.contains(DEBUG_MESSAGE_HEADER)) {
                // This is an ASCII Debug message
                message = getDebugMessage();
                message.setData(messageData);
            } else if (messageData.contains(STATUS_MESSAGE_HEADER)) {
                // This is an ASCII Status message
                message = getStatusMessage();
                message.setData(messageData);
            } else if (messageData.contains(ERROR_MESSAGE_HEADER)) {
                // This is an ASCII Error message
                message = getErrorMessage();
                message.setData(messageData);
            } else if (messageData.contains(COMMAND_MESSAGE_HEADER)) {
                message = getCommandDataMessage();
                message.setData(messageData);
            } else if (messageData.contains(V1_ANALOG_INPUT_HEADER)
                    || messageData.contains(V2_ANALOG_INPUT_HEADER)) {
                // This is an ASCII Analog Input Data message
                message = getAnalogInputDataMessage();
                message.setData(messageData);
            } else if (messageData.contains(V1_DIGITAL_INPUT_HEADER)
                    || messageData.contains(V2_DIGITAL_INPUT_HEADER)) {
                // This is an ASCII Digital Input Data message
                message = getDigitalInputDataMessage();
                message.setData(messageData);
            } else if (messageData.contains(V1_DIGITAL_OUTPUT_HEADER)) {
                // This is an ASCII Digital Output Data message
                message = getDigitalOutputDataMessage();
                message.setData(messageData);
            }else if (messageData.contains(DIGITAL_PWM_INPUT_HEADER)) {
                // This is an ASCII Digital PWM Input Data message
                message = getDigitalPWMInputDataMessage();
                message.setData(messageData);
            } else {
                // This is an unrecognized message format
                /*System.out.println(TAG + "Unrecognized Message" +messageData);*/
                message = null;
            }
        } catch (final Exception e) {
            e.printStackTrace();
            /*System.err.println("ERROR MESSAGE: "+messageData);*/
			/*System.err.println("Detected exception parsing message ("
					+ e.getClass().getSimpleName() + "). Message Data:");
			System.err.println(messageData);*///TODO REMOVE THIS ONCE FIRMWARE BECOMES UN-FUCKED.
            return null;
        }
        return message;
    }

    /**
     * Message type enumeration.
     */
    public static enum MESSAGE_TYPE {
        DEBUG, STATUS, ERROR, ANALOG_INPUT_DATA, DIGITAL_INPUT_DATA, DIGITAL_OUTPUT_DATA, COMMAND_DATA, PWM_INPUT_DATA;
    }
}

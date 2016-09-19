package com.tenkiv.tekdaqc.locator;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Encapsulation of a locator response packet data from a Tekdaqc.
 *
 * An array that contains the device locator response data.
 * The format of the data is as follows:
 * Byte        Description
 * --------    ------------------------
 * 0          TAG_STATUS
 * 1          Packet Length
 * 2          CMD_DISCOVER_TARGET
 * 3          Board Type
 * 4..35      Board ID (32 character serial number)
 * 36..39     Client IP Address
 * 40..45     MAC Address
 * 46..49     Firmware Version
 * 50..113    Application Title
 * 114        Checksum
 *
 * @author Tenkiv (software@tenkiv.com)
 * @since 1.0.0.0
 */
public final class LocatorResponse implements Externalizable {

    /**
     * Array index values
     */
    private static final int IDX_BOARD_TYPE = 3;
    private static final int IDX_BOARD_ID_START = 4;
    private static final int IDX_BOARD_ID_END = 35;
    private static final int IDX_MAC_START = 40;
    private static final int IDX_MAC_END = 45;
    private static final int IDX_FIRMWARE_START = 46;
    private static final int IDX_FIRMWARE_LENGTH = 4;
    private static final int IDX_APPLICATION_TITLE_START = 50;
    private static final int IDX_APPLICATION_TITLE_LENGTH = 64;

    protected String mHostIPAddress;
    protected char mType; // Byte 3
    protected String mSerial; // Bytes 4-35
    protected String mMACAddress; // Bytes 40-45
    protected String mFirmwareVersion; // Bytes 46-49
    protected String mTitle; // Bytes 50-113

    /**
     * Constructor provided for serialization. User code should not use this.
     */
    public LocatorResponse() {
    }

    /**
     * Constructs a new {@link LocatorResponse} from the provided data.
     *
     * @param hostIP {@link String} The IP Address the response came from.
     * @param data   {@link byte[]} The response data.
     */
    LocatorResponse(String hostIP, byte[] data) {
        // Convert the Firmware to a String representation
        final StringBuilder firmwareBuilder = new StringBuilder();
        for (int i = 0; i < IDX_FIRMWARE_LENGTH; ++i) {
            firmwareBuilder.append((int) data[IDX_FIRMWARE_START + i]);
            if (i != IDX_FIRMWARE_LENGTH - 1)
                firmwareBuilder.append('.');
        }

        // Convert the Serial to a String representation
        final StringBuilder serialBuilder = new StringBuilder();
        for (int i = IDX_BOARD_ID_START; i <= IDX_BOARD_ID_END; ++i) {
            if (data[i] == 0)
                break;

            serialBuilder.append((char) data[i]);
        }

        // Convert the MAC to a String representation
        final StringBuilder macBuilder = new StringBuilder();
        for (int i = IDX_MAC_START; i <= IDX_MAC_END; ++i) {
            macBuilder.append(String.format("%02x", data[i]));

            if (i != IDX_MAC_END)
                macBuilder.append(":");
        }

        mHostIPAddress = hostIP;
        mFirmwareVersion = firmwareBuilder.toString();
        mMACAddress = macBuilder.toString();
        mSerial = serialBuilder.toString();
        mTitle = new String(data, IDX_APPLICATION_TITLE_START, IDX_APPLICATION_TITLE_LENGTH).trim();
        mType = (char) data[IDX_BOARD_TYPE];
    }

    @Override
    public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException {
        mSerial = (String) input.readObject();
        mType = input.readChar();
        mHostIPAddress = (String) input.readObject();
        mMACAddress = (String) input.readObject();
        mFirmwareVersion = (String) input.readObject();
        mTitle = (String) input.readObject();
    }

    @Override
    public void writeExternal(ObjectOutput output) throws IOException {
        output.writeObject(mSerial);
        output.writeChar(mType);
        output.writeObject(mHostIPAddress);
        output.writeObject(mMACAddress);
        output.writeObject(mFirmwareVersion);
        output.writeObject(mTitle);
    }

    /**
     * Retrieve a copy of the IP Address string for this response.
     *
     * @return {@link String} The IP Address.
     */
    public String getHostIP() {
        return new String(mHostIPAddress);
    }

    /**
     * Retrieve a copy of the firmware version string for this response.
     *
     * @return {@link String} The firmware version.
     */
    public String getFirwareVersion() {
        return new String(mFirmwareVersion);
    }

    /**
     * Retrieve a copy of the MAC Address string for this response.
     *
     * @return {@link String} The MAC Address.
     */
    public String getMacAddress() {
        return new String(mMACAddress);
    }

    /**
     * Retrieve a copy of the serial number string for this response.
     *
     * @return {@link String} The serial number string.
     */
    public String getSerial() {
        return new String(mSerial);
    }

    /**
     * Retrieve a copy of the application name string for this response.
     *
     * @return {@link String} The application name.
     */
    public String getTitle() {
        return new String(mTitle);
    }

    /**
     * Retrieve the board revision for this response.
     *
     * @return {@code char} The board revision.
     */
    public char getType() {
        return mType;
    }

    /**
     * Validates that this response packet matches the search parameters.
     *
     * @param params {@link LocatorParams} The parameters used in the Tekdaqc search.
     * @return boolean True if this packet is valid.
     */
    public boolean isValid(LocatorParams params) {
        return LocatorParams.isValidResponse(params, this);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Locator Response: \n");
        builder.append("\tHOST IP Address: ").append(mHostIPAddress).append("\n");
        builder.append("\tMAC Address: ").append(mMACAddress).append("\n");
        builder.append("\tApplication Title: ").append(mTitle).append("\n");
        builder.append("\tBoard Type: ").append((char) mType).append("\n");
        builder.append("\tBoard ID: ").append(mSerial).append("\n");
        builder.append("\tFirmware Version: ").append(mFirmwareVersion).append("\n");
        return builder.toString();
    }
}

package com.tenkiv.tekdaqc.utility;

/**
 * Utility class to convert binary string to their hexadecimal representation.
 */
public class DigitalOutputUtilities {


    public static String intToHex(int num){
        return String.format("%04X", num & 0xFFFFF);
    }

    /**
     * Converts a binary string to hexadecimal. Hexadecimal is used in communication with the tekdaqc
     * for setting and reading the digital outputs.
     *
     * @param binaryString The string to be converted.
     * @return String in hexadecimal
     */
    public static String hexConversion(String binaryString) {

        final StringBuilder hexBuilder = new StringBuilder();

        while (binaryString.length() > 0) {

            final int decimal = Integer.parseInt(binaryString.substring(0, 4), 2);
            final String hexStr = Integer.toString(decimal, 16);

            hexBuilder.append(hexStr);

            binaryString = binaryString.substring(4, binaryString.length());
        }
        return hexBuilder.toString();
    }

    /**
     * Converts a boolean array to hexadecimal. Hexadecimal is used in communication with the tekdaqc
     * for setting and reading the digital outputs.
     *
     * @param digitalOutputData The string to be converted.
     * @return String in hexadecimal
     */
    public static String boolArrayConversion(final boolean[] digitalOutputData) {

        final StringBuilder binStringBuilder = new StringBuilder();

        for (final boolean digOut : digitalOutputData) {

            if (digOut) {
                binStringBuilder.append("1");

            } else {
                binStringBuilder.append("0");
            }
        }
        return (hexConversion(binStringBuilder.toString()));
    }

    /**
     * Converts a hexadecimal {@link String} to a binary string.
     *
     * @param hex The hex {@link String} to be converted.
     * @return The Binary string of the hex string.
     */
    public static String hexToBinary(final String hex) {
        String hex_char;
        String bin_char;
        String binary = "";
        int len = hex.length() / 2;
        for (int i = 0; i < len; i++) {
            hex_char = hex.substring(2 * i, 2 * i + 2);
            final int conv_int = Integer.parseInt(hex_char, 16);
            bin_char = Integer.toBinaryString(conv_int);
            bin_char = zeroPadBinChar(bin_char);
            if (i == 0) binary = bin_char;
            else binary = binary + bin_char;
            //out.printf("%s %s\n", hex_char,bin_char);
        }
        return binary;
    }

    public static String zeroPadBinChar(final String bin_char) {
        final int len = bin_char.length();
        if (len == 8) return bin_char;
        String zero_pad = "0";
        for (int i = 1; i < 8 - len; i++) zero_pad = zero_pad + "0";
        return zero_pad + bin_char;
    }
}

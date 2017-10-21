package com.tenkiv.tekdaqc

import java.io.*

internal const val FIRMWARE = "1.3.1.0"

internal const val IPADDR = "0.0.0.0"

internal const val MSG = "arbitrary"

internal const val PORT = 8000

internal const val SERIAL = "00000000000000000000000000000012"

internal const val TIMEOUT = 5000

internal const val TITLE = ""

internal const val TYPE = 'E'

internal const val MACADDR = "34:68:52:16:35:96"

internal const val TEST_ANALOG_INPUT_DATA = "?A0\r\n967711311300,-512"

internal const val TEST_DIGITAL_INPUT_DATA = "?D0\r\n967711311300,L"

internal const val TEST_COMMAND_MESSAGE_DATA = "Command Data Message\r\nMessage: SomeMessage"

internal const val TEST_DEBUG_MESSAGE_DATA = "Debug Message\r\nMessage: SomeMessage"

internal const val TEST_ERROR_MESSAGE_DATA = "Error Message\r\nMessage: SomeMessage"

internal const val TEST_STATUS_MESSAGE_DATA = "Status Message\r\nMessage: SomeMessage"

fun serializeToAny(serializable: Serializable): Any? {
    val byteOutStream = ByteArrayOutputStream()
    val outputStream = ObjectOutputStream(byteOutStream)
    outputStream.writeObject(serializable)
    outputStream.close()
    val array =  byteOutStream.toByteArray()
    val byteInStream = ByteArrayInputStream(array)
    val inputStream = ObjectInputStream(byteInStream)
    return  inputStream.readObject()
}


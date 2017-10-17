package com.tenkiv.tekdaqc.communication.command.queue

import com.tenkiv.tekdaqc.communication.command.queue.values.ABaseQueueVal
import com.tenkiv.tekdaqc.hardware.ATekdaqc
import com.tenkiv.tekdaqc.utility.TekdaqcCriticalError
import java.io.BufferedOutputStream
import java.io.IOException


private val COMMAND_WRITER_THREAD_NAME = "COMMAND_WRITER_THREAD_NAME"

/**
 * Class which is executed by [CommandQueueManager.mExecutor]. Writes out generated [Byte] of [ABaseQueueVal]
 * to the [ATekdaqc.getOutputStream]
 */
internal class CommandWriterThread(val tekdaqc: ATekdaqc, val mValue: ABaseQueueVal) :
        Thread(COMMAND_WRITER_THREAD_NAME) {

    override fun run() {
        try {
            writeToStream(mValue)
        } catch (e: IOException) {
            tekdaqc.criticalErrorNotification(TekdaqcCriticalError.TERMINAL_CONNECTION_DISRUPTION)
        }

    }

    /**
     * Method to get and write out command bytes to Telnet.

     * @param command The command to be executed
     * *
     * @throws IOException Generic exception to catch issues in Telnet.
     */
    @Throws(IOException::class)
    private fun writeToStream(command: ABaseQueueVal) {
        val out = BufferedOutputStream(tekdaqc.outputStream)
        out.write(command.generateCommandBytes())
        out.flush()
    }
}
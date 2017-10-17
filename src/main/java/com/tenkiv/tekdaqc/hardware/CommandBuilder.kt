package com.tenkiv.tekdaqc.hardware

import com.tenkiv.tekdaqc.communication.command.queue.Commands
import com.tenkiv.tekdaqc.communication.command.queue.Params
import com.tenkiv.tekdaqc.communication.command.queue.Task
import com.tenkiv.tekdaqc.communication.command.queue.values.ABaseQueueVal
import com.tenkiv.tekdaqc.communication.command.queue.values.QueueValue
import com.tenkiv.tekdaqc.utility.DigitalOutputUtilities
import java.lang.reflect.Array
import java.util.*

/**
 * Utility code which handles the creation of [ABaseQueueVal] for the purpose of adding them individually to a [Task] or
 * directly to the command queue via the [ATekdaqc].

 * @author Tenkiv (software@tenkiv.com)
 * *
 * @since v2.0.0.0
 */
object CommandBuilder {
    /**
     * Method to generate the "READ_ANALOG_INPUT" command with the given parameters.

     * @param input  The input to be read.
     * *
     * @param number The number of times input should be read. Note: "0" will sample indefinitely.
     * *
     * @return The [ABaseQueueVal] of the command.
     */
    fun readAnalogInput(input: Int, number: Int): ABaseQueueVal = QueueValue(
                Commands.READ_ANALOG_INPUT.ordinalCommandType,
                Pair(Params.INPUT, input.toByte()),
                Pair(Params.NUMBER, number.toByte()))

    /**
     * Method to generate the "READ_ANALOG_INPUT" command with a range of inputs.

     * @param start  The start of range of inputs to be read.
     * *
     * @param end    The inclusive end of the range of input to be read.
     * *
     * @param number The number of times the inputs should be read. Note: "0" will sample indefinitely.
     * *
     * @return The [ABaseQueueVal] of the command.
     */
    fun readAnalogInputRange(start: Int, end: Int, number: Int): ABaseQueueVal = QueueValue(
                Commands.READ_ANALOG_INPUT.ordinalCommandType,
                Pair(Params.INPUT, start.toString() + "-" + end),
                Pair(Params.NUMBER, number.toByte()))

    /**
     * Method to generate the "READ_ANALOG_INPUT" command with a set if inputs.

     * @param inputs The [Set] of inputs to be read.
     * *
     * @param number The number of times the inputs should be sampled. Note: "0" will sample indefinitely.
     * *
     * @return The [ABaseQueueVal] of the command.
     */
    fun readAnalogInputSet(inputs: Set<Int>, number: Int): ABaseQueueVal {

        val inputBuilder = StringBuilder()
        for (input in inputs) {
            inputBuilder.append(input).append(',')
        }
        inputBuilder.deleteCharAt(inputBuilder.lastIndex)

        return QueueValue(
                Commands.READ_ANALOG_INPUT.ordinalCommandType,
                Pair(Params.INPUT, inputBuilder.toString()),
                Pair(Params.NUMBER, number.toByte()))
    }

    /**
     * Method to generate the "READ_ANALOG_INPUT" command with the "ALL" parameter.

     * @param number The number of times to sample all inputs.
     * *
     * @return The [ABaseQueueVal] of the command.
     */
    fun readAllAnalogInput(number: Int): ABaseQueueVal = QueueValue(
                Commands.READ_ANALOG_INPUT.ordinalCommandType,
                Pair(Params.INPUT, "ALL"),
                Pair(Params.NUMBER, number.toByte()))

    /**
     * Method to generate the "READ_ANALOG_INPUT" command with the given parameters.

     * @param input  The input to be read.
     * *
     * @param number The number of times input should be read. Note: "0" will sample indefinitely.
     * *
     * @return The [ABaseQueueVal] of the command.
     */
    fun readDigitalInput(input: Int, number: Int): ABaseQueueVal = QueueValue(
                Commands.READ_DIGITAL_INPUT.ordinalCommandType,
                Pair(Params.INPUT, input.toByte()),
                Pair(Params.NUMBER, number.toByte()))

    /**
     * Method to generate the "READ_ANALOG_INPUT" command with a range of inputs.

     * @param start  The start of range of inputs to be read.
     * *
     * @param end    The inclusive end of the range of input to be read.
     * *
     * @param number The number of times the inputs should be read. Note: "0" will sample indefinitely.
     * *
     * @return The [ABaseQueueVal] of the command.
     */
    fun readDigitalInputRange(start: Int, end: Int, number: Int): ABaseQueueVal  = QueueValue(
                Commands.READ_DIGITAL_INPUT.ordinalCommandType,
                Pair(Params.INPUT, start.toString() + "-" + end),
                Pair(Params.NUMBER, number.toByte()))

    /**
     * Method to generate the "READ_ANALOG_INPUT" command with a set if inputs.

     * @param inputs The [Set] of inputs to be read.
     * *
     * @param number The number of times the inputs should be sampled. Note: "0" will sample indefinitely.
     * *
     * @return The [ABaseQueueVal] of the command.
     */
    fun readDigitalInputSet(inputs: Set<Int>, number: Int): ABaseQueueVal {

        val inputBuilder = StringBuilder()
        for (input in inputs) {
            inputBuilder.append(input).append(',')
        }
        inputBuilder.deleteCharAt(inputBuilder.lastIndex)

        return QueueValue(
                Commands.READ_DIGITAL_INPUT.ordinalCommandType,
                Pair(Params.INPUT, inputBuilder.toString()),
                Pair(Params.NUMBER, number.toByte()))

    }

    /**
     * Method to generate the "READ_ANALOG_INPUT" command with the "ALL" parameter.

     * @param number The number of times to sample all inputs.
     * *
     * @return The [ABaseQueueVal] of the command.
     */
    fun readAllDigitalInput(number: Int): ABaseQueueVal  = QueueValue(
                Commands.READ_DIGITAL_INPUT.ordinalCommandType,
                Pair(Params.INPUT, "ALL"),
                Pair(Params.NUMBER, number.toByte()))

    /**
     * Method to generate the "ADD_ANALOG_INPUT" command with the given parameters.

     * @param input The [AAnalogInput] to be added.
     * *
     * @return The [ABaseQueueVal] of the command.
     * *
     * @throws IllegalArgumentException Thrown if the [AAnalogInput] is of the improper type.
     * *
     */
    @Throws(IllegalArgumentException::class, IllegalStateException::class)
    fun addAnalogInput(input: AAnalogInput): ABaseQueueVal {
        val Input = input as? AnalogInput_RevD ?: throw IllegalArgumentException()
        val queueValue = QueueValue(
                Commands.ADD_ANALOG_INPUT.ordinalCommandType,
                Pair(Params.INPUT, Input.channelNumber.toByte()),
                Pair(Params.GAIN, Input.gain.gain),
                Pair(Params.RATE, Input.rate.rate),
                Pair(Params.BUFFER, Input.bufferState.name))
        return queueValue
    }

    /**
     * Method to generate the "ADD_DIGITAL_INPUT" command with the given parameters.

     * @param input The [DigitalInput] to be added.
     * *
     * @return The [ABaseQueueVal] of the command.
     * *
     * *
     * @throws IllegalArgumentException    Thrown if the [DigitalInput] is of the improper type.
     */
    @Throws(IllegalArgumentException::class)
    fun addDigitalInput(input: DigitalInput): ABaseQueueVal  = QueueValue(
                Commands.ADD_DIGITAL_INPUT.ordinalCommandType,
                Pair(Params.INPUT, input.channelNumber.toByte()))

    /**
     * Method to generate the "SET_DIGITAL_OUTPUTS" command with a [String] representing its binary state.
     * Ie. "001100110011"

     * @param binaryString The [String] representing the desired values for the digital outputs as a binary string.
     * *
     * @return The [ABaseQueueVal] of the command.
     */
    fun setDigitalOutputByBinaryString(binaryString: String): ABaseQueueVal {
        val hexOutput = DigitalOutputUtilities.hexConversion(binaryString)
        return QueueValue(
                Commands.SET_DIGITAL_OUTPUT.ordinalCommandType,
                Pair(Params.OUTPUT, hexOutput))
    }

    /**
     * Method to generate the "SET_DIGITAL_OUTPUTS" command with a [String] representing its binary state.
     * Ie. "001100110011"

     * @return The [ABaseQueueVal] of the command.
     */
    fun setDigitalOutputPulseWidthModulation(hex: String, dutyCycle: Int): ABaseQueueVal? {
        if (dutyCycle > 100 || dutyCycle < 0)
            throw IllegalArgumentException("Duty Cycle must not be less then 0 or greater than 100.")

        return QueueValue(
                Commands.SET_DIGITAL_OUTPUT.ordinalCommandType,
                Pair(Params.OUTPUT, hex),
                Pair(Params.DUTYCYCLE, dutyCycle))
    }

    /**
     * Method to generate the "SET_DIGITAL_OUTPUTS" command with a [String] representing its binary state
     * as a hex value. Ie. "FFFF"

     * @param hex The [String] representing the desired values for the digital outputs as a hexadecimal code.
     * *
     * @return The [ABaseQueueVal] of the command.
     */
    fun setDigitalOutputByHex(hex: String): ABaseQueueVal = QueueValue(
                Commands.SET_DIGITAL_OUTPUT.ordinalCommandType,
                Pair(Params.OUTPUT, hex))

    /**
     * Method to generate the "SET_DIGITAL_OUTPUTS" command with an array of booleans representing its desired state.

     * @param digitalOutputState The [Array] of [Boolean] representing the desired digital output state.
     * *
     * @return The [ABaseQueueVal] of the command.
     */
    fun setDigitalOutput(digitalOutputState: BooleanArray): ABaseQueueVal {
        val hexOutput = DigitalOutputUtilities.boolArrayConversion(digitalOutputState)
        return QueueValue(
                Commands.SET_DIGITAL_OUTPUT.ordinalCommandType,
                Pair(Params.OUTPUT, hexOutput))
    }

    /**
     * Method to generate the "REMOVE_ANALOG_INPUT" command from the given [AAnalogInput].

     * @param input The [AAnalogInput] to remove.
     * *
     * @return The [ABaseQueueVal] of the command.
     */
    fun removeAnalogInput(input: AAnalogInput): ABaseQueueVal = QueueValue(
                Commands.REMOVE_ANALOG_INPUT.ordinalCommandType,
                Pair(Params.INPUT, input.channelNumber.toByte()))

    /**
     * Method to generate the "REMOVE_ANALOG_INPUT" command from input number.

     * @param input The physical input of the number to be removed.
     * *
     * @return The [ABaseQueueVal] of the command.
     */
    fun removeAnalogInputByNumber(input: Int): ABaseQueueVal  = QueueValue(
                Commands.REMOVE_ANALOG_INPUT.ordinalCommandType,
                Pair(Params.INPUT, input.toByte()))

    /**
     * Method to generate a [List] of [ABaseQueueVal] representing the commands to remove all analog
     * inputs on the given [Map].

     * @param inputs The [Map] of the inputs to be removed.
     * *
     * @return The [List] of [ABaseQueueVal] representing the commands.
     *
     * @throws IllegalArgumentException
     */
    fun removeMappedAnalogInputs(inputs: Map<Int, AAnalogInput>): List<ABaseQueueVal> {
        val keys = inputs.keys
        val queueVals = keys
                .map { inputs[it] }
                .map {
                    QueueValue(
                            Commands.REMOVE_ANALOG_INPUT.ordinalCommandType,
                            Pair(Params.INPUT, it?.channelNumber?.toByte()?:throw IllegalArgumentException()))
                }
        return queueVals
    }

    /**
     * Method to generate a [ABaseQueueVal] representing the "REMOVE_DIGITAL_INPUT" command.

     * @param input The [DigitalInput] to be removed.
     * *
     * @return The [ABaseQueueVal] of the command.
     */
    fun removeDigitalInput(input: DigitalInput): ABaseQueueVal = QueueValue(
                Commands.REMOVE_DIGITAL_INPUT.ordinalCommandType,
                Pair(Params.INPUT, input.channelNumber.toByte()))

    /**
     * Method to generate a [ABaseQueueVal] representing the "REMOVE_DIGITAL_INPUT" command.

     * @param input The input to be removed.
     * *
     * @return The [ABaseQueueVal] of the command.
     */
    fun removeDigitalInputByNumber(input: Int): ABaseQueueVal  = QueueValue(
                Commands.REMOVE_DIGITAL_INPUT.ordinalCommandType,
                Pair(Params.INPUT, input.toByte()))

    /**
     * Method to generate a [List] of [ABaseQueueVal] representing the commands to remove all digital
     * inputs on the given [Map].

     * @param inputs The [Map] of the inputs to be removed.
     * *
     * @return The [List] of [ABaseQueueVal] representing the commands.
     * @throws IllegalArgumentException
     */
    fun removeMappedDigitalInputs(inputs: Map<Int, DigitalInput>): List<ABaseQueueVal> {
        val keys = inputs.keys
        val queueVals = keys
                .map { inputs[it] }
                .map {
                    QueueValue(
                            Commands.REMOVE_DIGITAL_INPUT.ordinalCommandType,
                            Pair(Params.INPUT, it?.channelNumber?.toByte() ?: throw IllegalArgumentException()))
                }
        return queueVals
    }

    /**
     * Method to generate a [List] of [ABaseQueueVal] representing commands to remove all known added digital inputs.
     * Note: The library only knows which inputs it has added on this session and not which were added to the
     * tekdaqc before connecting.

     * @return The [List] of [ABaseQueueVal] representing the commands.
     */
    fun deactivateAllDigitalInputs(): List<ABaseQueueVal> {
        val queueVals = (0 until Tekdaqc_RevD.DIGITAL_INPUT_COUNT).map {
            QueueValue(
                    Commands.REMOVE_DIGITAL_INPUT.ordinalCommandType,
                    Pair(Params.INPUT, it.toByte()))
        }
        return queueVals
    }

    /**
     * Method to generate a [List] of [ABaseQueueVal] representing commands to remove all known added analog inputs.
     * Note: The library only knows which inputs it has added on this session and not which were added to the
     * tekdaqc before connecting.

     * @return The [List] of [ABaseQueueVal] representing the commands.
     */
    fun deactivateAllAnalogInputs(): List<ABaseQueueVal> {
        val queueVals = ArrayList<ABaseQueueVal>()
        for (count in 0..Tekdaqc_RevD.ANALOG_INPUT_COUNT - 1) {
            val queueValue = QueueValue(
                    Commands.REMOVE_ANALOG_INPUT.ordinalCommandType,
                    Pair(Params.INPUT, count.toByte()))
            queueVals.add(queueValue)
        }
        return queueVals
    }

    /**
     * Method to generate a [ABaseQueueVal] representing the command "SET_ANALOG_SCALE" with the given parameters.

     * @param scale The desired [ATekdaqc.AnalogScale] to be set.
     * *
     * @return The [ABaseQueueVal] of the command.
     */
    fun setAnalogInputScale(scale: ATekdaqc.AnalogScale): ABaseQueueVal = QueueValue(
                Commands.SET_ANALOG_INPUT_SCALE.ordinalCommandType,
                Pair(Params.SCALE, scale.toString()))

    /**
     * Method to generate a [ABaseQueueVal] representing the command "GET_ANALOG_SCALE".

     * @return The [ABaseQueueVal] of the command.
     */
    fun getAnalogInputScale(): ABaseQueueVal = QueueValue(
                Commands.GET_ANALOG_INPUT_SCALE.ordinalCommandType)

    /**
     * Method to generate a [ABaseQueueVal] representing the command "SYSTEM_GCAL" with the given parameters.

     * WARNING: THIS COMMAND IS USED IN CALIBRATION. IT IS NOT DESIGNED FOR AVERAGE USERS AND SHOULD ONLY BE RUN BY
     * THOSE WITH BOTH THE PREREQUISITE KNOWLEDGE AND EQUIPMENT. IMPROPER OR INCOMPLETE CALIBRATION CAN
     * RESULT IN UNRELIABLE, INACCURATE, OR EMPTY ANALOG INPUT VALUES. UNDERTAKE RECALIBRATION AT YOUR OWN RISK.

     * @param input Inputs which are being calibrated
     * *
     * @return The [ABaseQueueVal] of the command.
     */
    fun systemGainCalibrate(input: Int): ABaseQueueVal = QueueValue(
                Commands.SYSTEM_GCAL.ordinalCommandType,
                Pair(Params.INPUT, input.toByte()))

    /**
     * Method to generate a [ABaseQueueVal] representing the command "READ_SYSTEM_GCAL".

     * WARNING: THIS COMMAND IS USED IN CALIBRATION. IT IS NOT DESIGNED FOR AVERAGE USERS AND SHOULD ONLY BE RUN BY
     * THOSE WITH BOTH THE PREREQUISITE KNOWLEDGE AND EQUIPMENT. IMPROPER OR INCOMPLETE CALIBRATION CAN
     * RESULT IN UNRELIABLE, INACCURATE, OR EMPTY ANALOG INPUT VALUES. UNDERTAKE RECALIBRATION AT YOUR OWN RISK.

     * @return The [ABaseQueueVal] of the command.
     */
    fun readSystemGainCalibration(): ABaseQueueVal = QueueValue(
                Commands.READ_SYSTEM_GCAL.ordinalCommandType)

    /**
     * Method to generate a [ABaseQueueVal] representing the command "READ_SELF_GCAL" with the given parameters.

     * WARNING: THIS COMMAND IS USED IN CALIBRATION. IT IS NOT DESIGNED FOR AVERAGE USERS AND SHOULD ONLY BE RUN BY
     * THOSE WITH BOTH THE PREREQUISITE KNOWLEDGE AND EQUIPMENT. IMPROPER OR INCOMPLETE CALIBRATION CAN
     * RESULT IN UNRELIABLE, INACCURATE, OR EMPTY ANALOG INPUT VALUES. UNDERTAKE RECALIBRATION AT YOUR OWN RISK.

     * @param gain   Gain of the cal to read.
     * *
     * @param rate   Rate of the cal to read.
     * *
     * @param buffer Buffer of the cal to read.
     * *
     * @return The [ABaseQueueVal] of the command.
     */
    fun readSelfGainCalibration(gain: AAnalogInput.Gain,
                                rate: AAnalogInput.Rate,
                                buffer: AnalogInput_RevD.BufferState): ABaseQueueVal = QueueValue(
                Commands.READ_SELF_GCAL.ordinalCommandType,
                Pair(Params.GAIN, gain.gain),
                Pair(Params.RATE, rate.rate),
                Pair(Params.BUFFER, buffer.name))

    /**
     * Method to generate a [ABaseQueueVal] representing the command "UPGRADE".

     * @return The [ABaseQueueVal] of the command.
     */
    fun upgrade(): ABaseQueueVal = QueueValue(
                Commands.UPGRADE.ordinalCommandType)

    /**
     * Method to generate a [ABaseQueueVal] representing the command "IDENTIFY".

     * @return The [ABaseQueueVal] of the command.
     */
    fun identify(): ABaseQueueVal = QueueValue(
                Commands.IDENTIFY.ordinalCommandType)

    /**
     * Method to generate a [ABaseQueueVal] representing the command "SAMPLE" with the given parameters.

     * @param number The number of times to sample. Note: "0" will sample indefinitely.
     * *
     * @return The [ABaseQueueVal] of the command.
     */
    fun sample(number: Int): ABaseQueueVal = QueueValue(
                Commands.SAMPLE.ordinalCommandType,
                Pair(Params.NUMBER, number))

    /**
     * Method to generate a [ABaseQueueVal] representing the command "HALT".

     * @return The [ABaseQueueVal] of the command.
     */
    fun halt(): ABaseQueueVal = QueueValue(
                Commands.HALT.ordinalCommandType)

    /**
     * Method to generate a [ABaseQueueVal] representing the command "SET_RTC" with the given parameters.

     * @param timestamp The current time to be set for the RTC.
     * *
     * @return The [ABaseQueueVal] of the command.
     */
    fun setRTC(timestamp: Long): ABaseQueueVal = QueueValue(
                Commands.WRITE_CALIBRATION_TEMP.ordinalCommandType,
                Pair(Params.VALUE, timestamp.toString()))

    /**
     * Method to generate a [ABaseQueueVal] representing the command "READ_ADC_REGISTERS".

     * @return The [ABaseQueueVal] of the command.
     */
    fun readADCRegisters(): ABaseQueueVal = QueueValue(
                Commands.READ_ADC_REGISTERS.ordinalCommandType)

    /**
     * Method to generate a [ABaseQueueVal] representing the command "GET_CAL_STATUS".

     * WARNING: THIS COMMAND IS USED IN CALIBRATION. IT IS NOT DESIGNED FOR AVERAGE USERS AND SHOULD ONLY BE RUN BY
     * THOSE WITH BOTH THE PREREQUISITE KNOWLEDGE AND EQUIPMENT. IMPROPER OR INCOMPLETE CALIBRATION CAN
     * RESULT IN UNRELIABLE, INACCURATE, OR EMPTY ANALOG INPUT VALUES. UNDERTAKE RECALIBRATION AT YOUR OWN RISK.

     * @return The [ABaseQueueVal] of the command.
     */
    fun getCalibrationStatus(): ABaseQueueVal = QueueValue(
                Commands.GET_CALIBRATION_STATUS.ordinalCommandType)

    /**
     * Method to generate a [ABaseQueueVal] representing the command "WRITE_CAL_TEMP" with the given parameters.

     * WARNING: THIS COMMAND IS USED IN CALIBRATION. IT IS NOT DESIGNED FOR AVERAGE USERS AND SHOULD ONLY BE RUN BY
     * THOSE WITH BOTH THE PREREQUISITE KNOWLEDGE AND EQUIPMENT. IMPROPER OR INCOMPLETE CALIBRATION CAN
     * RESULT IN UNRELIABLE, INACCURATE, OR EMPTY ANALOG INPUT VALUES. UNDERTAKE RECALIBRATION AT YOUR OWN RISK.

     * @param temp  Temperature of the index.
     * *
     * @param index Number of the index.
     * *
     * @return he [ABaseQueueVal] of the command.
     */
    fun writeCalibrationTemperature(temp: Double, index: Int): ABaseQueueVal  = QueueValue(
                Commands.WRITE_CALIBRATION_TEMP.ordinalCommandType,
                Pair(Params.TEMPERATURE, temp.toString()),
                Pair(Params.INDEX, index.toByte()))

    /**
     * Method to generate a [ABaseQueueVal] representing the command "WRITE_SYSTEM_GAIN_CAL" with the given parameters.

     * WARNING: THIS COMMAND IS USED IN CALIBRATION. IT IS NOT DESIGNED FOR AVERAGE USERS AND SHOULD ONLY BE RUN BY
     * THOSE WITH BOTH THE PREREQUISITE KNOWLEDGE AND EQUIPMENT. IMPROPER OR INCOMPLETE CALIBRATION CAN
     * RESULT IN UNRELIABLE, INACCURATE, OR EMPTY ANALOG INPUT VALUES. UNDERTAKE RECALIBRATION AT YOUR OWN RISK.

     * @param value  correction value.
     * *
     * @param gain   gain of value.
     * *
     * @param rate   rate of value.
     * *
     * @param buffer buffer of value.
     * *
     * @param scale  scale of value.
     * *
     * @param temp   temp index of value.
     * *
     * @return he [ABaseQueueVal] of the command.
     */
    fun writeGainCalibrationValue(value: Float,
                                  gain: AAnalogInput.Gain,
                                  rate: AAnalogInput.Rate,
                                  buffer: AnalogInput_RevD.BufferState,
                                  scale: ATekdaqc.AnalogScale,
                                  temp: Int): ABaseQueueVal = QueueValue(
                Commands.WRITE_GAIN_CALIBRATION_VALUE.ordinalCommandType,
                Pair(Params.TEMPERATURE, temp),
                Pair(Params.VALUE, value),
                Pair(Params.BUFFER, buffer.name),
                Pair(Params.RATE, rate.rate),
                Pair(Params.GAIN, gain.gain),
                Pair(Params.SCALE, scale.scale))

    /**
     * Method to generate a [ABaseQueueVal] representing the command "WRITE_CAL_VALID".

     * WARNING: THIS COMMAND IS USED IN CALIBRATION. IT IS NOT DESIGNED FOR AVERAGE USERS AND SHOULD ONLY BE RUN BY
     * THOSE WITH BOTH THE PREREQUISITE KNOWLEDGE AND EQUIPMENT. IMPROPER OR INCOMPLETE CALIBRATION CAN
     * RESULT IN UNRELIABLE, INACCURATE, OR EMPTY ANALOG INPUT VALUES. UNDERTAKE RECALIBRATION AT YOUR OWN RISK.

     * @return The [ABaseQueueVal] of the command.
     */
    fun writeCalibrationValid(): ABaseQueueVal = QueueValue(
                Commands.WRITE_CALIBRATION_VALID.ordinalCommandType)

    /**
     * Method to generate a [ABaseQueueVal] representing the command "DISCONNECT".

     * @return The [ABaseQueueVal] of the command.
     */
    fun disconnect(): ABaseQueueVal = QueueValue(
            Commands.DISCONNECT.ordinalCommandType)

    /**
     * Method to generate a [ABaseQueueVal] representing the command "READ_DIGITAL_OUTPUTS".

     * @return The [ABaseQueueVal] of the command.
     */
    fun readDigitalOutput(): ABaseQueueVal = QueueValue(
            Commands.READ_DIGITAL_OUTPUT.ordinalCommandType)

    /**
     * Method to generate a [ABaseQueueVal] representing the command "ENTER_CALIBRATION_MODE".

     * WARNING: THIS COMMAND IS USED IN CALIBRATION. IT IS NOT DESIGNED FOR AVERAGE USERS AND SHOULD ONLY BE RUN BY
     * THOSE WITH BOTH THE PREREQUISITE KNOWLEDGE AND EQUIPMENT. IMPROPER OR INCOMPLETE CALIBRATION CAN
     * RESULT IN UNRELIABLE, INACCURATE, OR EMPTY ANALOG INPUT VALUES. UNDERTAKE RECALIBRATION AT YOUR OWN RISK.

     * @return The [ABaseQueueVal] of the command.
     */
    fun enterCalibrationMode(): ABaseQueueVal = QueueValue(Commands.ENTER_CALIBRATION_MODE.ordinalCommandType)

    /**
     * Method to generate a [ABaseQueueVal] representing the command "EXIT_CALIBRATION_MODE".

     * WARNING: THIS COMMAND IS USED IN CALIBRATION. IT IS NOT DESIGNED FOR AVERAGE USERS AND SHOULD ONLY BE RUN BY
     * THOSE WITH BOTH THE PREREQUISITE KNOWLEDGE AND EQUIPMENT. IMPROPER OR INCOMPLETE CALIBRATION CAN
     * RESULT IN UNRELIABLE, INACCURATE, OR EMPTY ANALOG INPUT VALUES. UNDERTAKE RECALIBRATION AT YOUR OWN RISK.

     * @return The [ABaseQueueVal] of the command.
     */
    fun exitCalibrationMode(): ABaseQueueVal = QueueValue(Commands.EXIT_CALIBRATION_MODE.ordinalCommandType)

    /**
     * Method to generate a [ABaseQueueVal] representing the command "WRITE_SERIAL_NUMBER" with the given parameters.

     * WARNING: THIS COMMAND IS USED IN CALIBRATION. IT IS NOT DESIGNED FOR AVERAGE USERS AND SHOULD ONLY BE RUN BY
     * THOSE WITH BOTH THE PREREQUISITE KNOWLEDGE AND EQUIPMENT. IMPROPER OR INCOMPLETE CALIBRATION CAN
     * RESULT IN UNRELIABLE, INACCURATE, OR EMPTY ANALOG INPUT VALUES. UNDERTAKE RECALIBRATION AT YOUR OWN RISK.

     * @param serial The serial number to write.
     * *
     * @return The [ABaseQueueVal] of the command.
     */
    fun writeSerialNumber(serial: String): ABaseQueueVal = QueueValue(
            Commands.READ_DIGITAL_OUTPUT.ordinalCommandType,
            Pair(Params.VALUE, serial))

    /**
     * Method to generate a [ABaseQueueVal] representing the command "WRITE_MAC_ADDRESS" with the given parameters.

     * WARNING: THIS COMMAND IS USED IN CALIBRATION. IT IS NOT DESIGNED FOR AVERAGE USERS AND SHOULD ONLY BE RUN BY
     * THOSE WITH BOTH THE PREREQUISITE KNOWLEDGE AND EQUIPMENT. IMPROPER OR INCOMPLETE CALIBRATION CAN
     * RESULT IN UNRELIABLE, INACCURATE, OR EMPTY ANALOG INPUT VALUES. UNDERTAKE RECALIBRATION AT YOUR OWN RISK.

     * @param mac The MAC address to write.
     * *
     * @return The [ABaseQueueVal] of the command.
     */
    fun writeFactoryMacAddress(mac: Long): ABaseQueueVal = QueueValue(
            Commands.SET_FACTORY_MAC_ADDR.ordinalCommandType,
            Pair(Params.VALUE, mac.toString() + ""))

    /**
     * Method to generate a [ABaseQueueVal] representing the command "NONE".

     * @return The [ABaseQueueVal] of the command.
     */
    fun none(): ABaseQueueVal = QueueValue(Commands.NONE.ordinalCommandType)

    /**
     * Method to generate a [ABaseQueueVal] representing the command "SYSTEM_CAL".

     * WARNING: THIS COMMAND IS USED IN CALIBRATION. IT IS NOT DESIGNED FOR AVERAGE USERS AND SHOULD ONLY BE RUN BY
     * THOSE WITH BOTH THE PREREQUISITE KNOWLEDGE AND EQUIPMENT. IMPROPER OR INCOMPLETE CALIBRATION CAN
     * RESULT IN UNRELIABLE, INACCURATE, OR EMPTY ANALOG INPUT VALUES. UNDERTAKE RECALIBRATION AT YOUR OWN RISK.

     * @return The [ABaseQueueVal] of the command.
     */
    fun systemCalibrate(): ABaseQueueVal = QueueValue(Commands.SYSTEM_CAL.ordinalCommandType)

    /**
     * Method to generate a [ABaseQueueVal] representing the command "LIST_ANALOG_INPUTS".

     * @return The [ABaseQueueVal] of the command.
     */
    fun listAnalogInputs(): ABaseQueueVal = QueueValue(
            Commands.LIST_ANALOG_INPUTS.ordinalCommandType)

    /**
     * Adds a [DigitalInput] as PWM input.
     *
     * @param input The Input number of the Digital Input.
     */
    fun addPWMInput(input: Int): ABaseQueueVal = QueueValue(
            Commands.ADD_PWM_INPUT.ordinalCommandType,
            Pair(Params.INPUT, input))

    /**
     * Adds a [DigitalInput] as PWM input.
     *
     * @param input The Digital Input to be activated.
     */
    fun addPWMInput(input: DigitalInput): ABaseQueueVal = QueueValue(
            Commands.ADD_PWM_INPUT.ordinalCommandType,
            Pair(Params.INPUT, input.channelNumber))

    /**
     * Removes a [DigitalInput] PWM input.
     *
     * @param input The Input number of the Digital Input.
     */
    fun removePWMInput(input: Int): ABaseQueueVal = QueueValue(
            Commands.REMOVE_PWM_INPUT.ordinalCommandType,
            Pair(Params.INPUT, input))

    /**
     * Reads the PWM status of the [DigitalInput]s.
     *
     * @param input The input to be read.
     * @param number The samples to read.
     */
    fun readPWMInputs(input: Int, number: Int): ABaseQueueVal = QueueValue(
            Commands.READ_PWM_INPUT.ordinalCommandType,
            Pair(Params.INPUT, input),
            Pair(Params.NUMBER, number))

    /**
     * Lists all added PWM Inputs
     */
    fun listPWMInputs(): ABaseQueueVal = QueueValue(
            Commands.LIST_PWM_INPUTS.ordinalCommandType)

    /**
     * Sets the output timer for the PWM Output timer.
     *
     * @param time The output timer.
     */
    fun setPWMOutputTimer(time: Long) = QueueValue(
            Commands.SET_PWM_OUTPUT_TIMER.ordinalCommandType)
}
package com.tenkiv.tekdaqc.communication.command.queue.values

import com.tenkiv.tekdaqc.communication.command.queue.Commands
import com.tenkiv.tekdaqc.communication.command.queue.Params
import com.tenkiv.tekdaqc.communication.command.queue.QueueUtil
import java.io.NotSerializableException
import java.io.ObjectInput
import java.io.ObjectOutput
import java.util.*

/**
 * Created by tenkiv on 3/10/17.
 */
class QueueValue: ABaseQueueVal {

    val parameters = ArrayList<Pair<Params, Any>>()

    constructor(): super()

    constructor(command: Byte): super(command)

    constructor(command: Byte, vararg params: Pair<Params, Any>) : this(command) {
        parameters.addAll(params)
    }

    //constructor() : this(Commands.NONE.ordinalCommandType)

    override fun generateCommandBytes(): ByteArray {
        val builder = StringBuilder()
        builder.append(Commands.getValueFromOrdinal(mCommandType).name)

        parameters.forEach {
            builder.append(QueueUtil.GENERAL_DELIMETER)
            builder.append(QueueUtil.PARAMETER_FLAG)
            builder.append(it.first.name)
            builder.append(QueueUtil.KEY_VALUE_SEPARATOR)
            builder.append(it.second.toString())
        }

        builder.append(QueueUtil.COMMAND_EOF)
        return builder.toString().toByteArray()
    }


    override fun writeExternal(out: ObjectOutput) {
        super.writeExternal(out)

        out.writeInt(parameters.size)
        parameters.forEach {
            out.writeByte(it.first.ordinal)
            out.writeObject(it.second.toString())
        }


    }

    override fun readExternal(`in`: ObjectInput) {
        super.readExternal(`in`)

        val size = `in`.readInt()
        (0 until size).forEach {
            parameters.add(
                    Pair(Params.getValueFromOrdinal(`in`.readByte()),
                            `in`.readObject() as? String ?: throw NotSerializableException())
            )
        }
    }
}
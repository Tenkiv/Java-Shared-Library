package com.tenkiv.tekdaqc.communication.command.queue.value

import com.tenkiv.tekdaqc.communication.command.queue.Commands
import com.tenkiv.tekdaqc.communication.command.queue.Params
import com.tenkiv.tekdaqc.communication.command.queue.values.QueueValue
import com.tenkiv.tekdaqc.serializeToAny
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.ShouldSpec

/**
 * Class to test QueueValue objects and functions
 */
class QueueValueSpec: ShouldSpec({
    "Queue Value Spec"{
        var queueVal: QueueValue
        should("Have default values"){
            queueVal = QueueValue(Commands.NONE.ordinalCommandType)
            queueVal.mCommandType shouldBe Commands.NONE.ordinalCommandType

            queueVal.parameters.isEmpty() shouldBe true
        }

        should("Serialize correctly"){
            queueVal = QueueValue(
                    Commands.ADD_ANALOG_INPUT.ordinalCommandType,
                    Pair(Params.INPUT,0),
                    Pair(Params.RATE,0),
                    Pair(Params.GAIN,0),
                    Pair(Params.BUFFER,0))

            queueVal.parameters.size shouldBe 4

            val obj = serializeToAny(queueVal)

            (obj is QueueValue) shouldBe true

            (obj as? QueueValue)?.mCommandType shouldBe Commands.ADD_ANALOG_INPUT.ordinalCommandType

            (obj as? QueueValue)?.parameters?.size shouldBe queueVal.parameters.size
        }
    }
})

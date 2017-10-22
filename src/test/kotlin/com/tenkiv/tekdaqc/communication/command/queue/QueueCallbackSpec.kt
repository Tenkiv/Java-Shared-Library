package com.tenkiv.tekdaqc.communication.command.queue

import com.tenkiv.tekdaqc.communication.tasks.ITaskComplete
import com.tenkiv.tekdaqc.hardware.ATekdaqc
import com.tenkiv.tekdaqc.serializeToAny
import io.kotlintest.matchers.plusOrMinus
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.ShouldSpec

/**
 * Class to test QueueCallbacks
 */
class QueueCallbackSpec : ShouldSpec({
    "Queue Callback Spec"{

        var taskCompleteCheck = false

        var taskFailedCheck = false

        val callbackListener = object : ITaskComplete {
            override fun onTaskSuccess(tekdaqc: ATekdaqc?) {
                taskCompleteCheck = true
            }

            override fun onTaskFailed(tekdaqc: ATekdaqc?) {
                taskFailedCheck = true
            }
        }

        var queueCallback: QueueCallback

        should("Initialize correctly") {
            queueCallback = QueueCallback(callbackListener)

            queueCallback = QueueCallback(listOf(callbackListener))

            queueCallback = QueueCallback(listOf(callbackListener),false)
        }

        should("Have sane values"){
            queueCallback = QueueCallback()

            queueCallback.isInternalDelimiter shouldBe false

            queueCallback.removeCallback(callbackListener)

            queueCallback.addCallback(callbackListener)

            queueCallback.removeCallback(callbackListener)

        }

        should("Send callbacks successfully"){
            queueCallback = QueueCallback()

            queueCallback.addCallback(callbackListener)

            queueCallback.success(null)

            queueCallback.failure(null)

            taskCompleteCheck shouldBe true

            taskFailedCheck shouldBe true
        }

        should("Serialize"){

            queueCallback = QueueCallback()

            queueCallback.uid = UID

            queueCallback.uid shouldBe UID.plusOrMinus(UID_VARIANCE)

            val obj = serializeToAny(queueCallback)
            (obj is QueueCallback) shouldBe true
            (obj as? QueueCallback)?.uid shouldBe UID.plusOrMinus(UID_VARIANCE)
        }
    }
}){
    companion object {

        const val UID = 10.0

        const val UID_VARIANCE = 0.0001
    }
}
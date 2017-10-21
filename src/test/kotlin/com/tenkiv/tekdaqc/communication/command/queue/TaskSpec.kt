package com.tenkiv.tekdaqc.communication.command.queue

import com.tenkiv.tekdaqc.communication.tasks.ITaskComplete
import com.tenkiv.tekdaqc.hardware.ATekdaqc
import com.tenkiv.tekdaqc.hardware.CommandBuilder
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.ShouldSpec

/**
 * Class to test Tasks
 */
class TaskSpec : ShouldSpec({
    "Task Spec"{


        val callbackListener = object : ITaskComplete {
            override fun onTaskSuccess(tekdaqc: ATekdaqc?) {
            }

            override fun onTaskFailed(tekdaqc: ATekdaqc?) {
            }
        }

        var task: Task

        should("Initialize Successfully") {
            task = Task()

            task.commandList.size shouldBe 1
            task.listenersList.size shouldBe 0

            task = Task(callbackListener, List(50, { CommandBuilder.none() }))
            task.commandList.size shouldBe 51
            task.listenersList.size shouldBe 1

            task = Task(listOf(callbackListener), List(50, { CommandBuilder.none() }))
            task.commandList.size shouldBe 51
            task.listenersList.size shouldBe 1

            task = Task(List(50, { CommandBuilder.none() }))
            task.commandList.size shouldBe 51
            task.listenersList.size shouldBe 0

            task = Task(callbackListener)
            task.commandList.size shouldBe 1
            task.listenersList.size shouldBe 1

            task = Task(listOf(callbackListener))
            task.commandList.size shouldBe 1
            task.listenersList.size shouldBe 1

        }

        should("Check adding and removing") {
            task = Task()
            for (i in 1..50) {
                task.addCommand(CommandBuilder.none())
                task.commandList.size shouldBe i + 1
            }

            task.addListener(callbackListener)
            task.listenersList.size shouldBe 1

            task.addCommands(List(20, { CommandBuilder.none() }))
            task.commandList.size shouldBe 71

            val command = CommandBuilder.none()
            task.addCommand(command)
            task.commandList.size shouldBe 72

            task.removeCommand(command)
            task.commandList.size shouldBe 71

            task.removeListener(callbackListener)
            task.listenersList.size shouldBe 0
        }
    }
})

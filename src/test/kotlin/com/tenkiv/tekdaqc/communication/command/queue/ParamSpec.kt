package com.tenkiv.tekdaqc.communication.command.queue

import io.kotlintest.matchers.shouldEqual
import io.kotlintest.specs.ShouldSpec

/**
 * Class to test functions of Param class.
 */
class ParamSpec: ShouldSpec({
    "Param Spec"{
        val someParam = Params.INPUT

        should("Have identical values"){
            someParam shouldEqual Params.getValueFromOrdinal(0)

            someParam.ordinalCommandType shouldEqual 0.toByte()
        }
    }
})

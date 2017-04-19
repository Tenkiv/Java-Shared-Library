package com.tenkiv.tekdaqc.utility

import java.util.*

/**
 * Created by tenkiv on 2/15/17.
 */

fun Timer.reprepare(): Timer{
    this.cancel()
    this.purge()

    return Timer()
}
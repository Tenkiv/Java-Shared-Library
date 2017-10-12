package com.tenkiv.tekdaqc.utility;

/**
 * Listener for errors which may require board reset.
 */
public interface CriticalErrorListener {

    void onTekdaqcCriticalError(TekdaqcCriticalError criticalError);

}

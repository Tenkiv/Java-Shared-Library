package com.tenkiv.tekdaqc.communication.data_points;

import java.util.List;

/**
 * Provides a set of methods which code can implement to generate {@link DataPoint}s from a data source.
 *
 * @author Tenkiv (software@tenkiv.com)
 * @since v1.0.0.0
 */
public interface IDataPointFactory {

    /**
     * Create a {@link List} of {@link DataPoint}s from data.
     *
     * @return {@link List} of {@link DataPoint}s.
     */
    public DataPoint toDataPoints();
}

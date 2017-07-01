package org.codetome.taskomat.core;

/**
 * Represents the possible result types of a {@link Job}.
 */
public enum JobResultType {
    /**
     * Job successfully completed
     */
    SUCCESS,
    /**
     * Job failed
     */
    SERVICE_ERROR,
    /**
     * Job was not run due to a failure of a previous Job
     */
    JOB_IGNORED
}

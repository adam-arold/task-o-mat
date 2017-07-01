package org.codetome.taskomat.core;

import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static org.codetome.taskomat.core.JobResultType.JOB_IGNORED;
import static org.codetome.taskomat.core.JobResultType.SUCCESS;

/**
 * Represents the (optional) result of a {@link Job}.
 */
public class JobResult<T> {
    private Optional<T> result = empty();
    private Optional<Throwable> throwable = empty();
    private JobResultType jobResultType = JOB_IGNORED;
    private Optional<String> jobName = empty();

    public JobResult() {
    }

    public JobResult(final T result) {
        this.result = ofNullable(result);
    }

    public Optional<T> getResult() {
        return result;
    }

    public Optional<String> getJobName() {
        return jobName;
    }

    public boolean isSuccessful() {
        return SUCCESS.equals(jobResultType);
    }

    public Optional<String> getErrorMessage() {
        return throwable.get().getCause() == null ? empty() : ofNullable(throwable.get().getCause().getMessage());
    }

    Throwable getThrowable() {
        return throwable.get().getCause();
    }

    JobResultType getJobResultType() {
        return jobResultType;
    }

    void setJobName(final String jobName) {
        this.jobName = ofNullable(jobName);
    }

    void setJobResultType(final JobResultType jobResultType) {
        this.jobResultType = jobResultType;
    }

    void setException(final Throwable throwable) {
        this.throwable = ofNullable(throwable);
    }
}

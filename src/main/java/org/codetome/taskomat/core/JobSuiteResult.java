package org.codetome.taskomat.core;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;

/**
 * .
 */
public class JobSuiteResult {

    private final int failedJobCount;
    private final Map<Job, JobResult<?>> jobResults;
    private final int totalJobCount;

    /**
     * .
     */
    public JobSuiteResult(final Map<Job, JobResult<?>> jobResults, final int totalJobCount) {
        this.totalJobCount = totalJobCount;
        this.jobResults = jobResults;
        this.failedJobCount = totalJobCount - jobResults.size();
    }

    public int getTotalJobCount() {
        return totalJobCount;
    }

    public int getFailedJobCount() {
        return failedJobCount;
    }

    public boolean hasFailedJobs() {
        return failedJobCount > 0;
    }

    public Map<Job, JobResult<?>> getJobResults() {
        return jobResults; // TODO: immutable
    }

    /**
     * .
     */
    public List<Throwable> fetchAllErrors() {
        return jobResults.values().stream()
                .filter(jobResult -> !jobResult.isSuccessful())
                .map(jobResult -> jobResult.getThrowable())
                .collect(toList());
    }

    public List<JobResult<?>> fetchFailedJobResults() {
        return jobResults.values().stream()
                .filter(jobResult -> !jobResult.isSuccessful()).collect(toList());
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> getResultOf(final Job job) {
        final JobResult<?> jobResult = jobResults.get(job);
        return (Optional<T>) (jobResult == null ? empty() : jobResults.get(job).getResult());
    }

    public Optional<Throwable> getExceptionOf(final Job job) {
        final JobResult<?> jobResult = jobResults.get(job);
        return jobResult == null ? empty() : of(jobResults.get(job).getThrowable());
    }
}

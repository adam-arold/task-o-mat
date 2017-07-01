package org.codetome.taskomat.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static java.util.concurrent.Executors.newCachedThreadPool;
import static java.util.stream.Collectors.toList;
import static org.codetome.taskomat.core.JobResultType.SERVICE_ERROR;
import static org.codetome.taskomat.core.JobResultType.SUCCESS;

/**
 * This service can be used to run {@link JobSuite}s.
 */
public class JobExecutorService {

    /**
     * Runs a {@link JobSuite}. Blocks until either all {@link Job}s are
     * completed or an {@link Exception} was thrown.
     */
    public JobSuiteResult runJobSuite(final JobSuite jobSuite) {
        final ExecutorService executorService = newCachedThreadPool();
        final ExecutorCompletionService<JobResult<?>> executorCompletionService = new ExecutorCompletionService<>(executorService);
        final Map<Job, JobResult<?>> jobResults = new HashMap<>();
        final Map<Future<JobResult<?>>, Job> tempResults = new HashMap<>();
        while (jobSuite.hasJobsLeft()) {
            final Queue<Job> nextJobs = jobSuite.getNextJobs();
            int currentJobCount = nextJobs.size();
            nextJobs.forEach(job ->
            {
                final List<JobResult<?>> dependencyResults = jobSuite.getDependenciesOf(job)
                        .stream()
                        .map(currentJob -> jobResults.get(currentJob))
                        .collect(toList());
                job.setDependencyResults(dependencyResults);
                tempResults.put(executorCompletionService.submit(job), job);
            });
            int exceptionCount = 0;
            while (currentJobCount > 0) {
                JobResult<?> jobResult;
                Future<JobResult<?>> resultFuture = null;
                try {
                    resultFuture = executorCompletionService.take();
                    jobResult = resultFuture.get();
                    jobResult.setJobResultType(SUCCESS);
                    jobResults.put(tempResults.get(resultFuture), jobResult);
                } catch (InterruptedException | ExecutionException e) {
                    exceptionCount++;
                    jobResult = new JobResult<>();
                    jobResult.setException(e);
                    jobResult.setJobResultType(SERVICE_ERROR);
                    jobResults.put(tempResults.get(resultFuture), jobResult);
                }
                jobResult.setJobName(tempResults.get(resultFuture).getName());
                currentJobCount--;
            }
            if (exceptionCount > 0) {
                break;
            }
        }
        executorService.shutdown();
        return new JobSuiteResult(jobResults, jobSuite.getTotalJobCount());
    }
}

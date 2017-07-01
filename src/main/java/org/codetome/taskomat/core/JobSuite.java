package org.codetome.taskomat.core;


import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import static java.util.Collections.unmodifiableSet;

/**
 * Contains {@link Job}s in the correct (execution) order. It can be used in a
 * {@link JobExecutorService} to run the {@link Job}s. Use a
 * {@link JobSuiteBuilder} to create {@link JobSuite}s.
 */
public class JobSuite {
    /**
     * Contains the {@link Job}s as key-value pairs where the key is a
     * {@link Job} and the value is a {@link Set} of {@link Job}s which will be
     * executed after the key {@link Job}.
     */
    private final Map<Job, Set<Job>> dependencyGraph;

    /**
     * Contains the {@link Job}s in execution order.
     */
    private final Queue<Job> orderedJobs;

    private final Set<Job> finishedJobs = new HashSet<>();

    private final Job source;

    private final int totalJobCount;

    @SuppressWarnings({"PMD.UnusedLocalVariable", "unused"})
    JobSuite(final Map<Job, Set<Job>> dependencyGraph, final Queue<Job> orderedJobs, final Job source) {
        this.totalJobCount = orderedJobs.size() - 1; // because of technical job
        this.source = source;
        this.dependencyGraph = dependencyGraph;
        this.orderedJobs = orderedJobs;
        if (orderedJobs.peek() == source) {
            final Job notUsed = orderedJobs.poll(); // we just need to pop it
        }
        finishedJobs.add(source);
    }

    /**
     * Returns a sequence of {@link Job}s which can be run in parallel. Each
     * call to this method returns a new sequence until the {@link JobSuite} is
     * emptied.
     */
    Queue<Job> getNextJobs() {
        if (orderedJobs.isEmpty()) {
            throw new IllegalStateException("All jobs are already completed in this JobSuite");
        }
        Queue<Job> nextJobs = new LinkedList<>();
        Iterator<Job> orderedJobsIter = orderedJobs.iterator();
        Job nextJob = orderedJobsIter.next();
        while (nextJob != null && allItsDependenciesAreFinished(nextJob)) {
            nextJobs.add(nextJob);
            orderedJobsIter.remove();
            nextJob = orderedJobsIter.hasNext() ? orderedJobsIter.next() : null;
        }
        finishedJobs.addAll(nextJobs);
        return nextJobs;
    }

    int getTotalJobCount() {
        return totalJobCount;
    }

    boolean hasJobsLeft() {
        return !orderedJobs.isEmpty();
    }

    Set<Job> getDependenciesOf(final Job job) {
        return dependencyGraph.get(job).contains(source) ? Collections.emptySet() : unmodifiableSet(dependencyGraph.get(job));
    }

    private boolean allItsDependenciesAreFinished(final Job job) {
        if (job == null) {
            return true;
        }
        Set<Job> jobsNeedToBeFinished = new HashSet<>(dependencyGraph.get(job));
        jobsNeedToBeFinished.removeAll(finishedJobs);
        return jobsNeedToBeFinished.size() == 0;
    }

}

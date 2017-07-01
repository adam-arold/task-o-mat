package org.codetome.taskomat.core;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;

/**
 * Responsible for building a {@link JobSuite}. After the {@link JobSuite} is
 * complete this builder can't be used anymore.
 */
public class JobSuiteBuilder {
    private static final String JOB_NAME = "JOB_NAME";
    private static final Job TECHNICAL_ROOT_JOB = new Job(JOB_NAME) {

        @Override
        public JobResult<String> call() throws Exception {
            setDependencyResults(emptyList());
            return new JobResult<>(toString());
        }

        @Override
        public String toString() {
            return "TECHNICAL_ROOT_JOB";
        }
    };
    /**
     * Contains the {@link Job}s as key-value pairs where the key is a
     * {@link Job} and the value is a {@link Set} of {@link Job}s which the key
     * {@link Job} depends on.
     */
    private final Map<Job, Set<Job>> dependencyGraph = new HashMap<>();
    /**
     * Contains the {@link Job}s as key-value pairs where the key is a
     * {@link Job} and the value is a {@link Set} of {@link Job}s which will be
     * executed after the key {@link Job}.
     */
    private Map<Job, Set<Job>> jobOrderGraph;

    private final Job source = TECHNICAL_ROOT_JOB;
    /**
     * Contains the {@link Job}s in execution order.
     */
    private Queue<Job> topologicalOrder;

    private volatile boolean isBuilding = false;

    /**
     * Adds a {@link Job} without a dependency.
     */
    public JobSuiteBuilder addJob(final Job job) {
        return addJob(job, new Job[]{TECHNICAL_ROOT_JOB});
    }

    /**
     * Adds a job with dependencies. If job <code>A</code> has a dependency of
     * job <code>B</code> it means that <code>B</code> must be completed before
     * <code>A</code> can be started since <code>A</code> uses the result of
     * <code>B</code>.
     */
    public JobSuiteBuilder addJob(final Job job, final Job... jobDependencies) {
        if (isBuilding) {
            throw new RuntimeException("This builder is used up. Create a new one!");
        } else {
            dependencyGraph.putIfAbsent(job, new LinkedHashSet<>());
            dependencyGraph.get(job).addAll(asList(jobDependencies));
        }
        return this;
    }

    /**
     * Builds a {@link JobSuite} which contains all added {@link Job}s in the
     * correct (execution) order.
     *
     * @return {@link JobSuite} or throws {@link CircularDependencyException} if a circular dependency is detected.
     */
    public JobSuite build() {
        isBuilding = true;
        jobOrderGraph = reverseGraph(dependencyGraph);
        topologicalOrder = doTopologySort(copyGraph(dependencyGraph), copyGraph(jobOrderGraph));
        return new JobSuite(dependencyGraph, topologicalOrder, source);
    }

    private Queue<Job> doTopologySort(final Map<Job, Set<Job>> dependencyGraph, final Map<Job, Set<Job>> jobOrderGraph) {
        final Queue<Job> result = new LinkedList<>();
        final Queue<Job> queue = new LinkedList<>(singleton(TECHNICAL_ROOT_JOB));
        while (!queue.isEmpty()) {
            final Job currentNode = queue.poll();
            result.add(currentNode);
            final Iterator<Job> adjacentNodesIterator = jobOrderGraph.getOrDefault(currentNode, new LinkedHashSet<>()).iterator();
            while (adjacentNodesIterator.hasNext()) {
                final Job adjacentNode = adjacentNodesIterator.next();
                adjacentNodesIterator.remove();
                final Set<Job> dependsOn = dependencyGraph.getOrDefault(adjacentNode, new LinkedHashSet<>());
                dependsOn.remove(currentNode);
                if (dependsOn.isEmpty()) {
                    queue.add(adjacentNode);
                }
            }
            if (jobOrderGraph.getOrDefault(currentNode, new LinkedHashSet<>()).isEmpty()) {
                jobOrderGraph.remove(currentNode);
            }
        }
        if (jobOrderGraph.size() > 0) {
            throw new CircularDependencyException("Graph " + dependencyGraph + " has circular dependencies!");
        } else {
            return result;
        }
    }

    private Map<Job, Set<Job>> copyGraph(final Map<Job, Set<Job>> graph) {
        final Map<Job, Set<Job>> copy = new HashMap<>();
        graph.keySet().forEach(key ->
        {
            copy.put(key, new LinkedHashSet<>());
            graph.get(key).forEach(value -> copy.get(key).add(value));
        });
        return copy;
    }

    private Map<Job, Set<Job>> reverseGraph(final Map<Job, Set<Job>> graph) {
        final Map<Job, Set<Job>> lookup = new HashMap<>();
        graph.keySet().forEach(value ->
                graph.get(value).forEach(key ->
                {
                    lookup.putIfAbsent(key, new LinkedHashSet<>());
                    lookup.get(key).add(value);
                }));
        return lookup;
    }
}

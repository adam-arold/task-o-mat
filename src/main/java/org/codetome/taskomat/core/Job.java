package org.codetome.taskomat.core;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Callable;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.UUID.randomUUID;

/**
 * Represents a {@link Job} which is an abstraction over the {@link Callable}
 * interface. It contains the results of all {@link Job}s whose results are
 * needed in this {@link Job}.
 */
public abstract class Job implements Callable<JobResult<?>> {

    private final UUID uuid = randomUUID();
    private final String name;

    private List<JobResult<?>> dependencyResults = emptyList();

    public Job(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<JobResult<?>> getDependencyResults() {
        return unmodifiableList(dependencyResults);
    }

    @SuppressWarnings("unchecked")
    public <T> T getDependencyResult(final int parameterIndex) {
        return (T) dependencyResults.get(parameterIndex).getResult().get();
    }

    void setDependencyResults(final List<JobResult<?>> dependencyResults) {
        this.dependencyResults = dependencyResults;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Job job = (Job) obj;
        return Objects.equals(uuid, job.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
}

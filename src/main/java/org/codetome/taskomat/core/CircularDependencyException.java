package org.codetome.taskomat.core;

/**
 * Thrown if the topological sort detects a circular dependency in the DAG.
 */
public class CircularDependencyException extends RuntimeException {
    private static final long serialVersionUID = -4133518769724603211L;

    public CircularDependencyException(String message) {
        super(message);
    }
}

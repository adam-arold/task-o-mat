package org.codetome.taskomat.core;

import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.MockitoAnnotations.initMocks;

public class JobSuiteBuilderTest {

    private JobSuiteBuilder underTest;

    @Mock
    private Job dependentJobMock;

    @Mock
    private Job dependencyJobMock0;

    @Mock
    private Job dependencyJobMock1;

    @Mock
    private Job a;
    @Mock
    private Job b;
    @Mock
    private Job c;
    @Mock
    private Job d;
    @Mock
    private Job e;
    @Mock
    private Job f;
    @Mock
    private Job g;
    @Mock
    private Job h;

    @BeforeMethod
    public void setUp() {
        initMocks(this);
        underTest = new JobSuiteBuilder();
    }

    @Test
    public void shouldReturnJobSuiteWhenBuildCalled() {
        JobSuite result = underTest.build();
        assertThat(result).isNotNull();
    }

    @Test(expectedExceptions = CircularDependencyException.class)
    public void shouldThrowExceptionWhenBuildIsCalledWithCircularJobDependency() {
        underTest.addJob(dependentJobMock, dependencyJobMock0).addJob(dependencyJobMock0, dependentJobMock);
        underTest.build();
    }

    @Test(expectedExceptions = CircularDependencyException.class)
    public void shouldThrowExceptionWhenBuildIsCalledWithComplexCircularJobDependency() {
        underTest.addJob(dependentJobMock, dependencyJobMock0).addJob(dependencyJobMock0, dependencyJobMock1)//
                .addJob(dependencyJobMock1, dependentJobMock);
        underTest.build();
    }

    @Test(expectedExceptions = CircularDependencyException.class)
    public void shouldThrowExceptionWhenBuildIsCalledWithEvenMoreComplexCircularJobDependency() {
        underTest = new JobSuiteBuilder();
        underTest.addJob(f, c)//
                .addJob(g, c, a, d, e)//
                .addJob(h, e)//
                .addJob(c, a)//
                .addJob(d, a, b)//
                .addJob(e, b)//
                .addJob(a)//
                .addJob(b)//
                .addJob(a, g)//
                .build();
    }
}

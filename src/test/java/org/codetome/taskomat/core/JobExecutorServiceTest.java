package org.codetome.taskomat.core;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class JobExecutorServiceTest {

    private static final String EXPECTED_DEPENDENCY_JOB_RESULT = "FOOBAR";
    private static final String EXPECTED_JOB_RESULT = "WOMBAT";
    private static final String EXPECTED_DEPENDENT_JOB_RESULT = EXPECTED_DEPENDENCY_JOB_RESULT + " " + EXPECTED_JOB_RESULT;

    @InjectMocks
    private JobExecutorService underTest;

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
    }

    @Test
    public void shouldRunJobWithoutErrorAndCorrectResultWhenRunJobSuiteIsCalled() {
        final Job testJob = new Job("testJob") {

            @Override
            public JobResult<String> call() throws Exception {
                return new JobResult<String>(EXPECTED_JOB_RESULT);
            }
        };
        final JobSuite jobSuite = new JobSuiteBuilder().addJob(testJob).build();

        final JobSuiteResult result = underTest.runJobSuite(jobSuite);

        assertThat(result.hasFailedJobs()).isEqualTo(false);
        assertThat(result.getResultOf(testJob).get()).isEqualTo(EXPECTED_JOB_RESULT);
    }

    @Test
    public void shouldRunJobWithDependencyWithoutErrorAndCorrectResultWhenRunJobSuiteIsCalled() {
        final Job dependencyJob = new Job("dependencyJob") {

            @Override
            public JobResult<String> call() throws Exception {
                return new JobResult<String>(EXPECTED_DEPENDENCY_JOB_RESULT);
            }
        };
        final Job dependentJob = new Job("dependentJob") {

            @Override
            public JobResult<String> call() throws Exception {
                final String dependencyResult = this.<String>getDependencyResult(0);
                return new JobResult<String>(dependencyResult + " " + EXPECTED_JOB_RESULT);
            }
        };
        final JobSuite jobSuite = new JobSuiteBuilder().addJob(dependentJob, dependencyJob).addJob(dependencyJob).build();

        final JobSuiteResult result = underTest.runJobSuite(jobSuite);

        assertThat(result.hasFailedJobs()).isEqualTo(false);
        assertThat(result.getResultOf(dependentJob).get()).isEqualTo(EXPECTED_DEPENDENT_JOB_RESULT);
        assertThat(result.getResultOf(dependencyJob).get()).isEqualTo(EXPECTED_DEPENDENCY_JOB_RESULT);
    }

    @Test
    public void shouldStopAfterDependencyJobIsFailedWhenRunJobSuiteIsCalled() {
        final Job dependencyJob = new Job("dependencyJob") {

            @Override
            public JobResult<String> call() throws Exception {
                throw new RuntimeException();
            }
        };
        final Job dependentJob = new Job("dependentJob") {

            @Override
            public JobResult<String> call() throws Exception {
                final String dependencyResult = this.<String>getDependencyResult(0);
                return new JobResult<String>(dependencyResult + " " + EXPECTED_JOB_RESULT);
            }
        };
        final JobSuite jobSuite = new JobSuiteBuilder().addJob(dependentJob, dependencyJob).addJob(dependencyJob).build();

        final JobSuiteResult result = underTest.runJobSuite(jobSuite);

        assertThat(result.hasFailedJobs()).isEqualTo(true);
        assertThat(result.getFailedJobCount()).isEqualTo(1);
        assertThat(result.getResultOf(dependentJob).isPresent()).isEqualTo(false);
        assertThat(result.getResultOf(dependencyJob).isPresent()).isEqualTo(false);

    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void shouldStopAfterDependencyJobIsFailedWhenRunJobSuiteIsCalledWithComplexJobSuite() throws Exception {
        final JobSuite jobSuite = new JobSuiteBuilder()//
                .addJob(f, c)//
                .addJob(g, c, a, d, e)//
                .addJob(h, e)//
                .addJob(c, a)//
                .addJob(d, a, b)//
                .addJob(e, b)//
                .addJob(a)//
                .addJob(b)//
                .build();

        when(a.call()).thenReturn(new JobResult(1));
        when(b.call()).thenReturn(new JobResult(2));
        when(c.call()).thenThrow(RuntimeException.class);
        when(d.call()).thenReturn(new JobResult(3));
        when(e.call()).thenReturn(new JobResult(4));

        final JobSuiteResult result = underTest.runJobSuite(jobSuite);

        assertThat(result.hasFailedJobs()).isEqualTo(true);
        assertThat(result.getResultOf(a).get()).isEqualTo(1);
        assertThat(result.getResultOf(b).get()).isEqualTo(2);
        assertThat(result.getResultOf(d).get()).isEqualTo(3);
        assertThat(result.getResultOf(e).get()).isEqualTo(4);
        assertThat(result.getExceptionOf(c).get()).isInstanceOf(RuntimeException.class);
    }
}

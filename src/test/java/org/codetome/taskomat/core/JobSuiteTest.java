package org.codetome.taskomat.core;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Queue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.MockitoAnnotations.initMocks;

public class JobSuiteTest {

    private final Job a = new Job("a") {
        @Override
        public JobResult<?> call() throws Exception {
            return null;
        }
    };
    private final Job b = new Job("b") {
        @Override
        public JobResult<?> call() throws Exception {
            return null;
        }
    };
    private final Job c = new Job("c") {
        @Override
        public JobResult<?> call() throws Exception {
            return null;
        }
    };
    private final Job d = new Job("d") {
        @Override
        public JobResult<?> call() throws Exception {
            return null;
        }
    };
    private final Job e = new Job("e") {
        @Override
        public JobResult<?> call() throws Exception {
            return null;
        }
    };
    private final Job f = new Job("f") {
        @Override
        public JobResult<?> call() throws Exception {
            return null;
        }
    };
    private final Job g = new Job("g") {
        @Override
        public JobResult<?> call() throws Exception {
            return null;
        }
    };
    private final Job h = new Job("h")

    {
        @Override
        public JobResult<?> call() throws Exception {
            return null;
        }
    };
    private JobSuite underTest;

    @BeforeMethod
    public void setUp() {
        initMocks(this);
        initJobSuiteBuilder();
    }

    private void initJobSuiteBuilder() {
        underTest = new JobSuiteBuilder()//
                .addJob(f, c)//
                .addJob(g, c, a, d, e)//
                .addJob(h, e)//
                .addJob(c, a)//
                .addJob(d, a, b)//
                .addJob(e, b)//
                .addJob(a)//
                .addJob(b)//
                .build();
    }

    @Test
    public void shouldReturnCorrectNextJobsWhenEmptyingJobSuite() {
        final Queue<Job> first = underTest.getNextJobs();

        assertThat(first).hasSize(2);
        assertThat(first).containsOnly(a, b);

        final Queue<Job> second = underTest.getNextJobs();

        assertThat(second).hasSize(3);
        assertThat(second).containsOnly(c, d, e);

        final Queue<Job> third = underTest.getNextJobs();

        assertThat(third).hasSize(3);
        assertThat(third).containsOnly(f, g, h);

    }

    @Test
    public void shouldReturnFalseForHasFinishedWhenAllJobsAreExecuted() {
        underTest.getNextJobs();
        underTest.getNextJobs();
        underTest.getNextJobs();
        assertThat(underTest.hasJobsLeft()).isEqualTo(false);
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void shouldThrowExceptionWhenNoMoreJobsAreLeft() {
        underTest.getNextJobs();
        underTest.getNextJobs();
        underTest.getNextJobs();
        underTest.getNextJobs();
    }

    @Test(dataProvider = "getWombat")
    public void shouldReturnDependenciesOfAJobWhenGetDependenciesOfCalled(final Job job, final Job[] deps) {
        assertThat(underTest.getDependenciesOf(job)).hasSize(deps.length);
        assertThat(underTest.getDependenciesOf(job)).containsOnly(deps);
    }

    @DataProvider
    public Object[][] getWombat() {
        setUp();
        // @formatter:off
        return new Object[][]{
                {f, new Job[]{c}},
                {g, new Job[]{c, a, d, e}},
                {h, new Job[]{e}},
                {c, new Job[]{a}},
                {d, new Job[]{a, b}},
                {e, new Job[]{b}}};
        // @formatter:on
    }

}

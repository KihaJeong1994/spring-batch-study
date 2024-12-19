package com.example.springbatch.runner;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.batch.job.enabled", havingValue = "false")
public class JobRunner implements ApplicationRunner { // spring boot가 실행되면서 자동으로 실행

    private final JobLauncher jobLauncher;

    private final Job helloJob;

    @Override // 같은 JobParameters로 두번 실행하면 JobInstanceAlreadyCompleteException 예외 발생
    public void run(ApplicationArguments args) throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("name", "user2")
                .toJobParameters();
        jobLauncher.run(helloJob, jobParameters);
    }
}

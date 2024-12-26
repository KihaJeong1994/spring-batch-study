package com.example.springbatch.config;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class HelloJobConfiguration { // Job 정의

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
//    private final BatchConfigurer batchConfigurer;

    @Bean
    public Job helloJob() {
        // JobLauncer의 TaskExecutor를 비동기로 설정하고 싶다면, 다음과 같이 BatchConfigurer 의 JobLauncer를 가져와서 형변환해서 셋팅해야함
        // JobLauncer를 의존성 주입받아서 활용 시, 해당 빈을 실제 SimpleJobLauncer가 아니라 Proxy 객체이기 때문
//        SimpleJobLauncher jobLauncher = (SimpleJobLauncher) batchConfigurer.getJobLauncher();
//        jobLauncher.setTaskExecutor(new SimpleAsyncTaskExecutor());
        return jobBuilderFactory.get("helloJob")
                .start(helloStep1())
                .next(helloStep2())
                .build();

    }


    @Bean
    public Step helloStep1() {
        return stepBuilderFactory.get("helloStep1")
                .tasklet(((contribution, chunkContext) -> {
                    JobParameters jobParameters = contribution.getStepExecution().getJobParameters();
                    jobParameters.getString("name");
                    jobParameters.getLong("seq");
                    jobParameters.getDate("date");
                    jobParameters.getDouble("age");

                    Map<String, Object> jobParametersMap = chunkContext.getStepContext().getJobParameters();

                    System.out.println("=======================");
                    System.out.println(" >> Hello Spring Batch");
                    System.out.println("=======================");

                    ExecutionContext jobExecutionContext = contribution.getStepExecution().getJobExecution().getExecutionContext();
                    String jobName = chunkContext.getStepContext().getJobName();
                    jobExecutionContext.putString("jobName", jobName);
//                    throw new RuntimeException(); // Tasklet 수행 도중 에러가 발생하여도, ExecutionContext 안에 넣은 key, value는 롤백되지 않는다! -> 실패 이후 재시작할 때도 활용 가능

                    ExecutionContext stepExecutionContext = contribution.getStepExecution().getExecutionContext();
                    String stepName = chunkContext.getStepContext().getStepName();
                    stepExecutionContext.putString("stepName", stepName);

                    return RepeatStatus.FINISHED; // step은 Tasklet을 기본적으로 무한반복시킴
                }))
                .build();
    }

    @Bean
    public Step helloStep2() {
        return stepBuilderFactory.get("helloStep2")
                .tasklet(((contribution, chunkContext) -> {
                    System.out.println("=======================");
                    System.out.println(" >> step 2 was executed");
                    System.out.println("=======================");

                    ExecutionContext jobExecutionContext = contribution.getStepExecution().getJobExecution().getExecutionContext();
                    String jobName = jobExecutionContext.getString("jobName");
                    System.out.println("jobName = " + jobName);

                    ExecutionContext stepExecutionContext = contribution.getStepExecution().getExecutionContext();
                    Object stepName = stepExecutionContext.get("stepName");
                    System.out.println("stepName = " + stepName); // StepExecutionContext는 공유 안되는 것 확인 가능
                    return RepeatStatus.FINISHED; // step은 Tasklet을 기본적으로 무한반복시킴
                }))
                .build();
    }
}

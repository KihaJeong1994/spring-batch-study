package com.example.springbatch.config;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class HelloJobConfiguration { // Job 정의

  private final JobBuilderFactory jobBuilderFactory;
  private final StepBuilderFactory stepBuilderFactory;

  @Bean
  public Job helloJob() {
    return jobBuilderFactory.get("helloJob")
        .start(helloStep1())
        .next(helloStep2())
        .build();

  }


  @Bean
  public Step helloStep1() {
    return stepBuilderFactory.get("helloStep1")
        .tasklet(((contribution, chunkContext) -> {
          System.out.println("=======================");
          System.out.println(" >> Hello Spring Batch");
          System.out.println("=======================");
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
          return RepeatStatus.FINISHED; // step은 Tasklet을 기본적으로 무한반복시킴
        }))
        .build();
  }
}

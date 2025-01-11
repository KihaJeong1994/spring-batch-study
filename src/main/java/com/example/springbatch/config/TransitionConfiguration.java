package com.example.springbatch.config;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class TransitionConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job transitionJob() {
        return jobBuilderFactory.get("transitionJob")
                .start(transitionFlow())
                .build()
                .incrementer(new RunIdIncrementer())
                .build();

    }

    @Bean
    public Flow transitionFlow() {
        return new FlowBuilder<Flow>("transitionFlow")
                .start(transitionStep1())
                .on("FAILED")
                .to(transitionStep2())
                .on("*").stop()
                .from(transitionStep1())
                .on("*") // 위에서 step1()을 start 했으므로, A는 제외한 모든 상태
                .to(transitionStep3())
                .next(transitionStep4())
                .on("FAILED").end()
                .build();
    }

    @Bean
    public Step transitionStep1() {
        return stepBuilderFactory.get("transitionStep1")
                .tasklet(((contribution, chunkContext) -> {
                    System.out.println("transitionStep1 started");
                    return RepeatStatus.FINISHED;
                }))
                .build();
    }

    @Bean
    public Step transitionStep2() {
        return stepBuilderFactory.get("transitionStep2")
                .tasklet(((contribution, chunkContext) -> {
                    System.out.println("transitionStep2 started");
                    return RepeatStatus.FINISHED;
                }))
                .build();
    }

    @Bean
    public Step transitionStep3() {
        return stepBuilderFactory.get("transitionStep3")
                .tasklet(((contribution, chunkContext) -> {
                    System.out.println("transitionStep3 started");
                    return RepeatStatus.FINISHED;
                }))
                .build();
    }

    @Bean
    public Step transitionStep4() {
        return stepBuilderFactory.get("transitionStep4")
                .tasklet(((contribution, chunkContext) -> {
                    System.out.println("transitionStep4 started");
                    return RepeatStatus.FINISHED;
                }))
                .build();
    }
}

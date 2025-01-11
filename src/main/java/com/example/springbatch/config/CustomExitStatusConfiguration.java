package com.example.springbatch.config;

import com.example.springbatch.exitstatus.CustomExitStatus;
import com.example.springbatch.listener.PassCheckingListener;
import com.example.springbatch.step.GeneralStepFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class CustomExitStatusConfiguration {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final GeneralStepFactory generalStepFactory;

    @Bean
    public Job customExitStatusJob() {
        return jobBuilderFactory.get("customExitStatusJob")
                .start(customExitStatusFlow())
                .build()
                .incrementer(new RunIdIncrementer())
                .build();
    }

    @Bean
    public Flow customExitStatusFlow() {
        return new FlowBuilder<Flow>("customExitStatusFlow")
                .start(customExitStatusStep())
                .on("CUSTOM_FAILED")
                .to(generalStepFactory.generalStep2())
                .from(customExitStatusStep())
                .on("*")
                .to(generalStepFactory.generalStep3())
                .build();
    }

    @Bean // Bean에 등록하지 않으면 from()에서 같은 step으로 인식을 못하는듯?? 신기하다...
    public Step customExitStatusStep() {
        return stepBuilderFactory.get("customExitStatusStep")
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                        System.out.println("CustomExitStatusStep started");
                        contribution.setExitStatus(CustomExitStatus.CUSTOM_FAILED);
                        return RepeatStatus.FINISHED;
                    }
                })
                .listener(new PassCheckingListener())
                .build();
    }


}

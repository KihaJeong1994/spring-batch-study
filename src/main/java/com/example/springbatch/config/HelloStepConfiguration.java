package com.example.springbatch.config;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
@RequiredArgsConstructor
public class HelloStepConfiguration {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job helloJob2() {
        return jobBuilderFactory.get("helloJob2")
                .start(taskStep())
                .next(chunkStep())
                .incrementer(new RunIdIncrementer())
                .build();
    }

    @Bean
    public Step taskStep() {
        return stepBuilderFactory.get("taskStep")
                .tasklet(((contribution, chunkContext) -> {
                    System.out.println("taskStep was executed");
                    return RepeatStatus.FINISHED;
                }))
                .build();
    }

    @Bean
    public Step chunkStep() {
        return stepBuilderFactory.get("chunkStep")
                .<String, String>chunk(2)// SimpleStepBuilder를 통해서 TaskletStep을 만드는데, 이때 Tasklet은 내가 만든 Tasklet이 아니라 ChunkOrientedTasklet 사용.
                .reader(new ListItemReader<>(Arrays.asList("item1", "item2", "item3", "item4", "item5")))
                .processor(new ItemProcessor<String, String>() {
                    @Override
                    public String process(String item) {
                        return item.toUpperCase();
                    }
                })
                .writer(items -> {
                    items.forEach(item ->{
                        System.out.print(item);
                        System.out.print("&");
                    });
                    System.out.println();
                })
                .build();
    }
}

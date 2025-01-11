package com.example.springbatch.config;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
@RequiredArgsConstructor
public class ChunkConfiguration {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job chunkConfigurationJob() {
        return jobBuilderFactory.get("chunkConfigurationJob")
                .start(chunConfigurationkStep())
                .build();
    }

    @Bean
    public Step chunConfigurationkStep() {
        return stepBuilderFactory.get("chunConfigurationkStep")
                .<String, String>chunk(5)
                .reader(new ListItemReader<>(Arrays.asList("item1","item2","item3","item4","item5","item6","item7","item8","item9","item10")))
                .processor((ItemProcessor<String, String>) item -> "processed" + item)
                .writer(items -> {
                    System.out.println("items: " + items);
                })
                .build()
                ;
    }
}

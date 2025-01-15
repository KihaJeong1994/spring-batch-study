package com.example.springbatch.config;

import com.example.springbatch.reader.CustomItemStreamReader;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
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
//                .incrementer(new RunIdIncrementer())
                .build();
    }

    @Bean
    public Step chunConfigurationkStep() {
        return stepBuilderFactory.get("chunConfigurationkStep")
                .<String, String>chunk(5)
                .reader(new CustomItemStreamReader(Arrays.asList("item1","item2","item3","item4","item5","item6","item7","item8","item9","item10")))
                .processor((ItemProcessor<String, String>) item -> {
                    if(item.equals("item1")) {
                        return null; // Processor에서 null return 시 writer에서 사용하는 Chunk<O> 에서 자동 제외
                    }
                    return "processed" + item;
                })
                .writer(items -> {
                    System.out.println("items: " + items);
                })
                .build()
                ;
    }
}

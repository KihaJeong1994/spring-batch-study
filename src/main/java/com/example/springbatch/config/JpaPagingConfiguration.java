package com.example.springbatch.config;

import com.example.springbatch.data.Customer;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;
import java.util.Map;

@RequiredArgsConstructor
@Configuration
public class JpaPagingConfiguration {

    private final int chunkSize = 2;

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;

    @Bean
    public Job jpaPagingJob() {
        return jobBuilderFactory.get("jpaPagingJob")
                .start(jpaPagingStep())
                .incrementer(new RunIdIncrementer())
                .build();
    }

    @Bean
    public Step jpaPagingStep() {
        return stepBuilderFactory.get("jpaPagingStep")
                .<Customer, Customer>chunk(chunkSize)
                .reader(jpaPagingItemReader())
                .writer(items -> {
                    System.out.println("items: " + items);
                })
                .build()
                ;
    }

    @Bean
    public ItemReader<Customer> jpaPagingItemReader() {
        return new JpaPagingItemReaderBuilder<Customer>()
                .name("jpaPagingItemReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("select c from Customer c where firstName like :firstName order by lastName, firstName")
                .parameterValues(Map.of("firstName","A%"))
                .pageSize(chunkSize)
                .build();
    }
}

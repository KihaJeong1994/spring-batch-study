package com.example.springbatch.config.flatfile;

import com.example.springbatch.data.Customer;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
@RequiredArgsConstructor
public class FlatFileConfiguration {
  private final JobBuilderFactory jobBuilderFactory;
  private final StepBuilderFactory stepBuilderFactory;
  private final int chunkSize = 3;

  @Bean
  public Job flatFileJob() {
    return jobBuilderFactory.get("flatFileJob")
        .start(flatFileStep())
        .incrementer(new RunIdIncrementer())
        .build();
  }

  @Bean
  public Step flatFileStep() {
    return stepBuilderFactory.get("flatFileStep")
        .chunk(chunkSize)
        .reader(flatFileItemReader())
        .writer(items -> {
          System.out.println(items);
        })
        .build();
  }

  @Bean
  public ItemReader<Customer> flatFileItemReader() {
    FlatFileItemReader<Customer> flatFileItemReader = new FlatFileItemReader<>();
    flatFileItemReader.setResource(new ClassPathResource("file/customer.csv"));
    CustomerLineMapper lineMapper = new CustomerLineMapper();
    lineMapper.setLineTokenizer(new DelimitedLineTokenizer());
    lineMapper.setFieldSetMapper(new CustomerFieldSetMapper());

    flatFileItemReader.setLineMapper(lineMapper);
    flatFileItemReader.setLinesToSkip(1);
    return flatFileItemReader;
  }
}

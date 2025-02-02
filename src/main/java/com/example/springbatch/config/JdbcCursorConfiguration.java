package com.example.springbatch.config;

import com.example.springbatch.data.Customer;
import com.example.springbatch.data.Customer2;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@RequiredArgsConstructor
@Configuration
public class JdbcCursorConfiguration {

  private final int chunkSize = 10;

  private final JobBuilderFactory jobBuilderFactory;
  private final StepBuilderFactory stepBuilderFactory;
  private final DataSource dataSource;

  @Bean
  public Job jdbcCursorJob() {
    return jobBuilderFactory.get("jdbcCursorJob")
        .start(jdbcCursorStep())
        .incrementer(new RunIdIncrementer())
        .build();
  }

  @Bean
  public Step jdbcCursorStep() {
    return stepBuilderFactory.get("jdbcCursorStep")
        .<Customer, Customer2>chunk(chunkSize)
        .reader(jdbcCursorItemReader(dataSource))
        .writer(jdbcBatchItemWriter(dataSource))
        .build()
        ;
  }

  @Bean
  public ItemReader<Customer> jdbcCursorItemReader(DataSource dataSource) {
    return new JdbcCursorItemReaderBuilder<Customer>()
        .name("jdbcCursorItemReader")
        .fetchSize(chunkSize)
        .sql("select id, firstName, lastName, birthDate from customer where firstName like ? order by lastName, firstName")
        .beanRowMapper(Customer.class)
        .queryArguments("A%")
        .dataSource(dataSource)
        .build();
  }

  @Bean
  public ItemWriter<Customer2> jdbcBatchItemWriter(DataSource dataSource) {
    return new JdbcBatchItemWriterBuilder<Customer2>()
        .dataSource(dataSource)
        .beanMapped() // or columnMapped() 사용 가능
        .sql("insert into customer2 (firstName ,lastName ,birthDate) values ( :firstName, :lastName, :birthDate) ")
        .build();
  }
}

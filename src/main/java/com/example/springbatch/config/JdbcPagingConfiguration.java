package com.example.springbatch.config;

import com.example.springbatch.data.Customer;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Configuration
public class JdbcPagingConfiguration {

    private final int chunkSize = 2;

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource;

    @Bean
    public Job jdbcPagingJob() throws Exception {
        return jobBuilderFactory.get("jdbcPagingJob")
                .start(jdbcPagingStep())
                .incrementer(new RunIdIncrementer())
                .build();
    }

    @Bean
    public Step jdbcPagingStep() throws Exception {
        return stepBuilderFactory.get("jdbcPagingStep")
                .<Customer, Customer>chunk(chunkSize)
                .reader(jdbcPagingItemReader(dataSource))
                .writer(items -> {
                    System.out.println("items: " + items);
                })
                .build()
                ;
    }

    /**
     * JdbcPagingItemReader
     *
     * order by를 사용해서 데이터 순서 보장해야함
     * 멀티스레드 환경에서 Thread 안정성 보장 -> 별도 동기화 필요 x
     * PagingQueryProvider : 페이징 전략에 따른 쿼리문 ItemReader에게 제공. 데이터베이스마다 페이징 전략이 다르므로 서로 다른 PagingQueryProvider 사용
     * @param dataSource
     * @return
     */
    @Bean
    public ItemReader<Customer> jdbcPagingItemReader(DataSource dataSource) throws Exception {
        return new JdbcPagingItemReaderBuilder<Customer>()
                .name("jdbcPagingItemReader")
                .fetchSize(chunkSize)
                .pageSize(chunkSize)
                .beanRowMapper(Customer.class)
                .dataSource(dataSource)
                .queryProvider(createQueryProvider())
                .parameterValues(Map.of("firstName", "A%"))
                .build();
    }

    private PagingQueryProvider createQueryProvider() throws Exception {
        SqlPagingQueryProviderFactoryBean queryProvider = new SqlPagingQueryProviderFactoryBean();
        queryProvider.setDataSource(dataSource);
        queryProvider.setSelectClause("id, firstName, lastName, birthDate");
        queryProvider.setFromClause("from customer");
        queryProvider.setWhereClause("where firstName like :firstName");
        queryProvider.setSortKeys(Map.of("id", Order.ASCENDING));

        return queryProvider.getObject();
    }
}

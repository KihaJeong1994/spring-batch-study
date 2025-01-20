package com.example.springbatch.config;

import com.example.springbatch.data.Customer;
import com.example.springbatch.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.adapter.ItemReaderAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ItemReaderAdapterConfiguration {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final CustomerService customerService;
    private final int chunkSize = 10;

    @Bean
    public Job itemReaderAdapterJob() {
        return jobBuilderFactory.get("itemReaderAdapterJob")
                .start(itemReaderAdapterStep())
                .incrementer(new RunIdIncrementer())
                .build();
    }

    private Step itemReaderAdapterStep() {
        return stepBuilderFactory.get("itemReaderAdapterStep")
                .<Customer, Customer>chunk(chunkSize)
                .reader(itemReaderAdapterReader())
                .writer(items -> {
                    System.out.println(items);
                })
                .build();
    }

    /**
     * ItemReaderAdapter : 기존에 구현한 Service의 메소드를 활용 가능<br>
     * <p>
     * 단점 : targetObject와 targetMethod를 설정하면, ItemReader의 read()를 호출 시 설정한 메소드를 실행하게 되는데, 이 때 기대와는 다르게 동작<br>
     * 보통 List를 읽어서 하나씩 read()할 것을 기대하는데, 이 클래스를 사용하면, read()호출할 때마다 List를 읽어와서 끝까지 null을 반환하지 않게돼서 무한히 작동하게 됨<br>
     * 사용하고 싶다면, List를 읽어와서, 데이터를 하나씩 read하는 메소드를 구현해서 ItemReaderAdapter에 설정해야할텐데, 너무 번거로움
     *
     * @return
     */
    public ItemReader<Customer> itemReaderAdapterReader() {
        ItemReaderAdapter<Customer> itemReaderAdapter = new ItemReaderAdapter<>();
        itemReaderAdapter.setTargetObject(customerService);
        itemReaderAdapter.setTargetMethod("getAllCustomers");
        return itemReaderAdapter;
    }
}

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
//                    contribution.getStepExecution().setStatus(BatchStatus.FAILED);
                    return RepeatStatus.FINISHED;
                }))
//                .startLimit(2) // 원래는 해당 Step이 FAILED일 경우에 계속 반복해서 수행 가능한데, 실행 횟수 제한을 걸 수 있다
//                .allowStartIfComplete(true) // Job 실행 중 특정 Step에서 에러 발생 시, 그 이전에 성공한 Step들은 원래 재시작되지않는데, 항상 재시작되게 설정 가능. 유효성 검증, 꼭 필요한 사전작업 관련 Step 에 설정
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
                    items.forEach(item -> {
                        System.out.print(item);
                        System.out.print("&");
                    });
                    System.out.println();
                })
                .build();
    }
}

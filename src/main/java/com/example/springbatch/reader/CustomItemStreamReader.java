package com.example.springbatch.reader;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import java.util.List;

public class CustomItemStreamReader implements ItemStreamReader<String> { // ItemStream과 ItemReader를 모두 상속하는 인터페이스
  public CustomItemStreamReader(List<String> items) {
    this.items = items;
    this.index = 0;
  }

  private final List<String> items;
  private int index = -1;
  private boolean restart = false;

  @Override
  public String read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
    String item = null;

    if (index < items.size()) {
      item = items.get(index);
      index++;
    }

    if (index == 7 && !restart) { // 7로 하더라도 6부터 찍히는 이유 : update(ExecutionContext executionContext)가 chunk 단위의 동작을 시작할때마다 동작하여서, chunkSize일때 한번 update 동작하고, 그다음엔 안 동작해서
      throw new RuntimeException("Restart is Required!");
    }

    return item;
  }

  @Override
  public void open(ExecutionContext executionContext) throws ItemStreamException {
    if (executionContext.containsKey("index")) {
      index = executionContext.getInt("index");
      this.restart = true;
    } else {
      index = 0;
      executionContext.put("index", index);
    }
  }

  @Override
  public void update(ExecutionContext executionContext) throws ItemStreamException {
    // chunk 단위의 tasklet 실행할때마다 수행
    // executionContext에 재실행계획을 update에 넣어두는게 좋을 것 같은 이유 : chunk 프로세스는 chunk 단위로 트랜잭션이 실행되기 때문에, 매번 처리한 데이터를 executionContext에 넣어두는것 보다 chunk 단위로 기록하는게 더 나은듯?
    executionContext.put("index", index);
  }

  @Override
  public void close() throws ItemStreamException {
    System.out.println("close");
  }
}

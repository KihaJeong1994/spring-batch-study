package com.example.springbatch.config.flatfile;

import com.example.springbatch.data.Customer;
import lombok.Setter;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.LineTokenizer;

@Setter
public class CustomerLineMapper implements LineMapper<Customer> {

  private LineTokenizer lineTokenizer;
  private FieldSetMapper<Customer> fieldSetMapper;

  @Override
  public Customer mapLine(String line, int lineNumber) throws Exception {
    return fieldSetMapper.mapFieldSet(lineTokenizer.tokenize(line));
  }
}

package com.example.springbatch.config.flatfile;

import com.example.springbatch.data.Customer;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

public class CustomerFieldSetMapper implements FieldSetMapper<Customer> {
  @Override
  public Customer mapFieldSet(FieldSet fieldSet) throws BindException {
    if (fieldSet == null) {
      return null;
    }
    Customer customer = new Customer();
    customer.setId(fieldSet.readLong(0));
    customer.setFirstName(fieldSet.readString(1));
    customer.setLastName(fieldSet.readString(2));
    customer.setBirthDate(fieldSet.readString(3));
    return customer;
  }
}

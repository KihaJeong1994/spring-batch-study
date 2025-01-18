package com.example.springbatch.data;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "customer")
public class Customer {
    @Id
    private Long id;
    private String firstName;
    private String lastName;
    private String birthDate;
}

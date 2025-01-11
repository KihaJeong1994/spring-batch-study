package com.example.springbatch.exitstatus;

import org.springframework.batch.core.ExitStatus;

public class CustomExitStatus{
    public static final ExitStatus CUSTOM_FAILED = new ExitStatus("CUSTOM_FAILED");
}

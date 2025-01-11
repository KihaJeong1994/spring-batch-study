package com.example.springbatch.listener;

import com.example.springbatch.exitstatus.CustomExitStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.listener.StepExecutionListenerSupport;
import org.springframework.stereotype.Component;

@Component
public class PassCheckingListener extends StepExecutionListenerSupport {
    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        if(stepExecution.getExitStatus().equals(ExitStatus.FAILED)){
            return CustomExitStatus.CUSTOM_FAILED;
        }
        return super.afterStep(stepExecution);
    }
}

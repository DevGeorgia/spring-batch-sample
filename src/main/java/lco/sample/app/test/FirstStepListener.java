package lco.sample.app.test;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

@Component
public class FirstStepListener implements StepExecutionListener {
    @Override
    public void beforeStep(StepExecution stepExecution) {
        System.out.println("Before step : " + stepExecution.getStepName());
        System.out.println("Before step: " + stepExecution.getJobExecution().getExecutionContext());
        System.out.println("Before step : " + stepExecution.getExecutionContext());
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {

        System.out.println("After step : " + stepExecution.getStepName());
        System.out.println("After step: " + stepExecution.getJobExecution().getExecutionContext());
        System.out.println("After step : " + stepExecution.getExecutionContext());
        return null;
    }
}

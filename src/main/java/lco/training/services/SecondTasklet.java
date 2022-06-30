package lco.training.services;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Service;

@Service
public class SecondTasklet implements Tasklet {


    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
        System.out.println("This is 2nd tasklet");
        return RepeatStatus.FINISHED;
    }
}

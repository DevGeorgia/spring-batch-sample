package lco.sample.app.batchapi.services;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class FirstTasklet implements Tasklet {
    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
        System.out.println("This is first tasklet");

        Map<String, Object> params = chunkContext.getStepContext().getJobParameters();
        String inputFile = params.get("inputFile").toString();

        System.out.println("Input File : " + inputFile);

        return RepeatStatus.FINISHED;
    }
}

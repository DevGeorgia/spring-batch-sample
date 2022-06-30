package lco.training.config;

import lco.training.listener.FirstJobListener;
import lco.training.listener.FirstStepListener;
import lco.training.processor.FirstItemProcessor;
import lco.training.reader.FirstItemReader;
import lco.training.services.FirstTasklet;
import lco.training.services.SecondTasklet;
import lco.training.writer.FirstItemWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SampleJob {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private SecondTasklet secondTasklet;

    @Autowired
    private FirstTasklet firstTasklet;

    @Autowired
    private FirstJobListener firstJobListener;

    @Autowired
    private FirstStepListener firstStepListener;


    @Autowired
    private FirstItemReader firstItemReader;

    @Autowired
    private FirstItemProcessor firstItemProcessor;

    @Autowired
    private FirstItemWriter firstItemWriter;

    @Bean
    public Job firstJob() {
        // Basic Job with 2 steps
        return jobBuilderFactory.get("FirstJob")
                .incrementer(new RunIdIncrementer())
                .start(firstStep())
                .next(secondStep())
                .listener(firstJobListener)
                .build();
    }


    private Step firstStep() {
        return stepBuilderFactory.get("FirstStep").tasklet(firstTasklet).listener(firstStepListener).build();
    }


    private Step secondStep() {
        return stepBuilderFactory.get("SecondStep").tasklet(secondTasklet).build();
    }

    @Bean
    public Job secondJob() {
        // Job with Chunck Step
        return jobBuilderFactory.get("SecondJob")
                .incrementer(new RunIdIncrementer())
                .start(firstChunkStep())
                .next(secondStep())
                .build();
    }

    private Step firstChunkStep() {
        return stepBuilderFactory.get("First Chunk Step")
                .<Integer, Long>chunk(3)
                .reader(firstItemReader)
                //.processor(firstItemProcessor)
                .writer(firstItemWriter)
                .build();
    }


}

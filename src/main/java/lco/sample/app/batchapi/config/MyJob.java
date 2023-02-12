package lco.sample.app.batchapi.config;

import lco.sample.app.batchapi.listener.FirstJobListener;
import lco.sample.app.batchapi.listener.FirstStepListener;
import lco.sample.app.batchapi.model.StudentCsv;
import lco.sample.app.batchapi.model.StudentJson;
import lco.sample.app.batchapi.processor.StudentProcessor;
import lco.sample.app.batchapi.services.FirstTasklet;
import lco.sample.app.batchapi.writer.StudentWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.json.JacksonJsonObjectMarshaller;
import org.springframework.batch.item.json.JsonFileItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;


@Configuration
public class MyJob {

    @Autowired
    private FirstTasklet firstTasklet;

    @Autowired
    private FirstJobListener firstJobListener;

    @Autowired
    private FirstStepListener firstStepListener;

    @Autowired
    private StudentWriter studentWriter;

    @Autowired
    private StudentProcessor studentProcessor;


    @Bean
    public Job buildJob(JobRepository jobRepository, Step buildFirstStep, Step buildSecondStep) {
        return new JobBuilder("myJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(buildFirstStep)
                .next(buildSecondStep)
                .listener(firstJobListener)
                .build();
    }

    @Bean
    public Step buildFirstStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("FirstStep", jobRepository)
                .tasklet(firstTasklet, transactionManager)
                .listener(firstStepListener)
                .build();
    }

    @Bean
    public Step buildSecondStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("myStep", jobRepository)
                .<StudentCsv, StudentJson>chunk(3, transactionManager)
                .reader(flatFileItemReader(null))
                .processor(studentProcessor)
                .writer(jsonFileItemWriter(null))
                .listener(firstStepListener)
                .build();
    }

    @StepScope
    @Bean
    public FlatFileItemReader<StudentCsv> flatFileItemReader(
            @Value("#{jobParameters['inputFile']}") FileSystemResource fileSystemResource) {
        FlatFileItemReader<StudentCsv> flatFileItemReader =
                new FlatFileItemReader<StudentCsv>();

        flatFileItemReader.setResource(fileSystemResource);

        DefaultLineMapper<StudentCsv> defaultLineMapper =
                new DefaultLineMapper<StudentCsv>();

        DelimitedLineTokenizer delimitedLineTokenizer = new DelimitedLineTokenizer(";");
        delimitedLineTokenizer.setNames("ID", "First Name", "Last Name", "Email");

        defaultLineMapper.setLineTokenizer(delimitedLineTokenizer);

        BeanWrapperFieldSetMapper<StudentCsv> fieldSetMapper =
                new BeanWrapperFieldSetMapper<StudentCsv>();
        fieldSetMapper.setTargetType(StudentCsv.class);

        defaultLineMapper.setFieldSetMapper(fieldSetMapper);

        flatFileItemReader.setLineMapper(defaultLineMapper);


        flatFileItemReader.setLinesToSkip(1);

        return flatFileItemReader;
    }


    @StepScope
    @Bean
    public JsonFileItemWriter<StudentJson> jsonFileItemWriter(
            @Value("#{jobParameters['outputFile']}") FileSystemResource fileSystemResource) {
        JsonFileItemWriter<StudentJson> jsonFileItemWriter =
                new JsonFileItemWriter<>(fileSystemResource,
                        new JacksonJsonObjectMarshaller<StudentJson>());

        return jsonFileItemWriter;
    }


}

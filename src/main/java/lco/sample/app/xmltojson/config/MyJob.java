package lco.sample.app.xmltojson.config;


import lco.sample.app.xmltojson.listener.FirstJobListener;
import lco.sample.app.xmltojson.listener.FirstStepListener;
import lco.sample.app.xmltojson.model.StudentJson;
import lco.sample.app.xmltojson.model.StudentXml;
import lco.sample.app.xmltojson.processor.StudentProcessor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.json.JacksonJsonObjectMarshaller;
import org.springframework.batch.item.json.JsonFileItemWriter;
import org.springframework.batch.item.json.builder.JsonFileItemWriterBuilder;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.batch.item.xml.builder.StaxEventItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.transaction.PlatformTransactionManager;


@EnableBatchProcessing
@Configuration
public class MyJob {

    @Bean
    public Job buildJob(JobRepository jobRepository, FirstJobListener firstJobListener, Step buildFirstStep, Step buildSecondStep) {
        System.out.println("Build Job");
        return new JobBuilder("myJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(firstJobListener)
                .start(buildFirstStep)
                .next(buildSecondStep)
                .build();
    }

    @Bean
    public Step buildFirstStep(JobRepository jobRepository, Tasklet firstTasklet, PlatformTransactionManager transactionManager, FirstStepListener firstStepListener) {
        System.out.println("buildFirstStep");
        return new StepBuilder("FirstStep", jobRepository)
                .tasklet(firstTasklet, transactionManager)
                .listener(firstStepListener)
                .build();
    }

    @Bean
    public Step buildSecondStep(JobRepository jobRepository, StudentProcessor studentProcessor, PlatformTransactionManager transactionManager, FirstStepListener firstStepListener) {
        System.out.println("buildSecondStep");
        return new StepBuilder("myStep", jobRepository)
                .<StudentXml, StudentJson>chunk(3, transactionManager)
                .reader(staxEventItemReader())
                .processor(studentProcessor)
                .writer(jsonFileItemWriter(null))
                .listener(firstStepListener)
                .build();
    }


    @StepScope
    @Bean
    public StaxEventItemReader<StudentXml> staxEventItemReader() {
        /*StaxEventItemReader<StudentXml> staxEventItemReader =
                new StaxEventItemReader<StudentXml>();

        staxEventItemReader.setResource(fileSystemResource);
        staxEventItemReader.setFragmentRootElementName("student");
        staxEventItemReader.setUnmarshaller(new Jaxb2Marshaller() {
            {
                setClassesToBeBound(StudentXml.class);
            }
        });*/

        return new StaxEventItemReaderBuilder<StudentXml>()
                .name("studentItemReader")
                .resource(new ClassPathResource("/InputFiles/students-exams.xml"))
                .addFragmentRootElements("student")
                .unmarshaller(new Jaxb2Marshaller() {
                    @Override
                    public void setClassesToBeBound(Class<?>... classesToBeBound) {
                        super.setClassesToBeBound(StudentXml.class);
                    }
                })
                .build();
    }

    @StepScope
    @Bean
    public JsonFileItemWriter<StudentJson> jsonFileItemWriter(
            @Value("#{jobParameters['outputFile']}") FileSystemResource fileSystemResource) {
        /*JsonFileItemWriter<StudentJson> jsonFileItemWriter =
                new JsonFileItemWriter<>(fileSystemResource,
                        new JacksonJsonObjectMarshaller<StudentJson>());*/

        return new JsonFileItemWriterBuilder<StudentJson>()
                .name("studentItemWriter")
                .resource(fileSystemResource)
                .jsonObjectMarshaller(new JacksonJsonObjectMarshaller<StudentJson>())
                .build();
    }


}

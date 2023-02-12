package lco.sample.app.test;


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
import org.springframework.core.io.PathResource;
import org.springframework.core.io.WritableResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.transaction.PlatformTransactionManager;


@EnableBatchProcessing
@Configuration
public class MyJob {



    @Bean
    public Job buildJob(JobRepository jobRepository, FirstJobListener firstJobListener, Step buildFirstStep, Step buildSecondStep) {
        System.out.println("Build Job");
        return new JobBuilder("buildJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(firstJobListener)
                .flow(buildSecondStep)
                .end()

                //.start(buildFirstStep)
                //.next(buildSecondStep)
                .build();
    }

    @Bean
    public Step buildFirstStep(JobRepository jobRepository, PlatformTransactionManager transactionManager, FirstTasklet firstTasklet, FirstStepListener firstStepListener) {
        System.out.println("buildFirstStep");
        return new StepBuilder("buildFirstStep", jobRepository)
                //.listener(firstStepListener)
                .tasklet(firstTasklet, transactionManager)
                .build();
    }

    @Bean
    public Step buildSecondStep(JobRepository jobRepository, StudentProcessor studentProcessor, PlatformTransactionManager transactionManager, FirstStepListener firstStepListener) {
        System.out.println("buildSecondStep");
        return new StepBuilder("buildSecondStep", jobRepository)
                .listener(firstStepListener)
                .<StudentXml, StudentJson>chunk(3, transactionManager)
                .reader(staxEventItemReader())
                .processor(studentProcessor)
                .writer(jsonFileItemWriter())
                .build();
    }


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
        System.out.println("staxEventItemReader");
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

    @Bean
    public JsonFileItemWriter<StudentJson> jsonFileItemWriter() {
        /*JsonFileItemWriter<StudentJson> jsonFileItemWriter =
                new JsonFileItemWriter<>(fileSystemResource,
                        new JacksonJsonObjectMarshaller<StudentJson>());*/
        System.out.println("jsonFileItemWriter");
        String filePath = "D:\\DEV\\projects\\projetsGit\\spring-batch-sample\\src\\main\\resources\\OutputFiles\\output.json";
        WritableResource writableResource = new PathResource(filePath);
        System.out.println("WritableResource " + writableResource);
        System.out.println("jsonFileItemWriter");
        return new JsonFileItemWriterBuilder<StudentJson>()
                .name("studentItemWriter")
                .resource(writableResource)
                .jsonObjectMarshaller(new JacksonJsonObjectMarshaller<StudentJson>())
                .build();
    }


}

package lco.sample.app.jsontosql.config;


import lco.sample.app.jsontosql.listener.FirstJobListener;
import lco.sample.app.jsontosql.listener.FirstStepListener;
import lco.sample.app.jsontosql.model.StudentJdbc;
import lco.sample.app.jsontosql.model.StudentJson;
import lco.sample.app.jsontosql.processor.StudentProcessor;
import lco.sample.app.jsontosql.services.FirstTasklet;
import lco.sample.app.jsontosql.writer.StudentWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.ItemPreparedStatementSetter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.JsonItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import java.sql.PreparedStatement;
import java.sql.SQLException;


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


    @Autowired
    private StudentDataSource studentDataSource;


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
                .<StudentJson, StudentJdbc>chunk(3, transactionManager)
                .reader(jsonItemReader(null))
                .processor(studentProcessor)
                .writer(jdbcBatchItemWriterPrepareStatement())
                .listener(firstStepListener)
                .build();
    }

    @StepScope
    @Bean
    public JsonItemReader<StudentJson> jsonItemReader(
            @Value("#{jobParameters['inputFile']}") FileSystemResource fileSystemResource) {
        JsonItemReader<StudentJson> jsonItemReader = new JsonItemReader<StudentJson>();

        jsonItemReader.setResource(fileSystemResource);
        jsonItemReader.setJsonObjectReader(
                new JacksonJsonObjectReader<>(StudentJson.class));

        //jsonItemReader.setMaxItemCount(8);
        //jsonItemReader.setCurrentItemCount(2);

        return jsonItemReader;
    }


    @Bean
    public JdbcBatchItemWriter<StudentJdbc> jdbcBatchItemWriterPrepareStatement() {
        JdbcBatchItemWriter<StudentJdbc> jdbcBatchItemWriter =
                new JdbcBatchItemWriter<StudentJdbc>();

        jdbcBatchItemWriter.setDataSource(studentDataSource.universityDatasource());
        jdbcBatchItemWriter.setSql(
                "insert into student(id, first_name, last_name, email) "
                        + "values (?,?,?,?)");

        jdbcBatchItemWriter.setItemPreparedStatementSetter(
                new ItemPreparedStatementSetter<StudentJdbc>() {

                    @Override
                    public void setValues(StudentJdbc item, PreparedStatement ps) throws SQLException {
                        ps.setLong(1, item.getId());
                        ps.setString(2, item.getFirstName());
                        ps.setString(3, item.getLastName());
                    }
                });

        return jdbcBatchItemWriter;
    }


}

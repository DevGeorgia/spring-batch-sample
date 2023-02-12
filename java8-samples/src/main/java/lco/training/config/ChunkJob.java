package lco.training.config;

import lco.training.config.StudentDataSource;
import lco.training.listener.SkipListener;
import lco.training.listener.SkipListenerImpl;
import lco.training.model.*;
import lco.training.processor.StudentProcessor;
import lco.training.services.StudentService;
import lco.training.writer.StudentWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.skip.AlwaysSkipItemSkipPolicy;
import org.springframework.batch.item.adapter.ItemReaderAdapter;
import org.springframework.batch.item.adapter.ItemWriterAdapter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.ItemPreparedStatementSetter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.file.*;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.json.JacksonJsonObjectMarshaller;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.JsonFileItemWriter;
import org.springframework.batch.item.json.JsonItemReader;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.batch.item.xml.StaxEventItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.Writer;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;


@Configuration
public class ChunkJob {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private StudentWriter studentWriter;

    @Autowired
    private StudentDataSource studentDataSource;

    @Autowired
    private StudentService studentService;

    @Autowired
    private StudentProcessor studentProcessor;

    @Autowired
    private SkipListener skipListener;

    @Autowired
    private SkipListenerImpl skipListenerImp;

    @Bean
    public Job chunckJob() {
        // Job with Chunck Step
        return jobBuilderFactory.get("chunckJob")
                .incrementer(new RunIdIncrementer())
                .start(firstChunkStep())
                .build();
    }

    /*private Step firstChunkStep() {
        return stepBuilderFactory.get("First Chunk Step")
                .<Integer, Long>chunk(3)
                .reader(firstItemReader)
                //.processor(firstItemProcessor)
                .writer(firstItemWriter)
                .build();
    }*/

    private Step firstChunkStep() {
        return stepBuilderFactory.get("First Chunk Step")
                //.<StudentCsv, StudentCsv>chunk(3)
                //.<StudentJson, StudentJson>chunk(3)
                //.<StudentXml, StudentXml>chunk(3)
                .<StudentCsv, StudentJson>chunk(3)
                .reader(flatFileItemReader(null))
                //.processor(firstItemProcessor)
                .processor(studentProcessor)
                //.reader(jsonItemReader(null))
                //.reader(staxEventItemReader(null))
                //.reader(jdbcCursorItemReader())
                //.writer(studentWriter)
                //.writer(flatFileItemWriter(null))
                .writer(jsonFileItemWriter(null))
                // Fault tolerance - manage bad records by skipping unwanted exceptions
                .faultTolerant()
                //.skip(FlatFileParseException.class)
                //.skipLimit(1)
                // Skip all exceptions
                .skip(Throwable.class)
                .skipPolicy(new AlwaysSkipItemSkipPolicy())
                //Define how many times we want to retry if error and for which type of error
                .retryLimit(1)
                .retry(Throwable.class)
                //.writer(staxEventItemWriter(null))
                //.writer(itemWriterAdapter())
                //.writer(jdbcBatchItemWriterPrepareStatement())
                //.listener(skipListener)
                .listener(skipListenerImp)
                .build();
    }


    @StepScope
    @Bean
    public FlatFileItemReader<StudentCsv> flatFileItemReader(
            @Value("#{jobParameters['inputFile']}") FileSystemResource fileSystemResource) {
        FlatFileItemReader<StudentCsv> flatFileItemReader =
                new FlatFileItemReader<StudentCsv>();

        flatFileItemReader.setResource(fileSystemResource);

        /*flatFileItemReader.setLineMapper(new DefaultLineMapper<StudentCsv>() {
            {
                setLineTokenizer(new DelimitedLineTokenizer(";") {
                    {
                        setNames("ID", "First Name", "Last Name", "Email");
                        //setDelimiter(";");
                    }
                });

                setFieldSetMapper(new BeanWrapperFieldSetMapper<StudentCsv>() {
                    {
                        setTargetType(StudentCsv.class);
                    }
                });

            }
        });*/

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
    public JsonItemReader<StudentJson> jsonItemReader(
            @Value("#{jobParameters['inputFile']}") FileSystemResource fileSystemResource) {
        JsonItemReader<StudentJson> jsonItemReader =
                new JsonItemReader<StudentJson>();

        jsonItemReader.setResource(fileSystemResource);
        jsonItemReader.setJsonObjectReader(
                new JacksonJsonObjectReader<>(StudentJson.class));

        //jsonItemReader.setMaxItemCount(8);
        //jsonItemReader.setCurrentItemCount(2);

        return jsonItemReader;
    }


    @StepScope
    @Bean
    public StaxEventItemReader<StudentXml> staxEventItemReader(
            @Value("#{jobParameters['inputFile']}") FileSystemResource fileSystemResource) {
        StaxEventItemReader<StudentXml> staxEventItemReader =
                new StaxEventItemReader<StudentXml>();

        staxEventItemReader.setResource(fileSystemResource);
        staxEventItemReader.setFragmentRootElementName("student");
        staxEventItemReader.setUnmarshaller(new Jaxb2Marshaller() {
            {
                setClassesToBeBound(StudentXml.class);
            }
        });

        return staxEventItemReader;
    }


    @StepScope
    @Bean
    public JdbcCursorItemReader<StudentJdbc> jdbcCursorItemReader() {
        JdbcCursorItemReader<StudentJdbc> jdbcCursorItemReader =
                new JdbcCursorItemReader<StudentJdbc>();

        DataSource dataSourceUniversity = studentDataSource.universityDatasource();
        System.out.println("Datasource " + dataSourceUniversity);
        jdbcCursorItemReader.setDataSource(dataSourceUniversity);
        jdbcCursorItemReader.setSql(
                "select id, first_name as firstName, last_name as lastName,"
                        + "email from student");

        jdbcCursorItemReader.setRowMapper(new BeanPropertyRowMapper<StudentJdbc>() {
            {
                setMappedClass(StudentJdbc.class);
            }
        });

        //jdbcCursorItemReader.setCurrentItemCount(2);
        //jdbcCursorItemReader.setMaxItemCount(8);
        return jdbcCursorItemReader;
    }


    @StepScope
    @Bean
    public ItemReaderAdapter<StudentResponse> itemReaderAdapter() {
        ItemReaderAdapter<StudentResponse> itemReaderAdapter =
                new ItemReaderAdapter<StudentResponse>();

        itemReaderAdapter.setTargetObject(studentService);
        itemReaderAdapter.setTargetMethod("getStudent");

        return itemReaderAdapter;
    }


    @StepScope
    @Bean
    public FlatFileItemWriter<StudentJdbc> flatFileItemWriter(
            @Value("#{jobParameters['outputFile']}") FileSystemResource fileSystemResource) {
        FlatFileItemWriter<StudentJdbc> flatFileItemWriter =
                new FlatFileItemWriter<StudentJdbc>();

        flatFileItemWriter.setResource(fileSystemResource);

        flatFileItemWriter.setHeaderCallback(new FlatFileHeaderCallback() {
            @Override
            public void writeHeader(Writer writer) throws IOException {
                writer.write("Id;First Name;Last Name;Email");
            }
        });

        flatFileItemWriter.setLineAggregator(new DelimitedLineAggregator<StudentJdbc>() {
            {
                setDelimiter(";");
                setFieldExtractor(new BeanWrapperFieldExtractor<StudentJdbc>() {
                    {
                        setNames(new String[] {"id", "firstName", "lastName", "email"});
                    }
                });
            }
        });

        flatFileItemWriter.setFooterCallback(new FlatFileFooterCallback() {
            @Override
            public void writeFooter(Writer writer) throws IOException {
                writer.write("Created @ " + new Date());
            }
        });

        return flatFileItemWriter;
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


    @StepScope
    @Bean
    public StaxEventItemWriter<StudentJdbc> staxEventItemWriter(
            @Value("#{jobParameters['outputFile']}") FileSystemResource fileSystemResource) {
        StaxEventItemWriter<StudentJdbc> staxEventItemWriter =
                new StaxEventItemWriter<StudentJdbc>();

        staxEventItemWriter.setResource(fileSystemResource);
        staxEventItemWriter.setRootTagName("students");

        staxEventItemWriter.setMarshaller(new Jaxb2Marshaller() {
            {
                setClassesToBeBound(StudentJdbc.class);
            }
        });

        return staxEventItemWriter;
    }

    @Bean
    public JdbcBatchItemWriter<StudentCsv> jdbcBatchItemWriter() {
        JdbcBatchItemWriter<StudentCsv> jdbcBatchItemWriter =
                new JdbcBatchItemWriter<StudentCsv>();

        jdbcBatchItemWriter.setDataSource(studentDataSource.universityDatasource());
        jdbcBatchItemWriter.setSql(
                "insert into student(id, first_name, last_name, email) "
                        + "values (:id, :firstName, :lastName, :email)");

        jdbcBatchItemWriter.setItemSqlParameterSourceProvider(
                new BeanPropertyItemSqlParameterSourceProvider<StudentCsv>());

        return jdbcBatchItemWriter;
    }


    @Bean
    public JdbcBatchItemWriter<StudentCsv> jdbcBatchItemWriterPrepareStatement() {
        JdbcBatchItemWriter<StudentCsv> jdbcBatchItemWriter =
                new JdbcBatchItemWriter<StudentCsv>();

        jdbcBatchItemWriter.setDataSource(studentDataSource.universityDatasource());
        jdbcBatchItemWriter.setSql(
                "insert into student(id, first_name, last_name, email) "
                        + "values (?,?,?,?)");

        jdbcBatchItemWriter.setItemPreparedStatementSetter(
                new ItemPreparedStatementSetter<StudentCsv>() {

                    @Override
                    public void setValues(StudentCsv item, PreparedStatement ps) throws SQLException {
                        ps.setLong(1, item.getId());
                        ps.setString(2, item.getFirstName());
                        ps.setString(3, item.getLastName());
                        ps.setString(4, item.getEmail());
                    }
                });

        return jdbcBatchItemWriter;
    }



    public ItemWriterAdapter<StudentCsv> itemWriterAdapter() {
        ItemWriterAdapter<StudentCsv> itemWriterAdapter =
                new ItemWriterAdapter<StudentCsv>();

        itemWriterAdapter.setTargetObject(studentService);
        itemWriterAdapter.setTargetMethod("restCallToCreateStudent");

        return itemWriterAdapter;
    }
}

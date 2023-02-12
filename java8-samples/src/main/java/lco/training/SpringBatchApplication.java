package lco.training;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableBatchProcessing
//@EnableAsync
//@EnableScheduling
@ComponentScan({"lco.training.config", "lco.training.services", "lco.training.processor",
        "lco.training.reader", "lco.training.writer", "lco.training.model",
"lco.training.listener"})
public class SpringBatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBatchApplication.class, args);
    }
}

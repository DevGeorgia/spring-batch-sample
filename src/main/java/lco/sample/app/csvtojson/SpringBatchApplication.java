package lco.sample.app.csvtojson;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"lco.sample.app.csvtojson.config",
        "lco.sample.app.csvtojson.listener",
        "lco.sample.app.csvtojson.model",
        "lco.sample.app.csvtojson.processor",
        "lco.sample.app.csvtojson.services",
        "lco.sample.app.csvtojson.writer"})
public class SpringBatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBatchApplication.class, args);
    }
}

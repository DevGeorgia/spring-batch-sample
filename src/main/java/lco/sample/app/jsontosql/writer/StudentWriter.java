package lco.sample.app.jsontosql.writer;

import lco.sample.app.jsontosql.model.StudentJdbc;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StudentWriter implements ItemWriter<StudentJdbc> {
    @Override
    public void write(Chunk<? extends StudentJdbc> chunk) throws Exception {
        System.out.println("Inside Item Writer");
        chunk.forEach(System.out::println);
    }
}
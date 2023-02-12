package lco.sample.app.jsontosql.processor;

import lco.sample.app.jsontosql.model.StudentJdbc;
import lco.sample.app.jsontosql.model.StudentJson;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class StudentProcessor implements ItemProcessor<StudentJson, StudentJdbc> {

    @Override
    public StudentJdbc process(StudentJson item) throws Exception {
        System.out.println("Inside Item Processor");
        StudentJdbc studentJdbc = new StudentJdbc();
        studentJdbc.setId(item.getId());
        studentJdbc.setFirstName(item.getFirstName());
        studentJdbc.setLastName(item.getLastName());


        // Fault tolerance - manage bad records

        return studentJdbc;
    }

}

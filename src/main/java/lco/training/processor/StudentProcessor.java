package lco.training.processor;

import lco.training.model.StudentCsv;
import lco.training.model.StudentJson;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class StudentProcessor implements ItemProcessor<StudentCsv, StudentJson> {

    @Override
    public StudentJson process(StudentCsv item) throws Exception {
        System.out.println("Inside Item Processor");
        StudentJson studentJson = new StudentJson();
        studentJson.setId(item.getId());
        studentJson.setFirstName(item.getFirstName());
        studentJson.setLastName(item.getLastName());
        studentJson.setEmail(item.getEmail());

        // Fault tolerance - manage bad records

        return studentJson;
    }

}

package lco.sample.app.batchapi.processor;

import lco.sample.app.batchapi.model.StudentCsv;
import lco.sample.app.batchapi.model.StudentJson;
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
        studentJson.setArea(item.getArea());
        studentJson.setExamTitle(item.getExamTitle());
        studentJson.setNote(item.getNote());

        // Fault tolerance - manage bad records

        return studentJson;
    }

}

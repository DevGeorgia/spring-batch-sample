package lco.sample.app.xmltojson.processor;

import lco.sample.app.xmltojson.model.StudentJson;
import lco.sample.app.xmltojson.model.StudentXml;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class StudentProcessor implements ItemProcessor<StudentXml, StudentJson> {

    @Override
    public StudentJson process(StudentXml item) throws Exception {
        System.out.println("Inside Item Processor");
        StudentJson studentJson = new StudentJson();
        studentJson.setId(item.getId());
        studentJson.setFirstName(item.getFirstName());
        studentJson.setLastName(item.getLastName());

        // Fault tolerance - manage bad records

        return studentJson;
    }

}

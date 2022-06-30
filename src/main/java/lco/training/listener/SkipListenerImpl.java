package lco.training.listener;


import java.io.File;
import java.io.FileWriter;
import java.util.Date;

import lco.training.model.StudentCsv;
import lco.training.model.StudentJson;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.stereotype.Component;


@Component
public class SkipListenerImpl implements SkipListener<StudentCsv, StudentJson> {

    @Override
    public void onSkipInRead(Throwable th) {
        if(th instanceof FlatFileParseException) {
            createFile("spring-batch-sample\\src\\main\\resources\\BadRecords\\Reader\\SkipInRead.txt",
                    ((FlatFileParseException) th).getInput());
        }
    }

    @Override
    public void onSkipInWrite(StudentJson item, Throwable t) {
        createFile("spring-batch-sample\\src\\main\\resources\\BadRecords\\Reader\\SkipInProcess.txt.txt",
                item.toString());
    }

    @Override
    public void onSkipInProcess(StudentCsv item, Throwable t) {
        createFile("spring-batch-sample\\src\\main\\resources\\BadRecords\\Reader\\SkipInWriter.txt.txt",
                item.toString());
    }

    public void createFile(String filePath, String data) {
        try(FileWriter fileWriter = new FileWriter(new File(filePath), true)) {
            fileWriter.write(data + "," + new Date() + "\n");
        }catch(Exception e) {

        }
    }

}

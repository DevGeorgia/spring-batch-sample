package lco.training.listener;

import java.io.File;
import java.io.FileWriter;
import java.util.Date;

import lco.training.model.StudentCsv;
import lco.training.model.StudentJson;
import org.springframework.batch.core.annotation.OnSkipInProcess;
import org.springframework.batch.core.annotation.OnSkipInRead;
import org.springframework.batch.core.annotation.OnSkipInWrite;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.stereotype.Component;

@Component
public class SkipListener {

    @OnSkipInRead
    public void skipInRead(Throwable th) {
        if (th instanceof FlatFileParseException) {
            createFile("spring-batch-sample\\src\\main\\resources\\BadRecords\\Reader\\SkipInRead.txt",
                    ((FlatFileParseException) th).getInput());
        }
    }

    @OnSkipInProcess
    public void skipInProcess(StudentCsv studentCsv, Throwable th) {
        createFile("spring-batch-sample\\src\\main\\resources\\BadRecords\\Reader\\SkipInProcess.txt",
                studentCsv.toString());
    }


    @OnSkipInWrite
    public void skipInWriter(StudentJson studentJson, Throwable th) {
        createFile("spring-batch-sample\\src\\main\\resources\\BadRecords\\Reader\\SkipInWriter.txt",
                studentJson.toString());
    }

    public void createFile(String filePath, String data) {
        try (FileWriter fileWriter = new FileWriter(new File(filePath), true)) {
            fileWriter.write(data + "," + new Date() + "\n");
        } catch (Exception e) {

        }
    }
}

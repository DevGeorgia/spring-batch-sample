package lco.sample.app.batchapi.services;

import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class JobService {


    @Autowired
    private JobLauncher jobLauncher;

    @Qualifier("firstJob")
    @Autowired
    private Job firstJob;

    @Qualifier("secondJob")
    @Autowired
    private Job secondJob;


    @Async
    public void startJob(String jobName) {

        JobParametersBuilder jobBuilder= new JobParametersBuilder();
        jobBuilder.addLong("currentTime", System.currentTimeMillis());

        JobParameters jobParameters = jobBuilder.toJobParameters();


        try {
            JobExecution jobExecution = null;
            if(jobName.equals("firstJob")) {
               jobExecution = jobLauncher.run(firstJob, jobParameters);
            } else {
               jobExecution = jobLauncher.run(secondJob, jobParameters);
            }
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }


    }
}

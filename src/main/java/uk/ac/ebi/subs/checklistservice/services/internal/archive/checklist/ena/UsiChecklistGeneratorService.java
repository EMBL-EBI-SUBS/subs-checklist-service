package uk.ac.ebi.subs.checklistservice.services.internal.archive.checklist.ena;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@Service
public class UsiChecklistGeneratorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UsiChecklistGeneratorService.class);

    @Value("${checklist-service.archive.ena.checklist.conversionscript.path}")
    private String conversionScriptPathStr;

    public String generate(String checklistName) {
        LOGGER.debug("Running checklist conversion script for : {}", checklistName);

        ProcessBuilder processBuilder = new ProcessBuilder(conversionScriptPathStr, checklistName);
        Process process = null;
        String stdOut = null, stdErr = null;
        try {
            process = processBuilder.start();

            stdOut = IOUtils.toString(process.getInputStream(), StandardCharsets.UTF_8);
            stdErr = IOUtils.toString(process.getErrorStream(), StandardCharsets.UTF_8);

            if (process.waitFor(30, TimeUnit.SECONDS) == false) {
                throw new RuntimeException("Conversion script took too long to complete.");
            }
            if (process.exitValue() != 0) {
                throw new RuntimeException("Conversion script exited with error.");
            }

            return stdOut;
        } catch (Exception e) {
            throw new RuntimeException("Error running conversion script for : " + checklistName +
                    ", StandardOut : " + stdOut + ", StandardError : " + stdErr, e);
        }
    }
}

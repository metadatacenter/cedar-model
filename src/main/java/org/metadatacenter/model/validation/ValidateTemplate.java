package org.metadatacenter.model.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jackson.JacksonUtils;
import com.github.fge.jsonschema.core.report.LogLevel;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class ValidateTemplate
{
  private static final ObjectMapper MAPPER = JacksonUtils.newMapper();

  public static void main(String[] args)
  {
    if (args.length != 1)
      Usage();

    try {
      File templateFile = new File(args[0]);
      JsonNode templateNode = MAPPER.readTree(templateFile);

      CEDARModelValidator cedarModelValidator = new CEDARModelValidator();
      ProcessingReport processingReport = cedarModelValidator.validateTemplateNode(templateNode);
      for (ProcessingMessage processingMessage : processingReport) {
        processingMessage.setLogLevel(LogLevel.DEBUG);
        System.out.println("Message: " + processingMessage.getMessage());
      }
    } catch (IOException e) {
      System.err.println("IO exception: " + e.getMessage());
    } catch (IllegalArgumentException e) {
      System.err.println("Illegal argument exception: " + e.getMessage());
    }
  }

  private static void Usage()
  {
    System.err.println("Usage: " + ValidateTemplate.class.getName() + " <templateFileName>");
    System.exit(1);
  }
}

package com.redwyvern.statementobserver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redwyvern.statementobserver.codemodel.ClassFileCode;
import com.redwyvern.statementobserver.extractor.JavaSourceExtractor;
import com.redwyvern.util.ResourceUtil;
import org.junit.Test;

import java.io.InputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class ClassFileCodeSerializationTest {

    // Test java class input
    private static final String BASIC_CLASS = "basic";

    private static InputStream getInput(String testClassName) {
        return ResourceUtil.getInputStream("javaclassinput/" + testClassName + "Input.java");
    }

/*
    private static InputStream getExpected(String testClassName) {
        return ResourceUtil.getInputStream("statementrecognizer/" + testClassName + "Expected.java");
    }
*/


    @Test
    public void shouldSerializeBasicClass() throws JsonProcessingException {
        ClassFileCode classFileCode = JavaSourceExtractor.extractSource(getInput(BASIC_CLASS));

        ObjectMapper objectMapper = new ObjectMapper();

        String result = objectMapper.writeValueAsString(classFileCode);

        System.out.println("======= Result =======");
        System.out.println(result);
        System.out.println("======================");

    }
}

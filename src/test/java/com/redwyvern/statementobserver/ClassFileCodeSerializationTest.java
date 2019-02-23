package com.redwyvern.statementobserver;

import com.redwyvern.statementobserver.codemodel.ClassFileCode;
import com.redwyvern.statementobserver.extractor.JavaSourceExtractor;
import com.redwyvern.util.ResourceUtil;
import org.junit.Ignore;
import org.junit.Test;

import java.io.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

public class ClassFileCodeSerializationTest {

    // Test java class input
    private static final String COMPLEX_CLASS = "complexClass";

    // Test serialized java class files
    private static final String SERIALIZED_COMPLEX_CLASS = "statementobserver/au/org/weedon/redblacktree/complexClass.ser";

    public static final int COMPLEX_CLASS_LINE_COUNT = 119;
    public static final int COMPLEX_CLASS_METHOD_COUNT = 12;

    private static InputStream getInput(String testClassName) {
        return ResourceUtil.getInputStream("javaclassinput/" + testClassName + "Input.java");
    }

    @Test
    @Ignore
    // Utility test to generate serialized class files
    public void serializeClass() throws IOException, ClassNotFoundException {
        ClassFileCode originalClassFileCode = JavaSourceExtractor.extractSource(getInput("basic"));

        // Quick sanity check
        assertThat(originalClassFileCode.getCodeLineMap().size(), greaterThan(0));

        FileOutputStream outputStream = new FileOutputStream("c:/temp/basic.ser");

        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);

        objectOutputStream.writeObject(originalClassFileCode);
        objectOutputStream.flush();
        objectOutputStream.close();
    }

    @Test
    public void shouldSerializeComplexClass() throws IOException, ClassNotFoundException {
        ClassFileCode originalClassFileCode = JavaSourceExtractor.extractSource(getInput(COMPLEX_CLASS));

        // Quick sanity check
        assertThat(originalClassFileCode.getCodeLineMap().size(), greaterThan(0));

        // FileOutputStream outputStream = new FileOutputStream("c:/temp/serialized.txt");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);

        objectOutputStream.writeObject(originalClassFileCode);
        objectOutputStream.flush();
        objectOutputStream.close();

        byte[] expectedSerializedClass = ResourceUtil.getInputStream(SERIALIZED_COMPLEX_CLASS).readAllBytes();
        byte[] actualSerializedClass = outputStream.toByteArray();

        assertThat(actualSerializedClass, equalTo(expectedSerializedClass));
    }

    @Test
    public void shouldDeserializeComplexClass() throws IOException, ClassNotFoundException {

        ObjectInputStream objectInputStream = new ObjectInputStream(ResourceUtil.getInputStream(SERIALIZED_COMPLEX_CLASS));

        ClassFileCode deserializedClassFileCode = (ClassFileCode) objectInputStream.readObject();

        assertThat(deserializedClassFileCode.getCodeLineMap().size(), equalTo(COMPLEX_CLASS_LINE_COUNT));
        assertThat(deserializedClassFileCode.getClassMethodCodeMap().size(), equalTo(COMPLEX_CLASS_METHOD_COUNT));
    }
}

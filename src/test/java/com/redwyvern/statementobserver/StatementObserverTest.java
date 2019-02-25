package com.redwyvern.statementobserver;

import com.redwyvern.statementobserver.codemodel.Statement;
import com.redwyvern.util.ResourceUtil;
import net.openhft.compiler.CachedCompiler;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.redwyvern.util.ResourceUtil.getInstanceFromJavaResourceFile;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class StatementObserverTest {

    @Test
    public void shouldObserveCodeAsItExecutes() throws ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {


        //HelloWorldSubject helloWorldSubject = new HelloWorldSubject();


        CachedCompiler cc = new CachedCompiler(null, null);

        StatementSubject helloWorldSubject = getInstanceFromJavaResourceFile("au.org.weedon.redblacktree.HelloWorldSubject", "subjectgenerator/HelloWorldSubject.java");
        Method doStuffMethod = helloWorldSubject.getClass().getMethod("doStuff");

        List<String> statementList = new ArrayList<>();

        StatementObserver statementObserver = statementSubject -> {
            try {
                Statement executingStatement = statementSubject.getExecutingStatement();
                statementList.add(executingStatement.getStatementCode());
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        };

        helloWorldSubject.addObserver(statementObserver);
        doStuffMethod.invoke(helloWorldSubject);

        assertThat(statementList, equalTo(Arrays.asList(
                "System.out.println(\"Hello there...\");",
                "System.out.println(\"Do more stuff...\");"
        )));
    }

    @Test
    public void should2ObserveCodeAsItExecutes() throws ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        CachedCompiler cc = new CachedCompiler(null, null);

        Class aClass = cc.loadFromJava("com.example.CompileTest",
                "package com.example;\n" +
                        "public class CompileTest\n" +
                        "implements java.util.function.Supplier<String> {\n" +
                        "  public String get() {\n" +
                        "    return \"Hello World!\";\n" +
                        "  }\n" +
                        "}\n"
        );

        //Supplier<String> compilerTest = (Supplier<String>) aClass.newInstance();  // instance of the object declared in 'javaCode'
        //System.out.println(compilerTest.get());

        Object o = aClass.newInstance();  // instance of the object declared in 'javaCode'
        Method getMethod = o.getClass().getMethod("get");

        System.out.println("Result: " + getMethod.invoke(o));


        //System.out.println("Result: " + supplier.get());
    }

}

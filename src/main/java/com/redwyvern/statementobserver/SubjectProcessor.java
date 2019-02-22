package com.redwyvern.statementobserver;

import com.google.auto.service.AutoService;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.cglib.proxy.Mixin;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Set;

//@AutoService(Processor.class)
public class SubjectProcessor extends AbstractProcessor {

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotataions = new LinkedHashSet<>();
        annotataions.add(StatementObservable.class.getCanonicalName());
        return annotataions;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(StatementObservable.class)) {

            System.out.println("Checking: " + annotatedElement);
            // Check if a class has been annotated with @Factory
            if (annotatedElement.getKind() != ElementKind.CLASS) {
                System.out.println("Got element => " + annotatedElement);
            }
        }
        return true;
    }

/*

    public static class Example {
        public void callMe() {
            System.out.println("Hello there");
        }
    }

    public static void main(String[] programArgs) {
        Enhancer enhancer = new Enhancer();

        enhancer.setSuperclass(Example.class);
        enhancer.setCallback((MethodInterceptor) (obj, method, args, proxy) -> {
            if (method.getDeclaringClass() != Object.class && method.getName().equals("callMe")) {
                System.out.println("Hello Tom!");
            }
            if (method.getDeclaringClass() != Object.class && method.getName().equals("getMeStuff")) {
                System.out.println("oh snap!");
            }

            return null;
        });
        enhancer.setInterfaces(new Class[] { StatementSubject.class });

        Example proxyExample = (Example) enhancer.create();

        proxyExample.callMe();

*/
/*
        Interface2 crap = (Interface2) proxyExample;
        crap.getMeStuff();
*//*


*/
/*
        Mixin mixin = Mixin.create(
                new Class[]{ Interface1.class, Interface2.class, MixinInterface.class },
                new Object[]{ new Class1(), new Class2() }
        );
*//*

        //MixinInterface mixinDelegate = (MixinInterface) mixin;


    }
*/

}

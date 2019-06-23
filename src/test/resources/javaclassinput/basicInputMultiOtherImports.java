package au.org.weedon.redblacktree;

import au.org.weedon;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;


@StatementObservable
public class HelloWorld {

    public void doStuff() {
        System.out.println("Hello there...");
        System.out.println("Do more stuff...");
    }

}

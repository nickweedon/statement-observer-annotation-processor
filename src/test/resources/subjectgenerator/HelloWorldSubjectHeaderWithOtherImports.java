package au.org.weedon.redblacktree;

import com.redwyvern.statementobserver.StatementObservable;

import au.org.weedon;
import org.junit.Test;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@StatementObservable
public class HelloWorldSubject implements com.redwyvern.statementobserver.StatementSubject {

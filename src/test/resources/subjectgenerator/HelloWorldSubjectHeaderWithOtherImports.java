package au.org.weedon.redblacktree;

import au.org.weedon;
import org.junit.Test;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import com.redwyvern.statementobserver.StatementObservable;
import com.redwyvern.statementobserver.StatementSubject;

@StatementObservable
public class HelloWorldSubject implements StatementSubject {

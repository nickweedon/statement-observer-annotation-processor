package com.redwyvern.statementobserver.generator;

import com.redwyvern.javasource.Java9Lexer;
import com.redwyvern.javasource.Java9Parser;
import com.redwyvern.javasource.Java9ParserBaseVisitor;
import com.redwyvern.statementobserver.StatementObserver;
import com.redwyvern.statementobserver.StatementSubject;
import com.redwyvern.statementobserver.SubjectHelper;
import com.redwyvern.statementobserver.codemodel.ClassFileCode;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.stringtemplate.v4.ST;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;


public class SubjectGeneratorVisitor extends Java9ParserBaseVisitor<Void> {


    private final CommonTokenStream commonTokenStream;

    private String injectCode;
    private String originalClassName;
    private String generatedClassName;
    public static final String IMPLEMENTS_TEMPLATE = "implements <statementSubjectInterface>";


    public SubjectGeneratorVisitor(CommonTokenStream commonTokenStream) {
        this.commonTokenStream = commonTokenStream;
    }

    private static String getResourceText(String resourceFileName) {
        try {
            return new String(getResourceInputStream(resourceFileName).readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException("Exception while reading resource '" + resourceFileName + "'", e);
        }
    }

    private static InputStream getResourceInputStream(String resourceFileName) {
        ClassLoader classLoader = SubjectGeneratorVisitor.class.getClassLoader();
        URL resource = classLoader.getResource(resourceFileName);
        if(resource == null) {
            throw new RuntimeException("Could not find resource '" + resourceFileName + "'");
        }
        try {
            return resource.openStream();
        } catch (IOException e) {
            throw new RuntimeException("Exception while opening resource '" + resourceFileName + "'", e);
        }
    }

    @Override
    public Void visitIdentifier(Java9Parser.IdentifierContext ctx) {
        if(ctx.getParent().getRuleIndex() == Java9Parser.RULE_normalClassDeclaration) {
            processLHSWhiteSpace(ctx.start.getTokenIndex());
            originalClassName = ctx.getText();
            generatedClassName = originalClassName + "Subject";
            System.out.print(generatedClassName);
            // TODO: Make this work in all cases
            ST implementsTemplate = new ST(IMPLEMENTS_TEMPLATE);
            implementsTemplate.add("statementSubjectInterface", StatementSubject.class.getName());
            System.out.print(" " + implementsTemplate.render());

            return null;
        } else {
            return super.visitIdentifier(ctx);
        }
    }

    @Override
    public Void visitStatement(Java9Parser.StatementContext ctx) {
        injectCode = "tick(); ";
        return super.visitStatement(ctx);
    }

    @Override
    public Void visitClassBodyDeclaration(Java9Parser.ClassBodyDeclarationContext ctx) {
        super.visitClassBodyDeclaration(ctx);

        ST subjectCodeTemplate = new ST(getResourceText("generator/SubjectDeclTemplate.tmpl"));
        subjectCodeTemplate.add("className", generatedClassName);
        subjectCodeTemplate.add("subjectHelper", SubjectHelper.class.getName());
        subjectCodeTemplate.add("statementObserver", StatementObserver.class.getName());
        subjectCodeTemplate.add("classFileCode", ClassFileCode.class.getName());

        System.out.print(subjectCodeTemplate.render());

        return null;
    }

    private void processLHSWhiteSpace(int tokenIndex) {
        List<Token> methodChannel = commonTokenStream.getHiddenTokensToLeft(tokenIndex, Java9Lexer.WHITE_SPACE);

        if(methodChannel != null) {
            for(Token methodText : methodChannel) {
                String text = methodText.getText();
                System.out.print(text);
            }
        }
    }

    @Override
    public Void visitTerminal(TerminalNode node) {

        if(node.getSymbol().getType() == Java9Lexer.EOF) {
            return null;
        }

        processLHSWhiteSpace(node.getSymbol().getTokenIndex());
        if(injectCode != null) {
            System.out.print(injectCode);
            injectCode = null;
        }
        System.out.print(node.getText());

        return super.visitTerminal(node);
    }
}

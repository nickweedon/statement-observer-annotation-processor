package com.redwyvern.statementobserver.generator;

import com.redwyvern.javasource.Java9Lexer;
import com.redwyvern.javasource.Java9Parser;
import com.redwyvern.javasource.Java9ParserBaseVisitor;
import com.redwyvern.statementobserver.VisitorParentRuleHistory;
import com.redwyvern.statementobserver.StatementObserver;
import com.redwyvern.statementobserver.StatementSubject;
import com.redwyvern.statementobserver.SubjectHelper;
import com.redwyvern.statementobserver.codemodel.ClassFileCode;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.stringtemplate.v4.ST;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.*;


public class SubjectGeneratorVisitor extends Java9ParserBaseVisitor<Void> {

    private final CommonTokenStream commonTokenStream;

    private String injectCode;
    private String originalClassName;
    private String generatedClassName;
    private final OutputStream outputStream;
    private final PrintWriter printWriter;
    private final SubjectPreprocessResult preprocessResult;
    private final VisitorParentRuleHistory parentRuleHistory = new VisitorParentRuleHistory();

    private int insertImportAtRuleIdx = 0;
    private int insertImplementsAtRuleIdx = 0;

    private void output(String text) {
        printWriter.write(text);
        printWriter.flush();
    }

    private void outputTemplate(String template) {
        ST implementsTemplate = new ST(template);
        implementsTemplate.add("statementObservable", com.redwyvern.statementobserver.StatementObservable.class.getName());
        output(implementsTemplate.render());
    }

    // Determine if/where to add import statement
    private int calculateInsertImportRuleIdx() {
        if(preprocessResult.getImports().contains(com.redwyvern.statementobserver.StatementObservable.class.getName())) {
            // Don't add if it is already in the imports
            return 0;
        }

        if(preprocessResult.getImports().size() > 0) {
            return Java9Parser.RULE_importDeclaration;
        }

        if(preprocessResult.isHasPackageDeclaration()) {
            return Java9Parser.RULE_packageDeclaration;
        }

        return Java9Parser.RULE_ordinaryCompilation;
    }

    // Work out how we are going to add 'implements StatementSubject'
    // We need to append the StatementSubject interface at a different point
    // depending on whether the class already extends and/or implements.
    private int calculateInsertImplementsRuleIdx() {

        if(preprocessResult.isHasImplements()) {
            return Java9Parser.RULE_superinterfaces;
        }
        if(preprocessResult.isHasExtends()) {
            return Java9Parser.RULE_superclass;
        }
        return Java9Parser.RULE_identifier;
    }


    public SubjectGeneratorVisitor(CommonTokenStream commonTokenStream, OutputStream outputStream, SubjectPreprocessResult preprocessResult) {
        this.commonTokenStream = commonTokenStream;
        this.outputStream = outputStream;
        this.printWriter = new PrintWriter(outputStream);
        this.preprocessResult = preprocessResult;

        // Pre-calculate various code generation aspects
        this.insertImportAtRuleIdx = calculateInsertImportRuleIdx();
        this.insertImplementsAtRuleIdx = calculateInsertImplementsRuleIdx();
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
    public Void visitOrdinaryCompilation(Java9Parser.OrdinaryCompilationContext ctx) {

        Void result = super.visitOrdinaryCompilation(ctx);

        if(insertImportAtRuleIdx == ctx.getRuleIndex()) {
            outputTemplate("import <statementObservable>;");
        }

        return result;
    }

    @Override
    public Void visitPackageDeclaration(Java9Parser.PackageDeclarationContext ctx) {
        Void result = super.visitPackageDeclaration(ctx);

        if(insertImportAtRuleIdx == ctx.getRuleIndex()) {
            outputTemplate("\n\nimport <statementObservable>;");
        }

        return result;
    }

    @Override
    public Void visitImportDeclaration(Java9Parser.ImportDeclarationContext ctx) {
        Void result = super.visitImportDeclaration(ctx);

        if(insertImportAtRuleIdx == ctx.getRuleIndex() && ctx.getText().equals("import" + preprocessResult.getLastImportPackage() + ";")) {
            outputTemplate("\nimport <statementObservable>;");
        }

        return result;
    }

    private ParserRuleContext getDirectSingletonChildWithRule(ParserRuleContext ctx, int rule) {

        ParserRuleContext matchingChild = ctx;

        while(matchingChild.children != null && matchingChild.children.size() == 1) {
            if(!(matchingChild.getChild(0) instanceof ParserRuleContext)) {
                return null;
            }

            matchingChild = (ParserRuleContext)matchingChild.getChild(0);

            if(matchingChild.getRuleIndex() == rule) {
                return matchingChild;
            }
        }
        return null;
    }

    /**
     * Override this to build a map of parent objects, keyed by rule type.
     * Maintain not a single parent but a stack in case there are multiple parents of the same type.
     */
    @Override
    public Void visitChildren(RuleNode node) {
        return parentRuleHistory.processRuleNode(node, super::visitChildren);
    }

    @Override
    public Void visitIdentifier(Java9Parser.IdentifierContext ctx) {

        // If this is a non-nested/inner class then rewrite it as a subject
        if(ctx.getParent().getRuleIndex() == Java9Parser.RULE_normalClassDeclaration
                && parentRuleHistory.getParentRuleStack(Java9Parser.RULE_normalClassDeclaration).size() == 1) {

            processLHSWhiteSpace(ctx.start.getTokenIndex());
            originalClassName = ctx.getText();
            generatedClassName = originalClassName + "Subject";
            output(generatedClassName);

            // Add the 'implements subject' code to the class
            if(ctx.getRuleIndex() == insertImplementsAtRuleIdx) {
                // TODO: Make this work in all cases
                ST implementsTemplate = new ST(" implements <statementSubjectInterface>");
                implementsTemplate.add("statementSubjectInterface", StatementSubject.class.getName());
                output(implementsTemplate.render());
                insertImplementsAtRuleIdx = 0;
            }

            return null;
        }

        Void result = super.visitIdentifier(ctx);


        return result;

    }

    @Override
    public Void visitSuperinterfaces(Java9Parser.SuperinterfacesContext ctx) {
        Void result = super.visitSuperinterfaces(ctx);

        // Append the subject interface to the implements list
        if(ctx.getRuleIndex() == insertImplementsAtRuleIdx) {
            ST implementsTemplate = new ST(", <statementSubjectInterface>");
            implementsTemplate.add("statementSubjectInterface", StatementSubject.class.getName());
            output(implementsTemplate.render());
            insertImplementsAtRuleIdx = 0;
        }

        return result;
    }

    @Override
    public Void visitSuperclass(Java9Parser.SuperclassContext ctx) {
        Void result = super.visitSuperclass(ctx);
        // Add the 'implements subject' code to the class but after the 'extends' clause
        if(ctx.getRuleIndex() == insertImplementsAtRuleIdx) {
            ST implementsTemplate = new ST(" implements <statementSubjectInterface>");
            implementsTemplate.add("statementSubjectInterface", StatementSubject.class.getName());
            output(implementsTemplate.render());
            insertImplementsAtRuleIdx = 0;
        }

        return result;
    }

    @Override
    public Void visitStatement(Java9Parser.StatementContext ctx) {

        // Skip pre-pending tick if this is the start of a block
        if(getDirectSingletonChildWithRule(ctx, Java9Parser.RULE_block) != null) {
            return super.visitStatement(ctx);
        }
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

        output(subjectCodeTemplate.render());

        return null;
    }

    private void processLHSWhiteSpace(int tokenIndex) {
        List<Token> methodChannel = commonTokenStream.getHiddenTokensToLeft(tokenIndex, Java9Lexer.WHITE_SPACE);

        if(methodChannel != null) {
            for(Token methodText : methodChannel) {
                String text = methodText.getText();
                output(text);
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
            printWriter.print(injectCode);
            injectCode = null;
        }
        output(node.getText());

        return super.visitTerminal(node);
    }
}

package com.redwyvern.statementobserver.generator;

import com.redwyvern.javasource.Java9Lexer;
import com.redwyvern.javasource.Java9Parser;
import com.redwyvern.javasource.Java9ParserBaseVisitor;
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
    private final SubjectPreprocessResult subjectPreprocessResult;

    private int importInsertRuleIdx = 0;

    private void output(String text) {
        printWriter.write(text);
        printWriter.flush();
    }

    private void outputTemplate(String template) {
        ST implementsTemplate = new ST(template);
        implementsTemplate.add("statementObservable", com.redwyvern.statementobserver.StatementObservable.class.getName());
        output(implementsTemplate.render());
    }


    public SubjectGeneratorVisitor(CommonTokenStream commonTokenStream, OutputStream outputStream, SubjectPreprocessResult subjectPreprocessResult) {
        this.commonTokenStream = commonTokenStream;
        this.outputStream = outputStream;
        this.printWriter = new PrintWriter(outputStream);
        this.subjectPreprocessResult = subjectPreprocessResult;

        // Determine if/where to add import statement
        if(subjectPreprocessResult.getImports().contains(com.redwyvern.statementobserver.StatementObservable.class.getName())) {
            // Don't add if it is already in the imports
            importInsertRuleIdx = 0;
        } else if(subjectPreprocessResult.getImports().size() > 0) {
            importInsertRuleIdx = Java9Parser.RULE_importDeclaration;
        } else if(subjectPreprocessResult.isHasPackageDeclaration()) {
            importInsertRuleIdx = Java9Parser.RULE_packageDeclaration;
        } else {
            importInsertRuleIdx = Java9Parser.RULE_ordinaryCompilation;
        }
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

        if(importInsertRuleIdx == ctx.getRuleIndex()) {
            outputTemplate("import <statementObservable>;");
        }

        return result;
    }

    @Override
    public Void visitPackageDeclaration(Java9Parser.PackageDeclarationContext ctx) {
        Void result = super.visitPackageDeclaration(ctx);

        if(importInsertRuleIdx == ctx.getRuleIndex()) {
            outputTemplate("\n\nimport <statementObservable>;");
        }

        return result;
    }

    @Override
    public Void visitImportDeclaration(Java9Parser.ImportDeclarationContext ctx) {
        Void result = super.visitImportDeclaration(ctx);

        if(importInsertRuleIdx == ctx.getRuleIndex() && ctx.getText().equals("import" + subjectPreprocessResult.getLastImportPackage() + ";")) {
            outputTemplate("\nimport <statementObservable>;");
        }

        return result;
    }

    private enum InterfaceAppendPointEnum {
        EXTENDS,
        IMPLEMENTS,
        CLASS_NAME,
        NONE
    }
    private InterfaceAppendPointEnum interfaceAppendPoint;

    @Override
    public Void visitNormalClassDeclaration(Java9Parser.NormalClassDeclarationContext ctx) {

        // If this is a nested/inner class then skip
        if(getTopParentRule(Java9Parser.RULE_normalClassDeclaration).isPresent()) {
            return super.visitNormalClassDeclaration(ctx);
        }

        // Work out how we are going to add 'implements StatementSubject'
        // We need to append the StatementSubject interface at a different point
        // depending on whether the class already extends and/or implements.

        boolean implementsInterfaces = false;
        boolean extendsClass = false;

        for(ParseTree parseTree : ctx.children) {

            if(parseTree instanceof Java9Parser.SuperclassContext) {
                extendsClass = true;
            }
            if(parseTree instanceof Java9Parser.SuperinterfacesContext) {
                implementsInterfaces = true;
            }
        }
        if(implementsInterfaces) {
            interfaceAppendPoint = InterfaceAppendPointEnum.IMPLEMENTS;
            return super.visitNormalClassDeclaration(ctx);
        }
        if(extendsClass) {
            interfaceAppendPoint = InterfaceAppendPointEnum.EXTENDS;
            return super.visitNormalClassDeclaration(ctx);
        }
        interfaceAppendPoint = InterfaceAppendPointEnum.CLASS_NAME;

        return super.visitNormalClassDeclaration(ctx);
    }

    private Stack<ParserRuleContext> getParentRuleStack(int rule) {
        Stack<ParserRuleContext> parserRuleContextStack = parentRuleMap.get(rule);
        return Objects.requireNonNullElseGet(parserRuleContextStack, Stack::new);
    }

    private Optional<ParserRuleContext> getTopParentRule(int rule) {
        Stack<ParserRuleContext> parserRuleContextStack = parentRuleMap.get(rule);
        if(parserRuleContextStack == null) {
            return Optional.empty();
        }
        return Optional.of(parserRuleContextStack.peek());
    }

    private Optional<ParseTree> getLastChild(ParseTree parseTree) {
        final int childCount = parseTree.getChildCount();
        if(childCount == 0) {
            return Optional.empty();
        }
        return Optional.of(parseTree.getChild(childCount - 1));
    }


/*
    private ParserRuleContext getParentWithRule(ParserRuleContext ctx, int rule) {

        ParserRuleContext currentCtx = ctx.getParent();

        while(currentCtx.getRuleIndex() != rule) {
            currentCtx = currentCtx.getParent();
            if(currentCtx == null) {
                return null;
            }
        }
        return currentCtx;
    }
*/

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


    //TODO: Add max depth
    private ParserRuleContext getSingleChildWithRule(ParserRuleContext ctx, int rule) {

        if(ctx.getRuleIndex() == rule) {
            return ctx;
        }

        if(ctx.children == null || ctx.children.size() == 0) {
            return null;
        }

        for(ParseTree child : ctx.children) {
            if(!(child instanceof ParserRuleContext)) {
                continue;
            }
            ParserRuleContext childResult = getSingleChildWithRule((ParserRuleContext)child, rule);
            if(childResult != null) {
                return childResult;
            }
        }
        return null;
    }


    private Map<Integer, Stack<ParserRuleContext>> parentRuleMap = new HashMap<>();


    /**
     * Override this to build a map of parent objects, keyed by rule type.
     * Maintain not a single parent but a stack in case there are multiple parents of the same type.
     */
    @Override
    public Void visitChildren(RuleNode node) {

        ParserRuleContext parserRuleContext = (ParserRuleContext)node;

        final int ruleIndex = parserRuleContext.getRuleContext().getRuleIndex();

        parentRuleMap
                .computeIfAbsent(ruleIndex, (index) -> new Stack<>())
                .push(parserRuleContext);

        Void result = super.visitChildren(node);

        Stack<ParserRuleContext> parentRuleStack = parentRuleMap.get(ruleIndex);
        parentRuleStack.pop();
        if(parentRuleStack.size() == 0) {
            parentRuleMap.remove(ruleIndex);
        }

        return result;
    }

    @Override
    public Void visitIdentifier(Java9Parser.IdentifierContext ctx) {

        // If this is a non-nested/inner class then rewrite it as a subject
        if(ctx.getParent().getRuleIndex() == Java9Parser.RULE_normalClassDeclaration
                && getParentRuleStack(Java9Parser.RULE_normalClassDeclaration).size() == 1) {

            processLHSWhiteSpace(ctx.start.getTokenIndex());
            originalClassName = ctx.getText();
            generatedClassName = originalClassName + "Subject";
            output(generatedClassName);

            if(interfaceAppendPoint == InterfaceAppendPointEnum.CLASS_NAME) {
                // TODO: Make this work in all cases
                ST implementsTemplate = new ST(" implements <statementSubjectInterface>");
                implementsTemplate.add("statementSubjectInterface", StatementSubject.class.getName());
                output(implementsTemplate.render());
                interfaceAppendPoint = InterfaceAppendPointEnum.NONE;
            }

            return null;
        }


/*
        if(interfaceAppendPoint == InterfaceAppendPointEnum.CLASS_NAME) {
            if(ctx.getParent().getRuleIndex() == Java9Parser.RULE_classType
                && ctx.getParent().getParent().getRuleIndex() == Java9Parser.RULE_superclass) {

            }
        }
*/

        Void result = super.visitIdentifier(ctx);

        // If this is the last interface in the interface list then add the new interface
/*
        if(interfaceAppendPoint == InterfaceAppendPointEnum.IMPLEMENTS) {
            getTopParentRule(Java9Parser.RULE_interfaceTypeList).ifPresent((interfaceTypeListCtx) -> {
                if(getTopParentRule(Java9Parser.RULE_interfaceType).orElseThrow() == getLastChild(interfaceTypeListCtx).orElseThrow()) {
                    ST implementsTemplate = new ST(", <statementSubjectInterface>");
                    implementsTemplate.add("statementSubjectInterface", StatementSubject.class.getName());
                    output(implementsTemplate.render());
                }

            });
        }
*/

        return result;

    }

    @Override
    public Void visitSuperinterfaces(Java9Parser.SuperinterfacesContext ctx) {
        Void result = super.visitSuperinterfaces(ctx);

        if(interfaceAppendPoint == InterfaceAppendPointEnum.IMPLEMENTS) {
            ST implementsTemplate = new ST(", <statementSubjectInterface>");
            implementsTemplate.add("statementSubjectInterface", StatementSubject.class.getName());
            output(implementsTemplate.render());
            interfaceAppendPoint = InterfaceAppendPointEnum.NONE;
        }

        return result;
    }

    @Override
    public Void visitSuperclass(Java9Parser.SuperclassContext ctx) {
        Void result = super.visitSuperclass(ctx);
        if(interfaceAppendPoint == InterfaceAppendPointEnum.EXTENDS) {
            ST implementsTemplate = new ST(" implements <statementSubjectInterface>");
            implementsTemplate.add("statementSubjectInterface", StatementSubject.class.getName());
            output(implementsTemplate.render());
            interfaceAppendPoint = InterfaceAppendPointEnum.NONE;
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

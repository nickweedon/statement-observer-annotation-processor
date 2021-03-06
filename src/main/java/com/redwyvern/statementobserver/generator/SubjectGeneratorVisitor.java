package com.redwyvern.statementobserver.generator;

import com.google.common.collect.Lists;
import com.redwyvern.javasource.Java9Lexer;
import com.redwyvern.javasource.Java9Parser;
import com.redwyvern.javasource.Java9ParserBaseVisitor;
import com.redwyvern.statementobserver.VisitorParentRuleHistory;
import com.redwyvern.statementobserver.StatementObserver;
import com.redwyvern.statementobserver.StatementSubject;
import com.redwyvern.statementobserver.SubjectHelper;
import com.redwyvern.statementobserver.codemodel.ClassFileCode;
import lombok.Getter;
import lombok.Setter;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.xpath.XPath;
import org.stringtemplate.v4.ST;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SubjectGeneratorVisitor extends Java9ParserBaseVisitor<Void> {

    private final CommonTokenStream commonTokenStream;
    private final Java9Parser parser;

    private static final Function<String, String> IDENTITY_RULE_TRANSFORM = (rule) -> rule;
    private String originalClassName;
    private String generatedClassName;
    private OutputStream originalOutputStream;
    private OutputStream outputStream;
    private final PrintWriter printWriter;
    private final SubjectPreprocessResult preprocessResult;
    private final VisitorParentRuleHistory parentRuleHistory = new VisitorParentRuleHistory();

    @Getter
    @Setter
    static private class RuleTransform {
        private String lhsWhitespace;
        private final Function<String, String> ruleTransform;
        private final ByteArrayOutputStream ruleOutputStream = new ByteArrayOutputStream();

        private RuleTransform(Function<String, String> ruleTransform) {
            this.ruleTransform = ruleTransform;
        }

        public String getTransformedString() {
            return ruleTransform.apply(ruleOutputStream.toString());
        }
    }

    private final Stack<RuleTransform> ruleTransformStack = new Stack<>();

    private int insertImportAtRuleIdx = 0;
    private int insertImplementsAtRuleIdx = 0;

    private List<String> newImports = new ArrayList<>();

    private void output(String text) {
        try {
            outputStream.write(text.getBytes());
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //printWriter.write(text);
        //printWriter.flush();
    }

    private <T extends ParserRuleContext> Void setRuleTransform(Function<T, Void> superMethod, T ctx, Function<String, String> transform) {
        RuleTransform ruleTransform = new RuleTransform(transform);
        outputStream = ruleTransform.getRuleOutputStream();
        ruleTransformStack.push(ruleTransform);
        Void result = superMethod.apply(ctx);

        if(ruleTransformStack.pop() != ruleTransform) {
            throw new RuntimeException("Rule transform stack corruption!");
        }

        if(ruleTransformStack.size() > 0) {
            outputStream = ruleTransformStack.peek().getRuleOutputStream();
        } else {
            outputStream = originalOutputStream;
        }
        try {
            String streamText = ruleTransform.getTransformedString();
            outputStream.write(streamText.getBytes());
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    private void outputTemplate(String template) {
        ST outputTemplate = new ST(template);
        outputTemplate.add("className", generatedClassName);
        outputTemplate.add("subjectHelper", SubjectHelper.class.getName());
        outputTemplate.add("statementObserver", StatementObserver.class.getName());
        outputTemplate.add("statementSubjectInterfaceSimpleName", StatementSubject.class.getSimpleName());
        outputTemplate.add("statementSubjectInterface", StatementSubject.class.getName());
        outputTemplate.add("classFileCode", ClassFileCode.class.getName());
        outputTemplate.add("statementObservable", com.redwyvern.statementobserver.StatementObservable.class.getName());
        output(outputTemplate.render());
    }

    // Determine if/where to add import statement
    private int calculateInsertImportRuleIdx() {

        newImports.addAll(Arrays.asList(
            com.redwyvern.statementobserver.StatementObservable.class.getName(),
            com.redwyvern.statementobserver.StatementSubject.class.getName()
        ));


        newImports.removeAll(preprocessResult.getImports());

        if(newImports.isEmpty()) {
            // Don't add if all imports are already in the imports
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
        return Java9Parser.RULE_classBody;
    }


    public SubjectGeneratorVisitor(CommonTokenStream commonTokenStream, Java9Parser parser, OutputStream outputStream, SubjectPreprocessResult preprocessResult) {
        this.commonTokenStream = commonTokenStream;
        this.parser = parser;
        this.originalOutputStream = outputStream;
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
            output(String.format("import %s;", newImports.get(0)));
            for(int i = 1; i < newImports.size(); ++i) {
                output(String.format("\nimport %s;", newImports.get(i)));
            }
        }

        return result;
    }

    @Override
    public Void visitPackageDeclaration(Java9Parser.PackageDeclarationContext ctx) {
        Void result = super.visitPackageDeclaration(ctx);

        if(insertImportAtRuleIdx == ctx.getRuleIndex()) {
            output("\n");
            for (String newImport : newImports) {
                output(String.format("\nimport %s;", newImport));
            }
        }

        return result;
    }

    @Override
    public Void visitImportDeclaration(Java9Parser.ImportDeclarationContext ctx) {
        Void result = super.visitImportDeclaration(ctx);

/*
        if(insertImportAtRuleIdx == ctx.getRuleIndex() && ctx.getText().equals("import" + preprocessResult.getLastImportPackage() + ";")) {
            outputTemplate("\nimport <statementObservable>;");
        }
*/
        if(insertImportAtRuleIdx == ctx.getRuleIndex()
                && getRightParseTreeArray(ctx, (childCtx) -> (childCtx instanceof Java9Parser.ImportDeclarationContext)).size() == 0) {

            for (String newImport : newImports) {
                output(String.format("\nimport %s;", newImport));
            }
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

    @Override
    public Void visitClassBody(Java9Parser.ClassBodyContext ctx) {

        // Add the 'implements subject' code to the class
        if(ctx.getRuleIndex() == insertImplementsAtRuleIdx) {
            outputTemplate(" implements <statementSubjectInterfaceSimpleName>");
            insertImplementsAtRuleIdx = 0;
        }

        return super.visitClassBody(ctx);
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
            generatedClassName = " " + originalClassName + "Subject";
            output(generatedClassName);

            return null;
        }

        return super.visitIdentifier(ctx);

    }

    @Override
    public Void visitSuperinterfaces(Java9Parser.SuperinterfacesContext ctx) {
        Void result = super.visitSuperinterfaces(ctx);

        // Append the subject interface to the implements list
        if(ctx.getRuleIndex() == insertImplementsAtRuleIdx) {
            outputTemplate(", <statementSubjectInterfaceSimpleName>");
            insertImplementsAtRuleIdx = 0;
        }

        return result;
    }

    @Override
    public Void visitSuperclass(Java9Parser.SuperclassContext ctx) {
        Void result = super.visitSuperclass(ctx);
        // Add the 'implements subject' code to the class but after the 'extends' clause
        if(ctx.getRuleIndex() == insertImplementsAtRuleIdx) {
            outputTemplate(" implements <statementSubjectInterfaceSimpleName>");
            insertImplementsAtRuleIdx = 0;
        }

        return result;
    }

    private Pattern lhsWhitespaceSplitPattern = Pattern.compile("(^\\s*)(.*)", Pattern.DOTALL);

    private String lhsWhitespaceSplitTransform(String token, BiFunction<String, String, String> splitTransform) {
        Matcher matcher = lhsWhitespaceSplitPattern.matcher(token);
        if(matcher.matches()) {
            return splitTransform.apply(matcher.group(1), matcher.group(2));
        }
        return splitTransform.apply("", token);
    }

    private boolean isStatementBlock(ParseTree ctx) {
        return XPath.findAll(ctx, "/*/statementWithoutTrailingSubstatement/block", parser).size() == 1;
    }

    // Process the 'then' portion of if-then-else when it is not enclosed in a statement block
    @Override
    public Void visitStatementNoShortIf(Java9Parser.StatementNoShortIfContext ctx) {
        if(isStatementBlock(ctx)) {
            return super.visitStatementNoShortIf(ctx);
        }

        if(ctx.getParent().getRuleIndex() == Java9Parser.RULE_ifThenElseStatement) {
            return setRuleTransform(super::visitStatementNoShortIf, ctx,
                    (token) -> lhsWhitespaceSplitTransform(token,
                            (lhsWS, rhsToken) -> lhsWS + "{ tick(); " + rhsToken + " }"));
        }

        return super.visitStatementNoShortIf(ctx);
    }

    @Override
    public Void visitLambdaBody(Java9Parser.LambdaBodyContext ctx) {

        // No special processing needed at this point if the lambda expresssion is already in a block
        if(ctx.getChildCount() == 1 && ctx.getChild(0) instanceof Java9Parser.BlockContext) {
            return super.visitLambdaBody(ctx);
        }

        return setRuleTransform(super::visitLambdaBody, ctx,
                (token) -> lhsWhitespaceSplitTransform(token,
                        (lhsWS, rhsToken) -> lhsWS + "{ tick(); " + rhsToken + "; }"));

    }

    @Override
    public Void visitStatement(Java9Parser.StatementContext ctx) {
        return visitStatementType((context) -> super.visitStatement((Java9Parser.StatementContext)context), ctx);
    }

    @Override
    public Void visitLocalVariableDeclarationStatement(Java9Parser.LocalVariableDeclarationStatementContext ctx) {
        return visitStatementType((context) -> super.visitLocalVariableDeclarationStatement((Java9Parser.LocalVariableDeclarationStatementContext)context), ctx);
    }

    // Prefix the actual 'if' statement and the 'else' part of the if-then-else. Add enclosing block
    // when else statement is not a statement block.
    private Void visitStatementType(Function<ParserRuleContext, Void> superMethod, ParserRuleContext ctx) {
        // This prevents placing a 'tick()' in front of a statement block
        // For example we don't want: 'if (true) { stuff...} else tick() { stuff... }
        if(isStatementBlock(ctx)) {
            return superMethod.apply(ctx);
        }

        // We need to enclose this single statement in block quotes to insert the tick()
        if(ctx.getParent().getRuleIndex() == Java9Parser.RULE_ifThenElseStatement) {
            return setRuleTransform(superMethod, ctx,
                    (token) -> lhsWhitespaceSplitTransform(token,
                            (lhsWS, rhsToken) -> lhsWS + "{ tick(); " + rhsToken + " }"));
        }

        // Skip pre-pending tick if this is before the start of a block
        return setRuleTransform(superMethod, ctx,
                (token) -> lhsWhitespaceSplitTransform(token,
                        (lhsWS, rhsToken) -> lhsWS + "tick(); " + rhsToken));

    }

    private List<ParseTree> getRightParseTreeArray(ParseTree ctx) {
        return getRightParseTreeArray(ctx, (childCtx) -> true);
    }

    private List<ParseTree> getRightParseTreeArray(ParseTree ctx, Function<ParseTree, Boolean> filter) {
        List<ParseTree> parseTreeList = new ArrayList<>();

        final ParseTree parent = ctx.getParent();

        for(int i = parent.getChildCount() - 1; i >= 0 && parent.getChild(i) != ctx; --i) {
            if(filter.apply(parent.getChild(i))) {
                parseTreeList.add(parent.getChild(i));
            }
        }
        return Lists.reverse(parseTreeList);
    }

    private List<ParseTree> getLeftParseTreeArray(ParseTree ctx) {
        return getLeftParseTreeArray(ctx, (childCtx) -> true);
    }

    private List<ParseTree> getLeftParseTreeArray(ParseTree ctx, Function<ParseTree, Boolean> filter) {
        List<ParseTree> parseTreeList = new ArrayList<>();

        final ParseTree parent = ctx.getParent();

        for(int i = 0; i < parent.getChildCount() && parent.getChild(i) != ctx; ++i) {
            if(filter.apply(parent.getChild(i))) {
                parseTreeList.add(parent.getChild(i));
            }
        }
        return  parseTreeList;
    }


    @Override
    public Void visitClassBodyDeclaration(Java9Parser.ClassBodyDeclarationContext ctx) {

        Void result = super.visitClassBodyDeclaration(ctx);

        if(parentRuleHistory.getParentRuleStack(Java9Parser.RULE_classBody).size() != 1) {
            return result;
        }

        List<ParseTree> rightParseTreeList = getRightParseTreeArray(ctx);

        if(rightParseTreeList.size() != 1 || !rightParseTreeList.get(0).getText().equals("}")) {
            return result;
        }

        outputTemplate(getResourceText("generator/SubjectDeclTemplate.tmpl"));

        return result;
    }

    private String processLHSWhiteSpace(int tokenIndex) {

        StringBuilder whiteSpaceString = new StringBuilder();

        List<Token> methodChannel = commonTokenStream.getHiddenTokensToLeft(tokenIndex, Java9Lexer.WHITE_SPACE);

        if(methodChannel != null) {
            for(Token methodText : methodChannel) {
                String text = methodText.getText();
                whiteSpaceString.append(text);
            }
        }

        return whiteSpaceString.toString();
    }

    @Override
    public Void visit(ParseTree tree) {
        Void result = super.visit(tree);

        return result;
    }

    @Override
    public Void visitTerminal(TerminalNode node) {

        if(node.getSymbol().getType() == Java9Lexer.EOF) {
            return null;
        }

        output(processLHSWhiteSpace(node.getSymbol().getTokenIndex()) + node.getText());

        return super.visitTerminal(node);
    }


}

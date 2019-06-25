package com.redwyvern.statementobserver.generator;

import com.redwyvern.javasource.Java9Parser;
import com.redwyvern.javasource.Java9ParserBaseVisitor;
import com.redwyvern.statementobserver.VisitorParentRuleHistory;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.pattern.ParseTreeMatch;
import org.antlr.v4.runtime.tree.pattern.ParseTreePattern;
import org.antlr.v4.runtime.tree.xpath.XPath;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class SubjectPreprocessorVisitor  extends Java9ParserBaseVisitor<Void> {

    private final SubjectPreprocessResult result = new SubjectPreprocessResult();
    private final Java9Parser parser;
    private final VisitorParentRuleHistory parentRuleHistory = new VisitorParentRuleHistory();

    public SubjectPreprocessorVisitor(Java9Parser parser) {
        this.parser = parser;
    }

    @Override
    public Void visitChildren(RuleNode node) {
        return parentRuleHistory.processRuleNode(node, super::visitChildren);
    }

    @Override
    public Void visitPackageDeclaration(Java9Parser.PackageDeclarationContext ctx) {
        result.setHasPackageDeclaration(true);
        return super.visitPackageDeclaration(ctx);
    }

    private List<ParseTreeMatch> findAllOfPattern(ParseTree parseTree, String patternString, int rule, String xPath) {
        ParseTreePattern pattern = parser.compileParseTreePattern(patternString, rule);
        return pattern.findAll(parseTree, xPath);
    }

    private boolean hasPatternMatch(ParseTree parseTree, String patternString, int rule, String xPath) {
        return findAllOfPattern(parseTree, patternString, rule, xPath).size() > 0;
    }


    private List<String> findAllOfPatternVariable(ParseTree parseTree, String patternString, int rule, String xPath, String variable) {

        return findAllOfPattern(parseTree, patternString, rule, xPath).stream()
                .map((match) -> match.get(variable).getText()).collect(Collectors.toList());
    }


    @Override
    public Void visitOrdinaryCompilation(Java9Parser.OrdinaryCompilationContext ctx) {

        List<String> importList =
                findAllOfPatternVariable(ctx, "import <typeName>;", Java9Parser.RULE_singleTypeImportDeclaration, "//importDeclaration/*", "typeName");

        result.setImports(new HashSet<>(importList));
        if(!importList.isEmpty()) {
            result.setLastImportPackage(importList.get(importList.size() - 1));
        }

        return super.visitOrdinaryCompilation(ctx);
    }

    @Override
    public Void visitNormalClassDeclaration(Java9Parser.NormalClassDeclarationContext ctx) {

        // If this is a nested/inner class then skip it
        if(parentRuleHistory.getTopParentRule(Java9Parser.RULE_normalClassDeclaration).isPresent()) {
            return super.visitNormalClassDeclaration(ctx);
        }

        result.setHasExtends(XPath.findAll(ctx, "//superclass", parser).size() > 0);
        result.setHasImplements(XPath.findAll(ctx, "//superinterfaces", parser).size() > 0);

        return super.visitNormalClassDeclaration(ctx);
    }

    public SubjectPreprocessResult getResult() {
        return result;
    }
}

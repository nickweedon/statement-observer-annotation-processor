package com.redwyvern.statementobserver.extractor;

import com.google.common.collect.Range;
import com.redwyvern.javasource.Java9Parser;
import com.redwyvern.javasource.Java9ParserBaseVisitor;
import com.redwyvern.statementobserver.codemodel.ClassFileCode;
import com.redwyvern.statementobserver.codemodel.CodeLine;
import com.redwyvern.statementobserver.codemodel.Statement;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class JavaStatementRecognizerVisitor extends Java9ParserBaseVisitor<String> {

    private final ClassFileCode classFileCode;
    private final CommonTokenStream commonTokenStream;

    @Getter
    static class NestedStatementVisitor extends Java9ParserBaseVisitor<String> {
        private int tokenIndex = Integer.MAX_VALUE;
        private boolean isBlockStart = false;
        private boolean isStatement;

        @Override
        public String visitStatement(Java9Parser.StatementContext ctx) {

            isStatement = true;
            tokenIndex = ctx.start.getTokenIndex();
            return null;
        }

        @Override
        public String visitLocalVariableDeclarationStatement(Java9Parser.LocalVariableDeclarationStatementContext ctx) {

            isStatement = true;
            tokenIndex = ctx.start.getTokenIndex();
            return null;
        }

        @Override
        public String visitBlock(Java9Parser.BlockContext ctx) {
            tokenIndex = ctx.start.getTokenIndex();
            isBlockStart = true;
            return null;
        }

        @Override
        public String visitSwitchBlock(Java9Parser.SwitchBlockContext ctx) {
            // If this is a switch statement then consider the statement block to end
            // at the beginning of the the switch statement
            tokenIndex = ctx.start.getTokenIndex();
            return null;
        }
    }

    private void parseStatement(ParserRuleContext ctx, int stopTokenIndex) {

        Token startToken = ctx.start;
        int nextTokenIndex = ctx.start.getTokenIndex() + 1;

        CodeLine firstCodeLine, lastCodeLine;
        firstCodeLine = lastCodeLine = classFileCode.getCodeLineMap().get(startToken.getLine());

        if(firstCodeLine == null) {
            //throw new ParseException("No code line found for statement starting at line: " + startToken.getLine() + " => " + startToken.getText());
            // Just skip this statement if we don't care about it
            return;
        }
        if(firstCodeLine.getStatement() != null) {
            //TODO: Fix this

            StringBuilder currentStatement = new StringBuilder();
            for(int i = startToken.getTokenIndex(); i < stopTokenIndex; ++i) {
                Token currentToken = commonTokenStream.get(i);
                currentStatement.append(currentToken.getText());
            }
            //throw new RuntimeException("More than one statement found at code line: " + firstCodeLine.getCode());

            throw new RuntimeException("More than one statement found at code line " + firstCodeLine.getLineNumber() + "'\n"
                    + "Previous full statement: '" + firstCodeLine.getStatement().getStatementCode() + "'\n"
                    + "Previous individual code line: '" + firstCodeLine.getCode() + "'\n"
                    + "Current code being parsed: '" + currentStatement + "'"
            );

/*
            throw new ParseException("More than one statement found at code line: " + codeLine.getCode() + "Previous statement: '"
                    + codeLine.getCode().substring(
                            codeLine.getStatementRange().lowerEndpoint(), codeLine.getStatementRange().upperEndpoint())
                    + "' Additional statement first token: '" + startToken.getText() + "'"
            );
*/
            // Just skip this statement if we don't care about it
            //return super.visitStatement(ctx);
        }
        Token lastToken, currentToken;
        lastToken = currentToken = startToken;

        List<CodeLine> codeLines = new ArrayList<>();

        codeLines.add(firstCodeLine);

        //while(currentToken.getLine() == startToken.getLine() && nextTokenIndex <= stopTokenIndex + 1) {
        while(nextTokenIndex <= stopTokenIndex + 1) {
            if(currentToken.getLine() != lastCodeLine.getLineNumber()) {
                lastCodeLine = classFileCode.getCodeLineMap().get(currentToken.getLine());
                if(lastCodeLine == null) {
                    throw new RuntimeException("Statement spans line that is not a code line.");
                }
                codeLines.add(lastCodeLine);
            }
            lastToken = currentToken;
            currentToken = commonTokenStream.get(nextTokenIndex++);
        }

        Statement statement = new Statement(
                Range.closed(
                        startToken.getCharPositionInLine() - firstCodeLine.getClassMethodCode().getMethodIndent(),
                        lastToken.getCharPositionInLine() - firstCodeLine.getClassMethodCode().getMethodIndent()
                ),
                Range.closed(
                        firstCodeLine,
                        lastCodeLine
                )
        );

        for(CodeLine codeLine : codeLines) {
            codeLine.setStatement(statement);
        }

    }

    @Override
    public String visitLocalVariableDeclarationStatement(Java9Parser.LocalVariableDeclarationStatementContext ctx) {

        NestedStatementVisitor nestedStatementVisitor = new NestedStatementVisitor();
        if(ctx.getChildCount() > 0) {
            ctx.getChild(0).accept(nestedStatementVisitor);
        }

        final int tokenDistance = nestedStatementVisitor.getTokenIndex() - ctx.start.getTokenIndex();

        // If token distance is zero than this indicates that the statement 'is a' block. If it is non-zero
        // then this indicates that the statement 'has a' block.
        if(nestedStatementVisitor.isBlockStart && tokenDistance == 0) {
            return super.visitLocalVariableDeclarationStatement(ctx);
        }

        int stopTokenIndex = ctx.stop.getTokenIndex();

        /* TODO: This could later be enhanced so that there is the notion of having a line belong to more than one statement
            The tricky part would be ensuring that the correct statement is selected when performing the line lookup from
            the stack trace.
         */
        // If the nested statement visitor hit something
        if(nestedStatementVisitor.getTokenIndex() != Integer.MAX_VALUE) {
            int nestedStatementStartTokenIndex = nestedStatementVisitor.getTokenIndex();
            Token nestedStatementStartToken = commonTokenStream.get(nestedStatementStartTokenIndex);

            int startLine = ctx.start.getLine();

            if(nestedStatementStartToken.getLine() != ctx.stop.getLine()) {

                Token currentToken = nestedStatementStartToken;
                stopTokenIndex = nestedStatementStartTokenIndex;

                // Backtrack all the way to the first line of the original statement
                //noinspection StatementWithEmptyBody
                while(stopTokenIndex > 0 && currentToken.getLine() > startLine) {
                    --stopTokenIndex;
                    currentToken = commonTokenStream.get(stopTokenIndex);
                }
            }
        }

        parseStatement(ctx, stopTokenIndex);
        return super.visitLocalVariableDeclarationStatement(ctx);
    }


    @Override
    public String visitStatement(Java9Parser.StatementContext ctx) {

        NestedStatementVisitor nestedStatementVisitor = new NestedStatementVisitor();
        if(ctx.getChildCount() > 0) {
            ctx.getChild(0).accept(nestedStatementVisitor);
        }

        final int tokenDistance = nestedStatementVisitor.getTokenIndex() - ctx.start.getTokenIndex();

        // If token distance is zero than this indicates that the statement 'is a' block. If it is non-zero
        // then this indicates that the statement 'has a' block.
        if(nestedStatementVisitor.isBlockStart && tokenDistance == 0) {
            return super.visitStatement(ctx);
        }

        int stopTokenIndex = ctx.stop.getTokenIndex();

        /* TODO: This could later be enhanced so that there is the notion of having a line belong to more than one statement
            The tricky part would be ensuring that the correct statement is selected when performing the line lookup from
            the stack trace.
         */
        // If the nested statement visitor hit something
        if(nestedStatementVisitor.getTokenIndex() != Integer.MAX_VALUE) {
            int nestedStatementStartTokenIndex = nestedStatementVisitor.getTokenIndex();
            Token nestedStatementStartToken = commonTokenStream.get(nestedStatementStartTokenIndex);

            int startLine = ctx.start.getLine();

            if(nestedStatementStartToken.getLine() != ctx.stop.getLine()) {

                Token currentToken = nestedStatementStartToken;
                stopTokenIndex = nestedStatementStartTokenIndex;

                // Backtrack all the way to the first line of the original statement
                //noinspection StatementWithEmptyBody
                while(stopTokenIndex > 0 && currentToken.getLine() > startLine) {
                    --stopTokenIndex;
                    currentToken = commonTokenStream.get(stopTokenIndex);
                }
            }
        }

        parseStatement(ctx, stopTokenIndex);
        return super.visitStatement(ctx);
    }

    @Override
    public String visitBlockStatement(Java9Parser.BlockStatementContext ctx) {

        NestedStatementVisitor nestedStatementVisitor = new NestedStatementVisitor();
        if(ctx.getChildCount() > 0) {
            ctx.getChild(0).accept(nestedStatementVisitor);
        }

        if(nestedStatementVisitor.isBlockStart) {
            return super.visitBlockStatement(ctx);
        }

        // Make sure we don't parse the same statement twice
        if(nestedStatementVisitor.isStatement) {
            return super.visitBlockStatement(ctx);
        }

        int stopTokenIndex = ctx.stop.getTokenIndex();

        parseStatement(ctx, stopTokenIndex);

        return super.visitBlockStatement(ctx);
    }
}

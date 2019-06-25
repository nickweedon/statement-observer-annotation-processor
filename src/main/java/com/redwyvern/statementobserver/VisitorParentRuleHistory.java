package com.redwyvern.statementobserver;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.RuleNode;

import java.util.*;
import java.util.function.Function;

public class VisitorParentRuleHistory {

    private Map<Integer, Stack<ParserRuleContext>> parentRuleMap = new HashMap<>();

    public Optional<ParserRuleContext> getTopParentRule(int rule) {
        Stack<ParserRuleContext> parserRuleContextStack = parentRuleMap.get(rule);
        if(parserRuleContextStack == null) {
            return Optional.empty();
        }
        return Optional.of(parserRuleContextStack.peek());
    }

    public Stack<ParserRuleContext> getParentRuleStack(int rule) {
        Stack<ParserRuleContext> parserRuleContextStack = parentRuleMap.get(rule);
        return Objects.requireNonNullElseGet(parserRuleContextStack, Stack::new);
    }

    // Build the rule map, adding and removing nodes accordingly as the tree is traversed
    public Void processRuleNode(RuleNode node, Function<RuleNode, Void> visitChildren) {

        ParserRuleContext parserRuleContext = (ParserRuleContext)node;

        final int ruleIndex = parserRuleContext.getRuleContext().getRuleIndex();

        parentRuleMap
                .computeIfAbsent(ruleIndex, (index) -> new Stack<>())
                .push(parserRuleContext);

        Void result = visitChildren.apply(node);

        Stack<ParserRuleContext> parentRuleStack = parentRuleMap.get(ruleIndex);
        parentRuleStack.pop();
        if(parentRuleStack.size() == 0) {
            parentRuleMap.remove(ruleIndex);
        }

        return result;
    }


}

package au.org.weedon.redblacktree;

import com.redwyvern.statementobserver.StatementObservable;
import com.redwyvern.statementobserver.StatementSubject;

@StatementObservable
public class HelloWorldSubject implements StatementSubject {

    private void addNode(RBNode<V> currentNode, RBNode<V> valueNode) {

        tick(); if(compareNodes(valueNode.getValue(), currentNode.getValue()) > 0) {
            tick(); if(currentNode.getLeft().isNil()) {
                tick(); currentNode.setLeft(valueNode);
                tick(); valueNode.setParent(currentNode);
            } else {
                tick(); addNode(currentNode.getLeft(), valueNode);
            }
        } else {
            tick(); if(currentNode.getRight().isNil()) {
                tick(); currentNode.setRight(valueNode);
                tick(); valueNode.setParent(currentNode);
            } else {
                tick(); addNode(currentNode.getRight(), valueNode);
            }
        }
    }

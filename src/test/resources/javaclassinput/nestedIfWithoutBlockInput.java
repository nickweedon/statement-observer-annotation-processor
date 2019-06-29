package au.org.weedon.redblacktree;

import com.redwyvern.statementobserver.StatementObservable;

@StatementObservable
public class HelloWorld {

    private void addNode(RBNode<V> currentNode, RBNode<V> valueNode) {

        if(compareNodes(valueNode.getValue(), currentNode.getValue()) > 0) {
            if(currentNode.getLeft().isNil())
                currentNode.setLeft(valueNode);
            else
                addNode(currentNode.getLeft(), valueNode);
        } else {
            if(currentNode.getRight().isNil())
                currentNode.setRight(valueNode);
            else
                addNode(currentNode.getRight(), valueNode);
        }
    }
}

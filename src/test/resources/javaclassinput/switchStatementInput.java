package au.org.weedon.redblacktree;

import java.util.*;

public class RBTree<V extends Comparable> {

    public static class DFSNodeIterator<V> implements Iterator<RBNode<V>> {

        public enum TraversalOrder {Preorder, Inorder, Postorder};
        private TraversalOrder traversalOrder;
        private RBNode<V> queuedNode;

        private RBNode<V> getNextNode() {
            while (!dfsStack.empty()) {
                BinaryTreeStackFrame<V> stackFrame = dfsStack.pop();
                RBNode<V> node = stackFrame.getRbNode();

                switch (stackFrame.getTraversalState()) {
                    case Preorder: {
                        stackFrame.setTraversalState(TraversalOrder.Inorder);
                        dfsStack.add(stackFrame);
                        if (!node.getLeft().isNil()) {
                            dfsStack.add(new BinaryTreeStackFrame<V>(node.getLeft()));
                        }
                        if (traversalOrder == TraversalOrder.Preorder) {
                            return node;
                        }
                        break;
                    }
                    case Inorder: {
                        stackFrame.setTraversalState(TraversalOrder.Postorder);
                        dfsStack.add(stackFrame);
                        if (!node.getRight().isNil()) {
                            dfsStack.add(new BinaryTreeStackFrame<V>(node.getRight()));
                        }
                        if (traversalOrder == TraversalOrder.Inorder) {
                            return node;
                        }
                        break;
                    }
                    case Postorder: {
                        if (traversalOrder == TraversalOrder.Postorder) {
                            return node;
                        }
                        break;
                    }
                }
            }
            return null;
        }
    }
}
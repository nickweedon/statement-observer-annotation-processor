package au.org.weedon.redblacktree;

import java.util.*;

public class RBTree<V extends Comparable> {

    public static class DFSNodeIterator<V> implements Iterator<RBNode<V>> {

        public enum TraversalOrder {Preorder, Inorder, Postorder};
        private TraversalOrder traversalOrder;
        private RBNode<V> queuedNode;

        @Override
        public RBNode<V> next() {
            RBNode<V> returnedNode = queuedNode != null
                    ? queuedNode
                    : getNextNode();

            queuedNode = null;

            if(returnedNode == null) {
                throw new RuntimeException("DFSNodeIterator next() called before calling hasNext()");
            }

            return returnedNode;
        }

        public void add(V value) {

            if(head.isNil()) {
                head = nodeBuilder
                            .setValue(value)
                            .build();
                return;
            }

            addNode(head, nodeBuilder
                            .setValue(value)
                            .build());
        }

    }
}
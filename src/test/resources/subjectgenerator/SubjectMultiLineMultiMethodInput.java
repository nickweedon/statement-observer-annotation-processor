package au.org.weedon.redblacktree;

import java.util.*;
import com.redwyvern.statementobserver.StatementObservable;
import com.redwyvern.statementobserver.StatementSubject;

@StatementObservable
public class RBTreeSubject<V extends Comparable> {

    public static class DFSNodeIterator<V> implements Iterator<RBNode<V>>, StatementSubject {

        public enum TraversalOrder {Preorder, Inorder, Postorder};
        private TraversalOrder traversalOrder;
        private RBNode<V> queuedNode;

        @Override
        public RBNode<V> next() {
            tick(); RBNode<V> returnedNode = queuedNode != null
                    ? queuedNode
                    : getNextNode();

            tick(); queuedNode = null;

            tick(); if(returnedNode == null) {
                tick(); throw new RuntimeException("DFSNodeIterator next() called before calling hasNext()");
            }

            tick(); return returnedNode;
        }

        public void add(V value) {

            tick(); if(head.isNil()) {
                tick(); head = nodeBuilder
                            .setValue(value)
                            .build();
                tick(); return;
            }

            tick(); addNode(head, nodeBuilder
                            .setValue(value)
                            .build());
        }

    }

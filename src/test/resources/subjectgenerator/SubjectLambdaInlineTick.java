package au.org.weedon.redblacktree;

import java.util.*;

public class RBTree<V extends Comparable> implements com.redwyvern.statementobserver.StatementSubject {

    private RBNodeBuilder<V> nodeBuilder = new RBNodeBuilder<>();
    private RBNode<V> head;

    public Iterable<RBNode<V>> iterateNodesDFS(DFSNodeIterator.TraversalOrder traversalOrder) {
        tick(); return () -> { tick(); new DFSNodeIterator<>(head, traversalOrder); }
    }
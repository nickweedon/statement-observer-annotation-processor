package au.org.weedon.redblacktree;

import java.util.*;
import com.redwyvern.statementobserver.StatementObservable;
import com.redwyvern.statementobserver.StatementSubject;

public class RBTreeSubject<V extends Comparable> implements StatementSubject {

    private RBNodeBuilder<V> nodeBuilder = new RBNodeBuilder<>();
    private RBNode<V> head;

    public Iterable<RBNode<V>> iterateNodesDFS(DFSNodeIterator.TraversalOrder traversalOrder) {
        tick(); return () -> {
            tick(); System.out.println("Hello world!");
            tick(); return new DFSNodeIterator<>(head, traversalOrder);
        };
    }
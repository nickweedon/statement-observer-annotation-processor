package au.org.weedon.redblacktree;

import java.util.*;

public class RBTree<V extends Comparable> {

    private RBNodeBuilder<V> nodeBuilder = new RBNodeBuilder<>();
    private RBNode<V> head;

    public Iterable<RBNode<V>> iterateNodesDFS(DFSNodeIterator.TraversalOrder traversalOrder) {
        return () -> {
            return new DFSNodeIterator<>(head, traversalOrder);
        };
    }
}
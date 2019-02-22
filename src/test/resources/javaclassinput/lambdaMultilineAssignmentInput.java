package au.org.weedon.redblacktree;

import java.util.*;

public class RBTree<V extends Comparable> {

    public Iterable<RBNode<V>> iterateNodesDFS(DFSNodeIterator.TraversalOrder traversalOrder) {
        Iterable<RBNode<V>> sillyIterator = () -> {
            return new DFSNodeIterator<>(head, traversalOrder);
        };
        return null;
    }
}

Iterable<RBNode<V>> RBTree#iterateNodesDFS(DFSNodeIterator.TraversalOrder traversalOrder) {
    **Iterable<RBNode<V>> sillyIterator = () -> **{
        **return new DFSNodeIterator<>(head, traversalOrder)**;
    };
    **return null**;
}

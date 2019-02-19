
RBNode<V> RBTree.DFSNodeIterator#next() {
    **RBNode<V> returnedNode = queuedNode != null
            ? queuedNode
            : getNextNode()**;
    **queuedNode = null**;
    **if(returnedNode == null) **{
        **throw new RuntimeException("DFSNodeIterator next() called before calling hasNext()")**;
    }
    **return returnedNode**;
}


void RBTree.DFSNodeIterator#add(V value) {
    **if(head.isNil()) **{
        **head = nodeBuilder
                    .setValue(value)
                    .build()**;
        **return**;
    }
    **addNode(head, nodeBuilder
                    .setValue(value)
                    .build())**;
}

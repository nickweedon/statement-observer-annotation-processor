void HelloWorld#addNode(RBNode<V> currentNode, RBNode<V> valueNode) {
    **if(compareNodes(valueNode.getValue(), currentNode.getValue()) > 0) {**
        **if(currentNode.getLeft().isNil()) {**
            **currentNode.setLeft(valueNode)**;
            **valueNode.setParent(currentNode)**;
        } else {
            **addNode(currentNode.getLeft(), valueNode)**;
        }
    } else {
        **if(currentNode.getRight().isNil()) {**
            **currentNode.setRight(valueNode)**;
            **valueNode.setParent(currentNode)**;
        } else {
            **addNode(currentNode.getRight(), valueNode)**;
        }
    }
}
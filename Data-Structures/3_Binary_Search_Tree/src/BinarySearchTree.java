import java.util.ArrayList;

/** BinarySearchTree is the construction and operation class for a binary search tree, using nodes from the Node class.
 * @author Alexander Schoolcraft
 * @version 1.0 04/07/2023: Complete
 * @version 1.1 04/11/2023: Updated some of the methods of efficiency, finalized comments, and cleaned up the code some for readability
 * @version 2.0 04/13/2023: added a toString method, and fixed some logic issues
 */
public class BinarySearchTree {
	Node root;

	/** Constructor class that creates an empty tree (root is null)
	 */
	public BinarySearchTree() {
		this.root = null;
	}
	/** Constructor class that creates a tree rooted at the passed parameter
	 * @param root: Node to root the tree at
	 */
	public BinarySearchTree(Node root) {
		this.root = root;
	}

	/** isEmpty check if the tree's root is null, and if it is, returns true. Otherwise returns false.
	 * @return Boolean: true if the root (and thus tree) is null, false otherwise
	 */
	public boolean isEmpty() {
	    if (this.root == null) {
		return true;
	    }
	    else {
		return false;
	    }
	}

	/** setRoot sets the root of an empty tree to a new node that stores the given parameter
	 * @param e: Integer to store as the element of the new root node
	 * @return boolean: true if the operation was completed successfully, false otherwise
	 */
	public boolean setRoot(Integer e) {
	    if (this.isEmpty()) {
		this.root=new Node(e);
		return true;
	    }
	    else {
		return false;
	    }
	}

	/** size returns the number of nodes recursively for a tree or subtree rooted at a given node.
	 * @param root: Node that is the root of the tree or subtree
	 * @return int: total number of nodes found on the tree, or -1 if the passed root is not on the tree
	 */
	public int size(Node root) {
	    if (this.isEmpty()) { //base case, if tree is empty, size is 0
		return 0;
	    }
	    if (this.search(this.root, root.getElement()) == null) { //if tree is not empty, but the node passed as the root is not on the tree
		return -1;
	    }
	    
	    //series of 4 if/else-if statements to determine how to recursively call this method depending on which children the root node has
	    if (root.getLeft() != null && root.getRight() != null) {
		return (this.size(root.getLeft()) + this.size(root.getRight())) + 1;
	    }
	    else if (root.getLeft() != null && root.getRight() == null) {
		return this.size(root.getLeft()) + 1;
	    }
	    else if (root.getLeft() == null && root.getRight() != null) {
		return this.size(root.getRight()) + 1;
	    }
	    else if (root.getLeft() == null && root.getRight() == null) {
		return 1;
	    }
	    return 0;
	}

	/** depth finds the depth from the root of the argument tree of a given Node
	 * @param target: Node that the method is calculating the depth of
	 * @return int: the depth of the Node, or -1 if the Node is not on the tree
	 */
	public int depth(Node target){
	    int depth = 0;
	    Node curr = this.root;
	    
	    //fail condition, target is not on the tree
	    if (this.search(this.root, target.getElement()) == null) {
		return -1;
	    }
	    
	    while (target.getElement() != curr.getElement()) { //while loop that moves down the tree from the root to the target node, iterating a counting variable each time
		depth++;
		if (curr.getElement() > target.getElement()) {
		    if (curr.getLeft() == null) { //should never be true, unless there is some sort of logic fault or a node is manually set incorrectly
			return -1;
		    }
		    curr = curr.getLeft();
		}
		if (curr.getElement() < target.getElement()) {
		    if (curr.getRight() == null) {//should never be true, unless there is some sort of logic fault or a node is manually set incorrectly
			return -1;
		    }
		    curr = curr.getRight();
		}
	    }
	    
	    return depth;
	}

	/** height finds the maximum number of steps to reach an external node from a given node, going down
	 * @param start: Node to act as the "root" of the subtree
	 * @return int: the height of the Node from the bottom, or -1 if the node is not on the tree
	 */
	public int height(Node start){
	    //fail condition, node is not on the tree
	    if (this.search(this.root, start.getElement()) == null) {
		return -1;
	    }
	    
	    //base case, node has no children
	    if (this.isExternal(start)) {
		return 1;
	    }
	    //three if/else statements to determine which direction to recursively call this method and count up the height
	    if (start.getLeft() != null && start.getRight() == null) {
		return this.height(start.getLeft()) + 1;
	    }
	    else if (start.getLeft() == null && start.getRight() != null) {
		return this.height(start.getRight()) + 1;
	    }
	    else {
		return Math.max(this.height(start.getLeft()), this.height(start.getRight())) + 1;
	    }
	}

	/** numChildren finds the number of direct children of target Node
	 * @param target: Node parent to determine how many children it has
	 * @return int: number of children of the target Node, or -1 if the node is not on the tree
	 */
	public int numChildren(Node target) {
	    //fail condition, target is not on the tree
	    if (this.search(this.root, target.getElement()) == null) {
		return -1;
	    }
	    
	    int numChildren = 0;
	    if (target.getLeft() != null) {
		numChildren += 1;
	    }
	    if (target.getRight() != null) {
		numChildren += 1;
	    }
	    return numChildren;
	}
	
	/** isInternal determines if the given Node is an internal Node, as defined by having at least one child
	 * @param target: Node to determine if it is internal or not
	 * @return boolean: true if it is internal, false otherwise
	 */
	public boolean isInternal(Node target) {
	    //fail condition, target is not on the tree
	    if (this.search(this.root, target.getElement()) == null) {
		return false;
	    }
	    return (this.numChildren(target) > 0);
	}
	
	/** isExternal determines if the given node is an external Node, as defined by having exactly 0 children
	 * @param target: Node to determine if it is external or not
	 * @return boolean: true if it is external, false otherwise
	 */
	public boolean isExternal(Node target) {
	    //fail condition, target is not on the tree
	    if (this.search(this.root, target.getElement()) == null) {
		return false;
	    }
	    return (this.numChildren(target) == 0);
	}
	
	/** findMin recursively moves through the list going through specifically the left children of each node, returning the left-most node, which should 
	 *     contain the lowest value.
	 * @param start: Node used as the root of the tree or subtree
	 * @return Node: the node that contains the lowest value and sits on the farthest left
	 */
	public Node findMin(Node start) {
	    //fail condition, start is not on the tree
	    if (this.search(this.root, start.getElement()) == null) {
		return null;
	    }
	    if (start.getLeft() == null) {
		return start;
	    }
	    else {
		return this.findMin(start.getLeft());
	    }
	}
	
	/** findMax recursively moves through the list going through specifically the right children of each node, returning the right-most node, which should 
	 *     contain the highest value.
	 * @param start: Node used as the root of the tree or subtree
	 * @return Node: the node that contains the highest value and sits on the farthest right
	 */
	public Node findMax(Node start) {
	    //fail condition, start is not on the tree
	    if (this.search(this.root, start.getElement()) == null) {
		return null;
	    }
	    if (start.getRight() == null) {
		return start;
	    }
	    else {
		return this.findMax(start.getRight());
	    }
	}
	
	/** insert finds the appropriate spot on the list to insert a new node by comparing the new element to the current node, and determining
	 *    which side to continue down, recursively calling itself until it gets to the location to insert it.
	 * @param newElement: Integer to make the new Node from
	 * @param start: Node root of the tree or subtree for insertion
	 * @return boolean: true if the Node is properly inserted, false otherwise
	 */
	public boolean insert(Integer newElement, Node start) {
	    if (start.getElement() > newElement && start.getLeft() == null) { //is less than current root, and root does not have a left child
		start.setLeft(new Node(newElement));                          //inserts the new node in the empty left child spot
		return true;
	    }
	    if (start.getElement() > newElement && start.getLeft() != null) { //is less than current root, but the root does have a left child
		return this.insert(newElement, start.getLeft());              //recursive call continuing using the root's left child as new root
	    }
	    if (start.getElement() < newElement && start.getRight() == null) { //is more than current root, and root does not have a right child
		start.setRight(new Node(newElement));                          //inserts the new node in the empty right child spot
		return true;
	    }
	    if (start.getElement() < newElement && start.getRight() != null) { //is more than current root, but the root does have a right child
		return this.insert(newElement, start.getRight());              //recursive call continuing using the root's right child as new root
	    }
	    return false;                                                      //insertion fails, most likely due to a duplicate node
	}
	
	/** search looks for a node with a given element value
	 * @param start: Node root of the tree or subtree
	 * @param target: int to find within the tree
	 * @return Node that contains the target value
	 */
	public Node search(Node start, int target) {
	    if (start.getElement() == target) {
		return start;
	    }
	    else if (start.getElement() > target && start.getLeft() != null) {
		return this.search(start.getLeft(), target);
	    }
	    else if (start.getElement() < target && start.getRight() != null) {
		return this.search(start.getRight(), target);
	    }
	    return null; //target is not in the tree
	}
	
	/** parent recursively iterates through the tree from the root down to identify the parent of a given node
	 * @param start: Node root of the tree or subtree
	 * @param target: Node to find the parent of
	 * @param parent: Node current parent of the start node
	 * @return Node: the parent of the target node
	 */
	public Node parent(Node start, Node target, Node parent) {
		if (start.getElement() == target.getElement()) {
		    return parent;
		}
		else {
		    if (start.getElement() > target.getElement() && start.getLeft() != null) {
			return this.parent(start.getLeft(), target, start);
		    }
		    if (start.getElement() < target.getElement() && start.getRight() != null) {
			return this.parent(start.getRight(), target, start);
		    }
		}
		return null; //fail condition, target node is not on the tree, or the tree is incorrectly constructed
	}
	
	/** remove deletes the node containing a target integer value, and replaces it according to remove algorithms
	 * @param start: Node root of the tree or subtree to find and remove
	 * @param target: int target of node element to remove
	 */
	public void remove(Node start, int target) {
	    //set up reference nodes to manipulate and remove
	    Node targetNode = this.search(start, target);
	    Node parentNode = this.parent(start, targetNode, null);
	    
	    if (targetNode == null) { //fail condition, no nodes contain the integer
		return;
	    }
	    
	    //if the target node has 2 children, replaces the target node with the minimum node on the subtree rooted at target's right child
	    if (targetNode.getRight() != null && targetNode.getLeft() != null) {
		Node replace = this.findMin(targetNode.getRight());
		Node replaceParent = this.parent(targetNode, replace, parentNode);

		if (targetNode.getElement() < parentNode.getElement()) {
		    parentNode.setLeft(replace);
		}
		else if (targetNode.getElement() > parentNode.getElement()) {
		    parentNode.setRight(replace);
		}
		if (targetNode.getLeft() != null) {
		    replace.setLeft(targetNode.getLeft());
		}
		if (targetNode.getRight() != null) {
		    replace.setRight(targetNode.getRight());
		}
		replaceParent.setLeft(null);
	    }
	    
	    //two if statements (with nested if statements to find correct side) for if there is only one child, to move that one up
	    if (targetNode.getRight() != null && targetNode.getLeft() == null) {
		if (targetNode.getElement() < parentNode.getElement()) {
		    parentNode.setLeft(targetNode.getRight());
		}
		if (targetNode.getElement() > parentNode.getElement()) {
		    parentNode.setRight(targetNode.getRight());
		}
	    }
	    if (targetNode.getRight() == null && targetNode.getLeft() != null) {
		if (targetNode.getElement() < parentNode.getElement()) {
		    parentNode.setLeft(targetNode.getLeft());
		}
		if (targetNode.getElement() > parentNode.getElement()) {
		    parentNode.setRight(targetNode.getLeft());
		}
	    }
	    
	    //if the target is external, just removes the node
	    if (this.isExternal(targetNode)) {
		if (targetNode.getElement() < parentNode.getElement()) {
		    parentNode.setLeft(null);
		}
		else if (targetNode.getElement() > parentNode.getElement()) {
		    parentNode.setRight(null);
		}
	    }
	}
	
	/** inOrder traverses the list following the In Order traversal algorithm (left child, self, right child, recursively), to put the elements in ascending order
	 * @param start: Node root of the tree or subtree to start at
	 * @param list: ArrayList<Integer> to store the elements into
	 * @return ArrayList<Integer>: the ordered list of elements
	 */
	public ArrayList<Integer> inOrder(Node start, ArrayList<Integer> list) {
	    if (start.getLeft() != null) {
		this.inOrder(start.getLeft(), list);
	    }
	    list.add(start.getElement());
	    if (start.getRight() != null) {
		this.inOrder(start.getRight(), list);
	    }
	    return list;
	}
	
	/** isIdentical takes two root nodes, and determines if both trees or subtrees they root are the same, i.e. have the same elements in each node
	 * and no node that the other does not have.
	 * @param xRoot: Node root of the first tree or subtree
	 * @param yRoot: Node root of the second tree or subtree
	 * @return boolean: true if the trees or subtrees are identical, false otherwise
	 */
	public boolean isIdentical(Node xRoot, Node yRoot) {
	    //several catch cases that show that the trees or subtrees are not identical
	    if (xRoot.getElement() != yRoot.getElement() ||                      //elements do not match
		    (xRoot.getLeft() != null && yRoot.getLeft() == null) ||      //one node has a left child, while the other does not
		    (xRoot.getLeft() == null && yRoot.getLeft() != null) ||
		    (xRoot.getRight() != null && yRoot.getRight() == null) ||    //one node has a right child, while the other does not
		    (xRoot.getRight() == null && yRoot.getRight() != null)) {
		return false;
	    }
	    //cascading if statements to check for the various possibilities of children, to recursively call this method to determine if the trees
	    //are identical
	    else {
		//base case, if the nodes are external and none of the above fail conditions are met, then they are identical
		if (this.isExternal(xRoot) && this.isExternal(yRoot)) {
		    return true;
		}
		//both nodes have left children, but no right children
		if (xRoot.getLeft() != null && yRoot.getLeft() != null && xRoot.getRight() == null && yRoot.getRight() == null) {
		    return this.isIdentical(xRoot.getLeft(), yRoot.getLeft());
		}
		//both nodes have right children, but no left children
		if (xRoot.getLeft() == null && yRoot.getLeft() == null && xRoot.getRight() != null && yRoot.getRight() != null) {
		    return this.isIdentical(xRoot.getRight(), yRoot.getRight());
		}
		//both nodes have both left and right children
		else {
		    return (this.isIdentical(xRoot.getLeft(), yRoot.getLeft()) && this.isIdentical(xRoot.getRight(), yRoot.getRight()));
		}
	    }
	}

	/** cutBranch removes the subtree rooted at start
	 * @param start: Node root of the subtree to remove
	 */
       public void cutBranch(Node start) {
	   //two if statements to recursively call to completely remove all children of the root passed
	   if (start.getLeft() != null) {
	       this.cutBranch(start.getLeft());
	   }
	   if (start.getRight() != null) {
	       this.cutBranch(start.getRight());
	   }
	   
	   //creates a parent node reference for the current node, sets it's left or right pointer to null, and sets the element of the current node to null
	   Node parentNode = this.parent(this.root, start, null);
	   if (start.getElement() < parentNode.getElement()) {
	       parentNode.setLeft(null);
	   }
	   if (start.getElement() > parentNode.getElement()) {
	       parentNode.setRight(null);
	   }
       }

        /** findLCA traverses the tree to find the lowest common ancestor of two given nodes, and returns that node.
         * @param xStart: Node, one of two to find the common ancestor of
         * @param yStart: Node, one of two to find the common ancestor of
         * @return Node: the lowest common ancestor of the two node, found by traversing the tree in the direction of the nodes, until that direction
         * 		differs
         */
        public Node findLCA (Node xStart, Node yStart) {
            if (this.isEmpty()) { //base case, if the argument tree is empty, both nodes do not exist on the tree
        	return null;
            }
            
            if (xStart == this.root || yStart == this.root) { //if either passed node is the root of the tree, it is the lowest common ancestor
        	return this.root;
            }
            
            if (this.search(this.root, xStart.getElement()) == null || this.search(this.root, yStart.getElement()) == null) { //checks that both nodes are actually on the tree, if not, returns null
        	return null;
            }
            
            Node curr = this.root;
            
            //this while loop traverses the tree from the root in the direction of the two nodes. as soon as the direction of the two nodes differs,
            //it breaks out of the loop, as that is the lowest common ancestor, and returns that node
            while (curr != null) {
        	if (xStart.getElement() > curr.getElement() && yStart.getElement() > curr.getElement()) {
        	    curr = curr.getRight();
        	}
        	if (xStart.getElement() < curr.getElement() && yStart.getElement() < curr.getElement()) {
        	    curr = curr.getLeft();
        	}
        	else {
        	    break;
        	}
            }
            
            return curr;
        }
        
        public String toString() {
            String ret = "";
            
            if (this.root.getLeft() == null) {
        	ret = this.root.toString();
            }
            
            if (this.root.getLeft() != null) {
        	ret = this.toString(ret, this.root.getLeft());
            }
            ret = ret.concat(this.root.toString());
            if (this.root.getRight() != null) {
        	ret = ret.concat(this.toString(ret, this.root.getLeft()));
            }
            
            return ret;
        }

	private String toString(String ret, Node curr) {
	    if (curr.getLeft() != null) {
		ret = this.toString(ret, curr.getLeft());
	    }
	    if (ret == null) {
		ret = curr.toString();
	    }
	    else {
		ret = ret.concat(curr.toString());
	    }
	    if (curr.getRight() != null) {
		ret = this.toString(ret, curr.getRight());
	    }
	    
	    return ret;
	}


}

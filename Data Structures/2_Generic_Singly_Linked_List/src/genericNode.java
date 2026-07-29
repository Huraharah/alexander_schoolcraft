/**
 * genericNode creates and operates the nodes of generic type <E> objects, as well as defines the comparable methods.
 * @author Alexander Schoolcraft
 * @version 2.23.2023
 */
public class genericNode <E extends Comparable<E>> {
	private E item;
	private genericNode<E> next;

	/** Constructor for a node that does not have a follow-on node, i.e. the last node in the list.
	 * @param item: Generic type <E> object to add to the contents of the node.
	 */
	public genericNode(E item) {
		this.item = item;
		this.next = null;
	}

	/** Constructor for a node that does have a node to point to, i.e. one that is in the middle or front of the list.
	 * @param item: Generic type <E> object to add to the contents of the node.
	 * @param next: The next node that this node will reference
	 */
	public genericNode(E item, genericNode<E> next) {
		this.item = item;
		this.next = next;
	}

	/** 4x getter and setter methods, one each for each parameter of the nodes.
	 */
	
	public void setItem(E item) {
		this.item = item;
	}
	
	public void setNext(genericNode<E> next) {
	    this.next = next;
	}
	
	public genericNode<E> getNext() {
		return next;
	}
	
	public E getItem() {
		return item;
	}
	
	/** compareTo that uses Comparable, that takes the item from the argument node and compares it to the target item, according to the parent object class of the items.
	 * @param target: Generic <E> type Comparable object to compare against the contents of the node.
	 * @return int, in compliance with the parent object class's method of comparison.
	 */
	public int compareTo(E target) {
	    return this.item.compareTo(target);
	}

	/** equals that uses Comparable, confirms if the contents of the argument node is the same as the target item, according to the parent object class of the items
	 * @param target: Generic <E> type Comparable object to compare against the contents of the node.
	 * @return boolean, true if the items are the same, and false if they are not, in compliance with the parent object class's method of comparison.
	 */
	public boolean equals (E target) {
	    return this.item.equals(target);
	}
	
	/** compareTo that uses Comparable, that takes the item from the node argument and compares it to the contents of the target node, according to the parent object class of the items.
	 * @param target: Generic node containing an <E> type Comparable object to compare against the contents of the node.
	 * @return int, in compliance with the parent object class's method of comparison.
	 */
	public int compareTo (genericNode<E> target) {
	    return this.item.compareTo(target.item);
	}
	
	/** equals that uses Comparable, confirms if the contents of the argument node is the same as the contents of the target node, according to the parent object class of the items
	 * @param target: Generic node containing an <E> type Comparable object to compare against the contents of the node.
	 * @return boolean, true if the items are the same, and false if they are not, in compliance with the parent object class's method of comparison.
	 */
	public boolean equals (genericNode<E> target) {
	    return this.item.equals(target.item);
	}
}

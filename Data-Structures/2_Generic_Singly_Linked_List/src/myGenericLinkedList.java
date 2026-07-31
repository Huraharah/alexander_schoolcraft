/**
 * myGenericLinkedList creates a linked list using the generic tag <E> to allow it to apply to any list of objects, tracked with a head and tail reference variable, and performs various functions on the list,
 * including adding and removing nodes, keeping the list sorted, and a variety of search options for the list.
 * @author Alexander Schoolcraft
 * @version 02.23.2023
 */
    @SuppressWarnings({"unused", "unchecked", "rawtypes"})
public class myGenericLinkedList <E extends Comparable> {
	genericNode head;//a reference variable refers to the first node of the linked list
	genericNode tail;//a reference variable refers to the last node of the linked list

	/** Basic constructor method that creates the head and tail reference variables, and sets them to null initially.
	 * @param n/a
	 * @return n/a
	 */
	public myGenericLinkedList(){
		head=null;
		tail=null;
	}

	/** isEmpty checks if the list is empty by verifying that both the head and tail are referencing null, and returns true if it is, and false otherwise.
	 * @param n/a
	 * @return boolean: True if the list is empty, false if it is not
	 */
	public boolean isEmpty(){
		if (this.head == null && this.tail == null) {
		    return true;
		}
		
		return false;
	}

	/** size iterates through the list with a while loop, increasing a counter for each node, then returns that counter, indicating the size of the list.
	 * @param n/a
	 * @return counter: int that is the total number of nodes in the list.
	 */
	public int size(){
		genericNode curr = this.head;
		int counter = 0;
		
		while (curr != null) {
		    counter ++;
		    curr = curr.getNext();
		}
		
		return counter;
	}

	/** search takes a target parameter of the generic type <E>, and searches through the list to confirm if the object is on the list, using the specific object class's equals method.
	 * @param target: generic <E> object to find
	 * @return boolean: true if the object is found in the list, false otherwise.
	 */
	public boolean search(E target){
		genericNode curr = this.head;
		
		while (curr != null) {
		    if (curr.equals(target)) {
			return true;
		    }
		    else {
			curr = curr.getNext();
		    }
		}
		
		return false;
	}

	/** getMax and getMin are a pair of methods that, due to the sorted nature of the list, return the tail and head referenced items from the list, respectively.
	 * @param n/a
	 * @return the last and first item on the list, respectively.
	 */
	public E getMax() {
		return (E)this.tail.getItem();
	}
	public E getMin() {
		return (E)this.head.getItem();
	}

	/**addFront takes a new object, and if the item is less than the current head, as ruled by the object class's compareTo method, or if the list is empty, creates a new node of that item, and adds it to 
	 * the front of the list.
	 * @param newItem: <E> type generic item to add to the front of the list.
	 * @return boolean: true if the item is added to the list, false if it is not for any reason.
	 */
	public boolean addFront(E newItem){
	    	//Checks if the list is empty through the isEmpty method, above, and if it is, creates a new node of the item, and sets both the head and tail references to the new node.
		if (this.isEmpty()) {
		    genericNode front = new genericNode (newItem);
		    this.head = front;
		    this.tail = this.head;
		    return true;
		}
		
		//If there is nodes already on the list, checks if the new item is less than the first node, and, if it is, adds it to the list, as a new node, in the front, making the connections as needed.
		if (this.head.compareTo(newItem) > 0) {
		    genericNode newNode = new genericNode(newItem, this.head);
		    this.head = newNode;
		    return true;
		}
		
		//If the new item is not less than the first node, the item will not be added.
		return false;
	}


	/** Similar to addFront, addEnd takes a new item, and checks if it belongs on the end of the list, and adds it there if it does.
	 * @param newItem: <E> type generic item to add to the end of the list.
	 * @return boolean: true if the item is added to the list, false if it is not for any reason.
	 */
	
	public boolean addEnd(E newItem){
	    	//Checks if the item is all ready on the list, and if it is, returns false, without adding the item, and exits the method.
		if (this.search(newItem)) {
		    return false;
		}
		
		//Checks if the list is empty, and if it is, adds the item as the only node, updating both reference variables in the process.
		if (this.isEmpty()) {
		    genericNode back = new genericNode (newItem);
		    this.head = back;
		    this.tail = this.head;
		    return true;
		}
		
		//If there are items in the list, compares the last item to the new item, and if the new item is greater, adds it to the end, and sets the tail reference to the new node.
		if (this.tail.compareTo(newItem) < 0) {
		    genericNode newNode = new genericNode(newItem);
		    this.tail.setNext(newNode);
		    this.tail = newNode;
		    return true;
		}
		
		//If the item is not greater than the tail node, the item will not be added.
		return false;
	}

	/** insert runs through the list, and creates a node containing the newItem, and sorts it into the proper location on the list, using the compareTo method.
	 * @param newItem: generic E-type item to add to the list
	 * @return boolean: true if the item is added to the list, false if it is not for any reason (usually it is all ready on the list)
	 */
	
	public boolean insert(E newItem){
		genericNode curr = this.head;
		genericNode prev = this.head;
		
		if (this.isEmpty()) { //if the list is empty, will add the newItem to a node at the front of the list, through the addFront node.
		    return this.addFront(newItem);
		}
	
		//while loop to determine if the item is anywhere in the list, and if it is, return false
		while (curr != null && newItem.compareTo(curr.getItem()) >= 0) {
		    if (curr.equals(newItem)) {
			return false;
		    }
		    prev = curr;
		    curr = curr.getNext();
		}
		
		//series of if statements to cover the potential special cases, IE the node goes in the front or back.
		if (curr == head) {
		    genericNode newNode = new genericNode(newItem, head);
		    head = newNode;
		    return true;
		}
		else if (curr != null) {
		    genericNode newNode = new genericNode(newItem, curr);
		    prev.setNext(newNode);
		    return true;
		}
		else if (curr == null) {
		    return this.addEnd(newItem);
		}
		
		//catch return, should be impossible to reach and it dead code.
		return false;
	}

	/** remove effectively deletes a node out of a list, by setting the prior node's next reference to the next node.
	 * @param target: Generic E-type item to remove.
	 * @return boolean: true if the item is on the list and is removed, false otherwise.
	 */
	public boolean remove(E target){
	    	genericNode curr = this.head;
		genericNode prev = this.head;
		
		if (this.isEmpty()) {
		    return false;
		}
		
		if (this.size() == 1 && curr.equals(target)) {
		    this.head = null;
		    this.tail = null;
		    return true;
		}
		if (this.head.equals(target)) {
		    return this.removeFirst();
		}
		
		while (curr != null) {
		    if (!curr.equals(target)) {
			prev = curr;
			curr = curr.getNext();
		    }
		    else if (curr.equals(target) && curr.getNext() != null) {
			prev.setNext(curr.getNext());
			return true;
		    }
		    else if (curr.equals(target) && curr == tail) {
			return this.removeLast();
		    }
		}
		
		return false;
	}

	/** removeFirst deletes the first node by pointing the "head" reference to the second.
	 * @param n/a
	 * @return boolean: true if there was a node to remove, false if the list is empty
	 */
	public boolean removeFirst(){
		if (this.isEmpty()) {
		    return false;
		}
		
		this.head = this.head.getNext();
		if (this.head.getNext() == null) {
		    this.tail = this.head;
		}
		
		return true;
		
	}

	/** removeLast deletes the first node by pointing the "tail" reference to the second to last node and removing it's pointer.
	 * @param n/a
	 * @return boolean: true if there was a node to remove, false if the list is empty
	 */
	public boolean removeLast(){
		if (this.isEmpty()) {
		    return false;
		}
		
		if (this.head.getNext() == null) {
		    this.head = null;
		    this.tail = null;
		    return true;
		}
		
	    	genericNode curr = this.head;
		
		while (curr.getNext().getNext() != null) {
		    curr = curr.getNext();
		}
		
		curr.setNext(null);
		this.tail = curr;
		
		return true;
	}

	/** merge takes the parameter list, and merges it with the argument list, while keeping the list sorted.
	 * @param anotherList: myGenericLinkedList that is of the same type of objects as the argument list, to be sorted into the argument list.
	 * @return n/a
	 */
	public void merge(myGenericLinkedList anotherList){	
	    	genericNode curr = anotherList.head;
	    	
	    	while (curr != null) {
	    	    this.insert((E)curr.getItem());  //calls the insert method above, inserting the node from the parameter list into the correct spot in the argument list.
	    	    curr = curr.getNext();
	    	    if (this.tail.getNext() != null) {
	    		this.tail = this.tail.getNext();
	    	    }
	    	}
	}

	/** middleElement finds and returns the content of the middle-most node of the list.
	 * @param n/a
	 * @return mid.getItem(): Contents of the middle-most node. If there are an even number of nodes, rounds up.
	 */
	public E middleElement(){
	    	if (this.head == null) {
	    	    return null;
	    	}
	    	
	    	if (this.head == this.tail) {
	    	    return (E) this.head.getItem();
	    	}
	    	
	    	genericNode curr = this.head;
		genericNode mid = this.head;
		
		while (curr != null && curr.getNext() != null) {
		    mid = mid.getNext();
		    curr = curr.getNext().getNext();
		}
		
		return (E)mid.getItem();
	}

	/** toString iterates through the list, building a String to return by calling the node contents' toString method, and concatenating the result to the string each cycle, then returns the full string.
	 * @param n/a
	 * @return printable: String containing the concatenated contents of all the nodes in the list, in compliance with the item's toString methods.
	 */
	public String toString(){
		String printable = "";
		genericNode curr = this.head;
		
		while (curr != null && curr.getNext() != null) {
		    printable += curr.getItem().toString() + " -> ";
		    curr = curr.getNext();
		}
		
		printable += curr.getItem().toString() + "\n";
		
		return printable;
	}
}

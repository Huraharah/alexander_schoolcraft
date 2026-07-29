public class Node {
	private Student item;
	private Node next;

	public Node(Student item) {
		this.item = item;
		this.next = null;
	}

	public Node(Student item, Node next) {
		this.item = item;
		this.next = next;
	}

	public Student getItem() {
		return item;
	}

	public void setItem(Student item) {
		this.item = item;
	}

	public Node getNext() {
		return next;
	}

	public void setNext(Node next) {
		this.next = next;
	}


}

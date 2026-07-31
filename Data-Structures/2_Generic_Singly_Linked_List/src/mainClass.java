
public class mainClass {

	public static void main(String[] args) {
	    	//the list of student objects to add to the generic linked list, that is initialized and parameterized as a Student list
		Student john= new Student("John","1111");
		Student lauren= new Student("Lauren","2222");
		Student phillip= new Student("Philip","3333");
		Student alicia= new Student("Alicia","4444");
		Student alex= new Student("Alex","5555");
		Student bridget= new Student("Bridget","6666");
		Student kyle= new Student("Kyle","7777");
		Student julie= new Student("Julie","8888");
		Student paul = new Student("Paul","9999");
		Student chloe = new Student("Chloe","0000");

		//creates the two lists of Student objects, which operate on the myGenericLinkedList class.
		myGenericLinkedList<Student> list1 = new myGenericLinkedList<Student>();
		myGenericLinkedList<Student> list2 = new myGenericLinkedList<Student>();
		myGenericLinkedList<Student> list = new myGenericLinkedList<Student>();
		
		//test bench of the different methods within the myGenericLinkedList class, that checks that all of the methods are working properly.
		System.out.println("Removing the first element of 1: " + list1.removeFirst() + " (Expected: false)");
		System.out.println("Removing the last element of 1: " + list1.removeLast() + " (Expected: false)");
		System.out.println("Removing Chloe from 1: " + list1.remove(chloe) + " (Expected: false)");
		System.out.println("Adding John to the front of 1: " + list1.addFront(john) + " (Expected: true)");
		System.out.println("Adding Lauren to the front of 1: " + list1.addFront(lauren) + " (Expected: false)");
		System.out.println("Adding Lauren to the end of 1: " + list1.addEnd(lauren) + " (Expected: true)");
		System.out.println("Adding Chloe to the end of 1: " + list1.addEnd(chloe) + " (Expected: false)");
		System.out.println("Adding Chloe to 1: " + list1.insert(chloe) + " (Expected: true)");
		System.out.println("Adding Lauren to 1: " + list1.insert(lauren) + " (Expected: false)");
		System.out.println("Adding Bridget to 1: " + list1.insert(bridget) + " (Expected: true)");
		System.out.println("The middle element of 1 is: " + list1.middleElement() + " (Expected: Lauren)");
		System.out.println("Adding Alex to 2: " + list2.insert(alex) + " (Expected: true)");
		System.out.println("Adding Phillip to 2: " + list2.insert(phillip) + " (Expected: true)");
		System.out.println("Adding Paul to 2: " + list2.insert(paul) + " (Expected: true)");
		System.out.println("Adding Lauren to 2: " + list2.insert(lauren) + " (Expected: true)");
		System.out.println("Adding Alicia to 2: " + list2.insert(alicia) + " (Expected: true)");
		System.out.println("Adding Kyle to 2: " + list2.insert(kyle) + " (Expected: true)");
		System.out.println("Adding Julie to 2: " + list2.insert(julie) + " (Expected: true)");
		System.out.println("First element of 1: " + list1.head.getItem() + " (Expected: Chloe)");
		System.out.println("Last element of 1: " + list1.tail.getItem() + " (Expected: Bridget)");
		System.out.println("First element of 2: " + list2.head.getItem() + " (Expected: Lauren)");
		System.out.println("Last element of 2: " + list2.tail.getItem() + " (Expected: Paul)");
		System.out.println("List 1:\n" + list1 + "(Expected: 0, 1, 2, 6)");
		System.out.println("List 1 length: " + list1.size() + " (Expected: 4)");
		System.out.println("List 2:\n" + list2 + "(Expected: 2, 3, 4, 5, 7, 8, 9)");
		System.out.println("List 2 length: " + list2.size() + " (Expected: 7)");
		list.merge(list1);
		list1.merge(list2);
		list2.merge(list);
		System.out.println("Merged List (1):\n" + list1 + "(Expected: 0, 1, 2, 3, 4, 5, 6, 7, 8, 9)");
		System.out.println("Merged List (2):\n" + list2 + "(Expected: 0, 1, 2, 3, 4, 5, 6, 7, 8, 9)");
		System.out.println("Tail reference: " + list1.getMax() + " (Expected: Paul)");
		System.out.println("Head reference: " + list1.getMin() + " (Expected: Chloe)");
		System.out.println("List 1 length: " + list1.size() + " (Expected: 10)");
		System.out.println("The middle element of 1 is: " + list1.middleElement() + " (Expected: Alex)");
		System.out.println("Removing Chloe from 1: " + list1.remove(chloe) + " (Expected: true)");
		System.out.println("Removing the first from 1: " + list1.removeFirst() + " (Expected: true)");
		System.out.println("Removing the last from  1: " + list1.removeLast() + " (Expected: true)");
		System.out.println("Removing Chloe from 1: " + list1.remove(chloe) + " (Expected: false)");
		System.out.println("List 1:\n" + list1 + "(Expected: 2, 3, 4, 5, 6, 7, 8)");

	}

}

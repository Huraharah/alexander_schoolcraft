import java.util.ArrayList;

public class mainClass {

	public static void main(String[] args) {
		BinarySearchTree mytree = new BinarySearchTree(new Node(45));
		System.out.println(mytree.insert(38, mytree.root) + " Expected: True");
		System.out.println(mytree.insert(68, mytree.root) + " Expected: True");
		System.out.println(mytree.insert(20, mytree.root) + " Expected: True");
		System.out.println(mytree.insert(40, mytree.root) + " Expected: True");
		System.out.println(mytree.insert(40, mytree.root) + " Expected: False");
		System.out.println(mytree.insert(51, mytree.root) + " Expected: True");
		System.out.println(mytree.insert(60, mytree.root) + " Expected: True");
		System.out.println(mytree.insert(10, mytree.root) + " Expected: True");
		System.out.println(mytree.insert(54, mytree.root) + " Expected: True");
		System.out.println(mytree.insert(70, mytree.root) + " Expected: True");
		System.out.println(mytree.insert(12, mytree.root) + " Expected: True");
		System.out.println(mytree.insert(3, mytree.root) + " Expected: True");
		System.out.println(mytree.insert(100, mytree.root) + " Expected: True");
		System.out.println(mytree.isEmpty() + " Expected: False");
		System.out.println(mytree.size(mytree.root) + " Expected: 13");
		System.out.println(mytree.size(new Node(22)) + " Expected: -1");
		System.out.println(mytree.search(mytree.root, 60) + " Expected: Node [60]");
		System.out.println(mytree.search(mytree.root, 0) + " Expected: null");
		
		ArrayList<Integer> tree = new ArrayList<Integer>();
		System.out.println(mytree.inOrder(mytree.root, tree) +" Expected: [3, 10, 12, 20, 38, 40, 45, 51, 54, 60, 68, 70, 100]");
		mytree.remove(mytree.root, 51);
		ArrayList<Integer> tree1 = new ArrayList<Integer>();
		System.out.println(mytree.inOrder(mytree.root, tree1) +" Expected: [3, 10, 12, 20, 38, 40, 45, 54, 60, 68, 70, 100]");
		
		
		System.out.println(mytree.depth(mytree.search(mytree.root, 60)) + " Expected: 3");
		System.out.println(mytree.height(mytree.search(mytree.root, 38)) + " Expected: 4");
		System.out.println(mytree.numChildren(mytree.search(mytree.root, 54)) + " Expected: 1");
		System.out.println(mytree.numChildren(mytree.search(mytree.root, 10)) + " Expected: 2");
		System.out.println(mytree.findMin(mytree.search(mytree.root, 38)) + " Expected: Node [3]");
		System.out.println(mytree.findMin(mytree.search(mytree.root, 68)) + " Expected: Node [54]");
		System.out.println(mytree.findMax(mytree.search(mytree.root, 38)) + " Expected: Node [40]");
		System.out.println(mytree.findMax(mytree.search(mytree.root, 68)) + " Expected: Node [100]");
		System.out.println(mytree.parent(mytree.root, mytree.search(mytree.root, 60), null) + " Expected: Node [54]");
		System.out.println(mytree.parent(mytree.root, mytree.search(mytree.root, 12), null) + " Expected: Node [10]");
		
		System.out.println(mytree.insert(39, mytree.root) + " Expected: True");
		System.out.println(mytree.insert(43, mytree.root) + " Expected: True");
		System.out.println(mytree.insert(50, mytree.root) + " Expected: True");
		System.out.println(mytree.insert(69, mytree.root) + " Expected: True");
		System.out.println(mytree.insert(49, mytree.root) + " Expected: True");
		System.out.println(mytree.insert(53, mytree.root) + " Expected: True");
		System.out.println(mytree.insert(41, mytree.root) + " Expected: True");
		System.out.println(mytree.insert(44, mytree.root) + " Expected: True");
		ArrayList<Integer> tree2 = new ArrayList<Integer>();
		System.out.println(mytree.inOrder(mytree.root, tree2) +" Expected: [3, 10, 12, 20, 38, 39, 40, 41, 43, 44, 45, 49, 50, 53, 54, 60, 68, 69, 70, 100]");

		
		BinarySearchTree emptyTree = new BinarySearchTree();
		System.out.println(emptyTree.isEmpty() + " Expected: True");
		System.out.println(emptyTree.size(new Node(0)) + " Expected: 0");
		System.out.println(emptyTree.setRoot(38) + " Expected: True");
		System.out.println(emptyTree.insert(40, emptyTree.root) + " Expected: True");
		System.out.println(emptyTree.insert(20, emptyTree.root) + " Expected: True");
		System.out.println(emptyTree.insert(39, emptyTree.root) + " Expected: True");
		System.out.println(emptyTree.insert(10, emptyTree.root) + " Expected: True");
		System.out.println(emptyTree.insert(3, emptyTree.root) + " Expected: True");
		System.out.println(emptyTree.insert(12, emptyTree.root) + " Expected: True");
		System.out.println(emptyTree.insert(43, emptyTree.root) + " Expected: True");
		System.out.println(emptyTree.insert(44, emptyTree.root) + " Expected: True");
		System.out.println(emptyTree.insert(41, emptyTree.root) + " Expected: True");
		
		System.out.println(emptyTree.isIdentical(emptyTree.root, mytree.search(mytree.root, 38)) + " Expected: True");
		emptyTree.remove(emptyTree.root, 12);
		System.out.println(emptyTree.isIdentical(emptyTree.root, mytree.search(mytree.root, 38)) + " Expected: False");
		System.out.println(mytree.isIdentical(mytree.root, mytree.root) + " Expected: True");
		
		
		mytree.cutBranch(mytree.search(mytree.root, 54));
		ArrayList<Integer> tree3 = new ArrayList<Integer>();
		System.out.println(mytree.inOrder(mytree.root, tree3) +" Expected: [3, 10, 12, 20, 38, 39, 40, 41, 43, 44, 45, 68, 69, 70, 100]");
		
		System.out.println(mytree.findLCA(mytree.search(mytree.root, 39), mytree.search(mytree.root, 10)) + " Expected: Node [38]");
		System.out.println(mytree.findLCA(mytree.search(mytree.root, 100), mytree.search(mytree.root, 10)) + " Expected: Node [45]");
		System.out.println(mytree);
	}
}

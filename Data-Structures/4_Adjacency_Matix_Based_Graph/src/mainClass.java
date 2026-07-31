/**This mainClass is a testbench driver class for the Graph object class
 * @author Alexander Schoolcraft
 * @author Lauren Andrews
 */
public class mainClass {
	public static void main(String[] args) {
		int[][] testGraph = 
			{{0,	80,	0,	25,	60,	0,	0,	0,	0,	0},
			{80,	0,	25,	0,	0,	0,	21,	0,	0,	0},
			{0,	25,	0,	40,	0,	20,	23,	0,	0,	0},
			{25,	0,	40,	0,	55,	35,	0,	0,	0,	0},
			{60,	0,	0,	55,	0,	0,	0,	0,	0,	0},
			{0,	0,	20,	35,	0,	0,	0,	20,	0,	0},
			{0,	21,	23,	0,	0,	0,	0,	25,	10,	0},
			{0,	0,	0,	0,	0,	20,	25,	0,	0,	12},
			{0,	0,	0,	0,	0,	0,	10,	0,	0,	30},
			{0,	0,	0,	0,	0,	0,	0,	12,	30,	0}};
		
		Graph graph = new Graph(testGraph);
		graph.print();
		System.out.println("*****************************************************************");
		System.out.println("Directed Graph? " + graph.isDirected() + " Expected: False");
		System.out.println("Weighted Graph? " + graph.isWeighted() + " Expected: True");
		System.out.println("Number of Verticies: " + graph.numVertices() + " Expected: 10");
		System.out.println("Number of Edges: " + graph.numEdges() + " Expected: 30");
		System.out.println("Vertex 6's neighbors: " + graph.getNeighbors(6) + " Expected: [1, 2, 7, 8]");
		System.out.println("Complete Graph? " + graph.isComplete() + " Expected: False");
		System.out.println("Connected Graph? " + graph.isConnected() + " Expected: True");
		System.out.println("Inserting edge from 6 to 4 with weight of 20 " + graph.insertEdge(6, 4, 20) + " Expected: True");
		System.out.println("Inserting edge from 6 to 2 with weight of 504 " + graph.insertEdge(6, 2, 504) + " Expected: False");
		System.out.println("*****************************************************************");
		graph.print();
		System.out.println("*****************************************************************");
		System.out.println("Directed Graph? " + graph.isDirected() + " Expected: True");
		System.out.println("Weighted Graph? " + graph.isWeighted() + " Expected: True");
		System.out.println("Max Degree Node: " + graph.hasMaxDegree() + " Expected: 6");
		System.out.println("BFS Traversal(0): \n\t\t" + graph.BFS(0) + "\nExpected: \t[0, 1, 3, 4, 2, 6, 5, 7, 8, 9]");
		System.out.println("BFS Traversal(1): \n\t\t" + graph.BFS(1) + "\nExpected: \t[1, 0, 2, 6, 3, 4, 5, 7, 8, 9]");
		System.out.println("BFS Traversal(2): \n\t\t" + graph.BFS(2) + "\nExpected: \t[2, 1, 3, 5, 6, 0, 4, 7, 8, 9]");
		System.out.println("BFS Traversal(3): \n\t\t" + graph.BFS(3) + "\nExpected: \t[3, 0, 2, 4, 5, 1, 6, 7, 8, 9]");
		System.out.println("BFS Traversal(4): \n\t\t" + graph.BFS(4) + "\nExpected: \t[4, 0, 3, 1, 2, 5, 6, 7, 8, 9]");
		System.out.println("BFS Traversal(5): \n\t\t" + graph.BFS(5) + "\nExpected: \t[5, 2, 3, 7, 1, 6, 0, 4, 9, 8]");
		System.out.println("BFS Traversal(6): \n\t\t" + graph.BFS(6) + "\nExpected: \t[6, 1, 2, 4, 7, 8, 0, 3, 5, 9]");
		System.out.println("BFS Traversal(7): \n\t\t" + graph.BFS(7) + "\nExpected: \t[7, 5, 6, 9, 2, 3, 1, 4, 8, 0]");
		System.out.println("BFS Traversal(8): \n\t\t" + graph.BFS(8) + "\nExpected: \t[8, 6, 9, 1, 2, 4, 7, 0, 3, 5]");
		System.out.println("BFS Traversal(9): \n\t\t" + graph.BFS(9) + "\nExpected: \t[9, 7, 8, 5, 6, 2, 3, 1, 4, 0]");
		System.out.println("DFS Traversal(0): \n\t\t" + graph.DFS(0) + "\nExpected: \t[0, 1, 2, 3, 4, 5, 7, 6, 8, 9]");
		System.out.println("DFS Traversal(1): \n\t\t" + graph.DFS(1) + "\nExpected: \t[1, 0, 3, 2, 5, 7, 6, 4, 8, 9]");
		System.out.println("DFS Traversal(2): \n\t\t" + graph.DFS(2) + "\nExpected: \t[2, 1, 0, 3, 4, 5, 7, 6, 8, 9]");
		System.out.println("DFS Traversal(3): \n\t\t" + graph.DFS(3) + "\nExpected: \t[3, 0, 1, 2, 5, 7, 6, 4, 8, 9]");
		System.out.println("DFS Traversal(4): \n\t\t" + graph.DFS(4) + "\nExpected: \t[4, 0, 1, 2, 3, 5, 7, 6, 8, 9]");
		System.out.println("DFS Traversal(5): \n\t\t" + graph.DFS(5) + "\nExpected: \t[5, 2, 1, 0, 3, 4, 6, 7, 9, 8]");
		System.out.println("DFS Traversal(6): \n\t\t" + graph.DFS(6) + "\nExpected: \t[6, 1, 0, 3, 2, 5, 7, 9, 8, 4]");
		System.out.println("DFS Traversal(7): \n\t\t" + graph.DFS(7) + "\nExpected: \t[7, 5, 2, 1, 0, 3, 4, 6, 8, 9]");
		System.out.println("DFS Traversal(8): \n\t\t" + graph.DFS(8) + "\nExpected: \t[8, 6, 1, 0, 3, 2, 5, 7, 9, 4]");
		System.out.println("DFS Traversal(9): \n\t\t" + graph.DFS(9) + "\nExpected: \t[9, 7, 5, 2, 1, 0, 3, 4, 6, 8]");
		System.out.println("*****************************************************************");
		System.out.println("Testing all logic pathways:");
		System.out.println("*****************************************************************");
		graph.removeWeights();
		graph.print();
		System.out.println("Complete Graph? " + graph.isComplete() + " Expected: False");
		System.out.println("Connected Graph? " + graph.isConnected() + " Expected: True");
		System.out.println("Directed Graph? " + graph.isDirected() + " Expected: True");
		System.out.println("Weighted Graph? " + graph.isWeighted() + " Expected: False");
		System.out.println("*****************************************************************");
		Graph graph1 = new Graph(testGraph);
		graph1.completeGraphing();
		graph1.print();
		System.out.println("Complete Graph? " + graph1.isComplete() + " Expected: True");
		System.out.println("Connected Graph? " + graph1.isConnected() + " Expected: True");
		System.out.println("Directed Graph? " + graph1.isDirected() + " Expected: False");
		System.out.println("Weighted Graph? " + graph1.isWeighted() + " Expected: True");
		System.out.println("*****************************************************************");
		graph1.removeNode(3);
		graph1.print();
		System.out.println("Complete Graph? " + graph1.isComplete() + " Expected: False");
		System.out.println("Connected Graph? " + graph1.isConnected() + " Expected: False");
		System.out.println("Directed Graph? " + graph1.isDirected() + " Expected: False");
		System.out.println("Weighted Graph? " + graph1.isWeighted() + " Expected: True");
		System.out.println("*****************************************************************");
		graph1.completeGraphing(15);
		graph1.print();
		System.out.println("Complete Graph? " + graph1.isComplete() + " Expected: True");
		System.out.println("Connected Graph? " + graph1.isConnected() + " Expected: True");
		System.out.println("Directed Graph? " + graph1.isDirected() + " Expected: False");
		System.out.println("Weighted Graph? " + graph1.isWeighted() + " Expected: True");
		System.out.println("*****************************************************************");
		graph1.removeWeightComplete();
		graph1.print();
		System.out.println("Complete Graph? " + graph1.isComplete() + " Expected: True");
		System.out.println("Connected Graph? " + graph1.isConnected() + " Expected: True");
		System.out.println("Directed Graph? " + graph1.isDirected() + " Expected: False");
		System.out.println("Weighted Graph? " + graph1.isWeighted() + " Expected: False");
		System.out.println("*****************************************************************");
	}
}

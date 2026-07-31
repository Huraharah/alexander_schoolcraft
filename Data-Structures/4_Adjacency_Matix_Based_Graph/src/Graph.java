import java.util.*;
/**Graph creates and operates a 2D array-based adjacency matrix for a graph data structure
 * @author Alexander Schoolcraft 
 * @author Lauren Andrews
 */
public class Graph {

	int[][] adjMatrix;
	
	/** Constructor class for the adjacency matrix
	 * @param adj
	 */
	public Graph(int[][] adj) {
		this.adjMatrix=adj;
	}
	
	/** Counts and returns the number of vertices.
	 * @return int: number of vertices in the graph
	 */
	public int numVertices() { //O(1)
		return adjMatrix.length;
	}
	/** Counts and returns the number of edges in the graph
	 * @return int: the number of edges within the graph
	 */
	public int numEdges() { //O(n*n)
		int count=0;
		for(int i=0;i<adjMatrix.length;i++)
			for(int j=0;j<adjMatrix.length;j++)
				if(adjMatrix[i][j]!=0) count++;
		return count;
	}
	
	/** Finds and returns an ArrayList of all vertices connected to a given vertex
	 * @param u: int index of the vertex to check
	 * @return ArrayList<Integer> of the indices of all vertices connected to the parameter vertex
	 */
	public ArrayList<Integer> getNeighbors(int u) { //O(n)
		ArrayList<Integer> list = new ArrayList<Integer>();
		for(int i=0;i<adjMatrix.length;i++)
			if(adjMatrix[u][i]!=0) list.add(i);
		return list;
	}
	
	/** Checks if any of the edges are not 1, returning true if any of the non-zero edges are not 1 and are thus weighted, false otherwise
	 * @return boolean: true if the graph is weighted, false otherwise
	 */
	public boolean isWeighted() { //O(n*n)
	    for (int i = 0; i < adjMatrix.length; i++) {
		for (int j = 0; j < adjMatrix[i].length; j++) {
		    if (adjMatrix[i][j] != 0 && adjMatrix[i][j] != 1) {
			return true;
		    }
		}
	    }
	    return false;
	}
	
	/** Checks if the two edge values between any two vertices is the same. If any are different, the graph is at least partially directed.
	 * @return boolean: true if the graph has any directed edges, false otherwise
	 */
	public boolean isDirected() { //O(n*n)
	    for (int i = 0; i < adjMatrix.length; i++) {
		for (int j = 0; j < i; j++) {
		    if (adjMatrix[i][j] !=  adjMatrix[j][i]) {
			return true;
		    }
		}
	    }
	    return false;
	}
	/** Checks if all vertices are connected to all other vertices
	 * @return boolean: true if all values other than when i=j are non-zero, false otherwise
	 */
	 public boolean isComplete() { //O(n*n)
	     for (int i = 0; i < adjMatrix.length; i++) {
		 for (int j = 0; j < adjMatrix[i].length; j++) {
		     if (i != j && adjMatrix[i][j] == 0) {
			 return false;
		     }
		 }
	     }
	     return true;
	}
	/** Checks if two given vertices are connected
	 * @param u: int index of first vertex to compare
	 * @param v: int index of second vertex to compare
	 * @return boolean: true if either of the given edge values are non-zero, false otherwise
	 */
	public boolean isNeighbor(int u, int v) { //O(1)
	    if (adjMatrix[u][v] != 0 || adjMatrix[v][u] != 0) {
		return true;
	    }
	    return false;
	}
	/** Counts the number of edges from a given vertex
	 * @param u: int index of vertex to count number of neighbors
	 * @return int: number of neighbors for the given vertex
	 */
	public int getDegree(int u) { //O(n)
	    int deg = 0;
	    for (int i = 0; i < adjMatrix[u].length; i++) {
		if (adjMatrix[u][i] != 0) {
		    deg ++;
		}
	    }
	    return deg;
	}
	
	/** Finds the vertex that has the most edges from it, using the getDegree method above
	 * @return int: index of vertex that has the most neighbors
	 */
	public int hasMaxDegree() { //O(n*n)
	    int max = 0;
	    int deg = 0;
	    for (int i = 0; i < adjMatrix.length; i++) {
		if (this.getDegree(i) > deg) {
		    deg = this.getDegree(i);
		    max = i;
		}
	    }
	    return max;
	}
	
	/** Creates a new edge of a given weight between two vertices
	 * @param u: int index of first vertex to connect
	 * @param v: int index of second vertex to connect
	 * @param weight: int value of the weight to add to the edge
	 * @return boolean: true if successful, false otherwise
	 */
	public boolean insertEdge(int u, int v, int weight) { //O(1)
	    if(adjMatrix[u][v] == 0) {
		adjMatrix[u][v] = weight;
		return true;
	    }
	    return false;
	}
	
	/** Removes the edge between two given vertices
	 * @param u: int index of first vertex to disconnect
	 * @param v: int index of second vertex to disconnect
	 * @return boolean: true if successful, false otherwise
	 */
	public boolean removeEdge(int u, int v) { //O(1)
	    if(adjMatrix[u][v] != 0) {
		adjMatrix[u][v] = 0;
		return true;
	    }
	    return false;
	}
	
	/** Creates an ArrayList<Integer> of the vertices, traversed according to BFS traversal algorithm
	 * @param start: int index of vertex to start the traversal from
	 * @return ArrayList<Integer> of the traversal through the graph, optimized for breadth
	 */
	public ArrayList<Integer> BFS(int start) {
	    Queue<Integer> nodes = new LinkedList<Integer>();
	    ArrayList<Integer> visited = new ArrayList<Integer>();
	    ArrayList<Integer> BFS = new ArrayList<Integer>();
	    
	    nodes.add(start);
	    visited.add(start);
	    
	    while (!nodes.isEmpty()) {
		BFS.add(nodes.peek());
		
		for (int i = 0; i< adjMatrix[nodes.peek()].length; i++) {
		    if (adjMatrix[nodes.peek()][i] != 0 && !visited.contains(i)) {
			visited.add(i);
			nodes.add(i);
		    }
		}
		nodes.poll();
	    }
	    return BFS;
	}
	
	/** Creates an ArrayList<Integer> of the vertices, by calling the recursive method, according to the DFS traversal algorithm
	 * @param start: int index of the vertex to start the traversal from
	 * @return ArrayList<Integer> containing the vertices, traversed in DFS order
	 */
	public ArrayList<Integer> DFS(int start) {
	    ArrayList<Integer> DFS = new ArrayList<Integer>();
	    ArrayList<Integer> visited = new ArrayList<Integer>();
	    recursiveDFS(start, visited, DFS);
	    return DFS;
	}
	
	/** Recursive method used to build the DFS traversal
	 * @param node: int index of the current vertex
	 * @Param visited: ArrayList<Integer> that contains the visited vertices
	 * @param DFS: ArrayList<Integer> that is the current DFS list
	 */
	private void recursiveDFS(int node, ArrayList<Integer> visited, ArrayList<Integer> DFS) {
	    visited.add(node);
	    DFS.add(node);

	    for (int i = 0; i < adjMatrix[node].length; i++) {
	        if (adjMatrix[node][i] != 0 && !visited.contains(i)) {
	            recursiveDFS(i, visited, DFS);
	        }
	    }
	}
	
	/** Compares the BFS traversal length to the number of vertices, and determines if the graph is fully connected
	 * @return boolean: true if every vertex in the graph is connected to the graph, false otherwise
	 */
	public boolean isConnected() {
	    if (this.BFS(0).size() == adjMatrix.length) {
		return true;
	    }
	    return false;
	}
	
	/** Prints out the adjacency matrix representation of the graph.
	 */
	public void print() {
	    for(int i = 0; i < adjMatrix.length; i ++) {
		for(int j = 0;j < adjMatrix.length ;j++)
		    System.out.print(adjMatrix[i][j]+"\t");
		System.out.println();
	    }		
	}
	//    EXTRA MATERIAL, helper methods used to help test all of the potential logic paths in the required methods    //
	
	/** Fills in the graph, completing the graph with edges of weight 1, without creating self-connection loops
	 */
	public void completeGraphing() {
	    for (int i = 0; i < adjMatrix.length; i++) {
		for (int j = 0; j < adjMatrix[i].length; j++) {
		    if (i != j && adjMatrix[i][j] == 0) {
			this.insertEdge(i, j, 1);
		    }
		}
	    }
	}
	
	/** Fills in the graph, completing the graph with edges of a given weight, without creating self-connection loops
	 * @param weight: int value of the weight to fill in the graph with
	 */
	public void completeGraphing(Integer weight) {
	    for (int i = 0; i < adjMatrix.length; i++) {
		for (int j = 0; j < adjMatrix[i].length; j++) {
		    if (i != j && adjMatrix[i][j] == 0) {
			this.insertEdge(i, j, weight);
		    }
		}
	    }
	}
	
	/** Completely removes a vertex from the graph by removing all edges connected to it, whether they are leaving it or arriving at it
	 * @param node: int index of the vertex to delete all edges from
	 */
	public void removeNode(Integer node) {
	    for (int i = 0; i < adjMatrix.length; i ++) {
		this.removeEdge(i, node);
		this.removeEdge(node, i);
	    }
	}
	
	/** Deletes all edges, then completes the graph with weight of 1 for all edges
	 */
	public void removeWeightComplete() {
	    for (int i = 0; i < adjMatrix.length; i ++) {
		for (int j = 0; j < adjMatrix[i].length; j ++) {
		    if (this.isNeighbor(i, j)) {
			this.removeEdge(i, j);
			this.removeEdge(j, i);
		    }
		}
	    }
	    this.completeGraphing();
	}
	
	/** Keeps the same edges as current graph, but removes weight on all by setting the weight to 1
	 */
	public void removeWeights() {
	    for (int i = 0; i < adjMatrix.length; i ++) {
		for (int j = 0; j < adjMatrix[i].length; j ++) {
		    if (adjMatrix[i][j] != 0) {
			adjMatrix[i][j] = 1;
		    }
		}
	    }
	}
}

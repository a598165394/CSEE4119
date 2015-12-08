
public class Edge implements Comparable<Edge>{
	public Vertex startVertex;
	public Vertex endVertex;
	public Double cost;
	public int visited=1;;
	
	public Edge(Vertex startVertex, Vertex endVertex, double cost){
		this.startVertex = startVertex;
		this.endVertex = endVertex;
		this.cost = cost;
	}
	
	
	@Override
	public int compareTo(Edge other) {
		return cost.compareTo(other.cost);
	}

}

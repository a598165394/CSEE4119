import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


public class Graph {
	protected Map<String,Vertex> vertices;
	protected Map<Double,String> pair;
	
	public Graph(){
		vertices = new HashMap<String, Vertex>();
	}
	
	public void addVertex(String name){
		Vertex v = new Vertex(name);
		addVertex(v);
	}
	
	public void addVertex(Vertex v){
		if(vertices.containsKey(v.name)) throw new IllegalArgumentException("The vertex is already exist");
		vertices.put(v.name, v);				
	}
	
	public Collection<Vertex> getVertices(){
		return vertices.values();
	}
	
	public Vertex getVertex(String key){
		return vertices.get(key);
	}
	
	public void addEdge(String startPoint, String endPoint, double cost){
		if(!vertices.containsKey(startPoint)) 	addVertex(startPoint);
		if(!vertices.containsKey(endPoint))     addVertex(endPoint);
		Vertex startVertex = vertices.get(startPoint);
		Vertex endVertex = vertices.get(endPoint);
		Edge go = new Edge(startVertex,endVertex,cost);
		Edge back = new Edge(endVertex,startVertex,cost);
		startVertex.addEdge(go);	
		endVertex.addEdge(back);
	}
	
	public void addEdge(String startPoint, String endPoint){
		addEdge(startPoint, endPoint,0);
	}
	 public void printAdjacencyList() {
		    for (String u : vertices.keySet()) {
		      StringBuilder sb = new StringBuilder();
		      sb.append(u);
		      sb.append(" -> [ ");
		      for (Edge e : vertices.get(u).getEdges()) {
		        sb.append(e.endVertex.name);
		        sb.append("(");
		        sb.append(e.cost);
		        sb.append(") ");
		      }
		      sb.append("]");
		      System.out.println(sb.toString());
		    }
		  }
}

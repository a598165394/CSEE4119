import java.util.LinkedList;
import java.util.List;


public class Vertex {
	public String name;
	private List<Edge> adjacent;
	public double cost = 0;
	public String ip;
	public int port;
	
	public Vertex backpointer=null;

	public Vertex(String name,double cost,String ip, int port){
		this.name = name;
		this.cost = cost;
		this.ip = ip;
		this.port = port;
		adjacent = new LinkedList<Edge>();
	}
	
	public Vertex(String vertexName){
		this(vertexName,0.0,"127.0.0.1",0);
	}
	
	public List<Edge> getEdges(){
		return adjacent;
	}
	
	public void addEdge(Edge edge){
		adjacent.add(edge);
	}
	
	public String toString(){
		return name;
	}
}

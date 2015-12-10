import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class bfclient {
	static Graph graph;
	private static ExecutorService executorService = Executors.newCachedThreadPool();
	private int listenPort;
	private int timeout;
//	private PriorityQueue<Client> q = new PriorityQueue<Client>();
	private StringBuilder sb;
//	private DatagramPacket dp;
//	private DatagramSocket ds;
	@SuppressWarnings("unchecked")
	public bfclient(String[] keyword) {
		sb = new StringBuilder();
		graph = new Graph();
	
		listenPort = Integer.parseInt(keyword[0]);
	
		timeout = Integer.parseInt(keyword[1]);
		if((keyword.length-2)%3!=0){
			System.out.println("Input format Wrong. Please rerun");
			System.exit(0);
		}
		String startPos = null;
		sb.append("ROUTE UPDATE");
		try {
			startPos = InetAddress.getLocalHost().getHostAddress()+":"+keyword[0];
			graph.addVertex(new Vertex(startPos, Integer.MAX_VALUE, InetAddress.getLocalHost().getHostAddress(), listenPort));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}	
		for(int i=2;i<keyword.length;i+=3){
	//		q.add(new Client(keyword[i],keyword[i+1],keyword[i+2]));
			sb.append("/"+keyword[i]+":"+keyword[i+1]+","+keyword[i+2]+","+keyword[0]+":"+keyword[1]+";");
			String name = keyword[i]+":"+keyword[i+1];
			graph.addVertex(new Vertex(name, Double.parseDouble(keyword[i+2]),keyword[i],Integer.parseInt(keyword[i+1])));
			graph.vertices.get(name).backpointer = graph.vertices.get(startPos);
	//		System.out.println(graph.vertices.get(name).parent);
			graph.addEdge(startPos, name, Double.parseDouble(keyword[i+2]));
		}
	
		doBford(startPos);
//		System.out.println("The first:"+startPos);
		sb = tableUpdate(graph,startPos);
		executorService.execute(new ReceiverThread(listenPort,timeout,startPos,graph));
		
		RecvSendLoop(graph,sb,listenPort,timeout,startPos);
	}
	private void doBford(String startPos) {
		Vertex start = 	bfclient.graph.vertices.get(startPos);
		for(Vertex v:bfclient.graph.vertices.values()){
	//		v.cost = Integer.MAX_VALUE;
	//		v.parent =null;
		}
		start.cost =0;
		// Relax
		for(int i=1;i<=bfclient.graph.vertices.size()-1;i++){
			for(Vertex vv:bfclient.graph.vertices.values()){
				for(Edge edge:vv.getEdges()){
			//		if(edge.visited==i){
				//		edge.visited+=1;
			//			if(edge.visited==bfclient.graph.vertices.size()-1) edge.visited=1;
						Vertex source = edge.startVertex;
						Vertex target = edge.endVertex;
						if(target.cost> source.cost+edge.cost){
							target.cost = source.cost+edge.cost;
							target.backpointer = source;
						}
			//		}
				}
			}
			for(Vertex vv:bfclient.graph.vertices.values()){
				for(Edge edge:vv.getEdges()){
						Vertex source = edge.startVertex;
						Vertex target = edge.endVertex;
						if(target.cost> source.cost+edge.cost){
							System.out.println("There is negative weight cycle exists");

						}
		//			}
				}
			}
			
		}
		SendDataToNeigh(startPos);
	
		
	}
	private void SendDataToNeigh(String startPos) {
		Vertex self =bfclient.graph.vertices.get(startPos);
		byte[] sendBuffer = new byte[4096];
		String temp = "";
		StringBuilder sb =new StringBuilder();
		StringBuilder route=new StringBuilder();
		sb.append("ROUTE UPDATE");
		for(Vertex v:bfclient.graph.vertices.values()){
			Vertex vp = v.backpointer;
			while(vp!=null){
		//		System.out.println(vp.name);
				if(temp.equals(vp.name)) break;
				route.append(vp.name+";");
				temp = vp.name;
				vp = vp.backpointer;
			}
			sb.append("/"+v.name+","+v.cost+","+route.toString());
		}
		sendBuffer = sb.toString().getBytes();
		for(Edge w:self.getEdges()){
			
			try {
				System.out.println("Firstly Send to:"+w.endVertex);
				DatagramSocket dsTemp = new DatagramSocket();	
				
				DatagramPacket dpTemp = new DatagramPacket(sendBuffer,sendBuffer.length,InetAddress.getByName(w.endVertex.ip),w.endVertex.port);
				dsTemp.send(dpTemp);
			} catch (SocketException e) {
				e.printStackTrace();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}		
	}
	




	private StringBuilder tableUpdate(Graph graph, String startPos) {
		StringBuilder temp =new StringBuilder();
		String lastParent = "";
		for(Vertex v:graph.vertices.values()){
			
			StringBuilder routetemp=new StringBuilder();
			Vertex vp = v.backpointer;
			while(vp!=null){
				if(lastParent.equals(vp.name)) break;
				routetemp.append(vp.name+";");
				lastParent = vp.name;
				vp = vp.backpointer;
			}
			if(routetemp.toString()==null){
				System.out.println("Line 158"+"/"+"Destination = "+v.name+",Cost = "+v.cost+",Link= ("+startPos+")");
				temp.append("/"+"Destination = "+v.name+",Cost = "+v.cost+","+startPos);
			}else{
				temp.append("/"+"Destination = "+v.name+",Cost = "+v.cost+","+routetemp.toString());
			}
			
			
		}
		return temp;
		
	}





	private void RecvSendLoop(Graph graph, StringBuilder sb, int listenPort2, int timeout2,
			String startPos) {
	    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(  
                System.in));  
	    String message;
	    for(;;){
	    	try {
				while((message=bufferedReader.readLine())!=null){
					if(message.trim().equals("SHOWRT")){
				//		System.out.println("The second Recv:"+startPos);
						StringBuilder sbTemp = new StringBuilder();
			//			System.out.println("Before Result");
						sbTemp = tableUpdate(graph,startPos);
				
						String[] table = sbTemp.toString().split("/");
						System.out.println("<Current Time> Distance vector list is:");
						for(int i=0;i<table.length;i++){
							if(table[i].length()!=0){ 
								String[] resTemp = table[i].trim().split(",");
								String[] secTemp = resTemp[0].trim().split("=");
							//	System.out.println(secTemp[1]);
								if(!secTemp[1].trim().equals(startPos.trim())){
									System.out.println(table[i]);
								}
								
							}
							
						}
					}else if(message.trim().equals("CLOSE")){
						
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
		
	}

	public static void main(String[] args) {
		new bfclient(args);
	//	String[] res={"4118","5"};
	//	String[] res={"4116","20","160.39.134.211", "4118","5.0"};
	//	String[] res={"4115", "3" ,"127.0.0.1","4116", "5.0" ,"127.0.0.1" ,"4118" ,"30.0"};
	//	new bfclient(res);
	}
	
}

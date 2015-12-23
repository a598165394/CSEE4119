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
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class bfclient {
	static Graph graph;
	private static ExecutorService executorService = Executors.newCachedThreadPool();
	static String breakInStart="";
	static String breakInRecv="";
	static String recvInStart="";
	static String recvInRecv="";
	private int listenPort;
	private int timeout;
	private StringBuilder sb;
	public double lastValue;
	public StringBuilder lastSb;
	static List<String> nameNode;
	static List<Long> timeRecv;
	static List<String> dieList;
	
	
	private String neighborClose;
	private Map<String,Double> neighbor;
	private String keywordLink ="N";
	@SuppressWarnings("unchecked")
	public bfclient(String[] keyword) {
		neighbor = new HashMap<String, Double>();
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
	
		doBford(startPos,keywordLink);
//		System.out.println("The first:"+startPos);
		sb = tableUpdate(graph,startPos);
		executorService.execute(new ReceiverThread(listenPort,timeout,startPos,graph));
		executorService.execute(new TimeCheck(timeout));
		RecvSendLoop(graph,sb,listenPort,timeout,startPos);
	}
	private void doBford(String startPos, String keywordLink) {
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
						Vertex source = edge.startVertex;
						Vertex target = edge.endVertex;
						if(target.cost> source.cost+edge.cost){
							if((source.name.equals(startPos) && target.name.equals(neighborClose)) ||(source.name.equals(neighborClose) && target.name.equals(startPos))) continue;
							target.cost = source.cost+edge.cost;
							target.backpointer = source;
						}
				}
			}
			for(Vertex vv:bfclient.graph.vertices.values()){
				for(Edge edge:vv.getEdges()){
						Vertex source = edge.startVertex;
						Vertex target = edge.endVertex;
						if(target.cost> source.cost+edge.cost){
				//			System.out.println("There is negative weight cycle exists");
						}
				}
			}
			
		}
		SendDataToNeigh(startPos,keywordLink);
	
		
	}
	private void SendDataToNeigh(String startPos, String keywordLink) {
		Vertex self =bfclient.graph.vertices.get(startPos);
		byte[] sendBuffer = new byte[4096];
		String temp = "";
		StringBuilder sb =new StringBuilder();
		StringBuilder route=new StringBuilder();
		
		sb.append("ROUTE UPDATE"+keywordLink);
		for(Vertex v:bfclient.graph.vertices.values()){
			Vertex vp = v.backpointer;
			while(vp!=null){
				if(temp.equals(vp.name)||route.toString().contains(vp.name)){
					break;
				}
				route.append(vp.name+";");
				temp = vp.name;
				vp = vp.backpointer;
			}
			sb.append("/"+v.name+","+v.cost+","+route.toString());
		}
		sendBuffer = sb.toString().getBytes();
		try {
			DatagramSocket dsTemp;
			if(keywordLink.equals("D"+startPos+";"+neighborClose+"!")){
				for(Vertex v:graph.vertices.values()){
					if(v.name.equals(startPos)) continue;
					dsTemp = new DatagramSocket();
					DatagramPacket dpTemp = new DatagramPacket(sendBuffer,sendBuffer.length,InetAddress.getByName(v.ip),v.port);
					dsTemp.send(dpTemp);
				}
			}else if(keywordLink.equals("R"+startPos+";"+neighborClose+"!")){
				for(Vertex v:graph.vertices.values()){
					if(v.name.equals(startPos)) continue;
					dsTemp = new DatagramSocket();
					DatagramPacket dpTemp = new DatagramPacket(sendBuffer,sendBuffer.length,InetAddress.getByName(v.ip),v.port);
					dsTemp.send(dpTemp);
				}
			
			}else{
				for(Edge w:self.getEdges()){
						dsTemp = new DatagramSocket();	
						DatagramPacket dpTemp = new DatagramPacket(sendBuffer,sendBuffer.length,InetAddress.getByName(w.endVertex.ip),w.endVertex.port);
						dsTemp.send(dpTemp);
				
				}	
			}
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	//			System.out.println("Line 158"+"/"+"Destination = "+v.name+",Cost = "+v.cost+",Link= ("+startPos+")");
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
					String linkdes = "";
					String linkRev = "";
					if(message.length()>=10){
						linkdes = message.trim().substring(0,8);
					}
					if(message.length()>6) linkRev = message.trim().substring(0,6);
					if(message.trim().equals("SHOWRT")){
						for(Edge w:graph.vertices.get(startPos).getEdges()){
					//		System.out.println(startPos+"<->"+w.endVertex.name+":"+w.cost);
						}
						
						StringBuilder sbTemp = new StringBuilder();
						sbTemp = TableUpdateForPrint(graph,startPos);
			//			System.out.println("The cost from "+startPos+"->"+"160.39.134.48:4115 is: "+graph.vertices.get("160.39.134.48:4115").cost);

						String[] table = sbTemp.toString().split("/");
			//			System.out.println("The current List size:"+nameNode.size());
						System.out.println("<Current Time> Distance vector list is:");
						for(Vertex v:graph.vertices.values()){
							if(v.name.equals(startPos)) continue;
						//	if(dieList.contains(v.name)) continue;
							if(v.cost>=Double.MAX_VALUE || v.cost>=Integer.MAX_VALUE) continue;
							if(v.backpointer==null){
								System.out.println("Destination = "+v.name+",Cost = "+v.cost+", Link= ("+v.name+")");
							}else{
								System.out.println("Destination = "+v.name+",Cost = "+v.cost+", Link= ("+v.backpointer+")");
							}
						
						}
					
//						for(int i=0;i<table.length;i++){
//							if(table[i].length()!=0){ 
//								String[] resTemp = table[i].trim().split(",");
//								String[] secTemp = resTemp[0].trim().split("=");
//
//								if(!secTemp[1].trim().equals(startPos.trim())){
// 									System.out.println(table[i]);
//								}
//								
//							}
//							
//						}
		//				System.out.println("The Backpointer for 192.168.0.8:4119 is: "+bfclient.graph.vertices.get("192.168.0.8:4119").backpointer.name);
					}else if(message.trim().equals("CLOSE")){
						for(Edge edge:graph.vertices.get(startPos).getEdges()){
							neighborClose= edge.endVertex.name;
							breakInStart = startPos;
							breakInRecv = neighborClose;
							SendLinkDown(startPos, lastSb, neighborClose);
						}
						System.exit(0);
					}else if(linkdes.equals("LINKDOWN")){
						for(Edge w:graph.vertices.get(startPos).getEdges()){
							if(neighbor.containsKey(w.endVertex.name)==false){
									neighbor.put(w.endVertex.name, w.cost);
							}
						}
						graph.backupVertices = graph.vertices;
						String neighName=message.trim().substring(8).trim();
						String[] sep = neighName.split(" ");
						neighborClose = sep[0]+":"+sep[1];
						breakInStart=startPos;
						breakInRecv=neighborClose;
						SendLinkDown(startPos,lastSb,neighborClose);
						
					}else if(linkRev.equals("LINKUP")){
						String neighName=message.trim().substring(6).trim();
						String[] sep = neighName.split(" ");
						neighborClose = sep[0];
						recvInStart=startPos;
						recvInRecv=neighborClose;
						resumeNeigh(startPos);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
		
	}

	
	private void resumeNeigh(String startPos) {
	
		graph.vertices.get(neighborClose).cost = neighbor.get(neighborClose);
		//	System.out.println("The original cost is :"+String.valueOf(graph.vertices.get(neighborClose).cost = neighbor.get(neighborClose)));
		String desInfo = "R"+startPos+";"+neighborClose+"!";
		for(Edge w:graph.vertices.get(startPos).getEdges()){
			if(w.endVertex.name.equals(neighborClose)){
				w.cost= neighbor.get(neighborClose);
			}
		}
		for(Edge w:graph.vertices.get(neighborClose).getEdges()){
			if(w.endVertex.name.equals(startPos)){
				w.cost = neighbor.get(neighborClose);
			}
		}
		doBford(startPos,desInfo);
		
	}
	private void SendLinkDown(String startPos, StringBuilder lastSb, String neighborClose) {
	//		neighbor.put(graph.vertices.get(neighborClose).name, graph.vertices.get(neighborClose).cost);
		
			graph.vertices.get(neighborClose).cost = Double.MAX_VALUE;
			String desInfo = "D"+startPos+";"+neighborClose+"!";
			for(Edge w:graph.vertices.get(startPos).getEdges()){
				if(w.endVertex.name.equals(neighborClose)){
					w.cost=Double.MAX_VALUE;
				}
			}
			doBfordRe(startPos,desInfo);

	
	}
	private void doBfordRe(String startPos, String desInfo) {
			Vertex start = 	bfclient.graph.vertices.get(startPos);
			for(Vertex v:bfclient.graph.vertices.values()){
				v.cost = Integer.MAX_VALUE;
			}
			start.cost =0;
			// Relax
			for(int i=1;i<=bfclient.graph.vertices.size()-1;i++){
				for(Vertex vv:bfclient.graph.vertices.values()){
					for(Edge edge:vv.getEdges()){
							Vertex source = edge.startVertex;
							Vertex target = edge.endVertex;
							if(target.cost> source.cost+edge.cost){
								if((source.name.equals(startPos) && target.name.equals(neighborClose)) ||(source.name.equals(neighborClose) && target.name.equals(startPos))) continue;
								target.cost = source.cost+edge.cost;
								target.backpointer = source;
							}
					}
				}
				for(Vertex vv:bfclient.graph.vertices.values()){
					for(Edge edge:vv.getEdges()){
							Vertex source = edge.startVertex;
							Vertex target = edge.endVertex;
							if(target.cost> source.cost+edge.cost){
							}
					}
				}
				
			}
			SendDataToNeigh(startPos,desInfo);
		
			
		
	}
	private StringBuilder TableUpdateForPrint(Graph graph, String startPos) {

			StringBuilder temp =new StringBuilder();
			String lastParent = "";
		
			for(Vertex v:graph.vertices.values()){
				
				StringBuilder routetemp=new StringBuilder();
				Vertex vp = v.backpointer;
				while(vp!=null){
					if(lastParent.equals(vp.name)||routetemp.toString().contains(vp.name)) break;
					routetemp.append(vp.name+";");
					
					lastParent = vp.name;
					vp = vp.backpointer;
				}
				if(routetemp.toString().trim().equals("")){
					temp.append("/"+"Destination = "+v.name+",Cost = "+v.cost+", Link= ("+v.name+")");
				}else{
					if(routetemp.toString().contains(startPos)){
						String[] tempString = routetemp.toString().split(";");
						temp.append("/"+"Destination = "+v.name+",Cost = "+v.cost+", Link= (");
						for(int i=0;i<tempString.length;i++){
							if(!tempString[i].equals(startPos)){
								temp.append(tempString[i]+" -> ");
							}
						}
						temp.append(v.name+")");
					}else{
						boolean neigh = false;
						for(Edge w:graph.vertices.get(startPos).getEdges()){
							if(w.endVertex==v){
								neigh=true;
								temp.append("/"+"Destination = "+v.name+",Cost = "+w.cost+","+" Link= ("+v.name+")");
								break;
							}
						}
						if(neigh==false){
							temp.append("/"+"Destination = "+v.name+",Cost = "+v.cost+","+""+v.backpointer);
						}
						
					}
					
				}
				
				
			}
		
		return temp;
	}
	public static void main(String[] args) {
		new bfclient(args);
//		String[] res={"4118","5"};
	//	String[] res={"4116","20","160.39.134.211", "4118","5.0"};
	//	String[] res={"4115", "3" ,"127.0.0.1","4116", "5.0" ,"127.0.0.1" ,"4118" ,"30.0"};
	//	new bfclient(res);
	}
	
}

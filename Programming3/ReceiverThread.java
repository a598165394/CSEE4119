import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;



public class ReceiverThread implements Runnable {
	private int timeout;
	private int listenPort;
	//private StringBuilder sb;
	private String startPos;
	DatagramPacket dp = null;
	DatagramSocket ds = null;
	private Timer finishDoBF;
	public boolean timerCancel= false;
	int buffersize = 4096;
	byte[] receiveBuffer;
	
	public ReceiverThread(int listenPort, int timeout, String startPos, Graph graph) {
		this.timeout = timeout;
		this.listenPort = listenPort;
		this.startPos = startPos;
		

	
	}
	@Override
	public void run() {
		
		try {
			ds  = new DatagramSocket(listenPort);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	
		while(true){
			try {
				timerCancel =false;
				receiveBuffer=  new byte[4096];
				dp = new DatagramPacket(receiveBuffer,receiveBuffer.length);
				ds.receive(dp);	
			} catch (IOException e) {
				e.printStackTrace();
			}
			if(receiveBuffer!=null){		
	//			System.out.println("Receive Data");
				byte[] message = new byte[12];
				
				System.arraycopy(receiveBuffer, 0, message, 0, 12);
				String title = new String(message);
				title = String.copyValueOf(title.toCharArray(), 0, message.length);
				if(title.toString().equals("ROUTE UPDATE")){
			//		System.out.println("Title is right");
					double edgeCost=0;
	
					String source="";
					byte[] data = new byte[receiveBuffer.length-12];
					System.arraycopy(receiveBuffer, 12, data, 0, receiveBuffer.length-12);
					String content = new String(data);
					content = String.copyValueOf(content.toCharArray(), 0, data.length);
					String[] newTable = content.toString().trim().split("/");
					for(int i=0;i<newTable.length;i++){
						String temp[] = newTable[i].trim().split(",");				
						if(!temp[0].trim().equals("")&&!bfclient.graph.vertices.containsKey(temp[0])&&temp[0].length()>1){
							source =temp[0];
			//				System.out.println("New Node:"+source);
						}
					}
					Renew(newTable,edgeCost,source);
				}
			}
			
		}
		
	}
	private void Renew(String[] newTable, double edgeCost, String source) {
		for(int i=0;i<newTable.length;i++){
			String[] nodeInfo = newTable[i].split(",");
			if(nodeInfo[0].equals(startPos) &&!bfclient.graph.vertices.containsKey(source)) {
				edgeCost = Double.parseDouble(nodeInfo[1]);
				String[] combine = source.split(":");
				if(combine.length>=2){
					bfclient.graph.addVertex(new Vertex(source, edgeCost, combine[0], Integer.parseInt(combine[1])));
		//			System.out.println(source+" cost:"+edgeCost);
					bfclient.graph.addEdge(startPos, source,edgeCost);
				}

			}
		}
		
		for(int i=0;i<newTable.length;i++){
			String[] nodeInfo = newTable[i].trim().split(",");
			if(nodeInfo.length!=2) continue;
			if(!bfclient.graph.vertices.containsKey(nodeInfo[0])){
				bfclient.graph.addVertex(new Vertex(nodeInfo[0],Double.parseDouble(nodeInfo[1])+edgeCost,dp.getAddress().getHostAddress().toString(),dp.getPort()));
				bfclient.graph.addEdge(nodeInfo[0], startPos,Double.parseDouble(nodeInfo[1])+edgeCost);
		//		System.out.println(nodeInfo[0]+" cost:"+Double.parseDouble(nodeInfo[1]));

			}else{
				// Renew the distance
				if(Double.parseDouble(nodeInfo[1])+edgeCost<bfclient.graph.vertices.get(nodeInfo[0]).cost){
			//		System.out.println("Line 106");
					bfclient.graph.vertices.get(nodeInfo[0]).cost = Double.parseDouble(nodeInfo[1])+edgeCost;
					bfclient.graph.vertices.get(nodeInfo[0]).parent = bfclient.graph.vertices.get(source);
				}
				
			}
		}
		
		timerCancel =false;
		 finishDoBF = new Timer();
		finishDoBF.schedule(new SendTask (startPos), timeout);
		doBford(startPos);	
		
	}
	private void doBford(String startPos) {
		Vertex start = 	bfclient.graph.vertices.get(startPos);
		for(Vertex v:bfclient.graph.vertices.values()){
	//		v.cost = Integer.MAX_VALUE;
			v.parent =null;
		}
		start.cost =0.0;
		// Relax
		for(int i=1;i<=bfclient.graph.vertices.size()-1;i++){
			for(Vertex vv:bfclient.graph.vertices.values()){
				for(Edge edge:vv.getEdges()){
					
					if(edge.visited==i){
						edge.visited+=1;
						if(edge.visited==bfclient.graph.vertices.size()-1) edge.visited=1;
						Vertex source = edge.startVertex;
						Vertex target = edge.endVertex;
						if(target.cost> source.cost+edge.cost){
							timerCancel=true;
				//			System.out.println(target+"change cost from"+target.cost+"->"+source.cost+"+"+edge.cost);
							target.cost = source.cost+edge.cost;
							target.parent = source;
						}
					}
				}
			}
			for(Vertex vv:bfclient.graph.vertices.values()){
				for(Edge edge:vv.getEdges()){
					if(edge.visited==1){
						edge.visited+=1;
						Vertex source = edge.startVertex;
						Vertex target = edge.endVertex;
						if(target.cost> source.cost+edge.cost){
							System.out.println("There is negative weight cycle exists");

						}
					}
				}
			}
			
		}
		if(timerCancel==true){
			finishDoBF.cancel();
			SendDataToNeigh();
			timerCancel =false;
		}
	
		
	}
	private void SendDataToNeigh() {
		Vertex self =bfclient.graph.vertices.get(startPos);
	
		byte[] sendBuffer = new byte[4096];
		StringBuilder sb =new StringBuilder();
		StringBuilder route=new StringBuilder();
		sb.append("ROUTE UPDATE");
		for(Vertex v:bfclient.graph.vertices.values()){
			Vertex vp = v.parent;
			while(vp!=null){
				route.append(vp.name+";");
				vp = vp.parent;
			}
			sb.append("/"+v.name+","+v.cost+","+route.toString());
		}
		sendBuffer = sb.toString().getBytes();
		for(Edge w:self.getEdges()){
			try {
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
	class SendTask extends TimerTask {
		private Vertex self;
		public SendTask( String startPos) {
			
		}

		@Override
		public void run() {
//			if(timerCancel==false){
				byte[] sendBuffer = new byte[4096];
		//		System.out.println("The start Node"+startPos);
				StringBuilder sb =new StringBuilder();
				sb.append("ROUTE UPDATE");
				StringBuilder route=new StringBuilder();
				for(Vertex v:bfclient.graph.vertices.values()){
	//				System.out.println(v.name);
					Vertex vp = v.parent;
					while(vp!=null){
						route.append(vp.name+";");
						vp = vp.parent;
					}
					sb.append("/"+v.name+","+v.cost+","+route.toString());
	//				System.out.println("Line 225:"+v.name+","+v.cost+","+route.toString());
				}
				sendBuffer = sb.toString().getBytes();
				self = bfclient.graph.vertices.get(startPos);
				for(Edge w:self.getEdges()){
					try {
		//				System.out.println("Prepare for sent to"+InetAddress.getByName(w.endVertex.ip)+"Port:"+w.endVertex.port);
						DatagramSocket ds_Send = new DatagramSocket();
						DatagramPacket dp_Send = new DatagramPacket(sendBuffer,sendBuffer.length,InetAddress.getByName(w.endVertex.ip),w.endVertex.port);
				//		ds_Send.send(dp_Send);
					} catch (SocketException e) {
						e.printStackTrace();
					} catch (UnknownHostException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}	
//			}

		}

	}

}

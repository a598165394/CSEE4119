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
	private boolean change=false;
	public int RecvNum=0;
	public int timeSendNum= 0;
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
			
				receiveBuffer = new byte[4096];
				dp = new DatagramPacket(receiveBuffer,receiveBuffer.length);
				ds.receive(dp);	
				if(finishDoBF!=null) finishDoBF.cancel();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if(receiveBuffer!=null&&dp.getLength()!=0){		
	//			System.out.println("Receive Data");
				byte[] message = new byte[12];
				RecvNum++;
				
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
						double newNodeCost=0.0;
						String temp[] = newTable[i].trim().split(",");				
						if(!temp[0].trim().equals("")&&!bfclient.graph.vertices.containsKey(temp[0])&&temp[0].length()>1){
							source =temp[0];
							for(int j=0;j<newTable.length;j++){
								String tempSub[] = newTable[j].trim().split(",");	
								if(tempSub[0].trim().equals(startPos)){
									newNodeCost = Double.parseDouble(tempSub[1]);
								}
							}
							String[] spec = source.split(":");
							bfclient.graph.addVertex(new Vertex(source, newNodeCost, spec[0], Integer.parseInt(spec[1])));
							bfclient.graph.addEdge(startPos, source,newNodeCost);
							bfclient.graph.vertices.get(source).backpointer = bfclient.graph.vertices.get(startPos);
							System.out.println("New Node:"+source);
						}
					}
					
					 finishDoBF = new Timer();
					finishDoBF.schedule(new SendTask (startPos), timeout*1000);
					Renew(newTable,edgeCost,source);
				}
				receiveBuffer=  new byte[4096];
			}
			
		}
		
	}
	private void Renew(String[] newTable, double edgeCost, String source) {
		String[] combine = null ;
		double fromCost=0.0;
		String fromPos="";
		for(int i=0;i<newTable.length;i++){
	
			String[] nodeInfo = newTable[i].split(",");
			if(nodeInfo.length<=1) continue;
			if(Double.parseDouble(nodeInfo[1])==0.0) {
			//	fromCost = Double.parseDouble(nodeInfo[1]);
				fromCost = bfclient.graph.vertices.get(nodeInfo[0]).cost;
				fromPos = nodeInfo[0];
			}
			if(nodeInfo[0].equals(startPos) &&!bfclient.graph.vertices.containsKey(source)) {
				edgeCost = Double.parseDouble(nodeInfo[1]);
				combine = source.split(":");
				if(combine.length>=2){
					
					bfclient.graph.addVertex(new Vertex(source, Double.MAX_VALUE, combine[0], Integer.parseInt(combine[1])));
					System.out.println("line 99: New Add Node"+bfclient.graph.vertices.get(source).name+"Cost:"+bfclient.graph.vertices.get(source).cost);
					bfclient.graph.addEdge(startPos, source,edgeCost);
				}

			}
		}
		
		for(int i=0;i<newTable.length;i++){
		
			String[] nodeInfo = newTable[i].trim().split(",");
		
			if(nodeInfo.length<1) continue;
	
			if(!bfclient.graph.vertices.containsKey(nodeInfo[0])){
				String[] temp = nodeInfo[0].split(":");
				bfclient.graph.addVertex(new Vertex(nodeInfo[0],Double.parseDouble(nodeInfo[1])+edgeCost,dp.getAddress().getHostAddress().toString(),Integer.parseInt(temp[1])));
	//			System.out.println("New Add Node"+bfclient.graph.vertices.get(nodeInfo[0]).name+"Cost"+bfclient.graph.vertices.get(nodeInfo[0]).cost);
				bfclient.graph.addEdge(nodeInfo[0], startPos,Double.parseDouble(nodeInfo[1])+edgeCost);
		//		System.out.println(nodeInfo[0]+" cost:"+Double.parseDouble(nodeInfo[1]));

			}else{
				// Renew the distance Still will need this sentence
				if(Double.parseDouble(nodeInfo[1])+fromCost<bfclient.graph.vertices.get(nodeInfo[0]).cost){
					System.out.println("Line 121");
					bfclient.graph.vertices.get(nodeInfo[0]).cost = Double.parseDouble(nodeInfo[1])+fromCost;
					bfclient.graph.vertices.get(nodeInfo[0]).backpointer = bfclient.graph.vertices.get(fromPos);
				}
				
			}
		}
		
	
		doBford(startPos);	
		
	}
	private void doBford(String startPos) {
		Vertex start = 	bfclient.graph.vertices.get(startPos);
		for(Vertex v:bfclient.graph.vertices.values()){
	//		v.cost = Integer.MAX_VALUE;
	//		v.parent =null;
		}
		start.cost =0.0;
		// Relax
		for(int i=1;i<=bfclient.graph.vertices.size()-1;i++){
			for(Vertex vv:bfclient.graph.vertices.values()){
				for(Edge edge:vv.getEdges()){		
						Vertex source = edge.startVertex;
						Vertex target = edge.endVertex;
						if(target.cost> source.cost+edge.cost){
							change = true;
							target.cost = source.cost+edge.cost;				
							target.backpointer = vv;
						}
				}
			}
			for(Vertex vv:bfclient.graph.vertices.values()){
				for(Edge edge:vv.getEdges()){
				//	if(edge.visited==1){
					//	edge.visited+=1;
						Vertex source = edge.startVertex;
						Vertex target = edge.endVertex;
						if(target.cost> source.cost+edge.cost){
							System.out.println("There is negative weight cycle exists");

						}
		//			}
				}
			}
			
		}
		if(change==true){
			finishDoBF.cancel();
			SendDataToNeigh();
			change =false;
		}
			
		
	
		
	}
	private void SendDataToNeigh() {
		Vertex self =bfclient.graph.vertices.get(startPos);
		RecvNum--;
		byte[] sendBuffer = new byte[4096];
		StringBuilder sb =new StringBuilder();
		StringBuilder route=new StringBuilder();
		sb.append("ROUTE UPDATE");
		String temp="";
		for(Vertex v:bfclient.graph.vertices.values()){
			Vertex vp = v.backpointer;
			while(vp!=null){
				if(vp.name.equals(temp)) break;
//				System.out.println("Line 198: BackPointer"+vp.name);
				route.append(vp.name+";");
				temp = vp.name;
				vp = vp.backpointer;
			}
			if(route.toString()==null){
				sb.append("/"+v.name+","+v.cost+","+startPos);
			//	System.out.println("/"+v.name+","+v.cost+","+startPos);
			}else{
				sb.append("/"+v.name+","+v.cost+","+route.toString());
		//		System.out.println("/"+v.name+","+v.cost+","+route.toString());
			}
		
		}
		sendBuffer = sb.toString().getBytes();
		for(Edge w:self.getEdges()){
			try {
				System.out.println("line 214: Send to IP:"+w.endVertex.ip+"Send to Port:"+w.endVertex.port);
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
			if(timeSendNum<=RecvNum){
				timeSendNum++;
				byte[] sendBuffer = new byte[4096];
	//			System.out.println("Send by timer");
				StringBuilder sb =new StringBuilder();
				sb.append("ROUTE UPDATE");
				StringBuilder route=new StringBuilder();
				String temp="";
				for(Vertex v:bfclient.graph.vertices.values()){
					Vertex vp = v.backpointer;
					while(vp!=null){
						if(temp.equals(vp.name)) break;
		//				System.out.println("Line 245: BackPointer"+vp.name);
						route.append(vp.name+";");
						temp = vp.name;
						vp = vp.backpointer;
					}
					if(route.toString()==null){
						sb.append("/"+v.name+","+v.cost+","+startPos);
					}else{
					sb.append("/"+v.name+","+v.cost+","+route.toString());
					}
				}
				sendBuffer = sb.toString().getBytes();
				self = bfclient.graph.vertices.get(startPos);
				for(Edge w:self.getEdges()){
					try {
						
						System.out.println("line 260:  sent to"+InetAddress.getByName(w.endVertex.ip)+"Port:"+w.endVertex.port);
						DatagramSocket ds_Send = new DatagramSocket();
						DatagramPacket dp_Send = new DatagramPacket(sendBuffer,sendBuffer.length,InetAddress.getByName(w.endVertex.ip),w.endVertex.port);
						ds_Send.send(dp_Send);
					} catch (SocketException e) {
						e.printStackTrace();
					} catch (UnknownHostException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}	
				this.cancel();
			}

		}

	}

}

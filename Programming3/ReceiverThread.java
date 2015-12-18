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
	private String startPos;
	DatagramPacket dp = null;
	DatagramSocket ds = null;
	private Timer finishDoBF;
	private boolean change=false;
	public int RecvNum=0;
	public int timeSendNum= 0;
	int buffersize = 4096;
	byte[] receiveBuffer;
	private String break1="";
	private String break2="";
	
	
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
					byte[] keyword = new byte[1];
					double edgeCost=0;
					String source="";
					System.arraycopy(receiveBuffer, 12, keyword, 0, 1);
					String keyName = new String(keyword);
					keyName =String.copyValueOf(keyName.toCharArray(),0,keyName.length());
					byte[] data = null;
					String content;
					String sendName="";
					String[] newTable=null;
					if(keyName.equals("D")){
						// Get concent
						data = new byte[receiveBuffer.length-13];
						System.arraycopy(receiveBuffer, 13, data, 0, receiveBuffer.length-13);
						content = new String(data);
						content = String.copyValueOf(content.toCharArray(), 0, data.length);
						String[] sepContent = content.toString().trim().split("!");
						
						// Change cost
						newTable = sepContent[1].trim().split("/");
						for(int i=0;i<newTable.length;i++){
							String temp[] = newTable[i].trim().split(",");
							if(temp[0].length()<=1) continue;
							if(Double.parseDouble(temp[1])==0.0){
								sendName = temp[0];
							}
						}
						// Get the Name
						String[] breakLink = sepContent[0].trim().split(";");
						break1= breakLink[0];
						break2= breakLink[1];
						for(Edge w:bfclient.graph.vertices.get(break2).getEdges()){
							if(w.endVertex.name.equals(break1)){
								w.cost = Double.MAX_VALUE;
							}
						}
						for(Edge w:bfclient.graph.vertices.get(break1).getEdges()){
							if(w.endVertex.name.equals(break2)){
								w.cost = Double.MAX_VALUE;
							}
						}
						System.out.println("Break line is:"+break1+"<->"+break2);
						boolean connectOrNot =false;
						for(Edge e:bfclient.graph.vertices.get(startPos).getEdges()){
							if(e.endVertex.name.equals(sendName)){
								for(int i=0;i<newTable.length;i++){
									String temp[] = newTable[i].trim().split(",");
									if(temp[0].length()<=1) continue;
									if(temp[1].equals(startPos)){
										bfclient.graph.vertices.get(break1).cost = Double.parseDouble(temp[0]);
										break;
									}
								}
								RecalculaBrek();
								connectOrNot = true;
								continue;
							}
						}
						if(connectOrNot==false){
							System.out.println("LINE 126");
							bfclient.graph.vertices.get(break1).cost = 999.0;
							finishDoBF = new Timer();
							finishDoBF.schedule(new SendTask (startPos), timeout*1000);
							doBfordRe(startPos);
						}
					
					}else if(keyName.equals("R")){
						
						data = new byte[receiveBuffer.length-13];
						System.arraycopy(receiveBuffer, 13, data, 0, receiveBuffer.length-13);
						content = new String(data);
						content = String.copyValueOf(content.toCharArray(), 0, data.length);
						String[] sepContent = content.toString().trim().split("!");
						double cost =0.0;
						// Change cost
						newTable = sepContent[1].trim().split("/");
						for(int i=0;i<newTable.length;i++){
							String temp[] = newTable[i].trim().split(",");
							if(temp[0].length()<=1) continue;
							if(Double.parseDouble(temp[1])==0.0){
								sendName = temp[0];
							}
							if(temp[0].equals(startPos)){
								cost = Double.parseDouble(temp[1]);
							}
							
						}
						// Get the Name
						String[] breakLink = sepContent[0].trim().split(";");
						break1= breakLink[0];
						break2= breakLink[1];
						for(Edge w:bfclient.graph.vertices.get(break2).getEdges()){
							if(w.endVertex.name.equals(break1)){
								w.cost = cost;
							}
						}
						for(Edge w:bfclient.graph.vertices.get(break1).getEdges()){
							if(w.endVertex.name.equals(break2)){
								w.cost = cost;
							}
						}
						System.out.println("Resume between is:"+break1+"<->"+break2+"Cost is:"+cost);
						boolean connectOrNot =false;
						for(Edge e:bfclient.graph.vertices.get(startPos).getEdges()){
							if(e.endVertex.name.equals(sendName)){
								for(int i=0;i<newTable.length;i++){
									String temp[] = newTable[i].trim().split(",");
									if(temp[0].length()<=1) continue;
									if(temp[1].equals(startPos)){
										bfclient.graph.vertices.get(break1).cost = cost;
										break;
									}
								}
								finishDoBF = new Timer();
								finishDoBF.schedule(new SendTask (startPos), timeout*1000);
								doBfordThird(startPos);
								connectOrNot = true;
								continue;
							}
						}
						if(connectOrNot==false){
							System.out.println("LINE 126");
							bfclient.graph.vertices.get(break1).cost = cost;
							finishDoBF = new Timer();
							finishDoBF.schedule(new SendTask (startPos), timeout*1000);
							doBfordThird(startPos);
						}
						doBfordThird(startPos);
					}else{
						data = new byte[receiveBuffer.length-12];
						System.arraycopy(receiveBuffer, 12, data, 0, receiveBuffer.length-12);
						content = new String(data);
						content = String.copyValueOf(content.toCharArray(), 0, data.length);
						newTable = content.toString().trim().split("/");
						for(int i=0;i<newTable.length;i++){
							double newNodeCost=0.0;
							String temp[] = newTable[i].trim().split(",");	
							if(temp[0].length()<=1) continue;
							if(Double.parseDouble(temp[1])==0.0){
								sendName = temp[0];
							}
							if(!temp[0].trim().equals("")&&!bfclient.graph.vertices.containsKey(temp[0])&&temp[0].length()>1){
								source =temp[0];
								for(int j=0;j<newTable.length;j++){
									String tempSub[] = newTable[j].trim().split(",");	
									if(tempSub[0].trim().equals(startPos)&&newNodeCost==0.0){
										newNodeCost = Double.parseDouble(tempSub[1]);
										j=0;
									}
								}
								String[] spec = source.split(":");
								bfclient.graph.addVertex(new Vertex(source, Double.MAX_VALUE, spec[0], Integer.parseInt(spec[1])));
								//Edge cost between two vertex should not be change
								if(sendName.equals(source)){
									
									bfclient.graph.addEdge(startPos, source,newNodeCost);
									bfclient.graph.vertices.get(source).backpointer = bfclient.graph.vertices.get(startPos);
									System.out.println("New Node:"+source+"<->"+startPos+" Cost:"+newNodeCost);
								}
				 				
							
							}
						}
			//			System.out.println("Receive Data from<-"+sendName);
						finishDoBF = new Timer();
						finishDoBF.schedule(new SendTask (startPos), timeout*1000);
						Renew(newTable,edgeCost,source);
					}
					
				}else if(title.toString().equals("DESTROY LINK")){
					byte[] data = new byte[receiveBuffer.length-12];
					System.arraycopy(receiveBuffer, 12, data, 0, receiveBuffer.length-12);
					String content = new String(data);
					content = String.copyValueOf(content.toCharArray(), 0, data.length);	
					bfclient.graph.vertices.get(content.trim()).cost=Double.MAX_VALUE;
					
				}
				receiveBuffer=  new byte[4096];
			}
			
		}
		
	}

	private void doBfordThird(String startPos) {
		Vertex start = 	bfclient.graph.vertices.get(startPos);
		for(Vertex v:bfclient.graph.vertices.values()){
			v.cost = Integer.MAX_VALUE;
//			v.b =null;
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
			
		}
		if(change==true){
			finishDoBF.cancel();
			SendDataToNeigh(false);
			change =false;
		}
			
		
	}
	private void RecalculaBrek() {
	//	bfclient.graph.vertices.get(break1).cost = 
		// bfclient.graph.vertices.get(break2).cost = 
		finishDoBF = new Timer();
		finishDoBF.schedule(new SendTask (startPos), timeout*1000);
	
		doBfordRe(startPos);
		
	}
	private void doBfordRe(String startPos) {
		Vertex start = 	bfclient.graph.vertices.get(startPos);
		for(Vertex v:bfclient.graph.vertices.values()){
			v.cost = Integer.MAX_VALUE;
//			v.backpointer =null;
		}
		start.cost =0.0;
		// Relax
		for(int i=1;i<=bfclient.graph.vertices.size()-1;i++){
			for(Vertex vv:bfclient.graph.vertices.values()){
				for(Edge edge:vv.getEdges()){		
						Vertex source = edge.startVertex;
						Vertex target = edge.endVertex;
						if((break1.trim().equals(source.name) && break2.trim().equals(target.name)) ||(break2.trim().equals(source.name) && break1.trim().equals(target.name))){
							continue;
						}
						if((bfclient.breakInRecv.trim().equals(source.name) && bfclient.breakInStart.trim().equals(target.name)) ||(bfclient.breakInStart.equals(source.name) && bfclient.breakInRecv.trim().equals(target.name))){
							continue;
						}
						if(target.cost> source.cost+edge.cost){
							double result = source.cost+edge.cost;
								
							System.out.println("Fom-Bend Alg+ From"+ target.name+"->"+source.name+"Is update from:"+target.cost+"->"+result);

							change = true;
							target.cost = source.cost+edge.cost;				
							target.backpointer = vv;
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
		if(change==true){
			finishDoBF.cancel();
			SendDataToNeigh(true);
			change =false;
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
			//		System.out.println("line 99: New Add Node"+bfclient.graph.vertices.get(source).name+"Cost:"+bfclient.graph.vertices.get(source).cost);
					if(bfclient.graph.vertices.get(source).backpointer.name.equals(startPos)) bfclient.graph.addEdge(startPos, source,edgeCost);
					System.out.println("New Node:"+source+"<->"+startPos+" Cost:"+edgeCost);
				}

			}
		}
		
		for(int i=0;i<newTable.length;i++){
		
			String[] nodeInfo = newTable[i].trim().split(",");
		
			if(nodeInfo.length<2) continue;
	
			if(!bfclient.graph.vertices.containsKey(nodeInfo[0])){
			
				String[] temp = nodeInfo[0].split(":");
				bfclient.graph.addVertex(new Vertex(nodeInfo[0],Double.parseDouble(nodeInfo[1])+edgeCost,dp.getAddress().getHostAddress().toString(),Integer.parseInt(temp[1])));
	//			System.out.println("New Add Node"+bfclient.graph.vertices.get(nodeInfo[0]).name+"Cost"+bfclient.graph.vertices.get(nodeInfo[0]).cost);
				bfclient.graph.addEdge(nodeInfo[0], startPos,Double.parseDouble(nodeInfo[1])+edgeCost);
		//		System.out.println(nodeInfo[0]+" cost:"+Double.parseDouble(nodeInfo[1]));

			}else{
			
				// Renew the distance Still will need this sentence
				if(Double.parseDouble(nodeInfo[1])+fromCost<bfclient.graph.vertices.get(nodeInfo[0]).cost){
					boolean neverUp = false;
//					if((break1.trim().equals(startPos.trim()) && break2.trim().equals(bfclient.graph.vertices.get(nodeInfo[0]).name))){
//						neverUp=true;
//						System.out.println("Line 265"+startPos+"<->"+bfclient.graph.vertices.get(nodeInfo[0]).name);
//						
//						continue;
//					}
//					if(	break1.trim().equals(bfclient.graph.vertices.get(nodeInfo[0]).name) && break2.trim().equals(startPos)){
//						neverUp=true;
//						System.out.println("Line 273"+startPos+"<->"+bfclient.graph.vertices.get(nodeInfo[0]).name);
//						
//						continue;
//					}
//
//					if((bfclient.breakInRecv.trim().equals(startPos) && bfclient.breakInStart.trim().equals(bfclient.graph.vertices.get(nodeInfo[0]).name)) ||(bfclient.breakInStart.equals(startPos) && bfclient.breakInRecv.trim().equals(bfclient.graph.vertices.get(nodeInfo[0]).name))){
//						neverUp=true;
//						continue;
//						
//					}
					if(neverUp==false){
						double result =  Double.parseDouble(nodeInfo[1])+fromCost;
						System.out.println("Line 268: From"+ bfclient.graph.vertices.get(fromPos)+"->"+bfclient.graph.vertices.get(nodeInfo[0])+"Is update from:"+bfclient.graph.vertices.get(nodeInfo[0]).cost+"->"+result);
						bfclient.graph.vertices.get(nodeInfo[0]).cost = Double.parseDouble(nodeInfo[1])+fromCost;
						if(nodeInfo.length>=3){
							String[] routeTemp = nodeInfo[2].split(";");
							if(routeTemp.length>=3){
								bfclient.graph.vertices.get(nodeInfo[0]).backpointer = 	bfclient.graph.vertices.get(routeTemp[1]);				
								for(int j=1;j<routeTemp.length-1;j++){
									bfclient.graph.vertices.get(routeTemp[j]).backpointer = bfclient.graph.vertices.get(routeTemp[j+1]);
								}
							}else{
								bfclient.graph.vertices.get(nodeInfo[0]).backpointer = bfclient.graph.vertices.get(fromPos);
							}
						}
						
					}
				
				
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
						if((break1.trim().equals(source.name) && break2.trim().equals(target.name)) ||(break2.trim().equals(source.name) && break1.trim().equals(target.name))){
				//			System.out.println("Works?Line 226");
							continue;
						}
						if((bfclient.breakInRecv.trim().equals(source.name) && bfclient.breakInStart.trim().equals(target.name)) ||(bfclient.breakInStart.equals(source.name) && bfclient.breakInRecv.trim().equals(target.name))){
					//		System.out.println("Works?Line 226");
							continue;
						}
						if(target.cost> source.cost+edge.cost){
							double result = source.cost+edge.cost;
							if(target.cost==15){
								System.out.println("The recently cost for"+target+" is:"+target.cost);
								System.out.println("The cost for "+source+"is:"+source.cost);
								System.out.println("The cost for edge is :"+edge.cost);
							}
							
						
							System.out.println("Fom-Bend Alg+ From"+ target.name+"->"+source.name+"Is update from:"+target.cost+"->"+result);

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
	//						System.out.println("There is negative weight cycle exists");

						}
		//			}
				}
			}
			
		}
		if(change==true){
			finishDoBF.cancel();
			SendDataToNeigh(false);
			change =false;
		}
			
		
	
		
	}
	private void SendDataToNeigh(boolean control) {
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
				if(vp.name.equals(temp)||route.toString().contains(vp.name)) break;
//				System.out.println("Line 198: BackPointer"+vp.name);
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
		for(Edge w:self.getEdges()){
			try {
	//			System.out.println("line 214: Send to IP:"+w.endVertex.ip+"Send to Port:"+w.endVertex.port);
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
						if(temp.equals(vp.name)||route.toString().contains(vp.name)) break;
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
						
//						System.out.println("line 260:  sent to"+InetAddress.getByName(w.endVertex.ip)+"Port:"+w.endVertex.port);
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

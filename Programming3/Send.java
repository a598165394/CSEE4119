import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.PriorityQueue;


public class Send implements Runnable {
	private DatagramPacket dp;
	private DatagramSocket ds;
	private byte[] sendData;
	private String start;
	
	public Send(DatagramPacket dp, DatagramSocket ds, StringBuilder sb,
			int sendPort, int timeout, String start) {
		this.dp = dp;
		this.ds = ds;
		this.start = start;
		sendData = sb.toString().getBytes();
		
	}

	@Override
	public void run() {
		
		try {
			if(bfclient.graph.vertices.get(start)!=null){
				for(Edge w:bfclient.graph.vertices.get(start).getEdges()){
					ds = new DatagramSocket();
					dp = new DatagramPacket(sendData,sendData.length,InetAddress.getByName(w.endVertex.ip),w.endVertex.port);
				}
					
			}
		
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

}

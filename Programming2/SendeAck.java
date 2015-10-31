import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;


public class SendeAck implements Runnable {
	private String IP;
	private int port;
	int loop =0;
	public SendeAck(String senderIP, String senderPort) {
		this.IP = senderIP;
		this.port =41191;
	//	this.port = Integer.parseInt(senderPort);
		
	}

	 
	@Override
	public void run() {
		while(true){
			if(Receiver.connectionExit!=false){
				try {
					Socket socket = new Socket(IP,Integer.valueOf(port));
					
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				while(true){
					if(loop ==Receiver.loop){
						
					}
				}
				
				
				
			}
			
			
		}
	}

}

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

//sender 开启的ack receive应该是作为一种服务器的形态，只接受，不做任何行为
public class ReceiveAck implements Runnable {
	private String IP_Address;
	private BufferedReader bufferedReader;
	private int portNumber;
	public int count;
	private Socket ackSocket;
	public ReceiveAck(String IP_Address, int portNumber){
	
		this.IP_Address = IP_Address;
		this.portNumber = portNumber;
//		try {
//			ServerSocket serverSocket = new ServerSocket(portNumber);
//			System.out.println("Waiting for Receiver to connect ");
//			 ackSocket = serverSocket.accept();
//			 System.out.println("Connection build successful");
//			 
//			
//			
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		
		
	}
	@Override
	public void run() {
		try {
			if(count==0){
				ServerSocket serverSocket = new ServerSocket(portNumber);
				System.out.println("Waiting for Receiver to connect ");
				 ackSocket = serverSocket.accept();
				 System.out.println("Connection build successful");
			}
			count++;
			String line;
			boolean exit=false;
			bufferedReader = new BufferedReader(new InputStreamReader(ackSocket.getInputStream()));
			while(true){
				while((line = bufferedReader.readLine())!=null){
					if(line.equals("close")){
						exit =true;
						break;
					}
					
					if(Tcp_Head.ackNumber==Integer.parseInt(line)){
						if(Sender.contentBuffer.size()!=0){
							Sender.contentBuffer.remove();
						}
						System.out.println("Ack Number send back: #" +line );
						Tcp_Head.ackNumber = Integer.parseInt(line)+1;
						Tcp_Head.sequenceNumber = Integer.parseInt(line)+1;
					}
	
					
				}
				if(exit ==true){
					break;
				}
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	}

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
	private Socket ackSocket;
	public ReceiveAck(String IP_Address, int portNumber){
		this.IP_Address = IP_Address;
		this.portNumber = portNumber;
		try {
			ServerSocket serverSocket = new ServerSocket(portNumber);
			 ackSocket = serverSocket.accept();
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
	@Override
	public void run() {
		try {
			String line;
			bufferedReader = new BufferedReader(new InputStreamReader(ackSocket.getInputStream()));
			while(true){
				while((line = bufferedReader.readLine())!=null){
					System.out.println("The received ACK Number: "+line);
					Tcp_Head.ackNumber = Integer.parseInt(line)+1;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	}

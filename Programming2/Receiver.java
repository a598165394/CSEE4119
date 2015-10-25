package receiver;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class Receiver {
	public Receiver(String fileName, String listeningPort, String senderIP,
			String senderPort, String logFile) {
		DatagramPacket dp = null;
		DatagramSocket ds = null;
		DataOutputStream fileOutput = null;
		String line;
		byte[] receiveBuffer = new byte[1024];
		try {
			ds = new DatagramSocket(Integer.parseInt(listeningPort));
			 fileOutput = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(fileName)));
		} catch (NumberFormatException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (SocketException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	

		while(true){
		try {
			//	if(dp!=null){
				
					dp = new DatagramPacket(receiveBuffer, receiveBuffer.length);
					ds.receive(dp);
					while((dp.getLength())!=0){
						fileOutput.write(dp.getData(),0,dp.getLength());
						fileOutput.flush();
						receiveBuffer = new byte[1024];
						dp = new DatagramPacket(receiveBuffer, receiveBuffer.length);
						ds.receive(dp);
					}
					ds.close();
					fileOutput.close();
			//	}
			
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch(NullPointerException e1){
			
		}
		}
	}

	public static void main(String[] args) {
	//	new Receiver(args[0],args[1],args[2],args[3],args[4]);
		new Receiver("file.txt","20000","160.39.250.240","20001","logfile.txt");
	}

}

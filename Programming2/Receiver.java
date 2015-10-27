

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import sun.security.krb5.internal.SeqNumber;

public class Receiver {
	private static ExecutorService executorService = Executors.newCachedThreadPool();  
	Queue<Integer> seqQueue = new LinkedList<Integer>();
	Queue<Integer> ackQueue = new LinkedList<Integer>();
	public static boolean connectionExit = false;
	public static boolean arrivedRight = false;
	public static int loop = 0;
	private PrintWriter printWriter;
	private Socket socket;
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
//		 executorService.execute(new SendeAck(senderIP, senderPort));
		} catch (NumberFormatException e2) {
			e2.printStackTrace();
		} catch (SocketException e2) {
			e2.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	

		while(true){
		try {
			//	if(dp!=null){
				int datalength ;
				int expectPck= 0;
				int lastAck =0;
					dp = new DatagramPacket(receiveBuffer, receiveBuffer.length);
					
					ds.receive(dp);
					datalength = dp.getLength();
					if(datalength!=0){
						 socket = new Socket(senderIP,Integer.valueOf(senderPort));
						 System.out.println("Send a socket connection");
		                printWriter = new PrintWriter(socket.getOutputStream(), true);  
		               
					}
					while(datalength!=0){
						loop+=1;
						arrivedRight =false;
						connectionExit = true;
						byte[] buf =dp.getData();
						byte[] header = new byte[20];
						System.arraycopy(buf, 0, header, 0, 20);
					
						byte[] data = new byte[buf.length-20];
						System.arraycopy(buf, 20, data, 0, buf.length-20);
						byte[] seq = new byte[4];
						System.arraycopy(header, 4, seq, 0, 4);
						byte[] checksum = new byte[2];
						System.arraycopy(header, 16, checksum, 0, 2);
						int sendlength = Byte2Int2(checksum);
						byte[] receiveChecksum = calcCheckSum(data);
						int receivelength = Byte2Int2(receiveChecksum);
						System.out.println("receivelength: "+ receivelength );
						System.out.println("sendlength: "+ sendlength);
						
						int sequenceNumber = Byte2Int(seq);
		//				seqQueue.add(sequenceNumber);
						if(sequenceNumber==lastAck && sendlength==receivelength){
						//checksum的实现
	
		                    printWriter.println(lastAck);
							lastAck+=1;
							if(header[13]==(byte) 0x1){
								printWriter.println("close");
								  break;
							}
		                    System.out.println("Receive successful. Seq: " + sequenceNumber);
		                  fileOutput.flush();
							
		                    fileOutput.write(data);
		                    fileOutput.flush();
				//			fileOutput.write(data,0,data.length);
						}else{
						    System.out.println("Receive failed. Seq: " + sequenceNumber);
		                    printWriter.println(lastAck);
						}
	
						dp = new DatagramPacket(receiveBuffer, receiveBuffer.length);
						ds.receive(dp);
						datalength = dp.getLength();
					}
					ds.close();
					  fileOutput.flush();
						fileOutput.flush();
					fileOutput.close();
					socket.close();
					System.exit(0);
					
			//	}
			
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch(NullPointerException e1){
			
		}
		}
	}
	private byte[] calcCheckSum(byte[] sendDateByte) {
		byte[] checksum = new byte[2];
		for (int i = 0; i < sendDateByte.length; i += 2) {  
            checksum[0] ^= sendDateByte[i];  
            checksum[1] ^= sendDateByte[i + 1];  
        }  
        checksum[0] = (byte) ~checksum[0];  
        checksum[1] = (byte) ~checksum[1];  		
		return checksum;
	}


	public static void main(String[] args) {
	//	new Receiver(args[0],args[1],args[2],args[3],args[4]);
		new Receiver("file2.txt","20000","127.0.0.1","20001","logfile.txt");
	}
	 public static byte[] Int2Byte(int number) {   
		  byte[] byteArray = new byte[4];   
		  byteArray[0] = (byte)((number >> 24) & 0xFF);
		  byteArray[1] = (byte)((number >> 16) & 0xFF);
		  byteArray[2] = (byte)((number >> 8) & 0xFF); 
		  byteArray[3] = (byte)(number & 0xFF);
		  return byteArray;
		 }
	 
	 
	 public  static int Byte2Int(byte[] byteArray) {
		 int number = byteArray[3] & 0xFF;
		 number |= ((byteArray[2] << 8) & 0xFF00);
		 number |= ((byteArray[1] << 16) & 0xFF0000);
		 number |= ((byteArray[0] << 24) & 0xFF000000);
		 return number;		 
	}
	 public static byte[] Int2Byte2(int number) {   
		  byte[] byteArray = new byte[2];   
		  byteArray[0] = (byte)(number & 0x00ff);
		  byteArray[1] = (byte)((number & 0xff00)>>8);
		  return byteArray;
		 }
	 
	 
	 public  static int Byte2Int2(byte[] byteArray) {
		 return  ((byteArray[1] << 8) & 0xff00) | (byteArray[0] & 0xff);
		 
	}
	 

}

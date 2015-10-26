

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
	//					System.out.println(Integer.parseInt(checksum.toString()));
						int sendlength = Byte2Int2(checksum);
			//			System.out.println(sendlength);
						int sequenceNumber = Byte2Int(seq);
						seqQueue.add(sequenceNumber);
				//		System.out.println(sequenceNumber==expectPck);
				//		System.out.println(lastAck);
					//	System.out.println(sequenceNumber);
						System.out.println(sendlength);
						System.out.println(dp.getData().length);
						if(seqQueue.remove()==lastAck && sendlength==buf.length){
						//checksum的实现
	
							lastAck+=1;
		                    printWriter.println(lastAck);
		                    System.out.println("Receive successful");
							fileOutput.write(data,0,data.length);
							fileOutput.flush();
						}else{
							System.out.println("Receive failed");
		                    printWriter.println(lastAck);
						}
	//					System.out.println(expectPck);
						
						receiveBuffer = new byte[1024];
						dp = new DatagramPacket(receiveBuffer, receiveBuffer.length);
						ds.receive(dp);
						datalength = dp.getLength();
					}
					ds.close();
					fileOutput.close();
					socket.close();
					
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
		new Receiver("file2.txt","20000","160.39.251.150","20001","logfile.txt");
	}
	 public static byte[] Int2Byte(int i) {   
		  byte[] result = new byte[4];   
		  result[0] = (byte)((i >> 24) & 0xFF);
		  result[1] = (byte)((i >> 16) & 0xFF);
		  result[2] = (byte)((i >> 8) & 0xFF); 
		  result[3] = (byte)(i & 0xFF);
		  return result;
		 }
	 
	 
	 public  static int Byte2Int(byte[] bytes) {
		 int num = bytes[3] & 0xFF;
		 num |= ((bytes[2] << 8) & 0xFF00);
		 num |= ((bytes[1] << 16) & 0xFF0000);
		 num |= ((bytes[0] << 24) & 0xFF000000);
		 return num;		 
	}
	 
	 public static byte[] Int2Byte2(int i) {   
		  byte[] result = new byte[2];   
		  result[0] = (byte)(i & 0x00ff);
		  result[1] = (byte)((i & 0xff00)>>8);
		  return result;
		 }
	 
	 
	 public  static int Byte2Int2(byte[] bytes) {
		 int num =   ((bytes[1] << 8) & 0xff00) | (bytes[0] & 0xff);
		 return num;		 
	}
	 

}

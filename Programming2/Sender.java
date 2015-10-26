

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Sender {
    private static ExecutorService executorService = Executors.newCachedThreadPool();  

	public Sender(String filePath, String remoteIP, String remotePort, String ackPort, String logFile, String windows_Size)  {
		try {
			Queue<byte[]> contentBuffer = new LinkedList<byte[]>();
			DatagramSocket ds = new DatagramSocket(Integer.parseInt(ackPort));
			DatagramPacket dp = null;
			DataOutputStream logStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(logFile)));
			DataInputStream fileStream = new DataInputStream(new BufferedInputStream(new FileInputStream(filePath)));
			String line;
			int data1024;
			Tcp_Head.window_Size = Integer.parseInt(windows_Size);
			Tcp_Head.sourcePort = Integer.parseInt(ackPort);
			Tcp_Head.destPort = Integer.parseInt(remotePort);
			int loop =0;
			int ackNumber=0,sequenceNumber=0;
			//Sender 在接受ack的时候是以一种服务器的形式存在着的
			
			 
		//	Thread receiveack = new ReceiveAck();
			//receiveack.start();
			byte[] sendDateByte = new byte[1004];
	//		while((line = fileStream.readLine())!=null){
			while((data1024 =fileStream.read(sendDateByte))!=-1){
				if(loop==1){
					 executorService.execute(new ReceiveAck(remoteIP,Integer.parseInt(ackPort))); 
				}
				int temp = ackNumber;
				ackNumber = Tcp_Head.ackNumber;
				byte[] header = new byte[Tcp_Head.headerLength+sendDateByte.length];
				byte[] seq = Int2Byte(sequenceNumber);
		//		System.arraycopy(ackPort.getBytes(), 0, header, 0, 2);
		//		System.arraycopy(remotePort.getBytes(), 0, header, 2, 4);
				System.arraycopy(seq, 0, header, 4, 4);
				byte[] ack = Int2Byte(ackNumber);
				System.arraycopy(ack, 0, header, 8, 4);
	
				System.arraycopy(Int2Byte2(Integer.parseInt(windows_Size)), 0, header, 14, 2);
				System.arraycopy(sendDateByte, 0, header, 20, sendDateByte.length);
				//Bytes类型的数据结合
				byte[] checksum = Int2Byte2(header.length);
				System.arraycopy(checksum, 0, header, 16, 2);
				System.out.println(header.length);
				contentBuffer.add(header);
				while(contentBuffer.size()>Integer.parseInt(windows_Size)){
					contentBuffer.remove();
				}
				dp = new DatagramPacket(header, header.length, InetAddress.getByName(remoteIP), Tcp_Head.destPort);
				ds.send(dp);
	//			Tcp_Head.ds.send(dp);
		
				loop +=1;
			
//				Timer timeout = new Timer();
//				timeout.schedule(new reTransimission(), 3000);
				
			//每次发送文件数据，我们应该同时发送sequence number,Receiver 接受到sequence number后看这是非是自己想要的sequence number,如果不是receiver直接弃package,发想要的ack给sender，如果将发送方大小数据固定会难以实现checksum
				logStream.flush();
				logWrite(logStream,filePath,sequenceNumber,remoteIP,remotePort);
				logStream.flush();
		    	sequenceNumber +=1;
		    	while(sequenceNumber>ackNumber+4){
		    		//读取最新的ackNumber
		    		
		    		ackNumber = Tcp_Head.ackNumber;//非完成版，暂时只做rst测试
		
		    	}
		    	
		    	
			}
			ds.close();
			logStream.close();
			fileStream.close();
			System.out.println("finished");
			
	    	
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch(IOException e1){
			e1.printStackTrace();
		} 
	}

	class reTransimission extends TimerTask{

		@Override
		public void run() {
			System.out.println("Retransimission data, finished this later");
		}
		
	}

	private void logWrite(DataOutputStream logStream, String filePath, int sequenceNumber, String remoteIP, String remotePort) throws IOException {
		Timestamp ts = new Timestamp(System.currentTimeMillis());
    	//byte[] timestamp = ts.toString().getBytes();
    	logStream.write(ts.toString().getBytes(),0,ts.toString().getBytes().length);
    	logStream.writeBytes(" ");
    	logStream.write(filePath.getBytes());
    	logStream.writeBytes(" ");
    	logStream.write(remoteIP.getBytes());
    	logStream.writeBytes(" ");
    	logStream.write(remotePort.getBytes());
    	logStream.writeBytes(" ");
    	String sequence = "sequence # " + String.valueOf(sequenceNumber);
    	logStream.write(sequence.getBytes());		
    	logStream.writeBytes("\n");
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
	 

	public static void main(String[] args) {
		
		new Sender("file.txt","160.39.251.150","20000","20001","logfile.txt","4");
		//new Sender(args[0],args[1],args[2],args[3],args[4],args[5]);
	}

}

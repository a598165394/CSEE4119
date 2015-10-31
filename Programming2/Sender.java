

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
    public static Queue<byte[]> contentBuffer = new LinkedList<byte[]>();
    public int reTime=200;
    public DatagramPacket dp;
    private DatagramSocket ds;
    int sendport = 41191;
	public Sender(String filePath, String remoteIP, String remotePort, String ackPort, String logFile, String windows_Size)  {
		try {
			ds = new DatagramSocket(sendport);
			dp = null;
			DataOutputStream logStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(logFile)));
			DataInputStream fileStream = new DataInputStream(new BufferedInputStream(new FileInputStream(filePath)));
			String line;
			int endNot;
			Tcp_Head.window_Size = Integer.parseInt(windows_Size);
			Tcp_Head.sourcePort = sendport;
			Tcp_Head.destPort = Integer.parseInt(remotePort);
			int loop =0;
			int ackNumber=0,sequenceNumber=0;
			//Sender 在接受ack的时候是以一种服务器的形式存在着的
			byte[] sendDateByte = new byte[104];
			byte[] header =new byte[Tcp_Head.headerLength+sendDateByte.length];
			byte[] seq,ack,checksum;
			while((endNot =fileStream.read(sendDateByte))!=-1 ){
				
				ackNumber = Tcp_Head.ackNumber;
				header = new byte[Tcp_Head.headerLength+sendDateByte.length];
				seq = Int2Byte(sequenceNumber);
				System.arraycopy(seq, 0, header, 4, 4);
				ack = Int2Byte(ackNumber);
				System.arraycopy(ack, 0, header, 8, 4);
				checksum =calcCheckSum(sendDateByte);
				System.arraycopy(checksum, 0, header, 16, 2);
				System.arraycopy(Int2Byte2(Integer.parseInt(windows_Size)), 0, header, 14, 2);
				System.arraycopy(sendDateByte, 0, header, 20, sendDateByte.length);
				//All the types of Bytes
				contentBuffer.add(header);
				dp = new DatagramPacket(header, header.length, InetAddress.getByName(remoteIP), Tcp_Head.destPort);
				ds.send(dp);	
				if(loop==0){
					 executorService.execute(new ReceiveAck(remoteIP,Integer.parseInt(ackPort))); 
				}
				System.out.println("send out : #"+sequenceNumber);
				
				loop +=1;
			    //the time for timeout should be = Estimated RTT + 4 Dev RTT
				//Estimated RTT = 0.875 Estimated RTT + 0.125 Sample RTT;
				// Dev RTT = 0.75 Dev RTT + 0.25 |Sample RTT - Estimated RTT|
				String x = "timout"+String.valueOf(sequenceNumber);
				Timer timeout = new Timer();
				timeout.schedule(new reTransimission(sequenceNumber,remoteIP), reTime);
				logStream.flush();
	//			sendDateByte = new byte[1024];
				logWrite(logStream,filePath,sequenceNumber,remoteIP,remotePort);
				logStream.flush();
		    	sequenceNumber +=1;
		    	Tcp_Head.sequenceNumber = sequenceNumber;
		    	
		    	while(sequenceNumber>ackNumber+4){
		    		//读取最新的ackNumber
		    		ackNumber = Tcp_Head.ackNumber;//非完成版，暂时只做rst测试
		    		Thread.sleep(20);
		//    		System.out.println("The seq in the block: # "+sequenceNumber);
		 //   		System.out.println("The ack in the block: # "+ackNumber);
		    		
		    	}
		    	sendDateByte = new byte[104];
			}
			while(sequenceNumber>ackNumber+1){
				ackNumber = Tcp_Head.ackNumber;//非完成版，暂时只做rst测试
				byte[] result = contentBuffer.peek();
				byte[] realseq = new byte[4];
				System.arraycopy(result, 4, realseq, 0, 4);
				System.out.println("The seq# we are going to retransfer: # " + Byte2Int(realseq));
				Calendar calender = Calendar.getInstance();
				long startTime = calender.getTimeInMillis();
				long recentTime;
		//		contentBuffer.add(result);
				dp = new DatagramPacket(result, result.length, InetAddress.getByName(remoteIP), Tcp_Head.destPort);
				ds.send(dp);
				while(Byte2Int(realseq)+1!=Tcp_Head.ackNumber){
	//				System.out.println("In this loop or not");
					Calendar secCalender = Calendar.getInstance();
					recentTime = secCalender.getTimeInMillis();
					if(recentTime-startTime>=reTime){
						byte[] seseq = new byte[4];
						System.arraycopy(result, 4, seseq, 0, 4);
						System.out.println("Second Retansfer: #  "+ Byte2Int(seseq));
						dp = new DatagramPacket(result, result.length, InetAddress.getByName(remoteIP), Tcp_Head.destPort);
						ds.send(dp);
						Calendar newCalendar = Calendar.getInstance();
						startTime = newCalendar.getTimeInMillis();
					}
					
				}
	    		Thread.sleep(20);
//	    		System.out.println("The seq in the block: # "+sequenceNumber);
//	    		System.out.println("The ack in the block: # "+ackNumber);
			}
			fileStream.close();
		
//			0x1 为1
			header[13] = (byte) 0x1;
			seq = Int2Byte(sequenceNumber);
			System.arraycopy(seq, 0, header, 4, 4);
			ack = Int2Byte(ackNumber);
			System.arraycopy(ack, 0, header, 8, 4);
			checksum =calcCheckSum(sendDateByte);
			System.arraycopy(checksum, 0, header, 16, 2);
			System.arraycopy(Int2Byte2(Integer.parseInt(windows_Size)), 0, header, 14, 2);
			System.arraycopy(sendDateByte, 0, header, 20, sendDateByte.length);
			contentBuffer.add(header);
			dp = new DatagramPacket(header, header.length, InetAddress.getByName(remoteIP), Tcp_Head.destPort);
			ds.send(dp);	
			while(sequenceNumber!=ackNumber){
				ackNumber = Tcp_Head.ackNumber;//非完成版，暂时只做rst测试
				byte[] result = contentBuffer.peek();
				byte[] realseq = new byte[4];
				System.arraycopy(result, 4, realseq, 0, 4);
				System.out.println("The seq# we are going to retransfer: # " + Byte2Int(realseq));
				Calendar calender = Calendar.getInstance();
				long startTime = calender.getTimeInMillis();
				long recentTime;
		//		contentBuffer.add(result);
				dp = new DatagramPacket(result, result.length, InetAddress.getByName(remoteIP), Tcp_Head.destPort);
				ds.send(dp);
				while(Byte2Int(realseq)+1!=Tcp_Head.ackNumber){
	//				System.out.println("In this loop or not");
					Calendar secCalender = Calendar.getInstance();
					recentTime = secCalender.getTimeInMillis();
					if(recentTime-startTime>=reTime){
						byte[] seseq = new byte[4];
						System.arraycopy(result, 4, seseq, 0, 4);
						System.out.println("Second Retansfer: #  "+ Byte2Int(seseq));
						dp = new DatagramPacket(result, result.length, InetAddress.getByName(remoteIP), Tcp_Head.destPort);
						ds.send(dp);
						Calendar newCalendar = Calendar.getInstance();
						startTime = newCalendar.getTimeInMillis();
					}
					
				}
	    		Thread.sleep(20);
	    //		System.out.println("The seq in the block: # "+sequenceNumber);
	    //		System.out.println("The ack in the block: # "+ackNumber);
			}
			ds.close();
			logStream.close();
			System.out.println("finished");
			System.exit(0);
	    	
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch(IOException e1){
			e1.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
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

	class reTransimission extends TimerTask{
		private String IP;
		private int seq;
		private boolean successful =false;
		public reTransimission(int oldSeq, String remoteIP) throws IOException {
			IP = remoteIP;
			seq = oldSeq;
			
		}

		@Override
		public void run() {
			try{
			if(Tcp_Head.ackNumber>seq){
		
			}else{
				System.out.println("Retransfer: # "+Tcp_Head.sequenceNumber);
				System.out.println(seq);
				byte[] result = contentBuffer.peek();
				byte[] realseq = new byte[4];
				System.arraycopy(result, 4, realseq, 0, 4);
				System.out.println("The seq# we are going to retransfer: # " + Byte2Int(realseq));
				Calendar calender = Calendar.getInstance();
				long startTime = calender.getTimeInMillis();
				long recentTime;
		//		contentBuffer.add(result);
				dp = new DatagramPacket(result, result.length, InetAddress.getByName(IP), Tcp_Head.destPort);
				ds.send(dp);
				while(Byte2Int(realseq)+1!=Tcp_Head.ackNumber){
					Calendar secCalender = Calendar.getInstance();
					recentTime = secCalender.getTimeInMillis();
					if(recentTime-startTime>=reTime){
						byte[] seseq = new byte[4];
						System.arraycopy(result, 4, seseq, 0, 4);
						System.out.println("Second Retansfer: #  "+ Byte2Int(seseq));
						dp = new DatagramPacket(result, result.length, InetAddress.getByName(IP), Tcp_Head.destPort);
						ds.send(dp);
						Calendar newCalendar = Calendar.getInstance();
						startTime = newCalendar.getTimeInMillis();
					}
					
				}
				
			}
			}catch (Exception e){
				e.printStackTrace();
			}

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
	 

	public static void main(String[] args) {
		new Sender("file.txt","160.39.135.223","41192","41193","logfile.txt","4");
		//new Sender(args[0],args[1],args[2],args[3],args[4],args[5]);
	}

}

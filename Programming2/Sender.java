

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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Sender {
	private int sendport = 41191; 
    private static ExecutorService executorService = Executors.newCachedThreadPool(); 
    public static Queue<byte[]> contentBuffer = new LinkedList<byte[]>();
    public static int estimatedRTT =200;
    public static int sampleRTT=0;

    public static ArrayList<Integer> sendTimeList = new ArrayList<Integer>();
    public static ArrayList<Integer> reSendList = new ArrayList<Integer>();
    public static ArrayList<Integer> unReSendList = new ArrayList<Integer>();
    public static int reTime=estimatedRTT;
    
    public DatagramPacket dp;
    private DatagramSocket ds;
	public int reTranNumber= 0;
  
    private int buffersize = 130;
    public int recLog=1;
	public Sender(String filePath, String remoteIP, String remotePort, String ackPort, String logFile, String windows_Size)  {
		try {
			ds = new DatagramSocket(sendport);
			dp = null;
			DataOutputStream logStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(logFile)));
			DataInputStream fileStream = new DataInputStream(new BufferedInputStream(new FileInputStream(filePath)));
			int endNot;
			Tcp_Head.window_Size = Integer.parseInt(windows_Size);
			Tcp_Head.sourcePort = sendport;
			Tcp_Head.destPort = Integer.parseInt(remotePort);
			int loop =0;
			int ackNumber=0,sequenceNumber=0;
			byte[] sendDateByte = new byte[buffersize];
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
				Calendar calender = Calendar.getInstance();
		    	long sendTime = calender.getTimeInMillis();
		    	int sendTimeInt = (int)sendTime;
				ds.send(dp);	
				if(loop==0){
					 executorService.execute(new ReceiveAck(remoteIP,Integer.parseInt(ackPort))); 
				}
				unReSendList.add(sequenceNumber);
				sendTimeList.add(sendTimeInt);
	//			System.out.println("send out : #"+sequenceNumber);
				
				loop +=1;
			    //the time for timeout should be = Estimated RTT + 4 Dev RTT
				//Estimated RTT = 0.875 Estimated RTT + 0.125 Sample RTT;
				// Dev RTT = 0.75 Dev RTT + 0.25 |Sample RTT - Estimated RTT|
				Timer timeout = new Timer();
				timeout.schedule(new reTransimission(sequenceNumber,remoteIP,logStream,filePath,remotePort,ackNumber,logFile,reTime), reTime);
				logStream.flush();
				logWrite(logStream,filePath,sequenceNumber,remoteIP,remotePort,ackNumber,"Send",estimatedRTT);
				logStream.flush();
		    	sequenceNumber +=1;
		    	Tcp_Head.sequenceNumber = sequenceNumber;
		    	
		    	while(sequenceNumber>ackNumber+4){
		    		//Read recently ackNumber
		    		ackNumber = Tcp_Head.ackNumber;
		    		Thread.sleep(20);
		    	}
		    	sendDateByte = new byte[buffersize];
			}
			Thread.sleep(100);
			while(sequenceNumber>ackNumber+1){
				ackNumber = Tcp_Head.ackNumber;
				byte[] result = contentBuffer.peek();
				byte[] realseq = new byte[4];
				if(contentBuffer.size()==0 || result ==null){
					continue;
				}
				System.arraycopy(result, 4, realseq, 0, 4);

//				System.out.println("Line 100 The seq# we are going to retransfer: # " + Byte2Int(realseq));
				
				Calendar calender = Calendar.getInstance();
				long startTime = calender.getTimeInMillis();
				long recentTime;
				int len =unReSendList.size();
					for(int i=0;i<len;i++){
						if(unReSendList.get(i)==Byte2Int(realseq)){
							
							unReSendList.remove(i);
							if(i<sendTimeList.size()){
								sendTimeList.remove(i);
							}
							break;
						}
					}
			
				dp = new DatagramPacket(result, result.length, InetAddress.getByName(remoteIP), Tcp_Head.destPort);
				ds.send(dp);
			
//				if(reSendList.size()==0){
//					reSendList.add(Byte2Int(realseq));
//				}
//				for(int i=0;i<reSendList.size();i++){
////	//				System.out.println("In the loop or not");
//					if(reSendList.get(i)==Byte2Int(realseq)){
//						break;
//					}
//					if(i==reSendList.size()-1){
//						reSendList.add(Byte2Int(realseq));
//					}
//				}
//				
				reTranNumber+=1;
				logWrite(logStream,filePath,Byte2Int(realseq),remoteIP,remotePort,ackNumber,"retransmitted",estimatedRTT);
				logStream.flush();
				while(Byte2Int(realseq)+1!=Tcp_Head.ackNumber){
					Calendar secCalender = Calendar.getInstance();
					recentTime = secCalender.getTimeInMillis();
					if(recentTime-startTime>=reTime){
						byte[] seseq = new byte[4];
						System.arraycopy(result, 4, seseq, 0, 4);
//						System.out.println("Second Retansfer: #  "+ Byte2Int(seseq));
						len = unReSendList.size();
						for(int i=0;i<len;i++){
							if(unReSendList.get(i)==Byte2Int(seseq)){
								unReSendList.remove(i);
								if(i<sendTimeList.size()){
									sendTimeList.remove(i);
								}
								break;
							}
						}
						dp = new DatagramPacket(result, result.length, InetAddress.getByName(remoteIP), Tcp_Head.destPort);
						ds.send(dp);
					
//						
//						if(reSendList.size()==0){
//							reSendList.add(Byte2Int(seseq));
//						}
//						for(int i=0;i<reSendList.size();i++){
//							if(reSendList.get(i)==Byte2Int(seseq)){
//								break;
//							}
//							if(i==reSendList.size()-1){
//								reSendList.add(Byte2Int(seseq));
//							}
//						}
						
						reTranNumber+=1;
						logWrite(logStream,filePath,Byte2Int(seseq),remoteIP,remotePort,ackNumber,"retransmitted",estimatedRTT);
						logStream.flush();
						Calendar newCalendar = Calendar.getInstance();
						startTime = newCalendar.getTimeInMillis();
					}
					
				}
	    		Thread.sleep(20);
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
			Calendar cal = Calendar.getInstance();
			long sendTime = cal.getTimeInMillis();
	    	int sendTimeInt = (int)sendTime;
			unReSendList.add(sequenceNumber);
			sendTimeList.add(sendTimeInt);
	//		reTranNumber+=1;
			logWrite(logStream,filePath,sequenceNumber,remoteIP,remotePort,ackNumber,"Send last package",estimatedRTT);
			logStream.flush();
//			Thread.sleep(20);
			while(sequenceNumber!=ackNumber){
				ackNumber = Tcp_Head.ackNumber;
				byte[] result = contentBuffer.peek();
//				if(result==null){
//					if(ackNumber>=sequenceNumber){
//						continue;
//					}
//					continue;
//				}
				byte[] realseq = new byte[4];
				System.arraycopy(result, 4, realseq, 0, 4);
			
//				System.out.println("Line 150 The seq# we are going to retransfer: # " + Byte2Int(realseq));
				Calendar calender = Calendar.getInstance();
				long startTime = calender.getTimeInMillis();
				long recentTime;
				int len = unReSendList.size();
				for(int i=0;i<len;i++){
					if(unReSendList.get(i)==Byte2Int(realseq)){
						unReSendList.remove(i);
						sendTimeList.remove(i);
						break;
					}
				}
		//		contentBuffer.add(result);
				dp = new DatagramPacket(result, result.length, InetAddress.getByName(remoteIP), Tcp_Head.destPort);
				ds.send(dp);
				reTranNumber+=1;
				logWrite(logStream,filePath,Byte2Int(realseq),remoteIP,remotePort,ackNumber,"Resend last package",estimatedRTT);
				logStream.flush();
				while(Byte2Int(realseq)+1!=Tcp_Head.ackNumber){
	//				System.out.println("In this loop or not");
					Calendar secCalender = Calendar.getInstance();
					recentTime = secCalender.getTimeInMillis();
					if(recentTime-startTime>=reTime){
						byte[] seseq = new byte[4];
						System.arraycopy(result, 4, seseq, 0, 4);
	//					System.out.println("Second Retansfer: #  "+ Byte2Int(seseq));
						len = unReSendList.size();
						for(int i=0;i<len;i++){
							if(unReSendList.get(i)==Byte2Int(seseq)){
								unReSendList.remove(i);
								if(i<sendTimeList.size()){
									sendTimeList.remove(i);
								}
								break;
							}
						}
						dp = new DatagramPacket(result, result.length, InetAddress.getByName(remoteIP), Tcp_Head.destPort);
						ds.send(dp);
						reTranNumber+=1;
						logWrite(logStream,filePath,Byte2Int(seseq),remoteIP,remotePort,ackNumber,"Resend last package",estimatedRTT);
						logStream.flush();
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
//			Iterator unitr = unReSendList.iterator();
//			System.out.println("Unresend seq: ");
//			while(unitr.hasNext()){
//				System.out.print(unitr.next()+" ");
//			}
//			System.out.println();
//			System.out.println("resend seq: ");
//			Iterator itr = reSendList.iterator();
//			while(itr.hasNext()){
//				System.out.print(itr.next()+ " ");
//			}
//			System.out.println();
			int totalbyteSend = (recLog-1)*buffersize;
			System.out.println("Delivery completed successfully");
			System.out.println("Total bytes send = "+ totalbyteSend);
			System.out.println("Segments sent = "+ (ackNumber+1));
			System.out.println("Segments retransmitted = " + (recLog-ackNumber-2));
			System.exit(0);
	    	
		} catch (FileNotFoundException e) {
			System.out.println("File not exits");
			System.out.println("Please re open the Sender!");
			System.exit(0);
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
		DataOutputStream logwriteStream;
		private String sourcefile;
		private String desPort;
		private int ack;
		
		public reTransimission(int oldSeq, String remoteIP, DataOutputStream logStream, String filePath, String remotePort, int ackNumber, String logFile, int estimatedRTT ) throws IOException {
			IP = remoteIP;
			seq = oldSeq;
			logwriteStream = logStream;
			sourcefile = filePath;
			desPort = remotePort;
			ack =ackNumber;
	//		logStream= new DataOutputStream(new BufferedOutputStream(new FileOutputStream(logFile)));
			
		}

		@Override
		public void run() {
			try{
			if(Tcp_Head.ackNumber>seq){
				
			}else{
	//			System.out.println("Retransfer: # "+Tcp_Head.sequenceNumber);
//				System.out.println(seq);
				byte[] result = contentBuffer.peek();
				byte[] realseq = new byte[4];
				if(contentBuffer.size()==0|| result ==null){
					return;
				}
				System.arraycopy(result, 4, realseq, 0, 4);
				int len = unReSendList.size();
				for(int i=0;i<len;i++){
					if(unReSendList.get(i)==Byte2Int(realseq)){
						unReSendList.remove(i);
						if(i<sendTimeList.size()){
							sendTimeList.remove(i);
						}
						break;
					}
				}
//				if(reSendList.size()==0){
//					reSendList.add(Byte2Int(realseq));
//				}
//				for(int i=0;i<reSendList.size();i++){
//					if(reSendList.get(i)==Byte2Int(realseq)){
//						break;
//					}
//					if(i==reSendList.size()-1){
//						reSendList.add(Byte2Int(realseq));
//					}
//				}
//				
//				System.out.println(" The seq# we are going to retransfer: # " + Byte2Int(realseq));
				Calendar calender = Calendar.getInstance();
				long startTime = calender.getTimeInMillis();
				long recentTime;
		//		contentBuffer.add(result);
				dp = new DatagramPacket(result, result.length, InetAddress.getByName(IP), Tcp_Head.destPort);
				ds.send(dp);
		
				logWrite(logwriteStream,sourcefile,Byte2Int(realseq),IP,desPort,ack,"retransmitted",estimatedRTT);
			
	
	
				reTranNumber+=1;
		//		logWrite(logStream,filePath,Byte2Int(realseq),IP,remotePort,ackNumber,"retransmitted",reTime);
	
				while(Byte2Int(realseq)+1!=Tcp_Head.ackNumber){
				
					Calendar secCalender = Calendar.getInstance();
					recentTime = secCalender.getTimeInMillis();
					if(recentTime-startTime>=reTime){
						byte[] seseq = new byte[4];
						System.arraycopy(result, 4, seseq, 0, 4);
						if(Tcp_Head.ackNumber>Byte2Int(seseq)){
							break;
						}
						dp = new DatagramPacket(result, result.length, InetAddress.getByName(IP), Tcp_Head.destPort);
						reTranNumber+=1;
						len = unReSendList.size();
						for(int i=0;i<len;i++){
							if(unReSendList.get(i)==Byte2Int(seseq)){
								unReSendList.remove(i);
								if(i<sendTimeList.size()){
									sendTimeList.remove(i);
								}
								break;
							}
						}
			
						logWrite(logwriteStream,sourcefile,Byte2Int(realseq),IP,desPort,ack,"retransmitted",estimatedRTT);
						ds.send(dp);
					
//						System.out.println(" The seq# we are going to retransfer: # " + Byte2Int(seseq));
//						if(reSendList.size()==0){
//							reSendList.add(Byte2Int(seseq));
//						}
//						for(int i=0;i<reSendList.size();i++){
//							if(reSendList.get(i)==Byte2Int(seseq)){
//								break;
//							}
//							if(i==reSendList.size()-1){
//								reSendList.add(Byte2Int(seseq));
//							}
//						}
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

	private void logWrite(DataOutputStream logStream, String filePath, int sequenceNumber, String remoteIP, String remotePort, int ackNumber, String flag, int RTT) throws IOException {
		Timestamp ts = new Timestamp(System.currentTimeMillis());
    	//byte[] timestamp = ts.toString().getBytes();
    	logStream.write(ts.toString().getBytes(),0,ts.toString().getBytes().length);
    	logStream.writeBytes(" "+filePath+" "+remoteIP+ " "+remotePort+ " Sequence # "+String.valueOf(sequenceNumber)+" ACK # "+String.valueOf(ackNumber)
    			+" "+flag+ " "+"Estimated RTT is "+ String.valueOf(RTT));
    	logStream.writeBytes(" "+String.valueOf(recLog));
    	recLog +=1;
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
//		new Sender("file.txt","127.0.0.1","41192","41195","logfileSend.txt","20");
		new Sender(args[0],args[1],args[2],args[3],args[4],args[5]);
	}

}

package sender;

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
import java.util.Timer;
import java.util.TimerTask;

public class Sender {

	public Sender(String filePath, String remoteIP, String remotePort, String ackPort, String logFile, String windows_Size)  {
		try {
			//didn't specific the sender port number
			DatagramSocket ds = new DatagramSocket(Integer.parseInt(ackPort));
			DatagramPacket dp = null;
			DataOutputStream logStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(logFile)));
			DataInputStream fileStream = new DataInputStream(new BufferedInputStream(new FileInputStream(filePath)));
			String line;
			int ackNumber=0,sequenceNumber=0;
		//	byte[] sendDateByte = new byte[1024];
			while((line = fileStream.readLine())!=null){
				dp = new DatagramPacket(line.getBytes(), line.getBytes().length, InetAddress.getByName(remoteIP), Integer.parseInt(remotePort));
				ds.send(dp);
			
//				Timer timeout = new Timer();
//				timeout.schedule(new reTransimission(), 3000);
				//每次发送文件数据，我们应该同时发送sequence number,Receiver 接受到sequence number后看这是非是自己想要的sequence number,如果不是receiver直接弃package,发想要的ack给sender)
				logWrite(logStream,filePath,sequenceNumber,remoteIP,remotePort);
		    	sequenceNumber +=1;
//		    	while(sequenceNumber>ackNumber+4){
//		    		//读取最新的ackNumber
//		    		ackNumber = ackNumber;//非完成版，暂时只做无rst测试
//		    		Thread.sleep(300);
//		    	}
		    	
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

	public static void main(String[] args) {
		new Sender("file.txt","160.39.250.240","20000","20001","logfile.txt","568");
		//new Sender(args[0],args[1],args[2],args[3],args[4],args[5]);
	}

}

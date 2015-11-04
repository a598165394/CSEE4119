import java.net.DatagramSocket;
import java.util.HashMap;


public class Tcp_Head {
	public static int sourcePort;
	public static int destPort;
	public static int sequenceNumber = 0;
	public static int ackNumber = 0;
	public static int headerLength = 20;
	public static int  MSS = 500;
	public static int window_Size ;
//	public static DatagramSocket ds;
//	public static String data;
}

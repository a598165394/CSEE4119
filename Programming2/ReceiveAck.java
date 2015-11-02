import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Calendar;
import java.util.Iterator;

//sender 开启的ack receive应该是作为一种服务器的形态，只接受，不做任何行为
public class ReceiveAck implements Runnable {
	private String IP_Address;
	private BufferedReader bufferedReader;
	private int portNumber;
	public int count;
	public int initRTT=0;
	private boolean unDpFirst= false;
	private boolean reNewEST=true;
	private Socket ackSocket;
	public static long receiveTime=0;
	private int t = 1;
	private double EWMA = 0;
	private double oldEWMA = 0;
	public ReceiveAck(String IP_Address, int portNumber){
		this.IP_Address = IP_Address;
		this.portNumber = portNumber;		
	}
	@Override
	public void run() {
		try {
			if(count==0){
				ServerSocket serverSocket = new ServerSocket(portNumber);
				 ackSocket = serverSocket.accept();
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
	//					System.out.println("Ack Number send back: #" +line );
						for(int j=0;j<Sender.unReSendList.size();j++){
	//						System.out.println("Receive Seq: # "+line+" "+Sender.unReSendList.get(j));
							if(Integer.parseInt(line)==Sender.unReSendList.get(j)){		
								unDpFirst = true;
								break;
							}
						}
						if(unDpFirst==true ){
							Calendar secCalender = Calendar.getInstance();
							receiveTime = secCalender.getTimeInMillis();
						
							int rightSendTime=0;
							for(int i=0;i<Sender.unReSendList.size();i++){
								if(Integer.parseInt(line)==Sender.unReSendList.get(i)){
									rightSendTime = Sender.sendTimeList.get(i);
									break;
								}
							}
							Sender.sampleRTT = ((int)receiveTime-rightSendTime);	
							if(Sender.sampleRTT>0){
								if(reNewEST==true){
									Sender.estimatedRTT = Sender.sampleRTT;
								}
								reNewEST=false;
//								System.out.println("Sample RTT: "+Sender.sampleRTT);
//								System.out.println("Estimated RTT: "+Sender.estimatedRTT);
//								System.out.println("Time out Time: "+ Sender.reTime);
//						
//								
//							
//								System.out.println("Time take for send and back for Seq "+line+" Time takes for round trip"+String.valueOf(Sender.sampleRTT));
								//Sender.sampleRTT = (Sender.sampleRTT*(Sender.unReSendList.size())+((int)receiveTime-rightSendTime))/(Sender.unReSendList.size()+1);
								Sender.estimatedRTT = (int) ((0.875*Sender.estimatedRTT)+(Sender.sampleRTT*0.125));
								if(t==1){
									EWMA=(2/(t+1))*Sender.estimatedRTT;
									oldEWMA =EWMA;
								}else{
									EWMA =(2/(t+1))*Sender.estimatedRTT+(1-(2/(t+1)))*oldEWMA;
									oldEWMA =EWMA;
								}
								EWMA = (0.75*(EWMA)+0.25*Math.abs(Sender.sampleRTT-Sender.estimatedRTT));
								oldEWMA = EWMA;
//								System.out.println("The Derivation is: "+EWMA);
								int time = (int) (Sender.estimatedRTT + 4*EWMA);
								if(time<5){
									Sender.reTime=4+time;
								}else{
									Sender.reTime = time;
								}
								t +=1;
							}
							
						}
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

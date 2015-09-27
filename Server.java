import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server extends Thread{
	private ServerSocket serverSocket;
	public int serverPort = 5357;
	public static int BLOCK_TIME = 60;

	public static int TIME_OUT = 8;
	 private static ExecutorService exec; 
	public static List<String> userList = new ArrayList<String>();
	public static List<Long> timeList = new ArrayList<Long>();
	public static List<Socket> clientList = new ArrayList<Socket>();
	public static List<Long> userTime = new ArrayList<Long>();
	public static List<Long> wholeTimeList = new ArrayList<Long>();
	public static List<String> wholeUserList = new ArrayList<String>();
	public  String line;
	
	public PrintWriter pw;
	public BufferedReader br;

	public static String[] usernamedatabase ={"columbia",
			"seas",
			"csee4119", 
			"foobar", 
			"windows", 
			"google", 
			"facebook", 
			"wikipedia", 
			"network"};
	public static String[] passworddatabase ={"116bway",
			"summerisover",
			"lotsofassignments",
			"passpass",
			"withglass",
			"partofalphabet",
			"wastetime",
			"donation",
			"seemsez"};

	public Server(){
		launchServer();
	}
	
	private void launchServer() {
		try{
			serverSocket = new ServerSocket(serverPort);
			exec = Executors.newCachedThreadPool();
			
			Socket clientSocket =null;
			while(true){
				 clientSocket = serverSocket.accept();
			
				 exec.execute(new ServerThread(clientSocket));
	
			}	
		}catch (Exception e){
			e.printStackTrace();
		}
		
	}

	
	static class cleanUserThread implements Runnable{
		private PrintWriter pw;
		private String line;
		public cleanUserThread(){
		
				
				
			
		}
	
		
		@Override
		public void run(){
			while(true){
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
				
					e1.printStackTrace();
				}
			List<Socket> tempSocketList = new ArrayList<Socket>();
			List<String> tempUserList = new ArrayList<String>();
			
			Calendar calender = Calendar.getInstance();
			long recentlyTime = calender.getTimeInMillis();
			for(int i=0;i<userTime.size();i++){
				if(recentlyTime-userTime.get(i)>=(TIME_OUT*60*1000)){
					tempSocketList.add(clientList.get(i));
					tempUserList.add((userList.get(i)));
					
				}
			}
			for (Socket inactiveSocket :tempSocketList ){
				try {
					
					pw = new PrintWriter(inactiveSocket.getOutputStream(),true);
					line = "logout";
					pw.println(line);	
				} catch (IOException e) {
				
					e.printStackTrace();
				}
				
			}
			
			for(int i=0;i<tempUserList.size();i++){
				for(int j=0;j<userList.size();j++){
					if(tempUserList.get(i).equals(userList.get(j))){
						userList.remove(j);
						userTime.remove(j);
						timeList.remove(j);
						clientList.remove(j);
						i=i-1;
						j=userList.size();
						
						break;
					}
				}
			}
			
		}
		}
	}
	
	
	
	static class ServerThread implements Runnable {
		
		private Socket socket;
		private BufferedReader br;
		private PrintWriter pw;
		private String line;
		private String specUser;
		public ServerThread(Socket socket) {
			
			this.socket = socket;
			  try {
				br = new BufferedReader(new InputStreamReader(socket  
				          .getInputStream()));
			
			} catch (IOException e) {
		
				e.printStackTrace();
			}
			
			 
			  
		}
	
		public void run(){
			  
			try{
				
				 br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				  
	   
				 int loginSuccessCount =0;
	            
	             int loginCount =1;
	             int wrongCount =0;
	             while ((line = br.readLine()) != null) {  
	            	 
	            	 if(loginSuccessCount ==0){
	            		 for(int i=0;i<usernamedatabase.length;i++){
	            			 if(line.trim().equals(usernamedatabase[i])){
	            				 loginSuccessCount+=1;
	            				 line ="Password:"; 	
	            				 pw = new PrintWriter(socket.getOutputStream(),true);
	            				 pw.println(line);
	            				 loginCount =1;
	      
	            			 	 break; 	
	            		 }else{
	            			 loginCount+=1;
	            			 if(loginCount==usernamedatabase.length){
	            				 line = "Username or Password Wrong! Try again!";
		            			 pw = new PrintWriter(socket.getOutputStream(),true);
		         				 pw.println(line);
		            			 wrongCount+=1;
		            			 BLOCK_TIME =BLOCK_TIME*1000;
		            			 if(wrongCount ==3){
		            				 Thread.sleep(BLOCK_TIME);
		            				
		            			 }
		            			 break;
		            		 }
	            		 }
	            		
	            		 }
	            	 }else if(loginSuccessCount ==1){
	            	 for(int i=0;i<passworddatabase.length;i++){
	            		 if(line.trim().equals(passworddatabase[i])){
	            			 
	            			 for(int k=0;k<userList.size();k++){
	          			 		if(usernamedatabase[i].equals(userList.get(k))){
	          			 			line = "Prohibit concurrent duplict Users";
	          			 			pw = new PrintWriter(socket.getOutputStream(),true);
			         				pw.println(line);
	 		            			 	break;
	          			 		}
	          			 	 }
	            			loginSuccessCount+=1;
	      			 		clientList.add(socket);
	          			 	userList.add(usernamedatabase[i]);
	          			 	wholeUserList.add(usernamedatabase[i]);
	          			 	Calendar calender = Calendar.getInstance();
	          			 	long loginTime =  calender.getTimeInMillis();
	          			 	
	          			 	timeList.add(loginTime);
	          			 	wholeTimeList.add(loginTime);
	          			 	userTime.add(loginTime);
	          			 	line = "Welcome to Simple Chat Server!"+ "\n"+"Command:";
	          			 	pw = new PrintWriter(socket.getOutputStream(),true);
	        				pw.println(line);
	        				loginCount=1;
	        				 exec.execute(new cleanUserThread());
	        				
	          			    break;
	            		 }else {
	            			 loginCount+=1;
	            			 if(loginCount==usernamedatabase.length){
		            			 line = "Username or Password Wrong! Try again!";
		            			 pw = new PrintWriter(socket.getOutputStream(),true);
		         				pw.println(line);
		            			 wrongCount+=1;
		            			 BLOCK_TIME = BLOCK_TIME*1000;
		            			 if(wrongCount ==3){
		            				 Thread.sleep(BLOCK_TIME);
		            		         }
		            			 break;
	            			 	}
	            		 }
	            	 }
	            		    
    			 	
	            	 
	             }
	             else if(loginSuccessCount>1){
	            	 for(int l=0;l<userList.size();l++){
	            		 if(socket.equals(clientList.get(l))){
	            			 Calendar calender = Calendar.getInstance();
		          			 long time =  calender.getTimeInMillis();
	            			 userTime.set(l, time);
	            		 }
	            	 }
	            	loginSuccessCount =3;
	            	String[] resultList = (line.trim()).split(" ");	
	            	String allOrNot ="";
	            	String typedOutput = "";
	    			int length = line.length();
	    			String[] commandArray= new String[length];
	    			for(int i=0;i<length;i++){
	    					Character tempCha = line.charAt(i);
	    					commandArray[i] = tempCha.toString();
	    			}
	    			
	    				if(length>=8){
	    					for(int i=0;i<7;i++){
	    						typedOutput+=commandArray[i];
	    					}
	    					if(length>16){
	    						for(int k=0;k<17;k++){
		    						allOrNot+=commandArray[k];
		    					}
	    					}
	    					specUser ="";
	    					specUser = resultList[0];
		    				specUser += " ";	
		    				try{
		    					specUser +=resultList[1];
		    				}catch (Exception e){
		    					line ="Wrong Command!!!Re-try!!";
								pw = new PrintWriter(socket.getOutputStream(),true);							
								pw.println(line);
		    				}
	    				}
	    				
	    				
	    				
						if((line.trim()).equals("whoelse")){
							
							int len=userList.size();
							String message ="";
							for(int i=0;i<len;i++){
								message+=userList.get(i);
								message+="\n";
							}
						
							pw = new PrintWriter(socket.getOutputStream(),true);
					
							pw.println(message);
	     		
	     			 	
						}
						else if(line.trim().equals("logout")){
							pw = new PrintWriter(socket.getOutputStream(),true);
							line = "logout";
							pw.println(line);
							
						}else if((allOrNot.trim()).equals("broadcast message")){
							String allMessage ="";
							for(int z=0;z<clientList.size();z++){
								if(socket.equals(clientList.get(z))){
									allMessage += userList.get(z);
									allMessage +=": ";
									break;
								}
							}
							for(int j=18;j<length;j++){
								allMessage+=commandArray[j];
							
							}
							for (Socket liveSocket :clientList ){
								pw = new PrintWriter(liveSocket.getOutputStream(),true);
								pw.println(allMessage);	
							}
						}else if((specUser.trim()).equals("broadcast user")){
		
							String specMessage = "";
							for(int u=0;u<clientList.size();u++){
								if(socket.equals(clientList.get(u))){
									specMessage += userList.get(u);
									specMessage +=": ";
									break;
								}
							}
							for(int i=0;i<resultList.length;i++){
								if(resultList[i].equals("message")){
									for(int z=i+1;z<resultList.length;z++){
										specMessage+= resultList[z];
										specMessage+= " ";
									}
									for(int j=2;j<=i-1;j++){
										for(int h=0;h<userList.size();h++){
											if(resultList[j].equals(userList.get(h))){
												pw = new PrintWriter((clientList.get(h)).getOutputStream(),true);							
												pw.println(specMessage);
												break;
											}
										}
										
									}
									
								}
							}
						}else if(resultList[0].equals("message")){
							String outputResult ="";
							int findNumber = 1;
							for(int i=0;i<userList.size();i++){
								if(resultList[1].equals(userList.get(i))){
									
									for(int z=0;z<clientList.size();z++){
										if(socket.equals(clientList.get(z))){
											outputResult += userList.get(z);
											outputResult +=": ";
											break;
										}
									}
									for(int j=2;j<resultList.length;j++){
										outputResult+=resultList[j];
										outputResult+=" ";
									}
									line = outputResult;
									pw = new PrintWriter((clientList.get(i)).getOutputStream(),true);								
									pw.println(line);
									
									break;
								}else if(findNumber == userList.size()){
									line ="Wrong Command!!!Re-try!!";
									pw = new PrintWriter(socket.getOutputStream(),true);							
									pw.println(line);
									
								}
								findNumber +=1;
							}
		            	 
						}else if((typedOutput.trim()).equals("wholast")){
							String timeString="";
							int time;
							for(int j=8;j<length;j++){
								timeString +=commandArray[j];
							}
							try{
								time = Integer.parseInt(timeString);
						
								if(time<=0|time >=60){
									line ="Wrong Command!!!Re-try!!";
								
									pw = new PrintWriter(socket.getOutputStream(),true);							
									pw.println(line);
								}else{
							
									Calendar calender = Calendar.getInstance();
			          			 	long rightNowTime =  calender.getTimeInMillis();
			          			 	time = time*60*1000;
			          			 	int sizeList = wholeUserList.size();
			          			 	String recentUser ="";
			          			 	for(int i=sizeList-1;i>=0;i--){
			          			 		if((rightNowTime-wholeTimeList.get(i))<=time){
			          			 			recentUser += wholeUserList.get(i)+"\n";
			          			 		}
			          			 	}
			          			 	
			          			 	pw = new PrintWriter(socket.getOutputStream(),true);							
									pw.println(recentUser);
								}
								
							}catch (Exception e){
								e.printStackTrace();
							
						
							}
						}
	             	}
	            	 
	          

	             }
				
			
				}
			catch (IOException  e){
					e.printStackTrace();
			}catch (InterruptedException e1){
				e1.printStackTrace();
			}
		}
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new Server();
		
	}

}

   



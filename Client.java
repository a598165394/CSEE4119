

import java.io.BufferedReader;  

import java.io.InputStreamReader;  
import java.io.PrintWriter;  
import java.net.InetAddress;
import java.net.Socket;  
import java.util.concurrent.ExecutorService;  
import java.util.concurrent.Executors;  
public class Client {
	 
	    private static ExecutorService executorService = Executors.newCachedThreadPool();  
	    public static int loginNumber =0;
	   
	  
	    public Client(String IP, String portNumber) {  
	        try {  
	        	
	            Socket socket = new Socket(IP,Integer.valueOf(portNumber));  
	            executorService.execute(new Sender(socket));  
	            System.out.println("Username:");
	            String message;  
	            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket  
	                    .getInputStream()));  
	           
	            while ((message = bufferedReader.readLine()) != null) {  
	                System.out.println(message); 
	                if (message.trim().equals("logout")) { 

	                    bufferedReader.close();  
	                    executorService.shutdownNow();  
	                    System.exit(0);
	                    break;  
	                }  
	            }  
	        } catch (Exception e) {  
	  
	        }  
	        
	      
	   
	    }  
	    
	   
	  
	    /** 
	     * Client get message from console;
	     * 
	     */  
	    static class Sender implements Runnable {  
	        private Socket socket;  
	  
	        public Sender(Socket socket) {  
	            this.socket = socket;  
	        }  
	  
	        public void run() {  
	            try {  
	                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(  
	                        System.in));  
	                PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);  
	                String message;  
	  
	                while (true) {  
	                    message = bufferedReader.readLine();  
	                    printWriter.println(message);
	              
	  
	                    if (message.trim().equals("logout")) { 
	                 
	                        printWriter.close();  
	                        bufferedReader.close();  
	                        executorService.shutdownNow();  
	                        break;  
	                    }  
	                }  
	            } catch (Exception e) {  
	                e.printStackTrace();  
	            }  
	        }  
	    }

	    public static void main(String[] args)  {  
	    	try{
	    		new Client(args[0],args[1]);  
	    
	    	}catch (Exception e){
	    		
	    	}
	        
	    }  
	 
}

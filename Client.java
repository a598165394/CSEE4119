

import java.io.BufferedReader;  

import java.io.InputStreamReader;  
import java.io.PrintWriter;  
import java.net.InetAddress;
import java.net.Socket;  
import java.util.concurrent.ExecutorService;  
import java.util.concurrent.Executors;  
public class Client {
	  	private static final int PORT = 5358;
	    private static ExecutorService exec = Executors.newCachedThreadPool();  
	    public static int loginNumber =0;
	   
	  
	    public Client() {  
	        try {  
	        	
	            Socket socket = new Socket(InetAddress.getLocalHost(), PORT);  
	            exec.execute(new Sender(socket));  
	            System.out.println("Username:");
	            
	            BufferedReader br = new BufferedReader(new InputStreamReader(socket  
	                    .getInputStream()));  
	            String msg;  
	            while ((msg = br.readLine()) != null) {  
	                System.out.println(msg); 
	                if (msg.trim().equals("logout")) { 

	                    br.close();  
	                    exec.shutdownNow();  
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
	                BufferedReader br = new BufferedReader(new InputStreamReader(  
	                        System.in));  
	                PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);  
	                String msg;  
	  
	                while (true) {  
	                    msg = br.readLine();  
	                    pw.println(msg);
	              
	  
	                    if (msg.trim().equals("logout")) { 
	                 
	                        pw.close();  
	                        br.close();  
	                        exec.shutdownNow();  
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
	    		new Client();  
	    	}catch (Exception e){
	    		
	    	}
	        
	    }  
	 
}

import java.util.Calendar;
import java.util.List;


public class TimeCheck implements Runnable {

	private long timeout;

	public TimeCheck(int timeout) {
	
		this.timeout = timeout;
	}

	@Override
	public  void run() {
		while(true){
			Calendar cald = Calendar.getInstance();
			 long time = cald.get(Calendar.HOUR_OF_DAY)*3600+cald.get(Calendar.MINUTE)*60+cald.get(Calendar.SECOND);
			if(bfclient.nameNode.size()!=0){
		//		System.out.println("Loops");
				
				 for(int i=0;i<=bfclient.timeRecv.size()-1;i++){
						
 			
 					 double boundaryCond = 0;
 					 if(timeout >=600){
 						boundaryCond = timeout*4;
 					 }else if(timeout<=10){
 						boundaryCond = timeout*15;
 					 }else{
 						boundaryCond = timeout*10;
 					 }
					 if(time-bfclient.timeRecv.get(i) > boundaryCond+80){
						 
						 System.out.println("Current time out:"+timeout+"; Close happend for: "+bfclient.nameNode.get(i)+"last Time:"+time+";Rec Time:"+bfclient.timeRecv.get(i));
						 bfclient.graph.vertices.get(bfclient.nameNode.get(i)).cost = Double.MAX_VALUE;
						 bfclient.dieList.add(bfclient.nameNode.get(i));
						 bfclient.timeRecv.remove(i);
						 bfclient.nameNode.remove(i);
						 i=0;
						 if(bfclient.timeRecv.size()==0) break;
					 }
				 }
			}else{
	//			System.out.println(bfclient.nameNode.size());
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}

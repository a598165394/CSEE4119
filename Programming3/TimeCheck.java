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
			
			if(bfclient.nameNode.size()!=0){
		//		System.out.println("Loops");
				
				 for(int i=0;i<=bfclient.timeRecv.size()-1;i++){
					 Calendar cald = Calendar.getInstance();
					 if(cald.getTimeInMillis()-bfclient.timeRecv.get(i) > 5*timeout*1000){
			//			 System.out.println("Close happend for: "+bfclient.nameNode.get(i));
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
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}

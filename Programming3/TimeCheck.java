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
 					 long time = cald.get(Calendar.HOUR_OF_DAY)*3600+cald.get(Calendar.MINUTE)*60+cald.get(Calendar.SECOND);
 					 long boundaryCond = 0;
 					 if(timeout >=600){
 						boundaryCond = timeout*4;
 					 }else if(timeout>=300){
 						boundaryCond = timeout*5;
 					 }else if(timeout>=150){
 						boundaryCond = timeout*6;
 					 }else if(timeout>=60){
 						boundaryCond = timeout*8;
 					 }else if(timeout>=30){
 						boundaryCond = timeout*10;
 					 }else if(timeout>=15){
 						boundaryCond = timeout*12;
 					 }else if(timeout>=5){
 						boundaryCond = timeout*13;
 					 }else if(timeout>=1){
 						boundaryCond = timeout*15;
 					 }
					 if(time-bfclient.timeRecv.get(i) > boundaryCond){
						 System.out.println("Close happend for: "+bfclient.nameNode.get(i));
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

# CSEE4119 hd2337 Hengming Dai java

	a. The function of my code is works almost same as the Programming+Assignment+2 described.
	b. The development environment for my code is ubuntu 14.10, Java version 1.6.0_36, Eclipse 3.8.1
	c.
       	   1. The way to run the code, frist we need unzip from the hd2337_java.zip.
	   2. Then type make in terminal, the ReceiveAck.class,Sender.class, Receiver.class and Tcp_Head.class will occur automatic.
	   3. Then you need revoke Receiver first, the way to invoke server will be  java Receiver file2.txt 41194 127.0.0.1 41193 logfileReceiver.txt
	   4. Then you need revoke Sender. The way to invoke sender will be java Sender file.txt 127.0.0.1 41192 41193 logfileSend.txt 20
	   5. The default sender udp port for Sender is 41191, which is used to match the source port in Proxy(-i), because the way I invoke the Proxy is ./newudpl -i127.0.0.1 -o127.0.0.1 -v -L99 -B90000 -O9 -d1 
	   6. If you want to change the Sender sent udp port, you can easily go the Sender.java in the line #30, the variable number is senderport. The line #30 is looks like " private int sendport = 41191; ". You can change this number to any number you want to match the format.
	   7. When set the delay as the big number , the programming will run a long time, sorry for the long time.
  

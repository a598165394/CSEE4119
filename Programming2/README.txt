# CSEE4119 hd2337 Hengming Dai java

	a. The function of my code is works almost same as the Programming+Assignment+2 described.
	b. The development environment for my code is ubuntu 14.10, Java version 1.6.0_36, Eclipse 3.8.1
	c.
       1. The way to run the code, frist we need unzip from the hd2337_java.zip.
	   2. Then type make in terminal, the ReceiveAck.class,Sender.class, Receiver.class and Tcp_Head.class will occur automatic.
	   3. In clic machine, we should not use 127.0.0.1 , due to the CLIC default setting, if using 127.0.0.1 it will throw  java.net.ConnectException sometimes.. So we should use exatcly Ip address for the machine , or we can use beijing to replace with the 128.59.15.30 if the clic machine we are using is beijing. So the easily way to invoke will be like: Following are the example how to use it if we are in the beijing clic machine 
		Proxy:
			./newudpl -ibeijing -obeijing -v -L9 -B90 -O4 -d1 
		Receiver:
			java Receiver file2.txt 41194 beijing 41195 
		Sender:
			java Sender file.txt beijing 41192 41195 logfileSend.txt 4
	   4. The default sender udp port for Sender is 41191, which is used to match the source port in Proxy(-i), because the way I invoke the Proxy is ./newudpl -i128.59.15.30 -o128.59.15.30 -v -L9 -B90 -O4 -d1 
	   5. Then you need revoke Receiver first, the way to invoke server will be java Receiver file2.txt 41194 128.59.15.30 41195 logfileReceiver.txt
	   6. Then you need revoke Sender. The way to invoke sender will be java Sender file.txt 128.59.15.30 41192 41195 logfileSend.txt 5
       7. If you want to change the Sender sent udp port, you can easily go the Sender.java in the line #28, the variable name is senderport. The line #30 is looks like " private int sendport = 41191; ". You can change this number to any number you want only need to match the default in the Proxy.
	   8. When set the delay as the big number , the programming will run a long time, sorry for the long time.
  

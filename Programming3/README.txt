# CSEE4119 hd2337 Hengming Dai java

	a. The function of my code is works almost same as the Programming+Assignment+3 described.
	b. The development environment for my code is ubuntu 14.10, Java version 1.6.0_36, Eclipse 3.8.1
	c.
	Type make, it will build the executable file. The way to using is showed below. The 160.39.134.48 is the local ip address. I test with 5 nodes.
	Example:
	java bfclient 4118 3
	java bfclient 4116 3 160.39.134.48 4118 5.0
	java bfclient 4115 3 160.39.134.48 4118 30.0 160.39.134.48 4116 5.0
	java bfclient 4117 3 160.39.134.48 4116 10
	java bfclient 4119 3 160.39.134.48 4118 3 160.39.134.48 4115 1.0
	
	In terminal for java bfclient 4115, type LINKDOWN 160.39.134.48 4118 to close the link between the 4118 and 4116. 
	In terminal for java bfclient 4115, type LINKUP 160.39.134.48:4118 to resume the last link between 4118 and 4116
	In terminal for java bfclient 4119, type CLOSE to Close the node 4119. After a while, the rest of node cann't reach 4119 anymore.
	By type SHOWRT it will allow to show the current routing table.

	d.The protocol I using is own protocol based on UDP and have extra 13 bytes. First 12 bytes is "ROUTE UPDATE", if packet's first 12 byte are not "ROUTE UPDATE", the receiver will reject it directly. If first 12 byte is "ROUTE UPDATE", now move on start check the 13th byte. If the 13th byte is " ", it means it just normal data transfer. If the 13th byte is "D", it means there are LINKDOWN happened, we need clear the break link. If the 13th byte is "N", it means there are LINKUP happened, we need resume the break link.

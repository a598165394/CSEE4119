JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
	 $(JC) $(JFLAGS) $*.java



CLASSES = \
	Receiver.java \
	ReceiveAck.java \
	Sender.java \
	Tcp_Head.java \

default: classes

classes: $(CLASSES:.java=.class)
	
clean:
	$(RM) *.class -- *~ logfileReceiver.txt logfileSend.txt file2.txt

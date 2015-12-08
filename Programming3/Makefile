JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
	 $(JC) $(JFLAGS) $*.java



CLASSES = \
	bfclient.java \
	Send.java \
	SendTask.java \
	Vertex.java \
	ReceiverThread.java \
	Edge.java \
	Graph.java \

default: classes

classes: $(CLASSES:.java=.class)
	
clean:
	$(RM) *.class -- *~ logfileReceiver.txt logfileSend.txt file2.txt stdout



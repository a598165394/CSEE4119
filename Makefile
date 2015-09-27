JCC = javac
JFLAGS = -g

Server.class: Server.java
	$(JCC) $(JFLAGS) Server.java

Client.class: Client.java
	$(JCC) $(JFLAGS) Convert.java

clean:
	$(RM) *.class




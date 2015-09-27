JAVAC = javac
JFLAGS = -encoding UTF-8




Server.class: Server.java
		javac Server.java

.PHONY: clean
clean:
	rm -f *.class Server -- *~



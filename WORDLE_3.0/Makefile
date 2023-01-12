JC=javac
JVM=java
CLASSPATH_DEP="./json-simple-1.1.1.jar":"./gson-2.10.jar":"./"

c_server:
	$(JC)  -cp $(CLASSPATH_DEP) ./WordleServerMain.java

c_client:
	$(JC)  -cp $(CLASSPATH_DEP) ./WordleClientMain.java

serverjar:
	java -jar WORDLE_3.0s.jar src/main/resources/server.properties

clientjar:
	java -jar WORDLE_3.0c.jar src/main/resources/client.properties

clean:
	lsof -t -i:39000| xargs kill -9

c_clean:
	rm *.class

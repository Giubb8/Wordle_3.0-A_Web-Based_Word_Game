JC=javac
JVM=java
CLASSPATH_DEP="./json-simple-1.1.1.jar":"./gson-2.10.jar":"./"

comp_server:
	$(JC)  -cp $(CLASSPATH_DEP) ./WordleServerMain.java

comp_client:
	$(JC)  -cp $(CLASSPATH_DEP) ./WordleClientMain.java

serverjar:
	java -jar WORDLE_3.0s.jar ../resources/server.properties

clientjar:
	java -jar WORDLE_3.0c.jar ../resources/client.properties

clean:
	lsof -t -i:39000| xargs kill -9

rm_comp:
	rm *.class

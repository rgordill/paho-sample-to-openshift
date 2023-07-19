#/bin/bash

## To build the project, use

# mvn clean compile assembly:assembly

# To run the project, use the following:

export JAVA_HOME=/home/rgordill/Clients/java/jdk1.7.0_80-x64
export PATH=$JAVA_HOME/jre/bin:$PATH

#export JAVA_HOME=/usr/lib/jvm/java-1.8.0
#export JAVA_HOME=/usr/lib/jvm/java-17
#export PATH=$JAVA_HOME/bin:$PATH

java -version
java -jar -Djavax.net.debug=all target/paho-sample-1.0-SNAPSHOT-jar-with-dependencies.jar

# Only for SunJSSE
#java -jar -Djavax.net.debug=all -Djavax.net.ssl.keyStore=src/main/resources/certs/server.jks -Djavax.net.ssl.keyStorePassword=changeit -Djavax.net.ssl.trustStore=src/main/resources/certs/server.jks -Djavax.net.ssl.trustStorePassword=changeit target/paho-sample-1.0-SNAPSHOT-jar-with-dependencies.jar

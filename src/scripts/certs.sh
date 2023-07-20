#!/bin/bash

## This is a sample script to gather the certs from a server, and add them to a java keystore
## It requires openssl from the os and keytool from java. Don't forget to use keytool from the corresponding java version!

JAVA_HOME=/home/rgordill/Clients/java/jdk1.7.0_80-x64
SERVERNAME=demo-mqtt-0-svc-rte-my-namespace.apps.my-domain.com

openssl s_client -showcerts -servername ${SERVERNAME} -connect ${SERVERNAME}:443 < /dev/null | awk '/BEGIN/,/END/{ if(/BEGIN/){a++}; out="cert"a".crt"; print >out}' 

export PATH=${JAVA_HOME}/bin:$PATH

for cert in *.crt; 
do 
  newname=$(openssl x509 -noout -subject -in $cert | sed -n 's/^.*CN =\(.*\)$/\1/; s/[ ,.*]/_/g; s/__/_/g; s/^_//g; s/@/_/g; p').pem; 
  keytool -importcert -alias $newname -file $cert -keystore ../main/resources/certs/server.jks -storepass changeit -noprompt
  rm $cert; 
done

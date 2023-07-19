# Sample MQTT client with paho to OpenShift brokers using SNI in old clients (JDK 7)

This repo contains a simple sample that creates a MQTT publisher to send messages to a MQTT broker deployed on OpenShift, that is exposed using the OCP routers (haproxy)

## The challenge

OCP routers expose all the endpoints through an haproxy using a wildcard domain (*.apps.\<domain\>). The target service is selected with the destination hostname in http headers or in ssl negotiation using SNI (Service Name Indication).

SNI was introduced in Java 7, and backported to Java 6, but it only works transparently in SunJSSE from Java 8. Indeed, in java 6 the business edition from Oracle is needed, and even [This issue](https://bugs.openjdk.org/browse/JDK-6985179) says it is included in Java 7, I have not been able to set up properly in last community edition (7u80).

There is another issue with TLS negotiation with newer servers, old algorithms like TLS v1.1 are considered insecure. That means that the JSSE provider needs to be configured for handshake using new versions like TLS v1.2.

However, there are lots of old terminals that needs java 7 runtimes. In [this referece](https://source.android.com/docs/setup/start/older-versions#jdk), Android 5.x and 6 uses JDK 7, which introduces this challenge if an app should be built for those terminals.

## Bountycastle to the rescue

[Bountycastle](https://www.bouncycastle.org) is another JSSE provider that implements SNI for Java 7. I have used this provider in the sample, to check out that it is a viable solution in this version.

Don't forget to check all the implications and certifications of an JSSE provider prior to use it in a production environment.

## The sample

This is a very basic sample to serve as guidance to build a MQTT publisher with Java 7. Paho MQTT java version that is the library that I used removed the support for Java 7 from 1.2.2, so I have used 1.2.1 for my test.

I have also used very simple properties and logback for logging, to avoid more dependencies into the picture.
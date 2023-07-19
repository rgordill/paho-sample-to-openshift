package com.mycompany.app;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Properties;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Example {
    private static final Logger logger = LoggerFactory.getLogger(Example.class);

    public static void main(String[] args) {

        try {
            Security.insertProviderAt(new org.bouncycastle.jsse.provider.BouncyCastleJsseProvider(), 1);

            Properties props = new Properties();
            props.load(Example.class.getClassLoader().getResourceAsStream("application.properties"));

            String broker = props.getProperty("broker");
            String clientId = props.getProperty("clientId");

            MqttClient sampleClient = new MqttClient(broker, clientId, null);

            String username = props.getProperty("username");
            String password = props.getProperty("password");
            int keepAlive = Integer.parseInt(props.getProperty("keepAlive"));
            String protocol = props.getProperty("protocol");
            String keyStoreFile = props.getProperty("keystoreFile");
            String keyStorePass = props.getProperty("keystorePass");

            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setUserName(username);
            connOpts.setPassword(password.toCharArray());
            connOpts.setKeepAliveInterval(keepAlive);
            connOpts.setCleanSession(true);
            connOpts.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);

            // sslContext.init(null, null, new SecureRandom());
            SSLSocketFactory socketFactory = getSocketFactory(keyStoreFile, keyStorePass, protocol);
            connOpts.setSocketFactory(socketFactory);

            logger.info("Connecting to broker: " + broker);
            sampleClient.connect(connOpts);
            logger.info("Connected");

            String content = props.getProperty("content");
            int qos = Integer.parseInt(props.getProperty("qos"));
            String topic = props.getProperty("topic");

            logger.info("Publishing message: " + content);
            MqttMessage message = new MqttMessage(content.getBytes());
            message.setQos(qos);
            sampleClient.publish(topic, message);
            logger.info("Message published");

            sampleClient.disconnect();
            logger.info("Disconnected");

            sampleClient.close();
        } catch (Exception e) {
            logger.error("Exception:", e);
        }
    }

    private static SSLSocketFactory getSocketFactory(
            String keystoreFile,
            String keystorePass,
            String protocol)
            throws GeneralSecurityException, IOException {

        System.setProperty("jsse.enableSNIExtension", "true");
        System.setProperty("org.bouncycastle.jsse.client.assumeOriginalHostName", "true");

        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());

        try (InputStream in = new FileInputStream(keystoreFile)) {
            keystore.load(in, keystorePass.toCharArray());
        }
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keystore, keystorePass.toCharArray());

        TrustManagerFactory trustManagerFactory = TrustManagerFactory
                .getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keystore);

        SSLContext sslContext = SSLContext.getInstance(protocol);
        sslContext.init(
                keyManagerFactory.getKeyManagers(),
                trustManagerFactory.getTrustManagers(),
                new SecureRandom());

        return sslContext.getSocketFactory();
    }

}
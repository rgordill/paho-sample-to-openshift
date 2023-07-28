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

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionHandler {

  private Properties props;
  private MqttClient mqc;

  public ConnectionHandler(Properties props) {
   Security.insertProviderAt(new org.bouncycastle.jsse.provider.BouncyCastleJsseProvider(), 1);

    this.props = props;
  }

  private static final Logger logger = LoggerFactory.getLogger(ConnectionHandler.class);

  public void connect() throws Exception {
    String broker = props.getProperty("broker");
    String clientId = props.getProperty("clientId");

    mqc = new MqttClient(broker, clientId, null);

    String username = props.getProperty("username");
    String password = props.getProperty("password");
    int keepAlive = Integer.parseInt(props.getProperty("keepAlive"));
    boolean cleanSession = Boolean.parseBoolean(props.getProperty("cleanSession"));
    String protocol = props.getProperty("protocol");
    String keyStoreFile = props.getProperty("keystoreFile");
    String keyStorePass = props.getProperty("keystorePass");

    MqttConnectOptions connOpts = new MqttConnectOptions();
    connOpts.setUserName(username);
    connOpts.setPassword(password.toCharArray());
    connOpts.setKeepAliveInterval(keepAlive);
    connOpts.setCleanSession(cleanSession);
    connOpts.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);

    // sslContext.init(null, null, new SecureRandom());
    SSLSocketFactory socketFactory = getSocketFactory(keyStoreFile, keyStorePass, protocol);
    connOpts.setSocketFactory(socketFactory);

    logger.info("Connecting to broker: " + broker);
    mqc.connect(connOpts);
    logger.info("Connected");
  }

  public void disconnect() throws Exception {
    mqc.disconnect();
    logger.info("Disconnected");

    mqc.close();
  }

  public void publish(String topic, String content, int qos) throws Exception {
    logger.info("Publishing message: " + content);
    MqttMessage message = new MqttMessage(content.getBytes());
    message.setQos(qos);

    mqc.publish(topic, message);
    logger.info("Message published");

  }

  public void subscribe(String topic, int qos, IMqttMessageListener callback) throws Exception{
    logger.info("Subscribing to topic: " + topic);
    mqc.subscribe(topic, qos, callback);
  }

  private SSLSocketFactory getSocketFactory(
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

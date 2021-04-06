package com.example.mqttapp;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String clientId = "projects/my-smart-device-2453c/locations/us-central1/registries/my-registry/devices/my-device2";

        String topic        = "/devices/my-device2/my-device-events";
        String content      = "Message from MqttPublishSample";
        int qos             = 1;
        String broker       = "ssl://mqtt.googleapis.com:8883";
        MemoryPersistence persistence = new MemoryPersistence();


        try {
            IotCorePasswordGenerator passwordGenerator = new IotCorePasswordGenerator("my-smart-device-2453c", getResources(), R.raw.private_key);
            MqttClient sampleClient = new MqttClient(broker, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setUserName("unused");
            InputStream inputStream = getResources().openRawResource(R.raw.roots);

            /*SocketFactory.SocketFactoryOptions socketFactoryOptions = new SocketFactory.SocketFactoryOptions();
            try {
                socketFactoryOptions.withCaInputStream(getResources().openRawResource(R.raw.roots));
                connOpts.setSocketFactory(new SocketFactory(socketFactoryOptions));
            } catch (Exception e) {
                e.printStackTrace();
            }*/


            InputStream is = getResources().openRawResource(R.raw.roots);
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate) certificateFactory.generateCertificate(is);
            String alias = "alias";//cert.getSubjectX500Principal().getName();

            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null);
            trustStore.setCertificateEntry(alias, cert);
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(trustStore, null);
            KeyManager[] keyManagers = kmf.getKeyManagers();

            TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
            tmf.init(trustStore);
            TrustManager[] trustManagers = tmf.getTrustManagers();

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagers, trustManagers, null);

            connOpts.setSocketFactory(sslContext.getSocketFactory());


            connOpts.setPassword(passwordGenerator.createJwtRsaPassword());
            connOpts.setCleanSession(true);
            connOpts.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);
            Log.d("tag_bug_bug","Connecting to broker: "+broker);
            sampleClient.connect(connOpts);
            Log.d("tag_bug_bug","Connected");
            Log.d("tag_bug_bug","Publishing message: "+content);
            MqttMessage message = new MqttMessage(content.getBytes());
            message.setQos(qos);
            sampleClient.publish(topic, message);
            Log.d("tag_bug_bug","Message published");
            //sampleClient.disconnect();
            Log.d("tag_bug_bug","Disconnected");
            //System.exit(0);
        } catch(Exception me) {
            Log.d("tag_bug_bug","msg "+me.getMessage());
            Log.d("tag_bug_bug","loc "+me.getLocalizedMessage());
            Log.d("tag_bug_bug","cause "+me.getCause());
            Log.d("tag_bug_bug","excep "+me);
            me.printStackTrace();
        }
    }
}
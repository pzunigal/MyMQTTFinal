package com.example.mymqttfinal;

import static android.graphics.Color.GREEN;
import static android.graphics.Color.RED;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;

public class MainActivity extends AppCompatActivity {
    String clienteID = "";
    //CVariables para la conexion al servidor MQTT
    static String MQTTHOST = "tcp://crudfirebase.clud.shoftr.io:1883";
    static String MQTTUSER = "CrudFireBasePzun";
    static String MQTTPASS = "syUQ2Fni1Z4HctbU";



    static String TOPIC = "LED";
    static String TOPIC_MSG_ON = "Encender";
    static String TOPIC_MSG_OFF = "Apagar";

    //Otra variables de la actividad
    MqttAndroidClient cliente;
    MqttConnectOptions opciones;
    Boolean permisoPublicar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Configuracion de la interfaz de usuario
        getNombreCliente();
        connectBroker();
        //Eventos de clic en botones
        Button btn_encendido = findViewById(R.id.btn_encender);
        btn_encendido.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enviarMensaje(TOPIC, TOPIC_MSG_OFF);
            }
        });
    }

    private void enviarMensaje(String topic, String msg) {
        checkConnection();
        if (this.permisoPublicar) {
            try {
                int qos = 0;
                this.cliente.publish(topic, msg.getBytes(), qos, false);
                Toast.makeText(getBaseContext(), topic + " : " + msg, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void checkConnection() {
        if (this.cliente.isConnected()) {
            this.permisoPublicar = true;
        } else {
            this.permisoPublicar = false;
            connectBroker();
        }
    }

    private void connectBroker() {
        this.cliente = new MqttAndroidClient(this.getApplicationContext(), MQTTHOST, this.clienteID);
        this.opciones = new MqttConnectOptions();
        this.opciones.setUserName(MQTTUSER);
        this.opciones.setPassword(MQTTPASS.toCharArray());
        try {
            IMqttToken token = this.cliente.connect(opciones);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Toast.makeText(getBaseContext(), "Conectado", Toast.LENGTH_SHORT).show();
                    mensajearTopic();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(getBaseContext(), "Conexión fallida", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void mensajearTopic() {
        try {
            this.cliente.subscribe(TOPIC, 0);
        } catch (MqttSecurityException e) {
            e.printStackTrace();
        } catch (MqttException e) {
            e.printStackTrace();
        }

        this.cliente.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Toast.makeText(getBaseContext(), "Se desconectó del servidor", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                TextView txtinfo = findViewById(R.id.txtInfo);

                if (topic.matches(TOPIC)) {
                    String msg = new String(message.getPayload());
                    if (msg.matches(TOPIC_MSG_ON)) {
                        txtinfo.setText(msg);
                        txtinfo.setBackgroundColor(GREEN);
                    }
                    if (msg.matches(TOPIC_MSG_OFF)) {
                        txtinfo.setText(msg);
                        txtinfo.setBackgroundColor(RED);
                    }
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }

    private void getNombreCliente() {
        String manufacturer = Build.MANUFACTURER;
        String modelName = Build.MODEL;
        this.clienteID = manufacturer + " " + modelName;

        TextView txtIdCliente = findViewById(R.id.txtInfo);
        txtIdCliente.setText(this.clienteID);
    }
}
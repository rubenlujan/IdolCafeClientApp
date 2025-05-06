package com.hrg.idolcafeclientapp.utils;

import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Arrays;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        if (!remoteMessage.getData().isEmpty()) {
            String title = remoteMessage.getData().get("title");
            assert title != null;
            String paymentId = "";
            if(title.contains("Pago")) {
                String body = remoteMessage.getData().get("body");
                String[] parts = title.split("\\|");
                if(parts.length > 1) {
                    title = parts[0];
                    paymentId = parts[1];
                }


                Intent intent = new Intent("PAYMENT_RECEIVE");
                intent.putExtra("title", title);
                intent.putExtra("body", body);
                intent.putExtra("paymentid", paymentId);

                Log.d("FCM", "Titulo: " + title);
                Log.d("FCM", "Cuerpo: " + body);
                Log.d("FCM", "Accion: " + intent.getAction());

                //sendBroadcast(intent);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            }
        }
    }


    @Override
    public void onNewToken(String token) {
        // Manejar la generación de un nuevo token
        Log.d("FCM", "Nuevo token: " + token);
        sendTokenToServer(token);
    }

    private void sendTokenToServer(String token) {
        // Enviar el token a tu servidor para que pueda enviar notificaciones a este dispositivo
    }
}

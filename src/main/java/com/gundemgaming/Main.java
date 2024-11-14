package com.gundemgaming;

import com.fazecast.jSerialComm.SerialPort;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Main {
    private static boolean apiCalled = false;
    public static void main(String[] args) {
        SerialPort arduinoPort = SerialPort.getCommPort("COM3");
        arduinoPort.setBaudRate(9600);
        arduinoPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);

        if (arduinoPort.openPort()) {
            System.out.println("Bağlantı kuruldu.");
        } else {
            System.out.println("Port açılırken hata oluştu.");
            return;
        }

        InputStream in = arduinoPort.getInputStream();
        new Thread(() -> {
            try {
                while (true) {
                    if (in.available() > 0 && !apiCalled) {
                        byte[] buffer = new byte[in.available()];
                        int len = in.read(buffer);
                        if (len > 0) {
                            String receivedData = new String(buffer, 0, len).trim();
                            System.out.println("Arduino'dan Gelen Veri: " + receivedData);

                            if (receivedData.equals("1")) {
                                sendApiRequest();
                                apiCalled = true;
                            }
                        }
                    }
                    Thread.sleep(100);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

    }

    private static void sendApiRequest() {
        try {
            URL url = new URL("http://localhost:8080/api/notification/send-notification");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            int responseCode = conn.getResponseCode();
            System.out.println("API Yanıt Kodu: " + responseCode);
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

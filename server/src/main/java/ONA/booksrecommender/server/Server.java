/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ONA.booksrecommender.server;

/**
 *
 * @author falzy
 */

import java.io.*;
import java.net.*;

public class Server {
    public static void main(String[] args) {
        int port = 1234;
        System.out.println("Avvio del server...");
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server in ascolto sulla porta " + port);
            
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Connessione accettata da " + socket.getInetAddress());
                
                // Nuovo thread per gestire ogni client
                new Thread(() -> {
                    try (
                        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
                    ) {
                        String message;
                        while ((message = in.readLine()) != null) {
                            System.out.println("Ricevuto: " + message);
                            out.println("Echo: " + message);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


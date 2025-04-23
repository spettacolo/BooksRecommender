/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ONA.booksrecommender.client;

/**
 *
 * @author falzy
 */

import java.io.*;
import java.net.*;

public class Client {
    public static void main(String[] args) {
        int porta = 1234;
        try (ServerSocket serverSocket = new ServerSocket(porta)) {
            System.out.println("Server in ascolto sulla porta " + porta);
            Socket socket = serverSocket.accept();
            System.out.println("Connessione accettata da " + socket.getInetAddress());

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            String messaggio;
            while ((messaggio = in.readLine()) != null) {
                System.out.println("Ricevuto: " + messaggio);
                out.println("Echo: " + messaggio);
            }

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

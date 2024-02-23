import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class ChatServer {
    static final int PORT = 12345;
    private static Set<String> connectedUsers = new HashSet<>();
    private static Set<ClientHandler> clients = new HashSet<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server listening on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);
                clientHandler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler extends Thread {
        private Socket clientSocket;
        private String userNickname;
        private PrintWriter writer;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try {
                Scanner scanner = new Scanner(clientSocket.getInputStream());
                writer = new PrintWriter(clientSocket.getOutputStream(), true);

                // Verificar si el nickname ya está en uso
                synchronized (connectedUsers) {
                    while (true) {
                        userNickname = scanner.nextLine();
                        if (!connectedUsers.contains(userNickname)) {
                            connectedUsers.add(userNickname);
                            break;
                        } else {
                            // Informar al cliente que el nickname está en uso
                            writer.println("/nicknameinuse");
                        }
                    }
                }

                // Notificar la conexión exitosa
                System.out.println(userNickname + " se conectó.");
                broadcast(userNickname + " se unió al chat.");

                // Enviar la lista de usuarios conectados al nuevo cliente
                sendConnectedUsers();

                // Manejar mensajes del cliente
                while (true) {
                    String clientMessage = scanner.nextLine();
                    if (clientMessage.equalsIgnoreCase("/quit")) {
                        break;
                    }
                    broadcast(userNickname + ": " + clientMessage);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                // Limpiar recursos y desconectar al usuario
                if (userNickname != null) {
                    connectedUsers.remove(userNickname);
                    broadcast(userNickname + " se fue del chat.");
                    System.out.println(userNickname + " se desconectó.");
                }

                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Eliminar el cliente de la lista de clientes
                clients.remove(this);

                // Enviar la lista actualizada de usuarios a los clientes restantes
                sendConnectedUsers();
            }
        }

        private void broadcast(String message) {
            synchronized (clients) {
                for (ClientHandler client : clients) {
                    client.writer.println(message);
                }
            }
        }

        private void sendConnectedUsers() {
            synchronized (clients) {
                StringBuilder userListMessage = new StringBuilder("/lisaUsuarios ");
                for (ClientHandler client : clients) {
                    userListMessage.append(client.userNickname).append(" ");
                }
                // Envía la lista de usuarios a todos los clientes
                for (ClientHandler client : clients) {
                    client.writer.println(userListMessage.toString());
                }
            }
        }
    }
}

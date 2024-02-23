import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class FirstClient extends JFrame {
    private JTextField nicknameField;
    private JButton joinButton;

    public FirstClient() {
        setTitle("First Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 100);
        setLocationRelativeTo(null);

        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());

        JPanel inputPanel = new JPanel(new FlowLayout());
        nicknameField = new JTextField(20);
        joinButton = new JButton("Join");
        joinButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String nickname = nicknameField.getText().trim();
                if (!nickname.isEmpty()) {
                    try {
                        joinChat(nickname);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    JOptionPane.showMessageDialog(FirstClient.this, "Please enter a nickname.");
                }
            }
        });

        inputPanel.add(new JLabel("Nickname:"));
        inputPanel.add(nicknameField);
        inputPanel.add(joinButton);

        mainPanel.add(inputPanel, BorderLayout.CENTER);
        add(mainPanel);
    }

    private void joinChat(String nickname) throws IOException {
        Socket socket = new Socket("localhost", ChatServer.PORT);
        Scanner scanner = new Scanner(socket.getInputStream());
        PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

        writer.println(nickname);

        new Thread(() -> {
            try {
                while (scanner.hasNextLine()) {
                    String serverMessage = scanner.nextLine();
                    handleServerMessage(serverMessage);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        new ChatWindow(socket).setVisible(true);
        setVisible(false);
    }

    private void handleServerMessage(String message) {
        // Maneja los mensajes del servidor, por ejemplo, actualiza la lista de usuarios conectados
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new FirstClient().setVisible(true);
            }
        });
    }
}

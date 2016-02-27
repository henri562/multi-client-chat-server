package multi.client.chat.server;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 *
 * @author Mengchuan Lin
 */
public class ChatServer extends JFrame {
    private JTextArea jta;
    private JPanel jpl;
    private final ArrayList<Socket> socketList;
    private static int clientNo;

    public ChatServer() {
        initComponents();
        socketList = new ArrayList<>();
        jta.append("Chat server started at " + new Date() + '\n');
    }

    private void initComponents() {
        //add components to panel
        jpl = new JPanel();
        jpl.setLayout(new BorderLayout());
        jta = new JTextArea();
        jta.setEditable(false);
        jpl.add(new JScrollPane(jta), BorderLayout.CENTER);

        //add components to frame
        setLayout(new BorderLayout());
        add(jpl, BorderLayout.CENTER);

        setTitle("Chat Server");
        Dimension d = new Dimension(500, 300);
        setSize(d);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocation(200, 300);
        setVisible(true);
    }

    private class handleClient implements Runnable {
        private final Socket clientSocket;

        public handleClient(Socket socket) {
            clientSocket = socket;
            socketList.add(socket);
            printClientInfo();
        }

        private void printClientInfo() {
            jta.append("Starting thread for Client " + ++clientNo
                              + " at " + new Date() + '\n');
            jta.append("Client " + clientNo + "'s host name is "
                              + clientSocket.getInetAddress().getHostName()
                              + '\n');
            jta.append("Client " + clientNo + "'s IP address is "
                              + clientSocket.getRemoteSocketAddress().toString()
                              + '\n');
        }

        @Override
        public void run() {
            boolean listening = true;
            while (listening) {
                try {
                    //read strings from client
                    DataInputStream fromClient =
                             new DataInputStream(clientSocket.getInputStream());
                    String name = fromClient.readUTF();
                    String message = fromClient.readUTF();

                    jta.append(name + ": " + message + '\n');

                    //send strings to all connected clients
                    for (Socket s : socketList) {
                        DataOutputStream toClient =
                                      new DataOutputStream(s.getOutputStream());
                        toClient.writeUTF(name);
                        toClient.writeUTF(message);
                    }
                }
                catch (IOException ioe) {
                    System.err.println(ioe);
                }
            }
        }
    }

    public static void main(String[] args) {
        //instantiate server object
        ChatServer s = new ChatServer();
        int portNum = 9999;
        boolean listening = true;

        //create server socket and listen for client connections indefinitely
        try (ServerSocket serverSocket = new ServerSocket(portNum)) {
            while (listening) {
                new Thread(s.new handleClient(serverSocket.accept())).start();
            }
        }
        catch (IOException ioe) {
            System.err.println("Could not listen on port " + portNum);
            System.exit(-1);
        }
    }
}

package multi.client.chat.server;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
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
    /* A mapping from sockets to DataOutputStreams. This allows us to avoid
    having to create a DataOutputStream each time we want to write to a stream
    */
    private final HashMap<Socket, DataOutputStream> outputStreams =
                                                                new HashMap<>();

    public ChatServer(int portNum) {
        initComponents();
        listen(portNum);
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

    private void listen(int portNum) {
        jta.append("Chat server started at " + new Date() + '\n');
        try {
            ServerSocket ss = new ServerSocket(portNum);
            //keep accepting connections from client
            while (true) {
                Socket s = ss.accept(); //grab incoming connection
                jta.append("Connection from " + s + '\n');
                //map socket to DataOutputStream
                DataOutputStream dout =
                                      new DataOutputStream(s.getOutputStream());
                outputStreams.put(s, dout);
                //start individual thread to deal with client
                new Thread(new handleClient(this, s)).start();
            }
        }
        catch (IOException ioe) {
            System.err.println(ioe);
        }
    }

    void sendToAll(String msg) {
        /* this is synchronized in the event that another thread is calling
        removeConnection() which also works on the same Hashmap */
        synchronized(outputStreams){
            for (Map.Entry<Socket, DataOutputStream> e :
                 outputStreams.entrySet()) {
                //get corresponding DataOutputStream object from Hashmap
                DataOutputStream dout = e.getValue();
                try {
                    dout.writeUTF(msg); //write to client
                }
                catch(IOException ioe) {
                    System.err.println(ioe);
                }
            }
        }
    }

    void removeConnection(Socket s) {
        //synchronized in case another thread is calling sendToAll()
        synchronized(outputStreams) {
            outputStreams.remove(s); //remove key(socket) from Hashmap
            try {
                jta.append("Connection removed\n");
                s.close();
            }
            catch (IOException ioe) {
                System.err.println(ioe);
            }
        }
    }

    private class handleClient implements Runnable {
        private final ChatServer server;
        private final Socket socket;

        public handleClient(ChatServer server, Socket socket) {
            this.server = server;
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                while(true) {
                    //read strings from client
                    DataInputStream din =
                                  new DataInputStream(socket.getInputStream());
                    String name = din.readUTF();
                    String message = din.readUTF();

                    jta.append(name + ": " + message + '\n');

                    //send strings to all connected clients
                    server.sendToAll(name);
                    server.sendToAll(message);
                }
            }
            catch (IOException ioe) {
                System.err.println(ioe);
            }
            finally {
                //if a client connection is closed, clean up
                server.removeConnection(socket);
            }
        }
    }

    public static void main(String[] args) {
        //instantiate server object
        int portNum = 9999;
        new ChatServer(portNum);
    }
}

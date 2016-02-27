package multi.client.chat.server;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 *
 * @author Mengchuan Lin
 */
public class ChatClient extends JFrame {
    private JTextArea jta;
    private JTextField jtfName, jtfMsg;
    private JPanel jpl1, jpl2, jpl3;
    private String name, message;

    public ChatClient() {
        initComponents();
        jtfMsg.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!jtfName.getText().isEmpty() &&
                    !jtfMsg.getText().isEmpty()) {
                    name = jtfName.getText().trim();
                    message = jtfMsg.getText().trim();
                    exchangeData();
                }
                else if (jtfName.getText().isEmpty())
                    JOptionPane.showMessageDialog(null, "Enter name.",
                                "No name detected",
                                JOptionPane.ERROR_MESSAGE);
                else
                    JOptionPane.showMessageDialog(null, "Enter chat message.",
                                "No message detected",
                                JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void initComponents() {
        //populate label panel
        jpl1 = new JPanel();
        jpl1.setLayout(new GridLayout(2, 1));
        jpl1.add(new JLabel("Name"));
        jpl1.add(new JLabel("Enter text"));

        //populate textfield panel
        jpl2 = new JPanel();
        jpl2.setLayout(new GridLayout(2, 1));
        jtfName = new JTextField();
        jtfName.setHorizontalAlignment(JTextField.RIGHT);
        jpl2.add(jtfName);
        jtfMsg = new JTextField();
        jtfMsg.setHorizontalAlignment(JTextField.RIGHT);
        jpl2.add(jtfMsg);

        //put label and button panel on jpl3
        jpl3 = new JPanel();
        jpl3.setLayout(new BorderLayout());
        jpl3.add(jpl1, BorderLayout.WEST);
        jpl3.add(jpl2, BorderLayout.CENTER);

        //align panels on frame
        setLayout(new BorderLayout());
        add(jpl3, BorderLayout.NORTH);
        jta = new JTextArea();
        jta.setEditable(false);
        add(new JScrollPane(jta), BorderLayout.CENTER);

        setTitle("Loan Client");
        Dimension d = new Dimension(400, 300);
        setSize(d);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocation(700, 300);
        setVisible(true);
    }

    private void exchangeData() {
        int portNum = 9999;
        try (Socket clientSocket = new Socket("localhost", portNum);) {
            DataInputStream in =
                             new DataInputStream(clientSocket.getInputStream());
            in.readUTF();

        }
        catch (IOException ioe) {
            System.err.println(ioe);
        }
    }

    public static void main(String[] args) {
        new ChatClient();
    }
}

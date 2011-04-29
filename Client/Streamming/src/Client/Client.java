package Client;

/* ------------------
 Client
 usage: java Client [Server hostname] [Server RTSP listening port] [Video file requested] [algoritmo criptografia]
 versão 2.0
 Author: Eduardo Henriques
 ---------------------- */

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.StringTokenizer;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

import monografia.commons.AESUtilities;
import monografia.commons.Algorithm;
import monografia.commons.BlowFishUtilities;
import monografia.commons.DESUtilities;
import monografia.commons.DESedeUtilities;
import monografia.commons.Utilities;

public class Client {

    // Handler for Pause button
    // -----------------------
    private class pauseButtonListener implements ActionListener {
        @Override
        public void actionPerformed(final ActionEvent e) {

            System.out.println("- PAUSE -");

            if (Client.state == RTSPState.PLAYING) {
                // increase RTSP sequence number
                RTSPSeqNb++;

                // Send PAUSE message to the server
                sendRtspRequest("PAUSE");

                // Wait for the response
                if (parseServerResponse() != 200) {
                    System.out.println("Resposta inválida do servidor");
                } else {
                    // change RTSP state and print out new state
                    Client.state = RTSPState.READY;
                    System.out.println("Novo estado do RTSP: READY");

                    // stop the timer
                    timer.stop();
                }
            }
            // else if state != PLAYING then do nothing
        }
    }

    // Handler for Play button
    // -----------------------
    private class playButtonListener implements ActionListener {
        @Override
        public void actionPerformed(final ActionEvent e) {

            System.out.println("- PLAY -");

            if (Client.state == RTSPState.READY) {
                // increase RTSP sequence number
                RTSPSeqNb++;

                // Send PLAY message to the server
                sendRtspRequest("PLAY");

                // Wait for the response
                if (parseServerResponse() != 200) {
                    System.out.println("Resposta inválida do servidor");
                } else {
                    // change RTSP state and print out new state
                    Client.state = RTSPState.PLAYING;
                    System.out.println("Novo estado do RTSP: PLAYING");

                    // start the timer
                    timer.start();
                }
            }// else if state != READY then do nothing
        }
    }

    // Handler for Setup button
    // -----------------------
    private class setupButtonListener implements ActionListener {
        @Override
        public void actionPerformed(final ActionEvent e) {

            System.out.println("- CONECTAR -");

            if (Client.state == RTSPState.INIT) {
                // Init non-blocking RTPsocket that will be used to receive data
                try {
                    // construct a new DatagramSocket to receive RTP packets
                    // from the server, on port RTP_RCV_PORT
                    RTPsocket = new DatagramSocket(Client.RTP_RCV_PORT);

                    // set TimeOut value of the socket to 5msec.
                    // tempo para o socket esperar receber um pacote. Caso o
                    // pacote não chegue nesse tempo um aviso é levantado,
                    // mas a execução continua, quando o pacote for recebido.
                    // RTPsocket.setSoTimeout(500);

                } catch (final SocketException se) {
                    System.out.println("Socket exception: " + se);
                    System.exit(0);
                }

                // init RTSP sequence number
                RTSPSeqNb = 1;

                // Send SETUP message to the server
                sendRtspRequest("SETUP " + Client.VideoFileName + " RTSP/1.0");

                // Wait for the response
                if (parseServerResponse() != 200) {
                    System.out.println("Resposta inválida do servidor");
                } else {
                    // change RTSP state and print new state
                    Client.state = RTSPState.READY;
                    System.out.println("Novo estado do RTSP: READY");
                }
            }// else if state != INIT then do nothing
        }
    }

    // Tratamento para o botão do Parar
    // --------------------------------
    private class stopButtonListener implements ActionListener {
        @Override
        public void actionPerformed(final ActionEvent e) {

            // TODO implementar o botão parar
            System.out.println("- PARAR -");

        }
    }

    // Handler for Teardown button
    // -----------------------
    private class tearButtonListener implements ActionListener {
        @Override
        public void actionPerformed(final ActionEvent e) {

            System.out.println("- FINALIZAR -");

            // increase RTSP sequence number
            RTSPSeqNb++;

            // Send TEARDOWN message to the server
            sendRtspRequest("TEARDOWN");

            // Wait for the response
            if (parseServerResponse() != 200) {
                System.out.println("Resposta inválida do servidor");
            } else {
                // change RTSP state and print out new state
                Client.state = RTSPState.INIT;
                System.out.println("Novo estado do RTSP: INIT");

                // stop the timer
                timer.stop();

                // exit
                System.exit(0);
            }
        }
    }

    private class timerListener implements ActionListener {
        @Override
        public void actionPerformed(final ActionEvent e) {

            // Construct a DatagramPacket to receive data from the UDP socket
            rcvdp = new DatagramPacket(buf, buf.length);

            try {
                // receive the UDP from the socket:
                RTPsocket.receive(rcvdp);

                // Os dados estão criptografados. A primeira coisa é
                // decriptografá-los.
                // Não estou interessado nos 15000 bytes. Só naquilo que
                // efetivamente foi preenchido.
                final byte[] encryptedBytes = Arrays.copyOfRange(rcvdp.getData(), 0, rcvdp.getLength());
            
                // Decriptografa
                byte[] decryptedBytes = null;
                switch (Client.algorithm) {
                    case AES:
                        decryptedBytes = decriptaAES(encryptedBytes);
                        break;
                    case BlowFish:
                        decryptedBytes = decriptaBlowfish(encryptedBytes);
                        break;
                    case DES:
                        decryptedBytes = decriptaDES(encryptedBytes);
                        break;
                    case DESede:
                        decryptedBytes = decriptaDES3(encryptedBytes);
                        break;
                    case XOR:
                        decryptedBytes = criptaDecripta(encryptedBytes);
                        break;
                    default:
                        throw new IllegalArgumentException("Algoritmo não previsto");
                }
                
                // Constroi o pacote com os dados decriptografados.
                final RTPPacket rtpPacket = new RTPPacket(decryptedBytes, decryptedBytes.length);
                // RTPPacket rtpPacket = new RTPPacket(rcvdp.getData(),
                // rcvdp.getLength());

                // print important header fields of the RTP packet received:
                System.out.println("Pegou pacote RTP com SeqNum # " + rtpPacket.getSequencenumber() + " TimeStamp " + rtpPacket.getTimestamp() + " ms, do tipo "
                        + rtpPacket.getPayloadType());

                // print header bitstream:
                rtpPacket.printHeader();

                // get the payload bitstream from the RTPpacket object
                final int payload_length = rtpPacket.getPayloadLength();
                final byte[] payload = new byte[payload_length];
                rtpPacket.getPayload(payload);

                // get an Image object from the payload bitstream
                final Toolkit toolkit = Toolkit.getDefaultToolkit();
                final Image image = toolkit.createImage(payload, 0, payload_length);

                // display the image as an ImageIcon object
                icon = new ImageIcon(image);
                iconLabel.setIcon(icon);
            } catch (final Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    // GUI
    // ----
    private final JFrame f = new JFrame("Client");
    private final JButton setupButton = new JButton("Conectar");
    private final JButton playButton = new JButton("Play");
    private final JButton pauseButton = new JButton("Pause");
    private final JButton stopButton = new JButton("Stop");
    private final JButton tearButton = new JButton("Finalizar");
    private final JPanel mainPanel = new JPanel();
    private final JPanel buttonPanel = new JPanel();
    private final JLabel iconLabel = new JLabel();

    private ImageIcon icon;
    // RTP variables:
    private DatagramPacket rcvdp; // UDP packet received from the server
    private DatagramSocket RTPsocket; // socket to be used to send and receive
    // UDP packets
    private static int RTP_RCV_PORT = 25000; // port where the client will
                                             // receive the RTP packets
    private final Timer timer; // timer used to receive data from the UDP socket
    private final byte[] buf; // buffer used to store data received from the

    // server
    // RTSP variables
    // rtsp states
    private static RTSPState state; // RTSP state == INIT or READY or PLAYING
    private Socket RTSPsocket; // socket used to send/receive RTSP messages
    // input and output stream filters
    private static BufferedReader RTSPBufferedReader;
    private static BufferedWriter RTSPBufferedWriter;
    private static String VideoFileName; // video file to request to the server
    private int RTSPSeqNb = 0; // Sequence number of RTSP messages within the
    // session
    private int RTSPid = 0; // ID of the RTSP session (given by the RTSP Server)

    private static Algorithm algorithm = null;

    public static void main(final String argv[]) throws Exception {

        if (argv.length != 4) {
            throw new IllegalArgumentException("Uso: host porta nome_do_arquivo algoritmo");
        }

        Client.algorithm = Algorithm.valueOf(argv[3]);

        // Create a Client object
        final Client theClient = new Client();

        // get server RTSP port and IP address from the command line
        final int rtspServerPort = Integer.parseInt(argv[1]);
        final String ServerHost = argv[0];
        final InetAddress ServerIPAddr = InetAddress.getByName(ServerHost);

        // get video filename to request:
        Client.VideoFileName = argv[2];

        // Establish a TCP connection with the server to exchange RTSP messages
        theClient.RTSPsocket = new Socket(ServerIPAddr, rtspServerPort);

        // Set input and output stream filters:
        Client.RTSPBufferedReader = new BufferedReader(new InputStreamReader(theClient.RTSPsocket.getInputStream()));
        Client.RTSPBufferedWriter = new BufferedWriter(new OutputStreamWriter(theClient.RTSPsocket.getOutputStream()));

        // init RTSP state:
        Client.state = RTSPState.INIT;
    }

    // ------------------------------------
    // Send RTSP Request
    // ------------------------------------

    // --------------------------
    // Constructor
    // --------------------------
    public Client() {

        // build GUI
        // --------------------------

        // Frame
        f.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent e) {
                System.exit(0);
            }
        });

        // Buttons
        buttonPanel.setLayout(new GridLayout(1, 0));
        buttonPanel.add(setupButton);
        buttonPanel.add(playButton);
        buttonPanel.add(pauseButton);
        buttonPanel.add(stopButton);
        buttonPanel.add(tearButton);
        setupButton.addActionListener(new setupButtonListener());
        playButton.addActionListener(new playButtonListener());
        pauseButton.addActionListener(new pauseButtonListener());
        stopButton.addActionListener(new stopButtonListener());
        tearButton.addActionListener(new tearButtonListener());

        // Image display label
        iconLabel.setIcon(null);

        // frame layout
        mainPanel.setLayout(null);
        mainPanel.add(iconLabel);
        mainPanel.add(buttonPanel);
        iconLabel.setBounds(0, 0, 380, 280);
        buttonPanel.setBounds(0, 280, 380, 50);

        f.getContentPane().add(mainPanel, BorderLayout.CENTER);
        f.setSize(new Dimension(390, 370));
        f.setVisible(true);

        // init timer
        // --------------------------
        timer = new Timer(20, new timerListener());
        timer.setInitialDelay(0);
        timer.setCoalesce(true);

        // allocate enough memory for the buffer used to receive data from the
        // server
        buf = new byte[15000];
    }

    private byte[] criptaDecripta(final byte[] texto) {
        return Utilities.xor(texto);
    }

    private byte[] decriptaAES(final byte[] bytesToEncrypt) throws Exception {
        return AESUtilities.decripta(bytesToEncrypt);
    }

    private byte[] decriptaBlowfish(final byte[] encryptedBytes) {
        return BlowFishUtilities.decripta(encryptedBytes);
    }

    private byte[] decriptaDES(final byte[] texto) {
        return DESUtilities.decripta(texto);
    }

    private byte[] decriptaDES3(final byte[] texto) throws Exception {
        return DESedeUtilities.decripta(texto);
    }

    // ------------------------------------
    // Parse Server Response
    // ------------------------------------
    private int parseServerResponse() {
        int reply_code = 0;

        try {
            // parse status line and extract the reply_code:
            final String StatusLine = Client.RTSPBufferedReader.readLine();
            System.out.println("RTSP Cliente - Recebido do servidor:");
            System.out.println(StatusLine);

            StringTokenizer tokens = new StringTokenizer(StatusLine);
            tokens.nextToken(); // skip over the RTSP version
            reply_code = Integer.parseInt(tokens.nextToken());

            // if reply code is OK get and print the 2 other lines
            if (reply_code == 200) {
                final String SeqNumLine = Client.RTSPBufferedReader.readLine();
                System.out.println(SeqNumLine);

                final String SessionLine = Client.RTSPBufferedReader.readLine();
                System.out.println(SessionLine);

                // if state == INIT gets the Session Id from the SessionLine
                tokens = new StringTokenizer(SessionLine);
                tokens.nextToken(); // skip over the Session:
                RTSPid = Integer.parseInt(tokens.nextToken());
            }
        } catch (final Exception ex) {
            System.out.println("Exception caught: " + ex);
            System.exit(0);
        }

        return reply_code;
    }

    private void sendRtspRequest(final String request_type) {
        try {
            // Use the RTSPBufferedWriter to write to the RTSP socket

            // write the request line:
            Client.RTSPBufferedWriter.write(request_type + "\n");

            // write the CSeq line:
            Client.RTSPBufferedWriter.write("CSeq: " + RTSPSeqNb + "\n");

            // check if request_type is equal to "SETUP" and in this case write
            // the Transport: line advertising to the server the port used to
            // receive the RTP packets RTP_RCV_PORT
            if (request_type.startsWith("SETUP")) {
                Client.RTSPBufferedWriter.write("Transport: RTP/UDP; client_port= " + Client.RTP_RCV_PORT + "\n");
            }
            // otherwise, write the Session line from the RTSPid field
            else {
                Client.RTSPBufferedWriter.write("Session: " + RTSPid + "\n");
            }

            Client.RTSPBufferedWriter.flush();
        } catch (final Exception ex) {
            System.out.println("Exception caught: " + ex);
            System.exit(0);
        }
    }

}// end of Class Client

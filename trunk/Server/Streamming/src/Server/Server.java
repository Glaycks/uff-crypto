package Server;

/* ------------------
 Server
 usage: java Server [RTSP listening port] [algoritmo criptografia]
 version 2.0
 Author: Eduardo Henriques
 ---------------------- */

import java.awt.BorderLayout;
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
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.StringTokenizer;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import monografia.commons.AESUtilities;
import monografia.commons.Algorithm;
import monografia.commons.BlowFishUtilities;
import monografia.commons.DESUtilities;
import monografia.commons.DESedeUtilities;
import monografia.commons.Utilities;
import monografia.desempenho.Analise;

public class Server extends JFrame implements ActionListener {

    // Apenas para prevenir warning de classe serializável sem serialVersionUID
    private static final long serialVersionUID = 1L;

    // RTP variables:
    private DatagramSocket rtpSocket; // socket to be used to send and receive
                                      // UDP
    // packets
    private DatagramPacket senddp; // UDP packet containing the video frames

    private InetAddress clientIPAddr; // Client IP address
    private int rtpDestPort = 0; // destination port for RTP packets (given by
                                 // the RTSP Client)

    // GUI:
    private final JLabel label;

    // Video variables:
    private int imagenb = 0; // image nb of the image currently transmitted
    private VideoStream video; // VideoStream object used to access video frames
    private static int MJPEG_TYPE = 26; // RTP payload type for MJPEG video
    private static int FRAME_PERIOD = 100; // Frame period of the video to
                                           // stream, in ms
    private static int VIDEO_LENGTH = 500; // length of the video in frames
    private final Timer timer; // timer used to send the images at the video
                               // frame rate
    private final byte[] buf; // buffer used to store the images to send to the
                              // client
    private static RTSPState state; // RTSP Server state == INIT or READY or
                                    // PLAY
    private Socket rtspSocket; // socket used to send/receive RTSP messages
    // input and output stream filters
    private static BufferedReader rtspBufferedReader;
    private static BufferedWriter rtspBufferedWriter;
    private static String videoFileName; // video file requested from the client
    private static int RTSP_ID = 123456; // ID of the RTSP session
    private static int RTSPSeqNb = 0; // Sequence number of RTSP messages within
                                      // the session
    private static final String CRLF = "\r\n";
    private static Algorithm algorithm = null;
    
    //aki
    Date data;

    public static void main(final String argv[]) throws Exception {
        if (argv.length != 2) {
            throw new IllegalArgumentException("Uso: porta algoritmo");
        }

        Server.algorithm = Algorithm.valueOf(argv[1]);

        // create a Server object
        final Server theServer = new Server();

        // show GUI:
        theServer.pack();
        theServer.setVisible(true);

        // get RTSP socket port from the command line
        final int rtspPort = Integer.parseInt(argv[0]);

        // Initiate TCP connection with the client for the RTSP session
        final ServerSocket listenSocket = new ServerSocket(rtspPort);
        theServer.rtspSocket = listenSocket.accept();
        listenSocket.close();

        // Get Client IP address
        theServer.clientIPAddr = theServer.rtspSocket.getInetAddress();

        // Initiate RTSPstate
        Server.state = RTSPState.INIT;

        // Set input and output stream filters:
        Server.rtspBufferedReader = new BufferedReader(new InputStreamReader(theServer.rtspSocket.getInputStream()));
        Server.rtspBufferedWriter = new BufferedWriter(new OutputStreamWriter(theServer.rtspSocket.getOutputStream()));

        // Wait for the SETUP message from the client
        RTSPMessageType requestType;
        boolean done = false;
        while (!done) {

            requestType = theServer.parseRtspRequest(); // blocking

            if (requestType == RTSPMessageType.SETUP) {
                done = true;

                // update RTSP state
                Server.state = RTSPState.READY;
                System.out.println("Novo estado do RSTP: READY");

                // Send response
                theServer.sendRtspResponse();

                // init the VideoStream object:
                theServer.video = new VideoStream(Server.videoFileName);

                // init RTP socket
                theServer.rtpSocket = new DatagramSocket();
            }
        }

        // loop to handle RTSP requests
        while (true) {
            // parse the request
            requestType = theServer.parseRtspRequest(); // blocking

            if (requestType == RTSPMessageType.PLAY && Server.state == RTSPState.READY) {
                // send back response
                theServer.sendRtspResponse();
                // start timer
                theServer.timer.start();
                // update state
                Server.state = RTSPState.PLAYING;
                System.out.println("Novo estado do RSTP: PLAYING");
            } else if (requestType == RTSPMessageType.PAUSE && Server.state == RTSPState.PLAYING) {
                // send back response
                theServer.sendRtspResponse();
                // stop timer
                theServer.timer.stop();
                // update state
                Server.state = RTSPState.READY;
                System.out.println("Novo estado do RSTP: READY");
            } else if (requestType == RTSPMessageType.TEARDOWN) {
                // send back response
                theServer.sendRtspResponse();
                // stop timer
                theServer.timer.stop();
                // close sockets
                theServer.rtspSocket.close();
                theServer.rtpSocket.close();

                System.exit(0);
            }
        }
    }

    // --------------------------------
    // Constructor
    // --------------------------------
    public Server() {
        // init Frame
        super("Server");

        // init Timer
        timer = new Timer(Server.FRAME_PERIOD, this);
        timer.setInitialDelay(0);
        timer.setCoalesce(true);
        
    	//aki
    	//Long inicio = data.getTime();
    	//System.out.println("inicio = " + inicio);

        // allocate memory for the sending buffer
        buf = new byte[15000];

        // Handler to close the main window
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent e) {
                // stop the timer and exit
                timer.stop();
                System.exit(0);
            }
        });

        // GUI:
        label = new JLabel("Quadro enviado #        ", SwingConstants.CENTER);
        getContentPane().add(label, BorderLayout.CENTER);
    }

    // ------------------------
    // Handler for timer
    // ------------------------
    @Override
    public void actionPerformed(final ActionEvent e) {
    	
        // if the current image nb is less than the length of the video
        if (imagenb < Server.VIDEO_LENGTH) {
            // update current imagenb
            imagenb++;

            try {
                // get next frame to send from the video, as well as its size
                final int imageLength = video.getNextFrame(buf);

                // Builds an RTPpacket object containing the frame
                final RTPPacket rtpPacket = new RTPPacket(Server.MJPEG_TYPE, imagenb, imagenb * Server.FRAME_PERIOD, buf, imageLength);

                // get to total length of the full rtp packet to send
                int packetLength = rtpPacket.getLength();

                // retrieve the packet bitstream and store it in an array of
                // bytes
                final byte[] packetBits = new byte[packetLength];
                rtpPacket.getPacket(packetBits);

                //aki
                int tamanhoTextoClaro = packetLength;
                System.out.println("tamanho do texto claro: " + tamanhoTextoClaro);

                // Criptografa
                byte[] encryptedBits = null;

                switch (Server.algorithm) {
                    case AES:
                        encryptedBits = encriptaAES(packetBits);
                        break;
                    case BlowFish:
                        encryptedBits = encriptaBlowfish(packetBits);
                        break;
                    case DES:
                        encryptedBits = encriptaDES(packetBits);
                        break;
                    case DESede:
                        encryptedBits = encriptaDES3(packetBits);
                        break;
                    case XOR:
                        encryptedBits = criptaDecripta(packetBits);
                        break;
                    default:
                        throw new IllegalArgumentException("Algoritmo não previsto");
                }

                packetLength = encryptedBits.length;

                //aki
                System.out.println("tamanho do texto cifrado: " + packetLength);
                Analise.acumulaValor(packetLength - tamanhoTextoClaro);

                // send the packet as a DatagramPacket over the UDP socket
                senddp = new DatagramPacket(encryptedBits, packetLength, clientIPAddr, rtpDestPort);

                rtpSocket.send(senddp);

                System.out.println("Quadro enviado #" + imagenb);
                // print the header bitstream
                rtpPacket.printHeader();

                // update GUI
                label.setText("Quadro enviado #" + imagenb);
            } catch (final Exception ex) {
                System.out.println("Exception caught: " + ex);
                System.exit(0);
            }
        } else {
            // if we have reached the end of the video file, stop the timer
            timer.stop();

        }
    }

    private byte[] criptaDecripta(final byte[] texto) {
        return Utilities.xor(texto);
    }

    private byte[] encriptaAES(final byte[] texto) throws Exception {
        return AESUtilities.encripta(texto);
    }

    private byte[] encriptaBlowfish(final byte[] texto) {
        return BlowFishUtilities.encripta(texto);
    }

    private byte[] encriptaDES(final byte[] texto) {
        return DESUtilities.encripta(texto);
    }

    private byte[] encriptaDES3(final byte[] texto) throws Exception {
        return DESedeUtilities.encripta(texto);
    }

    // ------------------------------------
    // Parse RTSP Request
    // ------------------------------------
    private RTSPMessageType parseRtspRequest() {
        RTSPMessageType requestType = null;
        try {
            // parse request line and extract the request_type:
            final String requestLine = Server.rtspBufferedReader.readLine();
            System.out.println("RTSP Servidor - Recebido do cliente:");
            System.out.println(requestLine);

            StringTokenizer tokens = new StringTokenizer(requestLine);
            final String requestTypeString = tokens.nextToken();

            // convert to request_type structure:
            if (new String(requestTypeString).compareTo("SETUP") == 0) {
                requestType = RTSPMessageType.SETUP;
            } else if (new String(requestTypeString).compareTo("PLAY") == 0) {
                requestType = RTSPMessageType.PLAY;
            } else if (new String(requestTypeString).compareTo("PAUSE") == 0) {
                requestType = RTSPMessageType.PAUSE;
            } else if (new String(requestTypeString).compareTo("TEARDOWN") == 0) {
                requestType = RTSPMessageType.TEARDOWN;
            }

            if (requestType == RTSPMessageType.SETUP) {
                // extract VideoFileName from RequestLine
                Server.videoFileName = tokens.nextToken();
            }

            // parse the SeqNumLine and extract CSeq field
            final String SeqNumLine = Server.rtspBufferedReader.readLine();
            System.out.println(SeqNumLine);
            tokens = new StringTokenizer(SeqNumLine);
            tokens.nextToken();
            Server.RTSPSeqNb = Integer.parseInt(tokens.nextToken());

            // get LastLine
            final String LastLine = Server.rtspBufferedReader.readLine();
            System.out.println(LastLine);

            if (requestType == RTSPMessageType.SETUP) {
                // extract RTP_dest_port from LastLine
                tokens = new StringTokenizer(LastLine);
                for (int i = 0; i < 3; i++) {
                    tokens.nextToken(); // skip unused stuff
                }
                rtpDestPort = Integer.parseInt(tokens.nextToken());
            }
            // else LastLine will be the SessionId line ... do not check for
            // now.
        } catch (final Exception ex) {
            System.out.println("Exception caught: " + ex);
            ex.printStackTrace();
            System.exit(0);
        }
        return requestType;
    }

    // ------------------------------------
    // Send RTSP Response
    // ------------------------------------
    private void sendRtspResponse() {
        try {
            Server.rtspBufferedWriter.write("RTSP/1.0 200 OK" + Server.CRLF);
            Server.rtspBufferedWriter.write("CSeq: " + Server.RTSPSeqNb + Server.CRLF);
            Server.rtspBufferedWriter.write("Session: " + Server.RTSP_ID + Server.CRLF);
            Server.rtspBufferedWriter.flush();
            System.out.println("RTSP Server - Sent response to Client.");
        } catch (final Exception ex) {
            System.out.println("Exception caught: " + ex);
            System.exit(0);
        }
    }
}

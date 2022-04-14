package protocol;

import client.Client;
import client.Message;
import client.MessageType;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This is just some example code to show you how to interact with the server using the provided
 * 'Client' class and two queues. Feel free to modify this code in any way you like!
 */

public class MyProtocol {

    // The host to connect to. Set this to localhost when using the audio interface tool.
    private static String SERVER_IP = "netsys.ewi.utwente.nl"; //"127.0.0.1";
    // The port to connect to. 8954 for the simulation server.
    private static int SERVER_PORT = 8954;
    // The frequency to use.
    private static int frequency = 10500;

    private BlockingQueue<Message> receivedQueue;
    private BlockingQueue<Message> sendingQueue;

    public MyProtocol(String server_ip, int server_port, int frequency) {
        receivedQueue = new LinkedBlockingQueue<Message>();
        sendingQueue = new LinkedBlockingQueue<Message>();

        // Give the client the Queues to use
        new Client(SERVER_IP, SERVER_PORT, frequency, receivedQueue, sendingQueue);

        // Start thread to handle received messages!
        new receiveThread(receivedQueue).start();

        // handle sending from stdin from this thread.
        try {
            Scanner console = new Scanner(System.in);
            String input = "";
            boolean quit = false;
            while (!quit) {
                input = console.nextLine(); // read input
                quit = inputParser(input);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean inputParser(String input) {
        String[] parsedInput = input.split(" ");
        switch (parsedInput[0].toLowerCase()) {
            case "quit":
                printMsg("Quiting!");
                return true;
            case "chat":
                if (parsedInput.length == 1) {
                    printErr("No message to send");
                    break;
                }

                // Reassemble message
                StringBuilder chat = new StringBuilder();
                for (int i = 1; i < parsedInput.length; i++) {
                    chat.append(parsedInput[i]).append(" ");
                }

                // Send message
                byte[] inputBytes = chat.toString().getBytes(); // get bytes from input
                // make a new byte buffer with the length of the
                ByteBuffer toSend = ByteBuffer.allocate(inputBytes.length);
                // input string
                // copy the input string into the byte buffer.
                toSend.put(inputBytes, 0, inputBytes.length);

                Message msg;
                if ((input.length()) > 2) {
                    msg = new Message(MessageType.DATA, toSend);
                } else {
                    msg = new Message(MessageType.DATA_SHORT, toSend);
                }
                try {
                    sendingQueue.put(msg);
                } catch (InterruptedException e) {
                    System.exit(2);
                }
                break;
            case "list":
                // TODO call protocol.Forwarding
                break;
            case "help":
                printMsg("Commands:" +
                         "\n\tchat - Send messages to others" +
                         "\n\tlist - Show participants in the network" +
                         "\n\thelp - Show this help message");
                break;
            default:
                printErr("Incorrect commands, write 'help' for a list of commands");
                break;
        }
        return false;
    }

    private void printErr(String err) {
        System.out.println("[ERR] " + err);
    }

    private void printMsg(String msg) {
        System.out.println(msg);
    }

    public static void main(String args[]) {
        if (args.length > 0) {
            frequency = Integer.parseInt(args[0]);
        }
        new MyProtocol(SERVER_IP, SERVER_PORT, frequency);
    }

    private class receiveThread extends Thread {
        private BlockingQueue<Message> receivedQueue;

        // Outer integer contains the source address of the packets
        // Inner integer contains the sequence number of the packets
        private HashMap<Integer, HashMap<Integer, Packet>> receivedPackets;

        public receiveThread(BlockingQueue<Message> receivedQueue) {
            super();
            this.receivedQueue = receivedQueue;
            this.receivedPackets = new HashMap<>();
        }

        public void printByteBuffer(ByteBuffer bytes, int bytesLength) {
            for (int i = 0; i < bytesLength; i++) {
                System.out.print(Byte.toString(bytes.get(i)) + " ");
            }
            System.out.println();
        }

        // Handle messages from the server / audio framework
        @Override
        public void run() {
            while (receivedQueue != null) {
                try {
                    Message m = receivedQueue.take();
                    messageTypeParser(m);
                } catch (InterruptedException e) {
                    System.err.println("Failed to take from queue: " + e);
                }
            }
        }

        private void messageTypeParser(Message received) {
            switch (received.getType()) {
                case BUSY:
                    // The channel is busy (A node is sending within our detection range)
                    System.out.println("BUSY");
                    break;
                case FREE:
                    // The channel is no longer busy (no nodes are sending within our
                    // detection range)
                    System.out.println("FREE");
                    break;
                case DATA:
                    // We received a data frame!
                    System.out.println("[RECEIVED] DATA");
                    Packet pck = new Packet();
                    pck.decode(received.getData().array(), MessageType.DATA);
                    packetParser(pck, MessageType.DATA);
                    break;
                case DATA_SHORT:
                    // We received a short data frame!
                    System.out.println("[RECEIVED] DATA_SHORT");
                    Packet pckShort = new Packet();
                    pckShort.decode(received.getData().array(), MessageType.DATA_SHORT);
                    packetParser(pckShort, MessageType.DATA_SHORT);
                    break;
                case DONE_SENDING:
                    // This node is done sending
                    System.out.println("DONE_SENDING");
                    break;
                case HELLO:
                    // Server / audio framework hello message.
                    // You don't have to handle this
                    System.out.println("HELLO");
                    break;
                case SENDING: // This node is sending
                    System.out.println("SENDING");
                    break;
                case END:
                    // Server / audio framework disconnect message.
                    // You don't have to handle this
                    System.out.println("END");
                    System.exit(0);
                    break;
                default:
                    break;
            }
        }

        private void packetParser(Packet pck, MessageType msgType) {
            if (msgType == MessageType.DATA_SHORT) {
                // TODO parse data short packets
                return;
            }

            if (pck.getPacketType() == 0) {
                addPckToHash(pck);
            } else if (pck.getPacketType() == 1) {
                // TODO Forwarding
            } else if (pck.getPacketType() == 2) {
                addPckToHash(pck);
                String reconstructedMessage = "";
                ArrayList<ArrayList<Byte>> msgs = new ArrayList<>();
                for (Packet tmp : receivedPackets.get(pck.getSource()).values()) {
                    ArrayList<Byte> tmpArr = new ArrayList<>();

                    for (byte b: tmp.getData()) {
                        tmpArr.add(b);
                    }
                    msgs.add(tmpArr);
                }
                TextSplit ts = new TextSplit();
                reconstructedMessage = ts.arrayOfArrayBackToText(msgs);
                System.out.println(reconstructedMessage);

                receivedPackets.put(pck.getSource(), new HashMap<>());
            }

            String message = new String(pck.getData(), StandardCharsets.UTF_8);
            System.out.println(message);
        }

        private void addPckToHash(Packet pck) {
            if (receivedPackets.containsKey(pck.getSource())) {
                receivedPackets.get(pck.getSource()).put(pck.getSeqNr(), pck);
            } else {
                HashMap<Integer, Packet> tmpSeqPck = new HashMap<>();
                tmpSeqPck.put(pck.getSeqNr(), pck);
                receivedPackets.put(pck.getSource(), tmpSeqPck);
            }
        }
    }
}


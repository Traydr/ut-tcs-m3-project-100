package protocol;

import client.Client;
import client.Message;
import client.MessageType;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MyProtocol {
    // FRAMEWORK START
    // The host to connect to. Set this to localhost when using the audio interface tool.
    private static final String SERVER_IP = "netsys.ewi.utwente.nl"; //"127.0.0.1";
    // The port to connect to. 8954 for the simulation server.
    private static final int SERVER_PORT = 8954;
    // The frequency to use.
    private static int frequency = 10500;
    // FRAMEWORK END
    // CONSTANTS START
    private static final int DATA_PACKET_LENGTH = 32;
    private static final int DATA_SHORT_PACKET_LENGTH = 2;
    private static final int DATA_DATA_LENGTH = 29;
    private static final int PACKET_TYPE_SENDING = 0;
    private static final int PACKET_TYPE_FORWARDING = 1;
    private static final int PACKET_TYPE_DONE_SENDING = 2;
    // CONSTANTS END
    // GLOBAL VARIABLES START
    private static int myAddress;
    private static int step;
    private Forwarding forwarding;
    private BlockingQueue<Message> receivedQueue;
    private BlockingQueue<Message> sendingQueue;
    private BlockingQueue<byte[]> bufferQueue;
    private MediumAccessControl mac;
    private ArrayList<Integer> connectedClients;
    // GLOBAL VARIABLES END

    public MyProtocol(String server_ip, int server_port, int frequency) {
        receivedQueue = new LinkedBlockingQueue<>();
        sendingQueue = new LinkedBlockingQueue<>();
        bufferQueue = new LinkedBlockingQueue<>();
        mac = new MediumAccessControl();

        myAddress = new Random().nextInt(14) + 1;
        forwarding = new Forwarding(myAddress);
        step = 0;
        connectedClients = new ArrayList<>();


        // Give the client the Queues to use
        new Client(server_ip, server_port, frequency, receivedQueue, sendingQueue);

        // Start thread to handle received messages!
        new receiveThread(receivedQueue).start();

        // Read input from user
        try {
            Scanner scanner = new Scanner(System.in);
            String input = "";
            boolean quit = false;
            while (!quit) {
                input = scanner.nextLine();
                quit = inputParser(input);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.exit(1);
    }

    /**
     * Parser through the input we get from the user
     *
     * @param input Input from the user
     * @return If quit then true, otherwise false
     */
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
                sendNetwork(TextSplit.textToBytes(chat.toString()));
                break;
            case "list":
                StringBuilder connections = new StringBuilder();
                connectedClients.forEach((n) -> connections.append("\n\t").append((n)));
                printMsg("Connected Clients:" + connections);
                break;
            case "help":
                printMsg("Commands:" +
                        "\n\tchat - Send messages to others" +
                        "\n\tlist - Show participants in the network" +
                        "\n\thelp - Show this help message" +
                        "\n\tquit - quit client");
                break;
            default:
                printErr("Incorrect commands, write 'help' for a list of commands");
                break;
        }
        return false;
    }

    private void sendNetwork(byte[] data) {
        if (data.length > DATA_DATA_LENGTH) {
            int i = 0;
            ArrayList<ArrayList<Byte>> splitBytes = TextSplit.splitTextBytes(data, DATA_DATA_LENGTH);
            for (ArrayList<Byte> pktArrayList : splitBytes) {
                byte[] tmpPkt = new byte[pktArrayList.size()];
                int k = 0;
                for (byte b : pktArrayList) {
                    tmpPkt[k] = b;
                    k++;
                }
                int destination = 1; // TODO Change later into dynamic!!!
                try {
                    if (pktArrayList == splitBytes.get(splitBytes.size() - 1)) {
                        bufferQueue.put(createDataPkt(myAddress, destination, PACKET_TYPE_DONE_SENDING, tmpPkt.length, i, tmpPkt));
                    } else {
                        bufferQueue.put(createDataPkt(myAddress, destination, PACKET_TYPE_SENDING, DATA_DATA_LENGTH, i, tmpPkt));
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                i++;
            }
        } else {
            int destination = 0; // TODO CHANGE THIS
            try {
                bufferQueue.put(createDataPkt(myAddress, destination, PACKET_TYPE_DONE_SENDING, data.length, 0, data));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (mac.canWeSend(receivedQueue, bufferQueue)) {
            sendRts(myAddress, 0, 0);
            mac.haveSentPacket();

            while (bufferQueue.size() > 0) {
                sendPacket(bufferQueue.remove());
            }
        } else {
            printErr("there has a collision occurred");
        }
    }

    /**
     * Send a packet to the network
     * @param pck byte[] of a fully formed packet
     */
    private void sendPacket(byte[] pck) {
        ByteBuffer sending = ByteBuffer.allocate(DATA_PACKET_LENGTH);
        sending.put(pck);
        try {
            sendingQueue.put(new Message(MessageType.DATA, sending));
        } catch (InterruptedException e) {
            System.exit(2);
        }
    }

    /**
     * Sends an RTS packet to the network
     *
     * @param src   Source address
     * @param dst   Destination address
     * @param ackNr Acknowledgement number
     */
    private void sendRts(int src, int dst, int ackNr) {
        ByteBuffer sending = ByteBuffer.allocate(DATA_SHORT_PACKET_LENGTH);
        sending.put(createDataShortPkt(src, dst, ackNr));
        try {
            sendingQueue.put(new Message(MessageType.DATA_SHORT, sending));
        } catch (InterruptedException e) {
            System.exit(2);
        }
    }

    /**
     * Creates a data packet based on input
     *
     * @param src     Source address
     * @param dst     Destination address (0 - broadcast all)
     * @param pktType Packet type (0 - data, 1 - forwarding, 2 - data finished)
     * @param dataLen Data length
     * @param seqNr   Sequence number
     * @param data    Data
     * @return A byte array of exactly 32 bytes to specification
     */
    private byte[] createDataPkt(int src, int dst, int pktType, int dataLen, int seqNr, byte[] data) {
        Packet pck = new Packet();
        pck.setSource(src);
        pck.setDestination(dst);
        pck.setPacketType(pktType);
        pck.setDataLen(dataLen);
        pck.setSeqNr(seqNr);
        pck.setData(data);
        return pck.makePkt(MessageType.DATA);
    }

    /**
     * Creates a data short packet based on input
     *
     * @param src   Source address
     * @param dst   Destination address (0 - broadcast all)
     * @param ackNr Acknowledgement number
     * @return A byte array of exactly 2 bytes to specification
     */
    private byte[] createDataShortPkt(int src, int dst, int ackNr) {
        Packet pck = new Packet();
        pck.setSource(src);
        pck.setDestination(dst);
        pck.setAckNr(ackNr);
        return pck.makePkt(MessageType.DATA_SHORT);
    }

    /**
     * Prints a msg with a '[ERR]' prefix
     *
     * @param err Error
     */
    private void printErr(String err) {
        System.out.println("[ERR] " + err);
    }

    /**
     * Function to shorthand print
     *
     * @param msg Message
     */
    private void printMsg(String msg) {
        System.out.println(msg);
    }

    public static void main(String[] args) {
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

        /**
         * Parses received messages according to message type
         *
         * @param received Message object
         */
        private void messageTypeParser(Message received) {
            mac.setPreviousMediumState(received.getType());
            switch (received.getType()) {
                case BUSY:
                    // The channel is busy (A node is sending within our detection range)
                    System.out.println("BUSY");
                    break;
                case FREE:
                    // The channel is no longer busy (no nodes are sending within our
                    // detection range)
                    System.out.println("[FREE]");
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
                    System.out.println("[CONNECTED]");
                    break;
                case SENDING: // This node is sending
                    System.out.println("[SENDING]");
                    break;
                case END:
                    // Server / audio framework disconnect message.
                    // You don't have to handle this
                    System.out.println("[END]");
                    System.exit(0);
                    break;
                default:
                    break;
            }
        }

        /**
         * Parses the packets it gets
         *
         * @param pck     Packet object to parse
         * @param msgType Packet type, DATA or DATA_SHORT
         */
        private void packetParser(Packet pck, MessageType msgType) {
            // Checks if our address is already in use
            if (!connectedClients.contains(pck.getSource())) {
                connectedClients.add(pck.getSource());
            }

            if (pck.getSource() == myAddress && !mac.isSentPacket()) {
                while (connectedClients.contains(myAddress)) {
                    myAddress = new Random().nextInt(14) + 1;
                }
            } else if (pck.getSource() == myAddress) {
                return;
            }

            step++;
            if (msgType == MessageType.DATA_SHORT) {
                // TODO parse data short packets
                return;
            }

            if (pck.getPacketType() == PACKET_TYPE_SENDING) {
                addPckToHash(pck);
            } else if (pck.getPacketType() == PACKET_TYPE_FORWARDING) {
            } else if (pck.getPacketType() == PACKET_TYPE_DONE_SENDING) {
                addPckToHash(pck);
                String reconstructedMessage = "";
                ArrayList<ArrayList<Byte>> msgs = new ArrayList<>();
                for (Packet tmp : receivedPackets.get(pck.getSource()).values()) {
                    ArrayList<Byte> tmpArr = new ArrayList<>();

                    for (byte b : tmp.getData()) {
                        tmpArr.add(b);
                    }
                    msgs.add(tmpArr);
                }
                reconstructedMessage = TextSplit.arrayOfArrayBackToText(msgs, pck.getDataLen());
                System.out.println(reconstructedMessage);

                receivedPackets.put(pck.getSource(), new HashMap<>());
            }
        }

        /**
         * Adds an input packet to global hashmap. If it doesn't find an available inner hashmap then it creates a new one
         *
         * @param pck Packet object
         */
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

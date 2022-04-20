package protocol;

import client.Client;
import client.Message;
import client.MessageType;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
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
    private static int highestSEQ = 0;
    private static int resentValue = 0;
    private static int resentSEQ = 0;
    private static int resentSRC = 0;
    // CONSTANTS END
    // GLOBAL VARIABLES START
    private static Node myAddress;
    private int[][] routingTable;
    private static int step;
    private Forwarding forwarding;
    private BlockingQueue<Message> receivedQueue;
    private BlockingQueue<Message> sendingQueue;
    private BlockingQueue<byte[]> bufferQueue;
    private MediumAccessControl mac;
    private ReliableTransfer reliableTransfer;
    private TimeOut timeOut;
    // List of connected client source addresses
    private ArrayList<Node> connectedClients;
    // Outer integer is source address, inner is sequence number, contains a list of packets that have not been ACK'd
    private HashMap<Integer, HashMap<Integer, byte[]>> unconfirmedPackets;
    // GLOBAL VARIABLES END
    private ArrayList<Packet> resentPacketList = new ArrayList<>();

    public MyProtocol(String server_ip, int server_port, int frequency) {
        receivedQueue = new LinkedBlockingQueue<>();
        sendingQueue = new LinkedBlockingQueue<>();
        bufferQueue = new LinkedBlockingQueue<>();
        mac = new MediumAccessControl();
        reliableTransfer = new ReliableTransfer();
        timeOut = new TimeOut(5, 10);

        myAddress = new Node(new Random().nextInt(14) + 1);
        forwarding = new Forwarding(myAddress);
        routingTable = new int[forwarding.NODE_COUNT][forwarding.NODE_COUNT];
        step = 0;
        connectedClients = new ArrayList<>();
        unconfirmedPackets = new HashMap<>();


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
                connectedClients.forEach((n) -> connections.append("\n\t").append((n).getAddress()));
                printMsg("Connected Clients:" + connections);
                break;
            case "send":
                sendBuffer();
                break;
            case "help":
                printMsg("Commands:" +
                        "\n\tchat - Send messages to others" +
                        "\n\tlist - Show participants in the network" +
                        "\n\thelp - Show this help message" +
                        "\n\tsend - Sends all packets currently in the buffer" +
                        "\n\tquit - quit client");
                break;
            default:
                printErr("Incorrect commands, write 'help' for a list of commands");
                break;
        }
        return false;
    }

    private void sendNetwork(byte[] data) {
        // Forwarding packets
//        byte[] forwardMatrix = forwarding.matrixToArray();
//        byte[] forwardPkt = createDataPkt(myAddress.getAddress(), 0, PACKET_TYPE_FORWARDING, forwardMatrix.length, 0, forwardMatrix);
//        try {
//            bufferQueue.put(forwardPkt);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }

        // Data packets
        if (data.length > DATA_DATA_LENGTH) {
            int i = 0;
            ArrayList<ArrayList<Byte>> splitBytes = TextSplit.splitTextBytes(data, DATA_DATA_LENGTH);
            for (ArrayList<Byte> pktArrayList : splitBytes) {
                // change Byte into byte
                byte[] tmpPkt = new byte[pktArrayList.size()];
                int k = 0;
                for (byte b : pktArrayList) {
                    tmpPkt[k] = b;
                    k++;
                }

                // Create the packets and add to buffer
                int destination = 0;
                byte[] pckBytes;
                if (pktArrayList == splitBytes.get(splitBytes.size() - 1)) {
                    pckBytes = createDataPkt(myAddress.getAddress(), destination, PACKET_TYPE_DONE_SENDING, tmpPkt.length, i, tmpPkt);
                } else {
                    pckBytes = createDataPkt(myAddress.getAddress(), destination, PACKET_TYPE_SENDING, DATA_DATA_LENGTH, i, tmpPkt);
                }
                putPckToUnconfirmed(pckBytes, i, destination);
                addPktToBuffer(pckBytes);

                if (unconfirmedPackets.containsKey(i)) {
                    return;
                } else {
                    i++;
                }
            }
        } else {
            int destination = 0;
            byte[] pckBytes = createDataPkt(myAddress.getAddress(), destination, PACKET_TYPE_DONE_SENDING, data.length, 0, data);
            putPckToUnconfirmed(pckBytes, 0, destination);
            addPktToBuffer(pckBytes);
        }
        sendBuffer();
    }

    /**
     * Send all the packets currently in the buffer
     */
    protected void sendBuffer() {
        int bufferTimeOffset = bufferQueue.size() * 2;
        if (mac.canWeSend(receivedQueue, bufferQueue)) {
            if (connectedClients.size() == 0) {
                connectedClients.add(myAddress);
            }

            sendRts(myAddress.getAddress(), 0, 0);
            mac.haveSentPacket();
            while (bufferQueue.size() > 0) {
                sendPacket(bufferQueue.remove());
            }
        } else if (bufferQueue.size() == 0) {
            printErr("Buffer is already empty");
        } else {
            printErr("Medium currently occupied, please [send] later");
        }
        new TimeOut(bufferTimeOffset, 2);
    }

    /**
     * Adds a packet of type to buffer queue
     *
     * @param pkt Packet
     */
    protected void addPktToBuffer(byte[] pkt) {
        try {
            bufferQueue.put(pkt);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Adds the packet to the unconfirmed hashmap
     *
     * @param pck         Packet
     * @param seqNr       Sequence number of packet
     * @param destination Destination of packet
     */
    private void putPckToUnconfirmed(byte[] pck, int seqNr, int destination) {
        // I know this function has duplicate code, but I don't know how to fix it without making it more complex
        if (destination == 0) {
            for (Node dest : connectedClients) {
                if (!unconfirmedPackets.containsKey(dest.getAddress())) {
                    HashMap<Integer, byte[]> unconfirmed = new HashMap<>();
                    unconfirmed.put(seqNr, pck);
                    unconfirmedPackets.put(dest.getAddress(), unconfirmed);
                } else {
                    unconfirmedPackets.get(dest.getAddress()).put(seqNr, pck);
                }
            }
        } else {
            if (!unconfirmedPackets.containsKey(destination)) {
                HashMap<Integer, byte[]> unconfirmed = new HashMap<>();
                unconfirmed.put(seqNr, pck);
                unconfirmedPackets.put(destination, unconfirmed);
            } else {
                unconfirmedPackets.get(destination).put(seqNr, pck);
            }
        }

    }

    /**
     * Send a packet to the network
     *
     * @param pck byte[] of a fully formed packet
     */
    protected void sendPacket(byte[] pck) {
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
            switch (received.getType()) {
                case BUSY:
                    mac.setPreviousMediumState(received.getType());
                    System.out.print("-> [BUSY]");
                    break;
                case FREE:
                    mac.setPreviousMediumState(received.getType());
                    System.out.print("-> [FREE]\n");
                    break;
                case DATA:
                    System.out.print("-> [RECEIVED_DATA]");
                    Packet pck = new Packet();
                    pck.decode(received.getData().array(), MessageType.DATA);
                    packetParser(pck, MessageType.DATA);
                    break;
                case DATA_SHORT:
                    System.out.print("-> [RECEIVED_DATA_SHORT]");
                    Packet pckShort = new Packet();
                    pckShort.decode(received.getData().array(), MessageType.DATA_SHORT);
                    packetParser(pckShort, MessageType.DATA_SHORT);
                    break;
                case DONE_SENDING:
                    System.out.print("-> [DONE_SENDING]");
                    break;
                case HELLO:
                    System.out.println("[CONNECTED]");
                    break;
                case SENDING:
                    System.out.print("-> [SENDING]");
                    break;
                case END:
                    System.out.println("[END]");
                    System.exit(0);
                    break;
                default:
                    printErr("Unrecognised MessageType");
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
            step++;
            // Checks if our address is already in use
            if (!checkIfAddressIsConnected(pck.getSource())) {
                connectedClients.add(new Node(pck.getSource()));
            }

            if (pck.getSource() == myAddress.getAddress() && !mac.isSentPacket()) {
                while (checkIfAddressIsConnected(myAddress.getAddress())) {
                    myAddress.setAddress(new Random().nextInt(14) + 1);
                }
            } else if (pck.getSource() == myAddress.getAddress()) {
                return;
            }

            // Parse DATA SHORT Packets
            if (msgType == MessageType.DATA_SHORT) {
                if (pck.getDestination() == myAddress.getAddress() && unconfirmedPackets.containsKey(pck.getSource())) {
                    if (unconfirmedPackets.get(pck.getSource()).containsKey(pck.getAckNr())) {
                        unconfirmedPackets.get(pck.getSource()).remove(pck.getAckNr());
                    }
                }
                return;
            }


            if (pck.getPacketType() == PACKET_TYPE_SENDING) {
                // If the packet isn't already in the hashmap, put it in
                if (!checkIfPckInHash(pck)) {
                    putPckToReceived(pck);
                }

                addPktToBuffer(createDataShortPkt(myAddress.getAddress(), pck.getSource(), pck.getSeqNr()));
            } else if (pck.getPacketType() == PACKET_TYPE_FORWARDING) {
                // Useless
                forwarding.init(findNodeByAddress(pck.getSource()), pck);
                forwarding.addStep(step);
                forwarding.pathFinding(routingTable);
            } else if (pck.getPacketType() == PACKET_TYPE_DONE_SENDING) {
                // Put packet to hashmap to be decoded
                putPckToReceived(pck);
                String reconstructedMessage = "";
                ArrayList<ArrayList<Byte>> msgs = new ArrayList<>();
                for (Packet tmp : receivedPackets.get(pck.getSource()).values()) {
                    // Adding packets for retransmission
                    addPktToBuffer(tmp.makePkt(MessageType.DATA));
                    // Reconstructing the message
                    ArrayList<Byte> tmpArr = new ArrayList<>();
                    for (byte b : tmp.getData()) {
                        tmpArr.add(b);
                    }
                    msgs.add(tmpArr);
                }
                reconstructedMessage = TextSplit.arrayOfArrayBackToText(msgs, pck.getDataLen());
                reconstructedMessage = "\n[FROM] " + pck.getSource() + ":\n\t" + reconstructedMessage;
                System.out.println(reconstructedMessage);

                // Retransmit packets
                new TimeOut(1, 5).run();
                sendBuffer();

                // Wipe the hashmap to prepare for the next message
                receivedPackets.put(pck.getSource(), new HashMap<>());
            }
        }

        /**
         * Puts a packet to the unconfirmedPackets Hashmap
         *
         * @param pck Packet obj
         */
        protected void putPckToReceived(Packet pck) {
            if (receivedPackets.containsKey(pck.getSource())) {
                receivedPackets.get(pck.getSource()).put(pck.getSeqNr(), pck);
            } else {
                HashMap<Integer, Packet> tmpSeqPck = new HashMap<>();
                tmpSeqPck.put(pck.getSeqNr(), pck);
                receivedPackets.put(pck.getSource(), tmpSeqPck);
            }
        }

        /**
         * Checks if the input packet is already in the hashmap
         *
         * @param pck Received packet obj
         * @return True if it is, False otherwise
         */
        private boolean checkIfPckInHash(Packet pck) {
            if (receivedPackets.containsKey(pck.getSource())) {
                return receivedPackets.get(pck.getSource()).containsKey(pck.getSeqNr());
            }
            return false;
        }

        /**
         * Sends an Acknowledgement
         *
         * @param pck        Packet
         * @param highestSEQ
         */
        public void sentAck(Packet pck, int highestSEQ) {
            if (reliableTransfer.hasReceived(receivedPackets)) {
                sendRts(myAddress.getAddress(), pck.getSource(), highestSEQ);
            }
        }

        /**
         * Checks if an address is already in the connected arraylist
         *
         * @param addr Address
         * @return true if connected, false otherwise
         */
        private boolean checkIfAddressIsConnected(int addr) {
            for (Node node : connectedClients) {
                if (node.getAddress() == addr) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Finds a node with the same address as input
         *
         * @param addr Address of client
         * @return Node object containing the address
         */
        private Node findNodeByAddress(int addr) {
            for (Node node : connectedClients) {
                if (node.getAddress() == addr) {
                    return node;
                }
            }
            return null;
        }
    }
}

package protocol;

import java.util.ArrayList;

public class ForwardingV2 {
    ArrayList<Node> contacts;
    ArrayList<Node> directNeighbours;
    ArrayList<ArrayList<Integer>> contactsNeighbours;
    Node addr;

    /**
     * Constructor
     *
     * @param addr Our address
     */
    public ForwardingV2(Node addr) {
        contacts = new ArrayList<>();
        directNeighbours = new ArrayList<>();
        contactsNeighbours = new ArrayList<>(3);
        this.addr = addr;

        // Add 3 ArrayLists to reserve indexes
        contactsNeighbours.add(new ArrayList<>());
        contactsNeighbours.add(new ArrayList<>());
        contactsNeighbours.add(new ArrayList<>());
    }

    /**
     * Decides whether a client should retransmit a packet
     *
     * @param src Source address of packet
     * @param dst Destination address of packet
     * @return True for re-transmission, false otherwise
     */
    public boolean shouldClientRetransmit(int src, int dst) {
        if (src == addr.getAddress() || dst == addr.getAddress()) {
            return false;
        }

        int srcNodeIndex = getIndexOfAddressInList(src, contacts);
        ArrayList<Integer> visited = tracePath(srcNodeIndex);

        for (Node node : directNeighbours) {
            if (!visited.contains(node.getAddress())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Traces all the clients that a packet has visited depending on from which address it was sent from
     *
     * @param srcIndex The source address index in contacts list
     * @return An Arraylist of visited addresses
     */
    private ArrayList<Integer> tracePath(int srcIndex) {
        ArrayList<Integer> clientsVisited = new ArrayList<>();
        clientsVisited.add(contacts.get(srcIndex).getAddress());

        for (int client : contactsNeighbours.get(srcIndex)) {
            if (!clientsVisited.contains(client)) {
                clientsVisited.add(client);
            }
        }

        int iterator = 0;
        while (!clientsVisited.contains(addr.getAddress()) && clientsVisited.size() < contacts.size() + 1 && iterator < 4) {
            for (int visitedClient : clientsVisited) {
                if (visitedClient == contacts.get(srcIndex).getAddress()) {
                    continue;
                }

                for (int client : contactsNeighbours.get(getIndexOfAddressInList(visitedClient, contacts))) {
                    if (!clientsVisited.contains(client)) {
                        clientsVisited.add(client);
                    }
                }
            }

            iterator++;
        }

        return clientsVisited;
    }

    /**
     * Adds a neighbour that is within sending distance
     *
     * @param neighbour Neighbour address
     */
    public void addDirectNeighbour(Node neighbour) {
        if (!isAddrInArrayList(neighbour.getAddress(), directNeighbours)) {
            directNeighbours.add(neighbour);
        }
        addContact(neighbour);
    }

    /**
     * Adds a client that is in the same network
     *
     * @param contact Contact address
     */
    public void addContact(Node contact) {
        if (!isAddrInArrayList(contact.getAddress(), contacts)) {
            contacts.add(contact);
        }
    }

    /**
     * Checks whether an address is already present within a certain list
     *
     * @param addr Address we are searching for
     * @param list List we want to search
     * @return True if it is in the list, false otherwise
     */
    private boolean isAddrInArrayList(int addr, ArrayList<Node> list) {
        for (Node n : list) {
            if (n.getAddress() == addr) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the index of an address in a specific list
     *
     * @param addr Address to find
     * @param list List to search through
     * @return Index of the address in the list
     */
    private int getIndexOfAddressInList(int addr, ArrayList<Node> list) {
        for (Node n : list) {
            if (n.getAddress() == addr) {
                return list.indexOf(n);
            }
        }
        return -1;
    }

    /**
     * Decodes the byte array into contactsNeighbours list
     *
     * @param data Data of a forwarding packet
     */
    public void decode(byte[] data) {
        int nodeCount = data[0];
        int indexOfSrc = getIndexOfAddressInList(data[1], contacts);
        ArrayList<Integer> neighbourList = new ArrayList<>();

        for (int i = 1; i < nodeCount; i++) {
            neighbourList.add((int) data[i]);
        }

        contactsNeighbours.set(indexOfSrc, neighbourList);
    }

    /**
     * Encodes that data in the object to a byte array
     *
     * @return Byte array of a forwarding packet
     */
    public byte[] encode() {
        byte[] data = new byte[32];
        data[0] = (byte) directNeighbours.size();
        data[1] = (byte) addr.getAddress();

        int i = 2;
        for (Node n : directNeighbours) {
            data[i] = (byte) n.getAddress();
            i++;
        }
        return data;
    }
}

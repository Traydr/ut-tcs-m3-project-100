package protocol;

import java.util.ArrayList;

public class ForwardingV2 {
    ArrayList<Node> contacts;
    ArrayList<Node> directNeighbours;
    Node addr;

    /**
     * Constructor
     *
     * @param addr Our address
     */
    public ForwardingV2(Node addr) {
        contacts = new ArrayList<>();
        directNeighbours = new ArrayList<>();
        this.addr = addr;
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
}

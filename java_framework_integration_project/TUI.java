public class TUI {
    TUIReader reader;
    Thread readerThread;

    /**
     * Creates a TUIReader and starts a new thread for it.
     */
    public TUI() {
        reader = new TUIReader(this);
        readerThread = new Thread(reader);
    }

    /**
     * Prints a message to the client.
     * @param msg
     */
    public void printMessage(String msg) {
        System.out.println(msg);
    }

    /**
     * Prints an error to the client
     * @param err
     */
    public void printError(String err) {
        System.out.println("[ERR] " + err);
    }

    /**
     * Prints the help message to the client
     */
    public void printHelp() {
        System.out.println("Commnads:" +
                           "\n\tchat - Send messages to others" +
                           "\n\tlist - Show participants in the network" +
                           "\n\thelp - Show this help message");
    }
}

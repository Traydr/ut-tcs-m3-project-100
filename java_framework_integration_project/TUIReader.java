import java.util.Scanner;

public class TUIReader implements Runnable{
    TUI tui;
    Scanner scanner;

    /**
     * Links the tui and starts a scanner
     * @param tui
     */
    public TUIReader(TUI tui) {
        scanner = new Scanner(System.in);
        this.tui = tui;
    }

    public void close() {
        scanner.close();
    }

    private void parseInput(String input) {
        String[] inputArr = input.split(" ");
        switch (inputArr[0].toLowerCase()) {
            case "chat":
                if (inputArr.length <= 1) {
                    tui.printError("No message to send");
                }
                //TODO send chats to others
                break;
            case "list":
                //TODO list participant
                break;
            case "help":
                tui.printHelp();
                break;
            default:
                tui.printMessage("Incorrect commands, write 'help' for a list of commands");
                break;
        }
    }

    @Override
    public void run() {
        try {
            while (scanner.hasNextLine()) {
                parseInput(scanner.nextLine());
            }
        } catch (Exception e) {
            close();
        }
    }
}

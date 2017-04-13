package chat.webside.interactive.model;

import java.util.ArrayList;
import java.util.List;

public class InteractiveMessage {

    private String message;

    private List<String> receivers = new ArrayList<>();

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getReceivers() {
        return receivers;
    }

    public void setReceivers(List<String> receivers) {
        this.receivers = receivers;
    }

    public void addReceiver(String receiver) {
        this.receivers.add(receiver);
    }

}

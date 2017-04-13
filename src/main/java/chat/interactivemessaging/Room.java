package chat.interactivemessaging;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Room {

    private static Logger logger = LoggerFactory.getLogger(Room.class);
    private String id;
    private Map<String, Client> clients = new ConcurrentHashMap<String, Client>();

    public Room(String id) {
    }

    public Client addClient(String id, Client client) {
        return clients.put(id, client);
    }

    public Client removeClient(String id) {
        return clients.remove(id);
    }

    public boolean isClientIn(Client client, Client... exclusions) {
        for (Client exclusion : exclusions) {
            if (client.equals(exclusion)) {
                return true;
            }
        }
        return false;
    }

    public Set<String> getAllClientId() {
        return clients.keySet();
    }

    public int getClientCount() {
        return clients.size();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}

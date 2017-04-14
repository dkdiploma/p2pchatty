package chat.interactivemessaging;

public class Client {

    private String id;
    private String roomId;

    public Client() {
    }

    ;

    public Client(String id, String roomId) {
        this.id = id;
        this.roomId = roomId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
}

package chat.interactivemessaging;

import chat.webside.controllers.CommonInteractionController;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.java_websocket.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RoomManager {
    
    @Autowired
    CommonInteractionController commonInteractionController;

    private static Logger logger = LoggerFactory.getLogger(RoomManager.class);
    private Map<String, Room> rooms = new ConcurrentHashMap<String, Room>();
    // <clientId, socket>
    private Map<String, WebSocket> clientIdSocketMap = new ConcurrentHashMap<String, WebSocket>();
    // <socket, clientId>
    private Map<WebSocket, Client> socketClientMap = new ConcurrentHashMap<WebSocket, Client>();

    public Room getRoom(String key) {
        return rooms.get(key);
    }

    public Room clearRoom(String key) {
        Room room = rooms.get(key);
        for (String clientId : room.getAllClientId()) {
            WebSocket socket = clientIdSocketMap.remove(clientId);
            socketClientMap.remove(socket);
        }
        return rooms.remove(key);
    }

    public void clearAllRooms() {
        clientIdSocketMap.clear();
        socketClientMap.clear();
        rooms.clear();
    }

    /**
     * 新客户加入房间
     *
     * @param socket 新加入的客户连接
     * @param data 包含房间1信息
     */
    private void joinEvent(String userId, JsonNode data) {
        List<String> result = new ArrayList<String>();
        String roomId = data.get(NodeKey.DATA_ROOM).asText();
        if (StringUtils.isEmpty(roomId)) {
            roomId = Constants.DEFAULT_ROOM_NAME;
        }
        Room room = getRoom(roomId);
        if (null != room && room.isRoomFull()) {
            // TODO tell current client the room is already full
        }
        //String clientId = UUID.randomUUID().toString();
        Client client = new Client(userId, roomId);
        // 向房间其他成员发送新用户添加事件
        if (null != room) {
            StringBuilder sb = new StringBuilder("{\"eventName\":\"_new_peer\",\"data\":{\"socketId\":\"");
            sb.append(userId).append("\"}}");
            String message = sb.toString();
            result.add(message);
            for (String id : room.getAllClientId()) {
                result.add(id);
            }
            commonInteractionController.sendSomethingToSomebody(result);
            result.clear();
            // room.broadcastToAll(message);
            logger.info("broadcast _new_peer event: {}", message);
        } else {
            room = new Room(roomId);
            rooms.put(roomId, room);
        }
        room.addClient(userId, client);
        //  clientIdSocketMap.put(clientId, socket);
        //    socketClientMap.put(socket, client);
        // 向当前成员发送 "_peers" 事件, 当前房间其他成员id和自己的id
        StringBuilder sb = new StringBuilder("{\"eventName\":\"_peers\",\"data\":{\"connections\":[");
        boolean hasOtherClient = false;
        for (String id : room.getAllClientId()) {
            if (!id.equals(userId)) {
                hasOtherClient = true;
                sb.append("\"").append(id).append("\",");
            }
        }
        if (hasOtherClient) {
            sb.deleteCharAt(sb.length() - 1);
        }
        sb.append("],\"you\":\"").append(userId).append("\"}}");
        String message = sb.toString();
        result.add(message);
        result.add(userId);
        commonInteractionController.sendSomethingToSomebody(result);
        // socket.send(message);
        logger.info("send back to new client:{}", message);
    }

    /**
     * 向另一个成员发送 ice_candidate 信息
     *
     * @param socket 发送源
     * @param data 包含要发送 给目标客户的 ice_candidate 信息和目标id
     */
    private void iceCandidateEvent(String userId, JsonNode data) {
        List<String> result = new ArrayList<>();
        String targetId = data.get(NodeKey.DATA_SOCKET_ID).asText();
        // Client client = socketClientMap.get(socket);
        // WebSocket target = clientIdSocketMap.get(targetId);
        if (null != targetId) {
            String label = data.get(NodeKey.DATA_LABEL).asText();
            String candidate = data.get(NodeKey.DATA_CANDIDATE).asText();
            StringBuilder sb = new StringBuilder("{\"eventName\":\"_ice_candidate\",\"data\":{\"label\":\"");
            sb.append(label).append("\",\"candidate\":\"").append(candidate).append("\",\"socketId\":\"")
                    .append(userId).append("\"}}");
            String message = sb.toString();
            result.add(message);
            result.add(targetId);
            commonInteractionController.sendSomethingToSomebody(result);
            //target.send(message);
            logger.info("send ice candidate to {} : {}", targetId, message);
        } else {
            logger.warn("socket({}) is nonexistent.");
        }
    }

    private void offerEvent(String userId, JsonNode data) {
        List<String> result = new ArrayList<>();
        String targetId = data.get(NodeKey.DATA_SOCKET_ID).asText();
        // Client client = socketClientMap.get(socket);
        // WebSocket target = clientIdSocketMap.get(targetId);
        if (null != targetId) {
            JsonNode sdp = data.get(NodeKey.DATA_SDP);
            StringBuilder sb = new StringBuilder("{\"eventName\":\"_offer\",\"data\":{\"sdp\":");
            sb.append(sdp.toString()).append(",\"socketId\":\"").append(userId).append("\"}}");
            String message = sb.toString();
            result.add(message);
            result.add(targetId);
            commonInteractionController.sendSomethingToSomebody(result);
            // target.send(message);
            logger.info("send offer to {} : {}", targetId, message);
        } else {
            logger.warn("socket({}) is nonexistent.");
        }
    }

    private void answerEvent(String userId, JsonNode data) {
        List<String> result = new ArrayList<>();
        String targetId = data.get(NodeKey.DATA_SOCKET_ID).asText();
        // Client client = socketClientMap.get(socket);
        //  WebSocket target = clientIdSocketMap.get(targetId);
        if (null != targetId) {
            JsonNode sdp = data.get(NodeKey.DATA_SDP);
            StringBuilder sb = new StringBuilder("{\"eventName\":\"_answer\",\"data\":{\"sdp\":");
            sb.append(sdp.toString()).append(",\"socketId\":\"").append(userId).append("\"}}");
            String message = sb.toString();
            result.add(message);
            result.add(targetId);
            commonInteractionController.sendSomethingToSomebody(result);
            //target.send(message);
            logger.info("send answer to {} : {}", targetId, message);
        } else {
            logger.warn("socket({}) is nonexistent.");
        }
    }

    public void eventDispatch(String userId, String message) {
        JsonNode node = JsonUtils.parse(message);
        String eventMethod = node.get(NodeKey.EVENT_KEY).asText();
        JsonNode data = node.get(NodeKey.DATA_KEY);
        if (NodeKey.EVENT_JOIN.equals(eventMethod)) {
            joinEvent(userId, data);
        } else if (NodeKey.EVENT_ICE_CANDIDATE.equals(eventMethod)) {
            iceCandidateEvent(userId, data);
        } else if (NodeKey.EVENT_OFFER.equals(eventMethod)) {
            offerEvent(userId, data);
        } else if (NodeKey.EVENT_ANSWER.equals(eventMethod)) {
            answerEvent(userId, data);
        } else {
            logger.error("unknown event :{}", eventMethod);
            // TODO socket 返回什么消息
        }
    }

    /**
     * socket 连接断开, 清理相关数据, 向该连接所处房间发送离开广播
     *
     * @param socket
     */
    public void removeSocket(WebSocket socket) {
        Client client = socketClientMap.remove(socket);
        String clientId = client.getId();
        clientIdSocketMap.remove(clientId);
        String roomId = client.getRoomId();
        Room room = rooms.get(roomId);
        room.removeClient(clientId);
        if (room.getClientCount() <= 0) {
            rooms.remove(roomId);
        } else {
            StringBuilder sb = new StringBuilder("{\"eventName\":\"_remove_peer\",\"data\":{\"socketId\":\"");
            sb.append(clientId).append("\"}}");
            String message = sb.toString();
        //    room.broadcastToAll(message);
            logger.info("{} leaves the room {}, broadcast: {}.", clientId, roomId, message);
        }
    }

    public Map<WebSocket, Client> getSocketClientMap() {
        return socketClientMap;
    }
}

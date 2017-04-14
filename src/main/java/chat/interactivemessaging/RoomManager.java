package chat.interactivemessaging;

import chat.dbside.models.Chat;
import chat.dbside.models.User;
import chat.dbside.services.ini.MediaService;
import chat.webside.controllers.CommonInteractionController;
import chat.webside.dao.UsersDao;
import chat.webside.interactive.model.InteractiveMessage;
import chat.webside.model.UserProfile;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class RoomManager {

    @Autowired
    CommonInteractionController commonInteractionController;

    @Autowired
    @Qualifier("EAVStorageServiceUser")
    private MediaService serviceEntity;

    @Autowired
    private UsersDao usersDao;

    private static Logger logger = LoggerFactory.getLogger(RoomManager.class);
    private Map<String, Room> rooms = new ConcurrentHashMap<>();

    public Room getRoom(String key) {
        return rooms.get(key);
    }

    private void joinEvent(String userId, JsonNode data) {
        InteractiveMessage messageDescriptor = new InteractiveMessage();
        String roomId = data.get(NodeKey.DATA_ROOM).asText();
        if (StringUtils.isEmpty(roomId)) {
            roomId = Constants.DEFAULT_ROOM_NAME;
        }
        Chat chat = (Chat) serviceEntity.findById(Integer.parseInt(roomId), Chat.class);
        UserProfile userProfile = usersDao.getUserProfile(userId);
        User user = (User) serviceEntity.findById(userProfile.getUserEntityId(), User.class);
        if (chat.getCreator().equals(user) || chat.getMembers().contains(user)) {
            Room room = getRoom(roomId);
            Client client = new Client(userId, roomId);
            if (null != room) {
                StringBuilder message = new StringBuilder("{\"eventName\":\"_new_peer\",\"data\":{\"socketId\":\"");
                message.append(userId).append("\"}}");
                messageDescriptor.setMessage(message.toString());
                for (String id : room.getAllClientId()) {
                    messageDescriptor.addReceiver(id);
                }
                commonInteractionController.sendSomethingToSomebody(messageDescriptor);
                messageDescriptor = new InteractiveMessage();
                logger.info("broadcast _new_peer event: {}", message);
            } else {
                room = new Room(roomId);
                rooms.put(roomId, room);
            }
            room.addClient(userId, client);
            StringBuilder message = new StringBuilder("{\"eventName\":\"_peers\",\"data\":{\"connections\":[");
            boolean hasOtherClient = false;
            for (String id : room.getAllClientId()) {
                if (!id.equals(userId)) {
                    hasOtherClient = true;
                    message.append("\"").append(id).append("\",");
                }
            }
            if (hasOtherClient) {
                message.deleteCharAt(message.length() - 1);
            }
            message.append("],\"you\":\"").append(userId).append("\"}}");
            messageDescriptor.setMessage(message.toString());
            messageDescriptor.addReceiver(userId);
            commonInteractionController.sendSomethingToSomebody(messageDescriptor);
            logger.info("send back to new client:{}", message);
        }
    }

    private void iceCandidateEvent(String userId, JsonNode data) {
        InteractiveMessage messageDescriptor = new InteractiveMessage();
        String targetId = data.get(NodeKey.DATA_SOCKET_ID).asText();
        if (null != targetId) {
            String label = data.get(NodeKey.DATA_LABEL).asText();
            String candidate = data.get(NodeKey.DATA_CANDIDATE).asText();
            StringBuilder message = new StringBuilder("{\"eventName\":\"_ice_candidate\",\"data\":{\"label\":\"");
            message.append(label).append("\",\"candidate\":\"").append(candidate).append("\",\"socketId\":\"")
                    .append(userId).append("\"}}");
            messageDescriptor.setMessage(message.toString());
            messageDescriptor.addReceiver(targetId);
            commonInteractionController.sendSomethingToSomebody(messageDescriptor);
            logger.info("send ice candidate to {} : {}", targetId, message);
        } else {
            logger.warn("socket({}) is nonexistent.");
        }
    }

    private void offerEvent(String userId, JsonNode data) {
        InteractiveMessage messageDescriptor = new InteractiveMessage();
        String targetId = data.get(NodeKey.DATA_SOCKET_ID).asText();
        if (null != targetId) {
            JsonNode sdp = data.get(NodeKey.DATA_SDP);
            StringBuilder message = new StringBuilder("{\"eventName\":\"_offer\",\"data\":{\"sdp\":");
            message.append(sdp.toString()).append(",\"socketId\":\"").append(userId).append("\"}}");
            messageDescriptor.setMessage(message.toString());
            messageDescriptor.addReceiver(targetId);
            commonInteractionController.sendSomethingToSomebody(messageDescriptor);
            logger.info("send offer to {} : {}", targetId, message);
        } else {
            logger.warn("socket({}) is nonexistent.");
        }
    }

    private void answerEvent(String userId, JsonNode data) {
        InteractiveMessage messageDescriptor = new InteractiveMessage();
        String targetId = data.get(NodeKey.DATA_SOCKET_ID).asText();
        if (null != targetId) {
            JsonNode sdp = data.get(NodeKey.DATA_SDP);
            StringBuilder message = new StringBuilder("{\"eventName\":\"_answer\",\"data\":{\"sdp\":");
            message.append(sdp.toString()).append(",\"socketId\":\"").append(userId).append("\"}}");
            messageDescriptor.setMessage(message.toString());
            messageDescriptor.addReceiver(targetId);
            commonInteractionController.sendSomethingToSomebody(messageDescriptor);
            logger.info("send answer to {} : {}", targetId, message);
        } else {
            logger.warn("socket({}) is nonexistent.");
        }
    }

    public void eventDispatch(String userId, String message) {
        JsonNode node = JsonUtils.parse(message);
        String eventMethod = node.get(NodeKey.EVENT_KEY).asText();
        JsonNode data = node.get(NodeKey.DATA_KEY);
        if (null == eventMethod) {
            logger.error("unknown event :{}", eventMethod);
        } else {
            switch (eventMethod) {
                case NodeKey.EVENT_JOIN:
                    joinEvent(userId, data);
                    break;
                case NodeKey.EVENT_ICE_CANDIDATE:
                    iceCandidateEvent(userId, data);
                    break;
                case NodeKey.EVENT_OFFER:
                    offerEvent(userId, data);
                    break;
                case NodeKey.EVENT_ANSWER:
                    answerEvent(userId, data);
                    break;
                case NodeKey.EVENT_REMOVE:
                    removeSocket(userId, data);
                    break;
                default:
                    logger.error("unknown event :{}", eventMethod);
                    break;
            }
        }
    }

    public void removeSocket(String userId, JsonNode data) {
        String requestedUser = data.get(NodeKey.USER_ID).asText();
        if (userId.equals(requestedUser)) {
            Room room = rooms.get(data.get(NodeKey.DATA_ROOM).asText());
            for (String clientId : room.getAllClientId()) {
                if (clientId.equals(userId)) {
                    room.removeClient(clientId);
                }
            }
            if (room.getClientCount() <= 0) {
                rooms.remove(data.get(NodeKey.DATA_ROOM).asText());
            } else {
                StringBuilder message = new StringBuilder("{\"eventName\":\"_remove_peer\",\"data\":{\"socketId\":\"");
                message.append(userId).append("\"}}");

                InteractiveMessage messageDescriptor = new InteractiveMessage();
                messageDescriptor.setMessage(message.toString());
                for (String clientId : room.getAllClientId()) {
                    messageDescriptor.addReceiver(clientId);
                }
                commonInteractionController.sendSomethingToSomebody(messageDescriptor);
                logger.info("{} leaves the room {}, broadcast: {}.", userId, data.get(NodeKey.DATA_ROOM).asText(), message.toString());
            }
        }
    }
}

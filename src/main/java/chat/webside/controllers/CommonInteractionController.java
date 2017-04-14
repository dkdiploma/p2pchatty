package chat.webside.controllers;

import chat.dbside.models.Chat;
import chat.dbside.models.Message;
import chat.dbside.models.User;
import chat.dbside.services.ini.MediaService;
import chat.interactivemessaging.RoomManager;
import chat.webside.controllers.util.ChatUtil;
import chat.webside.controllers.util.UserUtil;
import chat.webside.dao.UsersDao;
import chat.webside.interactive.InteractiveRepository;
import chat.webside.interactive.model.IntMessage;
import chat.webside.interactive.model.InteractiveComment;
import chat.webside.interactive.model.InteractiveMessage;
import chat.webside.interactive.model.InteractiveVote;
import chat.webside.model.UserProfile;
import chat.webside.model.ajax.AjaxResponseBody;
import chat.webside.model.ajax.SearchCriteria;
import chat.webside.model.ajax.NameAndImage;
import com.google.common.base.Strings;
import java.security.Principal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
public class CommonInteractionController {

    @Autowired
    private RoomManager roomManager;

    @Autowired
    private InteractiveRepository commonInteractiveHandler;

    @Autowired
    private UsersDao usersDao;

    @Autowired
    @Qualifier("EAVStorageServiceUser")
    private MediaService serviceEntity;

    @Autowired
    private SimpMessagingTemplate template;

    @Autowired
    private ChatUtil challengeDefUtil;

    @Autowired
    private UserUtil userUtil;

    private static final Logger logger
            = LoggerFactory.getLogger(CommonInteractionController.class);

    @MessageMapping("/interactive.like.{username}")
    public void like(@Payload InteractiveVote message, @DestinationVariable("username") String username, Principal principal) {
        UserProfile userProf = usersDao.getUserProfile(principal.getName());
        User user = (User) serviceEntity.findById(userProf.getUserEntityId(), User.class);
        if (user == null) {
            return;
        }
        Message comment = (Message) serviceEntity.findById(message.getIdOwner(), Message.class);

        boolean voteFor = message.getChangeVote() == 1;

        if (comment.getAuthor().equals(user)) {
            return;
        }
        message.setUserId(user.getId());
        if (voteFor) {
            if (comment.getVotesAgainst().contains(user)) {
                comment.rmVoteAgainst(user);
                //usersDao.deleteRelation(comment.getId(), user.getId(), 19);
                //remove in web
                message.setChangeVote(2);
            } else {
                //don't remove in web
                message.setChangeVote(1);
            }
            comment.addVoteFor(user);
            serviceEntity.update(comment);
            User author = comment.getAuthor();
            author.addRating(1);
            serviceEntity.update(author);
        } else {
            if (comment.getVotesFor().contains(user)) {
                comment.rmVoteFor(user);
                //usersDao.deleteRelation(comment.getId(), user.getId(), 18);
                message.setChangeVote(-2);
            } else {
                message.setChangeVote(-1);
            }

            comment.addVoteAgainst(user);
            serviceEntity.update(comment);
            User author = comment.getAuthor();
            author.addRating(-1);
            serviceEntity.update(author);
        }
        Set<String> candidates = commonInteractiveHandler.getCommonConnection4Object(message.getMainObjectId());
        for (String resp : candidates) {
            template.convertAndSend("/user/" + resp + "/exchange/like", message);
        }
    }

    @MessageMapping("/interactive.comment.{username}")
    public void interactiveComment(@Payload InteractiveComment message, @DestinationVariable("username") String username, Principal principal) {
        Integer mainObjectId = message.getMainObjectId();
        if (Strings.isNullOrEmpty(message.getMessageContent()) || message.getMessageContent().trim().length() > 250) {
            message.setStatus("FAIL");
        } else {
            UserProfile userProf = usersDao.getUserProfile(principal.getName());
            User user = (User) serviceEntity.findById(userProf.getUserEntityId(), User.class);

            Message newComment = new Message();
            newComment.setDate(DateUtils.addHours(new Date(), 3));
            newComment.setText(message.getMessageContent());
            newComment.setAuthor(user);
            serviceEntity.save(newComment);
            Integer id = message.getIdParent();
            if (id != null) {
                Message parentComment = (Message) serviceEntity.findById(id, Message.class);
                parentComment.addComment(newComment);
                serviceEntity.update(parentComment);
                message.setToWhom(parentComment.getAuthor().getName());
            } else {

                Chat chal = (Chat) serviceEntity.findById(mainObjectId, Chat.class);
                chal.addMessage(newComment);
                serviceEntity.update(chal);

            }

            message.setStatus("SUCCESS");
            message.setUserName(user.getName());
            message.setMessageId(newComment.getId());
            message.setDate(newComment.getDate());
            message.setUserId(user.getId());
            message.setAvatarImage(user.getCommentImageEntity().getBase64());
        }
        Set<String> candidates = commonInteractiveHandler.getCommonConnection4Object(mainObjectId);
        for (String resp : candidates) {
            template.convertAndSend("/user/" + resp + "/exchange/comment", message);
        }
    }

    public void sendSomethingToSomebody(InteractiveMessage messageDescriptor) {
        messageDescriptor.getReceivers().forEach((receiver) -> {
            template.convertAndSend("/user/" + receiver + "/exchange/webrtc", new IntMessage(receiver, messageDescriptor.getMessage()));
        });
    }

    @MessageMapping("/wswebrtc.{username}")
    public void webrtc(Principal p, @DestinationVariable("username") String username, String message) throws Exception {
        if (p != null) {
            roomManager.eventDispatch(p.getName(), message);
        }
//if (p != null) {
//            /**
//             * set the sender on server side so nobody is able to impersonate
//             * other users *
//             */
//            message.setFromUserID(p.getName());
//            /**
//             * forward it to all users if no destination user is set *
//             */
//            if ("".equals(message.getToUserID())) {
//                Set<String> candidates = commonInteractiveHandler.getCommonConnection4Object(18);
//
//                for (String resp : candidates) {
//                    template.convertAndSend("/user/" + resp + "/exchange/allchat", message);
//                    template.convertAndSend("/user/" + resp + "/exchange/like", message);
//                }
//            } else {
//                /**
//                 * forward it to the private queue of the destination user if it
//                 * is a private signaling message *
//                 */
//                Set<String> candidates = commonInteractiveHandler.getCommonConnection4Object(18);
//
//                for (String resp : candidates) {
//                    if (resp.equals(message.getToUserID())) {
//                        template.convertAndSend("/user/" + resp + "/exchange/private", message);
//                        template.convertAndSend("/user/" + resp + "/exchange/like", message);
//                    }
//                }
//                // template.convertAndSendToUser(message.getToUserID(), "/user/" + message.getToUserID() + "/exchange/private", message);
//            }
    }

    @RequestMapping(value = "/getFriends", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    AjaxResponseBody searchFriendsAjax(@RequestBody SearchCriteria search) {
        AjaxResponseBody result = new AjaxResponseBody();
        if (search != null) {
            List<User> users = userUtil.filterFriends(search.getFilter(), search.getUserId());

            if (users.size() > 0) {
                Map<Integer, NameAndImage> usersNames = new HashMap<>();
                users.forEach((user) -> {
                    NameAndImage nameAndImage = new NameAndImage();
                    nameAndImage.setName(user.getName());
                    usersNames.put(user.getId(), nameAndImage);
                });
                result.setCode("200");
                result.setMsg("");
                result.setResult(usersNames);
            } else {
                result.setCode("204");
                result.setMsg("No users");
            }
        } else {
            result.setCode("400");
            result.setMsg("Search criteria is empty");
        }
        return result;
    }

//    @RequestMapping(value = "/getChallenges", produces = "application/json")
//    @ResponseStatus(HttpStatus.OK)
//    public @ResponseBody
//    AjaxResponseBody searchChallengesViaAjax(@RequestBody SearchCriteria search) {
//
//        AjaxResponseBody result = new AjaxResponseBody();
//
//        if (search != null) {
//            List<Chat> challenge
//
//            if (challenges.size() > 0) {
//                Map<Integer, NameAndImage> chalNames = new HashMap<>();
//                for (Chat challenge : challenges) {
//                    NameAndImage nameAndImage = new NameAndImage();
//                    nameAndImage.setName(challenge.getName());
//                    chalNames.put(challenge.getId(), nameAndImage);
//                }
//                result.setCode("200");
//                result.setMsg("");
//                result.setResult(chalNames);
//            } else {
//                result.setCode("204");
//                result.setMsg("No challenges");
//            }
//        } else {
//            result.setCode("400");
//            result.setMsg("Search criteria is empty");
//        }
//        return result;
//    }
    @RequestMapping(value = "/getUsers", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    AjaxResponseBody searchUsers(@RequestBody SearchCriteria search) {

        AjaxResponseBody result = new AjaxResponseBody();

        if (search != null) {

            User currentUser = (User) serviceEntity.findById(search.getUserId(), User.class);
            List<User> filteredUsers = userUtil.filterUsers(search.getFilter(), currentUser);

            if (filteredUsers.size() > 0) {
                Map<Integer, NameAndImage> usersAjax = new HashMap<>();
                for (User user : filteredUsers) {
                    NameAndImage nameAndImage = new NameAndImage();
                    nameAndImage.setName(user.getName());
                    nameAndImage.setImage(user.getMainImageEntity().getBase64());
                    nameAndImage.setIsFriend(currentUser.getFriends().contains(user));
                    nameAndImage.setIsSubscriber(currentUser.getIncomingFriendRequestSenders().contains(user));
                    nameAndImage.setIsSubscripted(user.getIncomingFriendRequestSenders().contains(currentUser));
                    usersAjax.put(user.getId(), nameAndImage);
                }
                result.setCode("200");
                result.setMsg("");
                result.setResult(usersAjax);
            } else {
                result.setCode("204");
                result.setMsg("No users");
            }
        } else {
            result.setCode("400");
            result.setMsg("Search criteria is empty");
        }
        return result;
    }
}

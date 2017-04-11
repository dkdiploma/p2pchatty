package chat.webside.controllers.util;

import chat.dbside.models.Chat;
import chat.dbside.models.Image;
import chat.dbside.models.Request;
import chat.dbside.models.Tag;
import chat.dbside.models.User;
import chat.dbside.services.ini.MediaService;
import chat.webside.authorization.UserActionsProvider;
import chat.webside.authorization.thymeleaf.AuthorizationDialect;
import chat.webside.dao.UsersDao;
import chat.webside.imagesstorage.ImageStoreService;
import chat.webside.model.UserProfile;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

@Component
public class ChatUtil {

    @Autowired
    @Qualifier("EAVStorageServiceUser")
    private MediaService serviceEntity;

    @Autowired
    private AuthorizationDialect dialect;

    @Autowired
    private UserActionsProvider actionsProvider;

    @Autowired
    private MessageUtil messageUtil;

    @Autowired
    private InteractiveUtil interactiveUtil;

    @Autowired
    private UsersDao usersDao;

//    public List<Chat> filterChallenges(String filter, int userId) {
//        User user = (User) serviceEntity.findById(userId, User.class);
//
//        List<Chat> challenges = user.getCreatedChats();
//        List<Chat> filteredChallenges = new ArrayList<>();
//        for (Chat challenge : challenges) {
//            String name = challenge.getName();
//            if (name.toLowerCase().startsWith(filter.toLowerCase())) {
//                filteredChallenges.add(challenge);
//            }
//        }
//        return filteredChallenges;
//    }
    public void throwChat(int userId, User currentUser, int challengeId, String message) {
        Chat chal = (Chat) serviceEntity.findById(challengeId, Chat.class);
        User user = (User) serviceEntity.findById(userId, User.class);

        Request chatRequest = new Request();
        chatRequest.setDate(new Date());
        if (message != null) {
            chatRequest.setMessage(message);
        }
        serviceEntity.save(chatRequest);
        chatRequest.setSender(currentUser);
        chatRequest.setReceiver(user);
        chatRequest.setSubject(chal);
        serviceEntity.update(chatRequest);

        interactiveUtil.interactiveThrowChat(userId, chatRequest);
    }

    public void setModelForNewChat(HttpServletRequest request, User currentUser, Model model) {
        Chat newChat = new Chat();
        newChat.setIsToPersist(Boolean.FALSE);
        // newChat.setDate(new Date());
        model.addAttribute("chat", newChat);
        model.addAttribute("friends", currentUser.getFriends());
        //  List<Tag> tags = serviceEntity.getAll(Tag.class);
        // model.addAttribute("tags", tags);
        // model.addAttribute("selectedTags", new ArrayList<>());
    }

    public void setModelForNewOrUpdatedChalShow(Chat chat, HttpServletRequest request,
            User currentUser, UserProfile userProfile, Model model, String image, List<Integer> selectedFriendsIds) {

        User curDBUser = ((User) serviceEntity.findById(userProfile.getUserEntityId(), User.class));

        if (chat.getId() != null) {
            Chat chatToUpdate = (Chat) serviceEntity.findById(chat.getId(), Chat.class);
            chatToUpdate.setDescription(chat.getDescription());
            chatToUpdate.setName(chat.getName());
            chatToUpdate.setDate(new Date());
            //TODO:check if creator
            if (Objects.equals(chatToUpdate.getCreator().getId(), curDBUser.getId())) {
                serviceEntity.update(chatToUpdate);
            }
            if (!image.isEmpty()) {
                Image oldImage = chatToUpdate.getMainImageEntity();
                if (oldImage.getId() != null) {
                    oldImage.setIsMain(Boolean.FALSE);
                    serviceEntity.update(oldImage);
                }
            }
            // chatToUpdate.removeAllTags();
            serviceEntity.update(chatToUpdate);

        } else {
            chat.setCreator(curDBUser);
            chat.setDate(new Date());
            for (Integer id : selectedFriendsIds) {
                chat.addClient((User) serviceEntity.findById(id, User.class));
            }
            serviceEntity.save(chat);
//            chat.addClient(curDBUser);
//            serviceEntity.update(chat);
        }

        chat = (Chat) serviceEntity.findById(chat.getId(), Chat.class);

        //need to update or create image
        if (image.isEmpty()) {
            if (chat.getMainImageEntity().getId() == null) {
                Image img = new Image();
                img.setIsMain(Boolean.TRUE);
                img.setImageRef(ImageStoreService.getDEFAULT_IMAGE_ROUTE());
                serviceEntity.save(img);
                chat.addImage(img);
                serviceEntity.update(chat);
            }
        } else if (!image.isEmpty() && !StringUtils.isNumeric(image)) {
            String base64Image = image.split(",")[1];
            byte[] array = Base64.decodeBase64(base64Image);
            Image imageEntity = new Image();
            imageEntity.setIsMain(Boolean.TRUE);
            serviceEntity.save(imageEntity);
            try {
                ImageStoreService.saveImage(array, imageEntity);
                serviceEntity.update(imageEntity);
            } catch (Exception ex) {
                Logger.getLogger(UsersDao.class.getName()).log(Level.SEVERE, null, ex);
            }
            chat.addImage(imageEntity);
            serviceEntity.update(chat);
        } else if (StringUtils.isNumeric(image)) {
            Image newMainImage = (Image) serviceEntity.findById(Integer.valueOf(image), Image.class);
            newMainImage.setIsMain(Boolean.TRUE);
            serviceEntity.update(newMainImage);
        }
        model.addAttribute("challenge", chat);
        setModelForMembers(chat, model);
        messageUtil.setModelForComments(chat.getMessages(), request, currentUser, model);
    }

    private void setModelForMembers(Chat chat, Model model) {
        int membersOnPage = 6;
        List<User> members = chat.getMembers();
        model.addAttribute("listOfMembers", members.size() > membersOnPage ? members.subList(0, membersOnPage) : members.subList(0, members.size()));
        model.addAttribute("showMembersExtendenceButton", members.size() > membersOnPage);
    }

    public void setModelForChatShow(int id, HttpServletRequest request, User currentUser, Model model) {
        Chat chat = (Chat) serviceEntity.findById(id, Chat.class);
        dialect.setActions(actionsProvider.getActionsForChallengeDefinition(currentUser, chat));
        model.addAttribute("chat", chat);
        setModelForMembers(chat, model);
        if (currentUser != null) {
            model.addAttribute("friends", currentUser.getFriends());
        }
        model.addAttribute("userProfile", currentUser);
        model.addAttribute("tags", serviceEntity.getAll(Tag.class));
        //  model.addAttribute("selectedTags", challenge.getTags());
        messageUtil.setModelForComments(chat.getMessages(), request, currentUser, model);
    }

    public void setModelForShowAcceptors(HttpServletRequest request, Principal currentUser, Model model, int challengeId) {
        Chat challenge = (Chat) serviceEntity.findById(challengeId, Chat.class);
        // model.addAttribute("listSomething", new ArrayList<>(new HashSet<>(challenge.getAllAcceptors())));
        model.addAttribute("idParent", challengeId);
        model.addAttribute("handler", "profile");
        model.addAttribute("type", "acceptors");
    }

    public void setModelForBadDateNewChal(Chat challenge, HttpServletRequest request,
            Principal currentUser, Model model, String image, String imageName) {
        model.addAttribute("challenge", challenge);
        model.addAttribute("image64", image);
        model.addAttribute("imageName", imageName);
        model.addAttribute("tags", serviceEntity.getAll(Tag.class));
        List<Tag> tags = new ArrayList<>();

    }

    public void setModelForMain(HttpServletRequest request, Principal currentUser, Model model) {
        List<Chat> challenges = serviceEntity.getAll(Chat.class);
        // Collections.sort(challenges, Chat.COMPARE_BY_RATING);
//        Chat mainChallenge = challenges.remove(0);
        //  model.addAttribute("mainChallenge", mainChallenge);
        model.addAttribute("challenges", challenges);
        model.addAttribute("tag", "");
    }

//    public void setModelForMainFilteredByTag(HttpServletRequest request, Principal currentUser, Model model, int tagId) {
//        Tag tag = (Tag) serviceEntity.findById(tagId, Tag.class);
//        List<Chat> challenges = tag.getCreatedChats();
//        Chat mainChallenge = null;
//        if (!challenges.isEmpty()) {
//            Collections.sort(challenges, Chat.COMPARE_BY_RATING);
//            mainChallenge = challenges.remove(0);
//        }
//        model.addAttribute("mainChallenge", mainChallenge);
//        model.addAttribute("challenges", challenges);
//        model.addAttribute("tag", tag.getName());
//    }
}

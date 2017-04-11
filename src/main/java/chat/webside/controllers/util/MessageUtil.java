package chat.webside.controllers.util;

import chat.dbside.models.Chat;
import chat.dbside.models.Message;
import chat.dbside.models.User;
import chat.dbside.services.ini.MediaService;
import chat.webside.model.UserProfile;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

@Component
public class MessageUtil {

    @Autowired
    @Qualifier("EAVStorageServiceUser")
    private MediaService serviceEntity;

    public void addNewReply(int id, HttpServletRequest request, UserProfile currentUser, Model model, Message comment) {

        User curDBUser = (User) serviceEntity.findById(currentUser.getUserEntityId(), User.class);

        Message parentComment = (Message) serviceEntity.findById(id, Message.class);

        comment.setParentMessage(parentComment);
        comment.setDate(DateUtils.addHours(new Date(), 3));
        comment.setAuthor(curDBUser);
        serviceEntity.save(comment);
    }

    public void addNewMessage(int chalId, HttpServletRequest request, UserProfile userProfile, Model model, Message message) {
        User curDBUser = (User) serviceEntity.
                findById(userProfile.getUserEntityId(), User.class);

        Chat currentChat = (Chat) serviceEntity.findById(chalId, Chat.class);
        message.setDate(new Date());
        message.setAuthor(curDBUser);
        serviceEntity.save(message);

        currentChat.addMessage(message);
        serviceEntity.update(currentChat);
    }

    public void addNewInstanceComment(int chalId, HttpServletRequest request, UserProfile userProfile, Model model, Message comment) {
        User curDBUser = (User) serviceEntity.
                findById(userProfile.getUserEntityId(), User.class);

   //     ChallengeInstance currentChallenge = (ChallengeInstance) serviceEntity.findById(chalId, ChallengeInstance.class);
        comment.setDate(DateUtils.addHours(new Date(), 3));
        comment.setAuthor(curDBUser);
        serviceEntity.save(comment);
   //     currentChallenge.addComment(comment);
   //     serviceEntity.update(currentChallenge);
    }

    private void sortMessagesByDate(List<Message> messages) {
        Collections.sort(messages, Message.COMPARE_BY_DATE);
        for (Message comm : messages) {
            if (!comm.getMessages().isEmpty()) {
                sortMessagesByDate(comm.getMessages());
            }
        }

    }

    public void setModelForComments(List<Message> messages, HttpServletRequest request, User currentUser, Model model) {
        if (currentUser != null) {
            Message message = new Message();
            message.setDate(DateUtils.addHours(new Date(), 3));
            message.setAuthor(currentUser);
            model.addAttribute("message", message);
            Message hiddenMessage = new Message();
            hiddenMessage.setId(-1);
            hiddenMessage.setText("hidden Comment");
            hiddenMessage.setDate(DateUtils.addHours(new Date(), 3));
            hiddenMessage.setAuthor(currentUser);

            model.addAttribute("hiddenMessage", hiddenMessage);
        }
        int commentsCount = 0;

        for (Message comm : messages) {
            commentsCount++;
            commentsCount += comm.getSubCommentsCount();
        }
        sortMessagesByDate(messages);

        model.addAttribute("messagesCount", commentsCount);
        model.addAttribute("messages", messages);
    }

}

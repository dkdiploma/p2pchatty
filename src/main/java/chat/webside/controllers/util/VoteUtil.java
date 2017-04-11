package chat.webside.controllers.util;

import chat.dbside.models.Message;
import chat.dbside.models.User;
import chat.dbside.services.ini.MediaService;
import chat.webside.dao.UsersDao;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

@Component
public class VoteUtil {

    @Autowired
    @Qualifier("EAVStorageServiceUser")
    private MediaService serviceEntity;
    
    @Autowired
    private UsersDao usersDao;

   // @Autowired
  //  private ChallengeInstanceUtil challengeInsUtil;

    private static final Logger logger = LoggerFactory.getLogger(VoteUtil.class);

    public void setModelForCommentVote(HttpServletRequest request, User user, Model model, int commentId, boolean voteFor) {
        Message comment = (Message) serviceEntity.findById(commentId, Message.class);

        if (comment.getAuthor().equals(user)) {
            return;
        }
        
        if (voteFor) {
            if (comment.getVotesAgainst().contains(user)) {
                if (comment.rmVoteAgainst(user)) {
                    logger.info("remove comment VoteAgainst for comment " + comment.getId());
                    //usersDao.deleteRelation(commentId, user.getId(), 19);
                }
            }
            comment.addVoteFor(user);
            serviceEntity.update(comment);
            User author = comment.getAuthor();
            author.addRating(1);
            serviceEntity.update(author);
        } else {
            if (comment.getVotesFor().contains(user)) {
                if (comment.rmVoteFor(user)) {
                    logger.info("remove comment VoteFor for comment " + comment.getId()
                            + " user: " + user.getId());
                    //usersDao.deleteRelation(commentId, user.getId(), 18);
                }
            }
            serviceEntity.update(comment);
            
            comment.addVoteAgainst(user);
            serviceEntity.update(comment);
            User author = comment.getAuthor();
            author.addRating(-1);
            serviceEntity.update(author);
        }
    }
}

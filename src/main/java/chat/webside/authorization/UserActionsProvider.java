package chat.webside.authorization;

import chat.dbside.models.Chat;
import chat.dbside.models.User;
import java.util.HashSet;
import java.util.Set;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
public class UserActionsProvider {

    public Set<Action> getActionsForProfile(User userWhichMakesRequest, User userWhoseProfileRequested) {
        Set<Action> actions = new HashSet<>();
        if (userWhichMakesRequest.getId().equals(userWhoseProfileRequested.getId())) {
            actions.add(Action.EDIT_CHALLENGE);
            actions.add(Action.CREATE_CHALLENGE);
            actions.add(Action.DELETE_CHALLENGE);
            actions.add(Action.THROW_CHALLENGE_DEF);
            actions.add(Action.WATCH_UNACCEPTED_CHALLENGES);
            actions.add(Action.EDIT_PROFILE);
        } else {
            actions.add(Action.THROW_CHALLENGE_FOR_USER);
            if (!userWhichMakesRequest.getFriends().contains(userWhoseProfileRequested)
                    && !userWhoseProfileRequested.getIncomingFriendRequestSenders().contains(userWhichMakesRequest)
                    && !userWhichMakesRequest.getIncomingFriendRequestSenders().contains(userWhoseProfileRequested)) {
                actions.add(Action.ADD_FRIEND);
            }
        }
        return actions;
    }

    public Set<Action> getActionsForChallengeDefinition(User user, Chat challenge) {
        Set<Action> actions = new HashSet<>();
        if (user != null) {
            boolean canAccept = true;
            if (canAccept) {
                actions.add(Action.ACCEPT_CHALLENGE_DEF);
            }
            if (user.getId().equals(challenge.getCreator().getId())) {
                actions.add(Action.EDIT_CHALLENGE);
                actions.add(Action.DELETE_CHALLENGE);
                actions.add(Action.THROW_CHALLENGE_DEF);
            }
        }
        return actions;
    }

    
    public void canUpdateChallenge(User user, Chat challenge) {
        if (!getActionsForChallengeDefinition(user, challenge).contains(Action.EDIT_CHALLENGE)) {
            throw new AccessDeniedException("You don't have permission to access this page");
        }
    }

    public void canEditProfile(User user, User profileOwner) {
        if (!getActionsForProfile(user, profileOwner).contains(Action.EDIT_PROFILE)) {
            throw new AccessDeniedException("You don't have permission to access this page");
        }
    }
}

package chat.webside.services;

import chat.dbside.models.User;
import chat.webside.model.UserConnection;
import java.util.List;

public interface FriendsImportService {
    public List<User> importFriends(UserConnection connection);
}

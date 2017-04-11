package chat.dbside.models;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

import org.hibernate.annotations.Persister;
import org.hibernate.eav.EAVEntity;
import org.hibernate.eav.EAVGlobalContext;

import chat.dbside.eav.EAVPersister;
import chat.dbside.eav.collection.EAVCollectionPersister;

@Entity
@EAVEntity
@Persister(impl = EAVPersister.class)
public class User extends BaseEntity {

    public User() {
        super(EAVGlobalContext.getTypeOfEntity(User.class.getSimpleName().toLowerCase()).getId());
        backCreators = new ArrayList();
        images = new ArrayList();
        friends = new ArrayList();
        backReceivers = new ArrayList();
        backMembers= new ArrayList();
    }

    private String name;
    private Integer rating;
    //TODO change it (change mapping many-to-many to one-to-many

    @ManyToMany(cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    @JoinTable(name = "eav_relationship",
            joinColumns = @JoinColumn(name = "entity_id2", referencedColumnName = "entity_id"),
            inverseJoinColumns = @JoinColumn(name = "entity_id1", referencedColumnName = "entity_id")
    )
    @Persister(impl = EAVCollectionPersister.class)
    private List<Chat> backMembers;
    
    
    @ManyToMany(cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    @JoinTable(name = "eav_relationship",
            joinColumns = @JoinColumn(name = "entity_id2", referencedColumnName = "entity_id"),
            inverseJoinColumns = @JoinColumn(name = "entity_id1", referencedColumnName = "entity_id")
    )
    @Persister(impl = EAVCollectionPersister.class)
    private List<Chat> backCreators;

    @ManyToMany(cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    @JoinTable(name = "eav_relationship",
            joinColumns = @JoinColumn(name = "entity_id1", referencedColumnName = "entity_id"),
            inverseJoinColumns = @JoinColumn(name = "entity_id2", referencedColumnName = "entity_id")
    )
    @Persister(impl = EAVCollectionPersister.class)
    private List<Image> images;


    //TODO change friendsLeft and friendsRight, getFriends = friendsLeft + friendsRight
    @ManyToMany(cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    @JoinTable(name = "eav_relationship",
            joinColumns = @JoinColumn(name = "entity_id1", referencedColumnName = "entity_id"),
            inverseJoinColumns = @JoinColumn(name = "entity_id2", referencedColumnName = "entity_id")
    )
    @Persister(impl = EAVCollectionPersister.class)
    private List<User> friends;

;

    @ManyToMany(cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    @JoinTable(name = "eav_relationship",
            joinColumns = @JoinColumn(name = "entity_id2", referencedColumnName = "entity_id"),
            inverseJoinColumns = @JoinColumn(name = "entity_id1", referencedColumnName = "entity_id")
    )
    @Persister(impl = EAVCollectionPersister.class)
    private List<Request> backReceivers;




    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public List<Chat> getBackCreators() {
        return backCreators;
    }

    public void setBackCreators(List<Chat> backCreators) {
        this.backCreators = backCreators;
    }

    public List<Image> getImages() {
        return images;
    }

    public void setImages(List<Image> images) {
        this.images = images;
    }

   
    public List<User> getFriends() {
        return friends;
    }

    public void setFriends(List<User> friends) {
        this.friends = friends;
    }

  
    public List<Request> getBackReceivers() {
        return backReceivers;
    }

    public void setBackReceivers(List<Request> backRequestReceiver) {
        this.backReceivers = backRequestReceiver;
    }

    public void addFriend(User user) {
        friends.add(user);
    }

    public void removeFriendRequest(Request request) {
        backReceivers.remove(request);
        request.removeReceiver(this);
    }

    public List<Request> getIncomingRequests() {
        return getBackReceivers();
    }
    public List<Chat> getCreatedChats() {
        return getBackCreators();
    }

    public Image getMainImageEntity() {
        for (Image img : images) {
            if (img.getIsMain()) {
                return img;
            }
        }
        //TODO it possible?
        return new Image();
    }

    public Image getCommentImageEntity() {
        for (Image img : images) {
            if (img.getIsForComment()) {
                return img;
            }
        }
        //TODO it possible?
        return new Image();
    }

    public void addRating(Integer addPart) {
        rating += addPart;
    }

    public void addImage(Image img) {
        images.add(img);
    }

    public List<User> getIncomingFriendRequestSenders() {
        List<User> friendsRequests = new ArrayList();
        for (Request req : getBackReceivers()) {
            if (req.getSubject() == null) {
                friendsRequests.add(req.getSender());
            }
        }
        return friendsRequests;
    }

    public void addChallenge(Chat chal) {
        this.backCreators.add(chal);
    }
    public static final Comparator<User> COMPARE_BY_RATING = (User left, User right)
            -> Integer.signum(right.getRating() - left.getRating());

    public List<Chat> getChats() {
        return backMembers;
    }

    public void setChats(List<Chat> backMembers) {
        this.backMembers = backMembers;
    }
}

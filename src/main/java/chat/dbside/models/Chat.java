package chat.dbside.models;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Persister;
import org.hibernate.eav.EAVEntity;
import org.hibernate.eav.EAVGlobalContext;
import org.hibernate.validator.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import chat.dbside.eav.EAVPersister;
import chat.dbside.eav.collection.EAVCollectionPersister;

@Entity
@EAVEntity
@Persister(impl = EAVPersister.class)
public class Chat extends BaseEntity {

    private static final Logger logger = LoggerFactory.getLogger(Chat.class);

    public Chat() {
        super(EAVGlobalContext.getTypeOfEntity(Chat.class.getSimpleName().toLowerCase()).getId());
        images = new ArrayList();
        members = new ArrayList();
        messages = new ArrayList();
    }

    private String name;
    private String date;
    private String description;
    private String status;
    private Integer isToPersist;
    private Integer membersLimit=10;

    @ManyToMany(cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    @JoinTable(name = "eav_relationship",
            joinColumns = @JoinColumn(name = "entity_id1", referencedColumnName = "entity_id"),
            inverseJoinColumns = @JoinColumn(name = "entity_id2", referencedColumnName = "entity_id")
    )
    @Persister(impl = EAVCollectionPersister.class)
    private List<Image> images;

    //TODO chaldeftag
//    @ManyToMany(cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
//    @JoinTable(name = "eav_relationship",
//            joinColumns = @JoinColumn(name = "entity_id1", referencedColumnName = "entity_id"),
//            inverseJoinColumns = @JoinColumn(name = "entity_id2", referencedColumnName = "entity_id")
//    )
//    @Persister(impl = EAVCollectionPersister.class)
//    private List<Tag> tags;
    //TODO change it (change mapping many-to-many to one-to-many
    @ManyToMany(cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    @JoinTable(name = "eav_relationship",
            joinColumns = @JoinColumn(name = "entity_id1", referencedColumnName = "entity_id"),
            inverseJoinColumns = @JoinColumn(name = "entity_id2", referencedColumnName = "entity_id")
    )
    @Persister(impl = EAVCollectionPersister.class)
    private List<User> members;
    
    @ManyToMany(cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    @JoinTable(name = "eav_relationship",
            joinColumns = @JoinColumn(name = "entity_id1", referencedColumnName = "entity_id"),
            inverseJoinColumns = @JoinColumn(name = "entity_id2", referencedColumnName = "entity_id")
    )
    @Persister(impl = EAVCollectionPersister.class)
    private List<User> creators;

    @ManyToMany(cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    @JoinTable(name = "eav_relationship",
            joinColumns = @JoinColumn(name = "entity_id1", referencedColumnName = "entity_id"),
            inverseJoinColumns = @JoinColumn(name = "entity_id2", referencedColumnName = "entity_id")
    )
    @Persister(impl = EAVCollectionPersister.class)
    private List<Message> messages;

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> comments) {
        this.messages = comments;
    }

    public boolean isRoomFull() {
        return members.size() >= membersLimit;
    }
    
    public void addClient(User client) {
        if (isRoomFull()) {
            logger.error("the room {} is already full");
        } else {
            members.add(client);
        }
    }

    public void removeClient(User user) {
        members.remove(user);
    }
    

    public List<User> getMembers() {
        return members;
    }

    public void setMembers(List<User> creators) {
        this.members = creators;
    }

    @NotNull
    @NotBlank(message = "{error.name.blank}")
    @Size(min = 5, max = 40, message = "{error.name.length}")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name.trim();
    }

    public Date getDate() {
        try {
            DateFormat df = new SimpleDateFormat("EEE MMM dd kk:mm:ss z yyyy", Locale.ENGLISH);
            String ddt = this.date;
            Date result = df.parse(ddt);
            return result;
        } catch (Exception ex) {
            logger.error("null data" + ex.getMessage());
            return (new Date(0));
        }
    }
    //TODO change to base date integer

    public void setDate(Date date) {
        this.date = date.toString();
    }

    @NotNull
    @NotBlank(message = "{error.name.blank}")
    @Size(min = 5, max = 250, message = "{error.description.length}")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description.trim();
    }

    public List<Image> getImages() {
        return images;
    }

    public void setImages(List<Image> images) {
        this.images = images;
    }

    public Image getMainImageEntity() {
        for (Image img : images) {
            if (img.getIsMain()) {
                return img;
            }
        }
        //TODO is it possible?
        return new Image();
    }

    public void addMessage(Message message) {
        messages.add(message);
    }

    public void setCreator(User user) {
        creators = new ArrayList();
        creators.add(user);
    }
    //TODO change creator

    public User getCreator() {
        if (creators.size() >= 1) {
            return creators.get(0);
        }
        return new User();
    }

    public void addImage(Image image) {
        images.add(image);
    }

    public Boolean getIsToPersist() {
        return isToPersist == 1;
    }

    public void setIsToPersist(Boolean isToPersist) {
        this.isToPersist = isToPersist ? 1 : 0;
    }

}

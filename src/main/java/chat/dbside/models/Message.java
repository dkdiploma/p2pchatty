package chat.dbside.models;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Persister;
import org.hibernate.eav.EAVEntity;
import org.hibernate.eav.EAVGlobalContext;
import org.hibernate.validator.constraints.NotBlank;

import chat.dbside.eav.EAVPersister;
import chat.dbside.eav.collection.EAVCollectionPersister;

@Entity
@EAVEntity
@Persister(impl = EAVPersister.class)
public class Message extends BaseEntity {

    public Message() {
        super(EAVGlobalContext.getTypeOfEntity(Message.class.getSimpleName().toLowerCase()).getId());
        messages = new ArrayList();
        authors = new ArrayList();
        backComments = new ArrayList();
        votesAgainst = new ArrayList();
        votesFor = new ArrayList();
    }

    private String text;
    private Date date;

    @NotNull
    @NotBlank
    @Size(min = 3, max = 250, message = "{error.comment.length}")
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @ManyToMany(cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    @JoinTable(name = "eav_relationship",
            joinColumns = @JoinColumn(name = "entity_id1", referencedColumnName = "entity_id"),
            inverseJoinColumns = @JoinColumn(name = "entity_id2", referencedColumnName = "entity_id")
    )
    @Persister(impl = EAVCollectionPersister.class)
    private List<Image> images;

    @ManyToMany(cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    @JoinTable(name = "eav_relationship",
            joinColumns = @JoinColumn(name = "entity_id1", referencedColumnName = "entity_id"),
            inverseJoinColumns = @JoinColumn(name = "entity_id2", referencedColumnName = "entity_id")
    )
    @Persister(impl = EAVCollectionPersister.class)
    private List<Message> messages;

    @ManyToMany(cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    @JoinTable(name = "eav_relationship",
            joinColumns = @JoinColumn(name = "entity_id2", referencedColumnName = "entity_id"),
            inverseJoinColumns = @JoinColumn(name = "entity_id1", referencedColumnName = "entity_id")
    )
    @Persister(impl = EAVCollectionPersister.class)
    private List<Message> backComments;

    //TODO change many-to-many to one-to-many
    @ManyToMany(cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    @JoinTable(name = "eav_relationship",
            joinColumns = @JoinColumn(name = "entity_id1", referencedColumnName = "entity_id"),
            inverseJoinColumns = @JoinColumn(name = "entity_id2", referencedColumnName = "entity_id")
    )
    @Persister(impl = EAVCollectionPersister.class)
    private List<User> authors;
    @ManyToMany(cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    @JoinTable(name = "eav_relationship",
            joinColumns = @JoinColumn(name = "entity_id1", referencedColumnName = "entity_id"),
            inverseJoinColumns = @JoinColumn(name = "entity_id2", referencedColumnName = "entity_id")
    )
    @Persister(impl = EAVCollectionPersister.class)
    private List<User> votesFor;

    @ManyToMany(cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    @JoinTable(name = "eav_relationship",
            joinColumns = @JoinColumn(name = "entity_id1", referencedColumnName = "entity_id"),
            inverseJoinColumns = @JoinColumn(name = "entity_id2", referencedColumnName = "entity_id")
    )
    @Persister(impl = EAVCollectionPersister.class)
    private List<User> votesAgainst;

    public List<Message> getBackComments() {
        return backComments;
    }

    public void setBackComments(List<Message> backComments) {
        this.backComments = backComments;
    }

    public List<User> getAuthors() {
        return authors;
    }

    public void setAuthors(List<User> authors) {
        this.authors = authors;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> comments) {
        this.messages = comments;
    }

    public List<User> getVotesFor() {
        return votesFor;
    }

    public void setVotesFor(List<User> votesFor) {
        this.votesFor = votesFor;
    }

    public List<User> getVotesAgainst() {
        return votesAgainst;
    }

    public void setVotesAgainst(List<User> votesAgainst) {
        this.votesAgainst = votesAgainst;
    }

    public void addVoteFor(User voter) {
        votesFor.add(voter);
    }

    public void addVoteAgainst(User voter) {
        votesAgainst.add(voter);
    }

    public boolean rmVoteFor(User voter) {
        return votesFor.remove(voter);
    }

    public boolean rmVoteAgainst(User voter) {
        return votesAgainst.remove(voter);
    }

    public void setParentMessage(Message parent) {
        backComments = new ArrayList();
        backComments.add(parent);
    }

    public Message getParentMessage() {
        return backComments.size() == 0 ? null : backComments.get(0);
    }

    public User getAuthor() {
        return authors.get(0);
    }

    public void setAuthor(User user) {
        authors = new ArrayList();
        authors.add(user);
    }

    public void addComment(Message comment) {
        messages.add(comment);
    }

    public int getSubCommentsCount() {
        int result = 0;
        for (Message com : this.messages) {
            result++;
            result += com.getSubCommentsCount();
        }
        return result;
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

    public void addImage(Image image) {
        images.add(image);
    }

    public static final Comparator<Message> COMPARE_BY_DATE = (Message leftToCompare, Message rightToCompare)
            -> rightToCompare.getDate().compareTo(leftToCompare.getDate());

}

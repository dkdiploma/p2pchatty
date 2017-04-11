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
public class Tag extends BaseEntity {

    public Tag() {
        super(EAVGlobalContext.getTypeOfEntity(Tag.class.getSimpleName().toLowerCase()).getId());
        backTags = new ArrayList();
    }

    private String name;

    @ManyToMany(cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    @JoinTable(name = "eav_relationship",
            joinColumns = @JoinColumn(name = "entity_id2", referencedColumnName = "entity_id"),
            inverseJoinColumns = @JoinColumn(name = "entity_id1", referencedColumnName = "entity_id")
    )
    @Persister(impl = EAVCollectionPersister.class)
    private List<Chat> backTags;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Chat> getBackTags() {
        return backTags;
    }

    public void setBackTags(List<Chat> backTags) {
        this.backTags = backTags;
    }

    public void removeChallenge(Chat challenge) {
        backTags.clear();
    }

    public List<Chat> getChallenges() {
        return getBackTags();
    }
    public static final Comparator<Tag> COMPARE_BY_COUNT = (Tag left, Tag right)
            -> Integer.signum(right.getBackTags().size() - left.getBackTags().size());
}

package chat.dbside.dao.ini.impl;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;

import chat.dbside.dao.ini.MediaDao;
import chat.dbside.property.PropertyDB;

@Repository
public class MediaDaoProperty implements MediaDao<PropertyDB> {

    @PersistenceContext
    private EntityManager em;

    @Override
    public void save(PropertyDB entity) {
        em.persist(entity);
    }

    @Override
    public List<PropertyDB> getAll(Class<PropertyDB> classType) {
        List<PropertyDB> list = em.createQuery("from " + classType.getSimpleName(), classType).getResultList();
        return list;
    }

    @Override
    public void delete(PropertyDB entity) {
        em.remove(em.merge(entity));
    }

    @Override
    public void update(PropertyDB entity) {
        em.merge(entity);
    }

    @Override
    public PropertyDB findById(Object id, Class<PropertyDB> classType) {
        return em.find(classType, id);
    }

}

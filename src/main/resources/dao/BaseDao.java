package dao;

import javax.persistence.EntityManager;
import java.util.List;

public abstract class BaseDao<T, E> {
    EntityManager _entityManager = EntityManagerProvider.createEntityManager();

    public void insert(T... objects) {
        _entityManager.getTransaction().begin();
        for (T object : objects) {
            _entityManager.persist(object);
        }
        _entityManager.getTransaction().commit();
    }

    public T getById(E id) {
        return _entityManager.find(getClazz(), id);
    }

    public List<T> getAll() {
        return _entityManager.createQuery("SELECT obj FROM " + getClazz().getSimpleName() + " obj", getClazz())
                            .getResultList();
    }

    public void delete(T... objects) {
        _entityManager.getTransaction().begin();
        for (T object : objects) {
            _entityManager.remove(_entityManager.contains(object) ? object : _entityManager.merge(object));
        }
        _entityManager.getTransaction().commit();
    }

    public void close() {
        _entityManager.close();
    }

    protected abstract Class<T> getClazz();

}

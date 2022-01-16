package dao;

import javax.persistence.EntityManager;
import javax.persistence.Id;
import java.lang.reflect.Field;
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

    public void update(T... objects) {
        _entityManager.getTransaction().begin();
        for (T object : objects) {
            _entityManager.merge(object);
        }
        _entityManager.getTransaction().commit();
    }

    public void close() {
        _entityManager.close();
    }

    protected abstract Class<T> getClazz();

    public boolean merge(T old1, T obj2){
        Class<?> clazz = old1.getClass();
        while (clazz != null) {
            try {
                Field[] fields = clazz.getDeclaredFields();
                for (Field field : fields) {
                    if(!field.isAnnotationPresent(Id.class)){
                        field.setAccessible(true);
                        Object newValue = field.get(obj2);
                        field.set(old1,newValue);
                    }
                }
                return true;
            }catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
        return false;
    }

}

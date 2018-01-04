package util;

import model.asset.AssetSourceCredentials;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PersistenceHelper {

    private static EntityManager entityManager = null;

    public static EntityManager getEntityManager(String password) {
        if (entityManager != null) {
            return entityManager;
        }
        Map<String, String> properties = new HashMap<>();
        properties.put("javax.persistence.jdbc.password", password + " SecretPassword");
        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("MoneyManager", properties);
        entityManager = entityManagerFactory.createEntityManager();
        return entityManager;
    }

    public static <T> T saveObject(String databasePassword, T object) {
        EntityManager entityManager = PersistenceHelper.getEntityManager(databasePassword);
        try {
            entityManager.getTransaction().begin();
            object = entityManager.merge(object);
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
            entityManager.getTransaction().rollback();
        }
        return object;
    }

    public static <T> List<T> loadObjects(String databasePassword, Class<T> clazz) {
        String name = clazz.getSimpleName();
        EntityManager entityManager = PersistenceHelper.getEntityManager(databasePassword);
        try {
            entityManager.getTransaction().begin();
            Object object = entityManager.createQuery("from " + clazz.getSimpleName()).getResultList();
            entityManager.getTransaction().commit();
            return (List<T>) object;
        } catch (Exception e) {
            e.printStackTrace();
            entityManager.getTransaction().rollback();
        }
        return null;
    }

    public static AssetSourceCredentials loadAssetSourceCredentials(String databasePassword, Long credentialsId) {
        EntityManager entityManager = PersistenceHelper.getEntityManager(databasePassword);
        try {
            entityManager.getTransaction().begin();
            List objects = entityManager.createQuery("from " + AssetSourceCredentials.class.getName() + " where credentialsId = " + credentialsId).getResultList();
            entityManager.getTransaction().commit();
            if (!objects.isEmpty()) {
                return (AssetSourceCredentials) objects.get(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            entityManager.getTransaction().rollback();
        }
        return null;
    }

}
package com.mlesniak.homepage.config;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

/** @author Michael Lesniak (mail@mlesniak.com) */
@Stateless
public class ConfigDao {
    @PersistenceContext
    EntityManager em;

    private ConfigDO getDO(String key) {
        Query query = em.createQuery("SELECT c FROM ConfigDO c WHERE c.key = :key");
        query.setParameter("key", key);

        List results = query.getResultList();
        if (results.isEmpty()) {
            return null;
        }

        // Multiple result can not happen since the id is unique.
        return (ConfigDO) results.get(0);
    }

    public String get(String key) {
        ConfigDO configDO = getDO(key);
        if (configDO == null) {
            return null;
        }

        return configDO.getValue();
    }

    public void save(String key, String value) {
        ConfigDO config = getDO(key);
        if (config == null) {
            config = new ConfigDO(key, value);

        } else {
            config.setValue(value);
        }
        em.persist(config);
    }

    public void delete(String key) {
        Query query = em.createQuery("DELETE FROM ConfigDO c WHERE c.key = :key");
        query.setParameter("key", key);
        query.executeUpdate();
    }
}

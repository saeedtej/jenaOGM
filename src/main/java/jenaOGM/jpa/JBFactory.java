package jenaOGM.jpa;

import org.apache.jena.query.ReadWrite;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.persistence.Cache;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NamedNativeQuery;
import javax.persistence.PersistenceUnitUtil;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.metamodel.Metamodel;

/**
 * Provides a factory for entitymanager
 */
public class JBFactory implements EntityManagerFactory {

    private HashMap<String, NamedNativeQuery> _queries;
    // is factory session open
    private boolean isOpen;
    private String name;
    private JBProvider provider;
    private static ThreadLocal<JBEntityManager> local = new ThreadLocal<JBEntityManager>();

    /**
     * create a factory instance
     * @param p
     * @param n
     * @param queries
     */
    public JBFactory(JBProvider p, String n, HashMap<String, NamedNativeQuery> queries) {
        _queries = queries;
        name = n;
        provider = p;
        isOpen = true;
    }

    /**
     * close the factory
     */
    public synchronized void close() {
        _queries = null;
        isOpen = false;
        provider.notifyClosed(this);
        provider = null;
    }

    /**
     * constructs a TDB session with READ access mode
     * @return javax.persistence.EntityManager
     */
    public synchronized EntityManager createEntityManager() {
        if (local.get() == null) {
            if (!isOpen)
                throw new IllegalStateException("The factory is closed.");
            local.set(new JBEntityManager(_queries, ReadWrite.READ));
        }
        return local.get();
    }

    public synchronized EntityManager getCurrentEntityManager() {
        return local.get();
    }

    protected static synchronized ThreadLocal<JBEntityManager> getEMThreadLocal() {
        return local;
    }

    /**
     * constructs a TDB session
     *
     * @param map in which transaction type is set
     * @return
     */
    public EntityManager createEntityManager(Map map) {
        if (local.get() == null) {
            if (!isOpen)
                throw new IllegalStateException("The factory is closed.");
            local.set(new JBEntityManager(_queries, (ReadWrite) map.get("ReadWrite")));
        }
        return local.get();
    }

    public boolean isOpen() {
        return isOpen;
    }


    public Object getName() {
        return name;
    }


    public Cache getCache() {
        // TODO Auto-generated method stub
        return null;
    }


    public Metamodel getMetamodel() {
        // TODO Auto-generated method stub
        return null;
    }


    public Map<String, Object> getProperties() {
        // TODO Auto-generated method stub
        return null;
    }

    public Set<String> getSupportedProperties() {
        // TODO Auto-generated method stub
        return null;
    }

    public CriteriaBuilder getCriteriaBuilder() {
        // TODO Auto-generated method stub
        return null;
    }

    public PersistenceUnitUtil getPersistenceUnitUtil() {
        // TODO Auto-generated method stub
        return null;
    }


}

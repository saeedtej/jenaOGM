package jenaOGM.jpa;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.jena.query.ReadWrite;
import org.apache.jena.tdb.TDBFactory;
import javassist.util.proxy.ProxyFactory;

import javax.persistence.Embeddable;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityNotFoundException;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.GeneratedValue;
import javax.persistence.LockModeType;
import javax.persistence.NamedNativeQuery;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.metamodel.Metamodel;

import jenaOGM.AnnotationHelper;
import jenaOGM.Bean2RDF;
import jenaOGM.NotFoundException;
import jenaOGM.RDF2Bean;
import jenaOGM.ValuesContext;
import jenaOGM.binding.Persistable;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
/**
 * NEW, CLEAN, DIRTY 
 *
 */
public class JBEntityManager implements javax.persistence.EntityManager, AnnotationHelper {

        private static final String TRANSACTIONS_NOT_SUPPORTED = "This model does not support transactions.";
        private static final String CLOSED = "This EntityManager is closed.";
        protected Model _model;
        protected RDF2Bean _reader;
        protected Bean2RDF _writer;
        private HashMap<String, NamedNativeQuery> _queries;
        private boolean isOpen;
        private FlushModeType flushType = FlushModeType.COMMIT;
        private JBEntityTransaction ta;
        private HashSet<Object> cache;
        private Map<Class, Class> proxyCache;
        private FlushListener flushListener;
        private boolean pendingClose = false;
        private Dataset dataset;
        private ReadWrite mode;

        public JBEntityManager(HashMap<String, NamedNativeQuery> queries , ReadWrite readWrite ) {
                mode = readWrite;
                String directory = "/MyDatabases/Dataset1" ;
                dataset = TDBFactory.createDataset(directory) ;
                dataset.begin(ReadWrite.WRITE);

//              storeDesc = new StoreDesc(LayoutType.LayoutTripleNodesHash , DatabaseType.PostgreSQL);
//        		sqlConnection = DBConnection.getConnection();
//        		connection = SDBFactory.createConnection(sqlConnection);
//        		store = TDBFactory.connectStore(connection , storeDesc);
//        		dataset = SDBFactory.connectDataset(store);

        		_model = dataset.getDefaultModel();
                _writer = new Bean2RDF(_model , this);
                _reader = new RDF2Bean(_model , this);
                _queries = queries;
                isOpen = true;
                cache = new HashSet<Object>();
                proxyCache = new HashMap<Class, Class>();
                dataset.commit();
                dataset.end();

//                _model.write(System.out, "N3");
        }

        protected Dataset getDataset() {
            return dataset;
        }

        protected void commited() {
                if (pendingClose)
                        cleanup();
        }
        
        public void clear() {
                if (! isOpen)
                        throw new IllegalStateException(CLOSED);
                cache.clear();
        }

        public void close() {
                if (withinTransaction())
                        pendingClose = true;
                else
                        cleanup();
        }

        private void cleanup() {
                
        		cache.clear();
                _writer = null;
                _reader = null;
                ta = null;
                _queries = null;
                flushListener = null;
                isOpen = false;
                
                // close SDB
                dataset.close();
//                store.close();
//                connection.close();
                
                // close sql connection
//                try {
//					sqlConnection.close();
//				} catch (SQLException e) {
//					throw new DatabaseException("cannot close sql connection" , e);
//				}
                
                // remove threadlocal object
                JBFactory.getEMThreadLocal().remove();
        }

        public boolean contains(Object target) {
                if (! isOpen)
                        throw new IllegalStateException(CLOSED);
                return _reader.exists(target);
        }
 
        public JBQueryWrapper createNamedQuery(String name) {
                if (! isOpen)
                        throw new IllegalStateException(CLOSED);
                if (!_queries.containsKey(name))
                        throw new IllegalArgumentException(name + ": query not defined in entity.");
                NamedNativeQuery nnq = _queries.get(name);
                Class cls = (nnq.resultClass() == Void.TYPE) ? Object.class : nnq.resultClass();
                return new JBQueryWrapper(nnq.query(), this, cls);
        }

        public JBQueryWrapper createNativeQuery(String queryString) {
                return createNativeQuery(queryString, Object.class);
        }

        public JBQueryWrapper createNativeQuery(String arg0, Class arg1) {      
                if (! isOpen)
                        throw new IllegalStateException(CLOSED);
                return new JBQueryWrapper(arg0, this, arg1);
        }

        public Query createNativeQuery(String arg0, String arg1) {
                throw new UnsupportedOperationException("Use createNativeQuery(String, Class) instead.");
        }

        public Query createQuery(String arg0) {
                return createNativeQuery(arg0);
        }

        public <T> T find(Class<T> type, Object arg1) {
                if (! isOpen)
                        throw new IllegalStateException(CLOSED);
                try {
                        return _reader.load(type, arg1.toString());
                } catch (NotFoundException e) {
                        return null;
                } catch (Exception e) {
                        throw new PersistenceException(e);
                }
        }

        public void flush() {
                for (Object obj : cache) {
                        if (flushListener != null)
                                flushListener.notify(obj);
                    	/*if (obj instanceof Statement) {
                    		Property property = getModel().createProperty(((Statement)obj).getPredicate());
                    		Jenabean.instance().model().add(_writer.save(((Statement) obj).getSubject()) , property ,
                    				_writer.save(((Statement) obj).getObject()));                		
                    	} else {}*/
                        _writer.save(obj);

                }
                cache.clear();
        }

        public Model getDelegate() {
                return getModel();
        }

        public FlushModeType getFlushMode() {
                return flushType;
        }

        public <T> T getReference(Class<T> type, Object key) {
                if (! isOpen)
                        throw new IllegalStateException(CLOSED);
                try {
                        return _reader.load(type, key.toString());
                } catch (NotFoundException e) {
                        throw new EntityNotFoundException();
                }
        }

        public EntityTransaction getTransaction() {
                if (! isOpen)
                        throw new IllegalStateException(CLOSED);
//                if (! _model.supportsTransactions())
//                        throw new UnsupportedOperationException(TRANSACTIONS_NOT_SUPPORTED);
                if (ta == null) ta = new JBEntityTransaction(this);
//            _model.getGraph().getTransactionHandler(),
            return ta;
        }

        public boolean isOpen() {
                return isOpen;
        }

        public void joinTransaction() {
                throw new UnsupportedOperationException("This entity manager does not support JTA transactions");
        }

        public void lock(Object arg0, LockModeType arg1) {
                if (! isOpen)
                        throw new IllegalStateException(CLOSED);
                

        }

        public <T> T merge(T bean) {
                _writer.save(bean);
                return bean;
        }

        public void persist(Object bean) {
                if ( cache.contains(bean))
                        return;
                _reader.init(bean);
                if (withinTransaction())
                        cache.add(bean);
                else {
                	
                	/*if (bean instanceof Statement) {
                		Property property = getModel().createProperty(((Statement)bean).getPredicate());
                		Jenabean.instance().model().add(_writer.save(((Statement) bean).getSubject()) , property ,
                				_writer.save(((Statement) bean).getObject()));
                	} else {}*/
                    _writer.save(bean);

                	
                }
        }

        public <T> Collection<T> findAll(Class<T> clazz) {
        	return _reader.load(clazz);
        }

        private boolean withinTransaction() {
                return ta != null && ta.isActive();
        }

        public void refresh(Object bean) {
                _reader.load(bean);
        }

        public void remove(Object bean) {
                _writer.delete(bean);
        }

        public void setFlushMode(FlushModeType arg0) {
                flushType = arg0;
        }

        public Model getModel() {
                return _model;
        }


        // AnnotationHelper stuff
        public boolean isGenerated(ValuesContext ctx) {
                return ctx.getAccessibleObject().isAnnotationPresent(GeneratedValue.class);
        }

        public boolean isEmbedded(Object bean) {
                if (bean instanceof Persistable)
                        return isEmbedded(bean.getClass().getSuperclass());
                else
                        return isEmbedded(bean.getClass());
        }
        
        private boolean isEmbedded(Class c) {
                return c.isAnnotationPresent(Embeddable.class);
        }

        
        public Class getProxy(Class c) throws InstantiationException, IllegalAccessException {
                if (!proxyCache.containsKey(c)) 
                        cacheProxyClass(c);
                return proxyCache.get(c);
        }

        private void cacheProxyClass(Class c) {
                ProxyFactory f = new ProxyFactory();
                f.setInterfaces(new Class[] {Persistable.class});
                f.setHandler(new JBMethodHandler(cache));
                f.setSuperclass(c);
                proxyCache.put(c, f.createClass());
        }

        
        public boolean proxyRequired() {
                return false;
        }

        public void setFlushListener(FlushListener flushListener) {
                this.flushListener = flushListener;
        }

        public FlushListener getFlushListener() {
                return flushListener;
        }

        public void detach(Object o) {
                cache.remove(o);                
        }

        public <T> T find(Class<T> arg0, Object arg1, Map<String, Object> arg2) {
                // TODO Auto-generated method stub
                return null;
        }

        
        public <T> T find(Class<T> arg0, Object arg1, LockModeType arg2) {
                // TODO Auto-generated method stub
                return null;
        }

        
        public EntityManagerFactory getEntityManagerFactory() {
                // TODO Auto-generated method stub
                return null;
        }

        
        public LockModeType getLockMode(Object arg0) {
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

        
        public void lock(Object arg0, LockModeType arg1, Map<String, Object> arg2) {
                // TODO Auto-generated method stub
                
        }

        
        public void refresh(Object arg0, Map<String, Object> arg1) {
                // TODO Auto-generated method stub
                
        }

        
        public void refresh(Object arg0, LockModeType arg1) {
                // TODO Auto-generated method stub
                
        }

        
        public void refresh(Object arg0, LockModeType arg1, Map<String, Object> arg2) {
                // TODO Auto-generated method stub
                
        }

        
        public void setProperty(String arg0, Object arg1) {
                // TODO Auto-generated method stub
                
        }

        
        public <T> T unwrap(Class<T> arg0) {
                return (T)this._model;
        }

        public <T> TypedQuery<T> createNamedQuery(String arg0, Class<T> arg1) {
                // TODO Auto-generated method stub
                return null;
        }

        public <T> TypedQuery<T> createQuery(CriteriaQuery<T> arg0) {
                // TODO Auto-generated method stub
                return null;
        }

        public <T> TypedQuery<T> createQuery(String arg0, Class<T> arg1) {
                // TODO Auto-generated method stub
                return null;
        }

        
        public <T> T find(Class<T> arg0, Object arg1, LockModeType arg2,
                        Map<String, Object> arg3) {
                // TODO Auto-generated method stub
                return null;
        }

        public CriteriaBuilder getCriteriaBuilder() {
                // TODO Auto-generated method stub
                return null;
        }


        protected ReadWrite getMode() {
            return mode;
        }
}

package jenaOGM.jpa;

import java.net.URI;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Parameter;
import javax.persistence.Query;
import javax.persistence.TemporalType;

import jenaOGM.Namespace;
import jenaOGM.Sparql;
import jenaOGM.TypeWrapper;

import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.update.UpdateAction;

public class JBQueryWrapper implements Query {

        JBEntityManager em;
        String query;
        Class<?> type;
        QuerySolutionMap initialSettings;
        private int firstResult = 0;
        private int maxResult = -1;

        
        public JBQueryWrapper(String q, JBEntityManager entityManager, Class c) {
                query = q;
                type = c;
                initialSettings = new QuerySolutionMap() ;
                em = entityManager;
        }

        public int executeUpdate() {
                UpdateAction.parseExecute(query, em._model);
                return 0;
        }

        public List getResultList() {
                //List<Resource> results = Sparql.exec2(em._model, query, initialSettings);
                //return new LazyResults(results, em);
                return Sparql.exec(em._reader, type, query, initialSettings, firstResult, maxResult);
        }

        public Object getSingleResult() {
                List result = Sparql.exec( em._reader, type, query, initialSettings, 0, -1);
                if ( result.size() > 1 ) 
                        throw new NonUniqueResultException();
                else if ( result.size()==0 )
                        return null;
                return result.iterator().next();
        }

        public Query setFirstResult(int startPosition) {
                firstResult = startPosition;
                return this;
        }

        public Query setFlushMode(FlushModeType flushMode) {
                // TODO Auto-generated method stub
                return null;
        }

        public Query setHint(String hintName, Object value) {
                throw new UnsupportedOperationException("RDBMS specific, doesn't modify SPARQL queries.");
        }

        public Query setMaxResults(int maxResult) {
                this.maxResult = maxResult;
                return this;
        }

        public Query setParameter(String name, Object value) {
                if ( value instanceof URI) {
                        String uri = value.toString();
                        setUriParameter(name, uri);
                } else if ( isManagedEntity(value)) {
                        String uri = TypeWrapper.instanceURI(value);
                        setUriParameter(name, uri);
                } else if ( value instanceof Resource) {
                        initialSettings.add(name, (Resource)value);
                } else if (value instanceof Class) {
                        String uri = TypeWrapper.typeUri((Class)value);
                        setUriParameter(name, uri);
                } else {
                        initialSettings.add(name, em._model.createTypedLiteral(value));
                }
                return this;
        }

        private boolean isManagedEntity(Object value) {
                return value.getClass().isAnnotationPresent(Namespace.class) || value.getClass().isAnnotationPresent(Entity.class);
        }

        public void setUriParameter(String name, String uri) {
                RDFNode node = em._model.createResource(uri);
                initialSettings.add(name, node);
        }
        
        public Query setParameter(int position, Object value) {
                return setParameter(Integer.valueOf(position).toString(), value);
        }

        public Query setParameter(String name, Date value, TemporalType temporalType) {
                throw new UnsupportedOperationException("all dates become xsd:dateTime");       }

        public Query setParameter(String name, Calendar value,
                        TemporalType temporalType) {
                throw new UnsupportedOperationException("all dates become xsd:dateTime");
        }

        public Query setParameter(int position, Date value,
                        TemporalType temporalType) {
                throw new UnsupportedOperationException("all dates become xsd:dateTime");
        }

        public Query setParameter(int position, Calendar value,
                        TemporalType temporalType) {
                throw new UnsupportedOperationException("all dates become xsd:dateTime");
        }

        
        public int getFirstResult() {
                // TODO Auto-generated method stub
                return 0;
        }

        
        public FlushModeType getFlushMode() {
                // TODO Auto-generated method stub
                return null;
        }

        
        public Map<String, Object> getHints() {
                // TODO Auto-generated method stub
                return null;
        }

        
        public LockModeType getLockMode() {
                // TODO Auto-generated method stub
                return null;
        }

        
        public int getMaxResults() {
                // TODO Auto-generated method stub
                return 0;
        }

        
        public Map<String, Object> getNamedParameters() {
                // TODO Auto-generated method stub
                return null;
        }

        
        public List getPositionalParameters() {
                // TODO Auto-generated method stub
                return null;
        }

        
        public Set<String> getSupportedHints() {
                // TODO Auto-generated method stub
                return null;
        }

        
        public Query setLockMode(LockModeType arg0) {
                // TODO Auto-generated method stub
                return null;
        }

        
        public <T> T unwrap(Class<T> arg0) {
                // TODO Auto-generated method stub
                return null;
        }

        public Parameter<?> getParameter(String arg0) {
                // TODO Auto-generated method stub
                return null;
        }

        public Parameter<?> getParameter(int arg0) {
                // TODO Auto-generated method stub
                return null;
        }

        public <T> Parameter<T> getParameter(String arg0, Class<T> arg1) {
                // TODO Auto-generated method stub
                return null;
        }

        public <T> Parameter<T> getParameter(int arg0, Class<T> arg1) {
                // TODO Auto-generated method stub
                return null;
        }

        public <T> T getParameterValue(Parameter<T> arg0) {
                // TODO Auto-generated method stub
                return null;
        }

        public Object getParameterValue(String arg0) {
                // TODO Auto-generated method stub
                return null;
        }

        public Object getParameterValue(int arg0) {
                // TODO Auto-generated method stub
                return null;
        }

        public Set<Parameter<?>> getParameters() {
                // TODO Auto-generated method stub
                return null;
        }

        public boolean isBound(Parameter<?> arg0) {
                // TODO Auto-generated method stub
                return false;
        }

        public <T> Query setParameter(Parameter<T> arg0, T arg1) {
                // TODO Auto-generated method stub
                return null;
        }

        public Query setParameter(Parameter<Calendar> arg0, Calendar arg1,
                        TemporalType arg2) {
                // TODO Auto-generated method stub
                return null;
        }

        public Query setParameter(Parameter<Date> arg0, Date arg1, TemporalType arg2) {
                // TODO Auto-generated method stub
                return null;
        }

}

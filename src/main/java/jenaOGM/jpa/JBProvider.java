package jenaOGM.jpa;

import static org.apache.jena.graph.Node.ANY;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.PersistenceException;
import javax.persistence.spi.LoadState;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.ProviderUtil;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.tdb.TDBFactory;
import jenaOGM.Namespace;
import jenaOGM.ResolverUtil;
import jenaOGM.TypeWrapper;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.shared.Lock;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;

/**
 * Provide jenaOGM TDB - fuzeki persistence layer for accessing jena data flles.
 */
public class JBProvider implements PersistenceProvider {

    public static final String JAVACLASS = "http://thewebsemantic.com/javaclass";
    public static final String TWS_PACKAGE = "tws:package";
    public static final String ASSEMBLY = "META-INF/jenamodels.n3";
    private Model assembly = null;
    private HashMap<String, JBFactory> entityManagers;

    /**
     * called by the JPA api.  Looks for META-INF/jenamodels.n3 in
     * the classpath and uses this file as a Jena Assembler.
     */
    public JBProvider() {
        entityManagers = new HashMap<String, JBFactory>();
        try {
            assembly = findAssembly(ASSEMBLY);
        } catch (Exception e) {
            throw new PersistenceException(e);
        }
    }

    /**
     * Creates a new Jena Bean JPA provider given an existing assembler model.
     * An assembler tells Jena how to construct a model.  The model passed
     * to this constructor should contain triples according to the Jena Assembler
     * schema (http://jena.sourceforge.net/vocabularies/assembler.n3).
     *
     * @param m
     */
    public JBProvider(Model m) {
        entityManagers = new HashMap<String, JBFactory>();
        assembly = m;
    }

    /**
     * not supported operation
     * @param info
     * @param map
     * @return
     */
    public EntityManagerFactory createContainerEntityManagerFactory(
            PersistenceUnitInfo info, Map map) {
        throw new UnsupportedOperationException("");
    }

    /**
     * return a factory for entity manager
     * @param name
     * @param map
     * @return
     */
    public JBFactory createEntityManagerFactory(String name, Map map) {
        // return if exits
        if (entityManagers.containsKey(name))
            return entityManagers.get(name);
        // create an entity manager
        JBFactory f = _createEntityManagerFactory(name, map);
        if (f != null) {
            entityManagers.put(name, f);
        }
        return f;
    }

    /**
     * returns an entityManagerFactory
     * @param emName
     * @param map
     * @return
     */
    private JBFactory _createEntityManagerFactory(String emName, Map map) {
        if (assembly != null) {
            String uri = assembly.expandPrefix(emName);
            if (assembly.getGraph().contains(NodeFactory.createURI(uri), ANY, ANY)) {
                Resource r = assembly.getResource(uri);
                Model m = null;
                Dataset dataset = null;
                JBFactory factory = null;
                Connection sqlConnection = null;
                try {
                    // Make a TDB-backed dataset
                    String directory = "/MyDatabases/Dataset1";
                    dataset = TDBFactory.createDataset(directory);
                    dataset.begin(ReadWrite.WRITE);
                    // Get model inside the transaction
                    m = dataset.getDefaultModel();
                    m.enterCriticalSection(Lock.WRITE);
                    m.createProperty(JAVACLASS).addProperty(RDF.type, OWL.AnnotationProperty);
                    factory = new JBFactory(this, emName, bindAll(m, getPackages(r)));
                } catch (Exception e) {
                    throw new PersistenceException(e);
                } finally {
                    if (m != null) {
                        m.leaveCriticalSection();
                        m.close();
                        dataset.end();
                    }
                    if (sqlConnection != null) {
                        try {
                            sqlConnection.close();
                        } catch (SQLException e) {
                            throw new RuntimeException("cannot close sql connection", e);
                        }
                    }
                }
                return factory;
            }
        }
        return null;

    }


    protected void notifyClosed(JBFactory f) {
        this.entityManagers.remove(f.getName());
    }

    private String[] getPackages(Resource r) {
        String packageUri = assembly.expandPrefix(TWS_PACKAGE);
        Property p = assembly.createProperty(packageUri);
        StmtIterator it = r.listProperties(p);
        LinkedList<String> packageList = new LinkedList<String>();
        while (it.hasNext()) {
            String pname = it.nextStatement().getString();
            packageList.add(pname);
        }
        return packageList.toArray(new String[0]);
    }

    public Model findAssembly(String location) throws IOException {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> resources = loader.getResources(location);
        Model m = ModelFactory.createDefaultModel();
        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();
            m.read(url.toString(), "N3");
        }
        return m;
    }

    protected HashMap<String, NamedNativeQuery> bindAll(Model m, String... s) {
        HashMap<String, NamedNativeQuery> querymap = new HashMap<String, NamedNativeQuery>();
        ResolverUtil<Object> resolver = new ResolverUtil<Object>();
        resolver.findAnnotated(Namespace.class, s)
                .findAnnotated(Entity.class, s)
                .findAnnotated(Embeddable.class, s);
        bind(m, querymap, resolver.getClasses());
        return querymap;
    }

    private void bind(Model m, HashMap<String, NamedNativeQuery> querymap,
                      Set<Class<? extends Object>> classes) {
        for (Class<? extends Object> class1 : classes) {
            Property p = m.getProperty(JAVACLASS);
            m.getResource(TypeWrapper.typeUri(class1)).
                    addProperty(p, class1.getName());
            if (class1.isAnnotationPresent(NamedNativeQueries.class)) {
                storeNamedQuery(class1, querymap);
            }

        }
    }

    protected void storeNamedQuery(Class<? extends Object> class1, HashMap<String, NamedNativeQuery> querymap) {
        NamedNativeQueries queries = class1.getAnnotation(NamedNativeQueries.class);
        for (NamedNativeQuery query : queries.value()) {
            querymap.put(query.name(), query);
        }
    }


    public LoadState isLoaded(Object arg0) {
        // TODO Auto-generated method stub
        return null;
    }


    public LoadState isLoadedWithReference(Object arg0, String arg1) {
        // TODO Auto-generated method stub
        return null;
    }


    public LoadState isLoadedWithoutReference(Object arg0, String arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    public ProviderUtil getProviderUtil() {
        // TODO Auto-generated method stub
        return null;
    }

}

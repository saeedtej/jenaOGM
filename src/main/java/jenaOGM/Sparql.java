package jenaOGM;

import java.util.LinkedList;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.Lock;
import org.apache.jena.update.UpdateAction;

public class Sparql {
	
	/**
	 * Helpful for binding a query result set with a single solution
	 * subject to a particular java bean.  This returns a collection of beans.
	 * Queries are required to follow this pattern in the select clause:
	 * 
	 * <code>SELECT ?s WHERE ...</code>
	 * 
	 * Jenabean will attempt to create an instance of type <code>c</code> bound to 
	 * the RDF resources returned in your query.  It's important to use 
	 * name variable ?s.  This is the named variable Jenabean will expect.
	 * You should make sure that your query
	 * only returns one type or base type, for example, this snippet ensures that
	 * only resources of OWL type Bird are selected...
	 * 
	 * <code>SELECT ?s WHERE { ?s a :Bird ...</code>
	 * 
	 * If you SPARQL query returns heterogenous types, classcast exception
	 * will be thrown.
	 * 
	 * @param <T>
	 * @param m jenaOGM model
	 * @param c Java Class to which the OWL type is bound to
	 * @param query a full SPARQL query
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> LinkedList<T> exec(Model m, Class<T> c, String query) {
		RDF2Bean reader = new RDF2Bean(m);
		QueryExecution qexec = getQueryExec(m, query);
		LinkedList<T> beans = new LinkedList<T>();
		try {
			m.enterCriticalSection(Lock.READ);
			ResultSet results = qexec.execSelect();
			for (;results.hasNext();) {
				RDFNode node = resource(results);
				if (node.isResource()) {
					beans.add(reader.load(c, resource(results)));
				} else if (node.isLiteral()) {
					beans.add((T) node.asLiteral().getValue());
				}
			}				
			return beans;
		} finally {
			m.leaveCriticalSection();
			qexec.close();
		}
	}

	public static <T> LinkedList<T> exec(Model m, Class<T> c, String query, QuerySolution initialBindings) {
		RDF2Bean reader = new RDF2Bean(m);
		QueryExecution qexec = getQueryExec(m, query, initialBindings);
		LinkedList<T> beans = new LinkedList<T>();
		try {
			m.enterCriticalSection(Lock.READ);
			ResultSet results = qexec.execSelect();
			for (;results.hasNext();) {
				RDFNode node = resource(results);
				if (node.isResource()) {
					beans.add(reader.load(c, resource(results)));
				} else if (node.isLiteral()) {
					beans.add((T) node.asLiteral().getValue());
				}
			}
			return beans;
		} finally {
			m.leaveCriticalSection();
			qexec.close();
		}
	}
	
	public static <T> LinkedList<T> exec(RDF2Bean reader, Class<T> c, String query, QuerySolution initialBindings, int start, int max) {
		Model m = reader.getModel();
		QueryExecution qexec = getQueryExec(m, query, initialBindings);
		LinkedList<T> beans = new LinkedList<T>();
		try {
			m.enterCriticalSection(Lock.READ);
			ResultSet results = qexec.execSelect();
			
			for(int pos = 0; pos < start && results.hasNext(); pos++)
				results.next();
			for (;results.hasNext() && max!=0; max--) {
				RDFNode node = resource(results);
				if (node.isResource()) {
					beans.add(reader.load(c, node.asResource()));
				} else if (node.isLiteral()) {
					beans.add((T) node.asLiteral().getValue());
				}
			}				

			return beans;
		} finally {
			m.leaveCriticalSection();
			qexec.close();
		}
	}
	
	public static <T> LinkedList<Resource> exec2(Model m, String query, QuerySolution initialBindings) {
		QueryExecution qexec = getQueryExec(m, query, initialBindings);
		LinkedList<Resource> beans = new LinkedList<Resource>();
		try {
			m.enterCriticalSection(Lock.READ);
			ResultSet results = qexec.execSelect();
			for (;results.hasNext();) beans.add(resource(results).asResource());
			return beans;
		} finally {
			m.leaveCriticalSection();
			qexec.close();
		}
	}

	public static void update(Model m, String query, QuerySolution i) {
		UpdateAction.parseExecute(query, m);
	}
	
	private static QueryExecution getQueryExec(Model m, String query) {
		Query q = QueryFactory.create(query);
		return QueryExecutionFactory.create(q, m);
	}

	private static QueryExecution getQueryExec(Model m, String query, QuerySolution i) {
		Query q = QueryFactory.create(query);
		return QueryExecutionFactory.create(q, m, i);
	}
	
	private static RDFNode resource(ResultSet results) {
		QuerySolution solution = results.nextSolution();
 		return solution.get(solution.varNames().next());
	}
}

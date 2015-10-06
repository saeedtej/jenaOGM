package jenaOGM;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

public class ResourceSaver extends Saver {

	@Override
	public void save(Bean2RDF writer, Resource subject, Property property, Object o) {
		if (o==null) {
			subject.removeAll(property);
			return;
		}
		Model m = subject.getModel();
		subject.removeAll(property).addProperty(property,
				m.getResource(o.toString()));
	}

}

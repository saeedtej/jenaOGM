package jenaOGM.binding;

import jenaOGM.binder.Binder;

import org.apache.jena.ontology.OntClass;

public class Binding {

	private String ontClass;
	private Binder binder;
	
	public Binding(Binder b, OntClass c) {
		ontClass = c.getURI();
		binder = b;
	}

	public Binding(Binder b, String ontClassUri) {
		ontClass = ontClassUri;
		binder = b;
	}
	
	public void to(Class<?> c) {
		binder.save(c, ontClass);
	}
	
	
	

}

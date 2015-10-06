package jenaOGM.vocabulary;

import java.util.Collection;

import jenaOGM.As;
import jenaOGM.Namespace;
import jenaOGM.Thing;

 
@Namespace("http://www.w3.org/2000/01/rdf-schema#")
public interface Rdfs extends As {
	interface Class extends Rdfs {}
	interface Resource extends Rdfs {}
	
	Rdfs comment(Object o);
	Collection<String> comment();
	Rdfs label(Object o);
	Collection<String> label();
	Rdfs seeAlso(Object o);
	Collection<Thing> seeAlso();
}

package jenaOGM.vocabulary;

import jenaOGM.As;
import jenaOGM.Functional;
import jenaOGM.Namespace;

@Namespace("http://www.w3.org/2003/01/geo/wgs84_pos#")
public interface Geo extends As {
	
	interface Point extends Geo{}

	@Functional
	Geo lat(float l);
	Float lat();

	@Functional
	Geo long_(float l);
	Float long_();

}

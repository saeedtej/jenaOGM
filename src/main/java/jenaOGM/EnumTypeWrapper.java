package jenaOGM;

import static jenaOGM.Util.last;

import org.apache.jena.rdf.model.Resource;

public class EnumTypeWrapper extends TypeWrapper {
	
	public EnumTypeWrapper(Class<?> c) {
		super(c);
	}

	@Override
	public String uri(String id) {
		return typeUri() + '/' + id;
	}

	public String id(Object bean) {
		return ((Enum)bean).name();
	}
	
	public Object toBean(String uri) {
		Class<? extends Enum> d =  (Class<? extends Enum>)c;
		return Enum.valueOf(d, last(uri));
	}

	@Override
	public Object toProxyBean(Resource source, AnnotationHelper jpa) {
		return toBean(source.getURI());
	}

}

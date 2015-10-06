package jenaOGM.binding;

import static jenaOGM.TypeWrapper.instanceURI;
import static jenaOGM.TypeWrapper.wrap;

import java.util.Collection;

import jenaOGM.NotFoundException;

import org.apache.jena.rdf.model.Resource;

public class RdfBean<T> {

	private transient Jenabean binder;
	
	public RdfBean() {
		binder = Jenabean.instance();
	}

	public T load(String id) throws NotFoundException {
		return (T)binder.load(this.getClass(), id);
	}
	
	public T refresh() {
		return (T)binder.reader().load(this);
	}
	
	public T save() {
		binder.writer().save(this);
		return (T)this;
	}
	
	public void delete() {
		binder.writer().delete(this);
	}
	
	public T fill(String s) {
		binder.reader().fill(this, s);
		return (T)this;
	}
	
	public Collection<T> query(String query) {
		return (Collection<T>)binder.query(getClass(), query);
	}
	
	public T fill() {
		for (String pd : wrap(this.getClass()).collections())
			binder.reader().fill(this, pd);
		return (T)this;
	}
	
	public Resource asIndividual() {
		return binder.model().getResource(instanceURI(this));
	}
	
	
}

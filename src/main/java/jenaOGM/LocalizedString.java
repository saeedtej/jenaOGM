package jenaOGM;

import org.apache.jena.rdf.model.Literal;

public class LocalizedString {
	private String lang;
	private String value;
	
	public LocalizedString(String value, String lang) {
		this.lang = lang;
		this.value = value;
	}
	
	public LocalizedString(Literal l) {
		this((String)l.getValue(), l.getLanguage());
	}

   public String getLang() {
   	return lang;
   }

	public String toString() {
		return value;
	}
}

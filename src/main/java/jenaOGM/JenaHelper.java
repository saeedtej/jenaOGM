package jenaOGM;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.Calendar;
import java.util.Date;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.datatypes.xsd.XSDDateTime;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;

public class JenaHelper {

	public static Object convertLiteral(RDFNode node, Class<?> c) {
		return convertLiteral(node.as(Literal.class), c);
	}

	public static Object convertLiteral(Literal l, Class<?> c) {
		if (c.equals(Date.class)) {
			return date(l);
		} else if ( c.equals(Calendar.class)) {
			return ((XSDDateTime)l.getValue()).asCalendar();
		} else if (c.equals(BigDecimal.class)) {
			return bigDecimal(l);
		} else if ( Long.TYPE.equals(c)) {
			return l.getLong();
		} else if ( Double.TYPE.equals(c)) {
			return l.getDouble();
		} else if ( Character.TYPE.equals(c)) {
			return l.getValue().toString().charAt(0);
		} else if ( Short.TYPE.equals(c)) {
			return l.getShort();
		} else if ( LocalizedString.class.equals(c)) {
			return new LocalizedString(l);
		} else
			return l.getValue();		
	}
	
	public static Date date(Literal l) {
		XSDDateTime date = (XSDDateTime) l.getValue();
		return date.asCalendar().getTime();
	}
	
	public static Object bigDecimal(Literal l) {
		Object o = l.getDouble();
		System.out.println(o.getClass());
		return null;
	}

	public static Literal toLiteral(Model m, Object o) {	
		if (o instanceof String)
			return m.createTypedLiteral(o.toString());
		else if (o instanceof Date) {
			Calendar c = Calendar.getInstance();
			c.setTime((Date)o);
			return m.createTypedLiteral(c);
		}
		else if (o instanceof Integer)
			return m.createTypedLiteral(((Integer) o).intValue());
		else if (o instanceof Long)
			return m.createTypedLiteral(((Long) o).longValue());
		else if (o instanceof Short)
			return m.createTypedLiteral((Short)o);
		else if (o instanceof Float)
			return m.createTypedLiteral(((Float) o).floatValue());
		else if (o instanceof Double)
			return m.createTypedLiteral(((Double) o).doubleValue());
		else if (o instanceof Character)
			return m.createTypedLiteral(((Character) o).charValue());
		else if (o instanceof Boolean)
			return m.createTypedLiteral(((Boolean) o).booleanValue());
		else if (o instanceof Calendar)
			return m.createTypedLiteral((Calendar) o);
		else if (o instanceof BigDecimal)			
			return m.createTypedLiteral(((BigDecimal)o).doubleValue(), XSDDatatype.XSDdouble);
		else if (o instanceof BigInteger)
			return m.createTypedLiteral((BigInteger)o);
		else if (o instanceof URI)
			return m.createTypedLiteral(o, XSDDatatype.XSDanyURI);
		return null;
	}

}

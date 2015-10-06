package jenaOGM;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;


public class PrimitiveWrapper {
	private static final Set<Class<?>> WRAPPERS = new HashSet<Class<?>>();

	static {
		WRAPPERS.add(Byte.class);
		WRAPPERS.add(Short.class);
		WRAPPERS.add(Character.class);
		WRAPPERS.add(Integer.class);
		WRAPPERS.add(Long.class);
		WRAPPERS.add(Float.class);
		WRAPPERS.add(Double.class);
		WRAPPERS.add(Boolean.class);
		WRAPPERS.add(String.class);
		WRAPPERS.add(Date.class);
		WRAPPERS.add(Calendar.class);
		WRAPPERS.add(BigDecimal.class);
		WRAPPERS.add(BigInteger.class);
		WRAPPERS.add(LocalizedString.class);
	}

	public static boolean isPrimitive(Class<?> c) {
		return c.isPrimitive() || WRAPPERS.contains(c) || WRAPPERS.contains(c.getSuperclass());
	}

	public static boolean isPrimitive(Object o) {
		return isPrimitive(o.getClass());
	}
}

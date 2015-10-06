package jenaOGM.jpa;

import java.lang.reflect.Method;
import java.util.Set;

import jenaOGM.binding.Persistable;

import javassist.util.proxy.MethodHandler;

public class JBMethodHandler implements MethodHandler {
	        
	        private Set<Object> cache;
	        private static Method activate;
	        private boolean active = false;
	        
	        static {
	                try {
	                        activate = Persistable.class.getMethod("activate", null);
	                } catch (Exception e) {
	                        e.printStackTrace();
	                }
	        }
	        
	        public JBMethodHandler(Set cache) {
	                this.cache = cache;
	        }

	        public Object invoke(Object target, Method method, Method proxyMethod, Object[] arg3)
	                        throws Throwable {
	                if ( method.equals(activate))
	                        active = true;
	                if ( method.getName().startsWith("set") && active )
	                        cache.add(target);
	                return ( proxyMethod != null) ? proxyMethod.invoke(target, arg3) : null;
	        }

	}



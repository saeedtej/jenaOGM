package jenaOGM.jpa;


import javax.persistence.EntityManager;

import org.apache.jena.rdf.model.Model;

public class Util {
        public static JBEntityManager concrete(EntityManager em) {
                return (JBEntityManager)em;
        }
        
        public static Model model(EntityManager em) {
                return concrete(em).getModel();
        }
        
        public static void write(EntityManager em) {
                model(em).write(System.out, "N3");
        }
}

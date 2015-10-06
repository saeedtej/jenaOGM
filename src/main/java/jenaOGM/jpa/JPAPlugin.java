package jenaOGM.jpa;


public class JPAPlugin  {
		
/*	@Inject static EntityManagerFactory emf;
	
	@Override
	public void beforeActionInvocation(Method actionMethod) { 
		if (actionMethod.getAnnotation(Transactional.class) != null) {
            Map<String , Object> map = new HashMap<String , Object>();
            map.put("ReadWrite" , actionMethod.getAnnotation(Transactional.class).value());
			JBEntityManager em = (JBEntityManager) emf.createEntityManager(map);
            // Get model inside the transaction
			em.getTransaction().begin();
		}
	}
	
	
	@Override
	public void afterActionInvocation() {
		EntityManager em = ((JBFactory)emf).getCurrentEntityManager();
		if (em != null && em.getTransaction().isActive()) {
			em.getTransaction().commit();
			em.close();
		}
	}

	@Override
	public void onInvocationException(Throwable e) {
		EntityManager em = ((JBFactory)emf).getCurrentEntityManager();
		if (em != null && em.getTransaction().isActive()) {
            em.getTransaction().rollback();
			em.close();
		}
	}

	@Override
	public void onApplicationStop() {
		DBConnection.connectionPool.close();
	}
	*/
}

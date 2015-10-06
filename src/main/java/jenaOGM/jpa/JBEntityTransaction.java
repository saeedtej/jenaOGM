package jenaOGM.jpa;

import javax.persistence.EntityTransaction;
import javax.persistence.RollbackException;

import org.apache.jena.query.ReadWrite;

/**
 * Provide Jena tdb Session transaction
 **/
public class JBEntityTransaction implements EntityTransaction {

    private boolean isActive = false;
    private boolean isRollBackOnly = false;
    private JBEntityManager em;

    public JBEntityTransaction(JBEntityManager em) {
        this.em = em;
    }


    public void begin() {
        if (isActive)
            throw new IllegalStateException("Transaction is already active.  Nested transactions are not supported.");
        em.getDataset().begin(em.getMode());
        isActive = true;
    }


    public void commit() {
        if (!isActive)
            throw new IllegalStateException("transaction is not active");

        try {
            em.flush();
            em.getDataset().commit();
            isActive = false;
            em.commited();
        } catch (Exception e) {
            throw new RollbackException(e);
        }
    }


    public boolean getRollbackOnly() {
        if (!isActive)
            throw new IllegalStateException("transaction is not active");
        return isRollBackOnly;
    }


    public boolean isActive() {
        return isActive;
    }


    public void rollback() {
        if (!isActive)
            throw new IllegalStateException("transaction is not active");
        em.getDataset().abort();
        isActive = false;
    }


    public void setRollbackOnly() {
        isRollBackOnly = true;
    }

}

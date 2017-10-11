package com.natedennis.data.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.natedennis.Parser;
import com.natedennis.data.domain.AccessLog;

public class AccessLogDAO {
   
    private final Logger logger = LoggerFactory.getLogger(AccessLogDAO.class);
	
	public void persist(AccessLog accessLog) {
        // Create an EntityManager
        EntityManager manager = Parser.ENTITY_MANAGER_FACTORY.createEntityManager();
        EntityTransaction transaction = null;

        try {
            // Get a transaction
            transaction = manager.getTransaction();
            // Begin the transaction
            transaction.begin();

            manager.persist(accessLog);

            // Commit the transaction
            transaction.commit();
        } catch (Exception ex) {
            // If there are any exceptions, roll back the changes
            if (transaction != null) {
                transaction.rollback();
            }
            // Print the Exception
            logger.error("accesslog persist error: {}", ex);
        } finally {
            // Close the EntityManager
            manager.close();
        }
    }

    /**
     * Read all the AccessLog.
     * 
     * @return a List of AccessLog
     */
    public List<AccessLog> readAll() {

        List<AccessLog> students = null;

        // Create an EntityManager
        EntityManager manager = Parser.ENTITY_MANAGER_FACTORY.createEntityManager();
        EntityTransaction transaction = null;

        try {
            // Get a transaction
            transaction = manager.getTransaction();
            // Begin the transaction
            transaction.begin();

            // Get a List of AccessLog
            students = manager.createQuery("SELECT a FROM AccessLog a", AccessLog.class).getResultList();

            // Commit the transaction
            transaction.commit();
        } catch (Exception ex) {
            // If there are any exceptions, roll back the changes
            if (transaction != null) {
                transaction.rollback();
            }
            // Print the Exception
            logger.error("accesslog select error: {}", ex);
        } finally {
            // Close the EntityManager
            manager.close();
        }
        return students;
    }

    /**
     * Delete the existing Student.
     * 
     * @param id
     */
    public void delete(int id) {
        // Create an EntityManager
        EntityManager manager = Parser.ENTITY_MANAGER_FACTORY.createEntityManager();
        EntityTransaction transaction = null;

        try {
            // Get a transaction
            transaction = manager.getTransaction();
            // Begin the transaction
            transaction.begin();

            // Get the AccessLog object
            AccessLog stu = manager.find(AccessLog.class, id);
            

            // Delete the AccessLog
            manager.remove(stu);

            // Commit the transaction
            transaction.commit();
        } catch (Exception ex) {
            // If there are any exceptions, roll back the changes
            if (transaction != null) {
                transaction.rollback();
            }
            // Print the Exception
            logger.error("accesslog delete error: {}", ex);
        } finally {
            // Close the EntityManager
            manager.close();
        }
    }

    /**
     * Update the existing AccessLog.
     * 
     * @param accessLog
     */
    public void merge(AccessLog accessLog) {
        // Create an EntityManager
        EntityManager manager = Parser.ENTITY_MANAGER_FACTORY.createEntityManager();
        EntityTransaction transaction = null;

        try {
            // Get a transaction
            transaction = manager.getTransaction();
            // Begin the transaction
            transaction.begin();
            

            // Update the AccessLog
            manager.merge(accessLog);

            // Commit the transaction
            transaction.commit();
        } catch (Exception ex) {
            // If there are any exceptions, roll back the changes
            if (transaction != null) {
                transaction.rollback();
            }
            // Print the Exception
            logger.error("accesslog persist error: {}", ex);
        } finally {
            // Close the EntityManager
            manager.close();
        }
    }
}

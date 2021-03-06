package com.natedennis.data.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.natedennis.Parser;
import com.natedennis.data.EntityManagerProvider;
import com.natedennis.data.domain.AccessLog;

public class AccessLogDAO {
   
    private final Logger logger = LoggerFactory.getLogger(AccessLogDAO.class);

	public void bulkPersist(List<AccessLog> accessLogs, int batchSize) {
        // Create an EntityManager
        EntityManager manager = EntityManagerProvider.getEM();;
        		manager.unwrap(Session.class )
        	    .setJdbcBatchSize( 50 );
        
        EntityTransaction transaction = null;

        try {
            // Get a transaction
            transaction = manager.getTransaction();
            // Begin the transaction
            transaction.begin();
            
            int i = 0;
            for (AccessLog a : accessLogs) {
                 if (a != null) {

                     manager.persist(a);
            	                     
            	    if (i++ % batchSize == 0) {
                        manager.clear();
            	    	manager.flush();
                    }
                 }
              }
            manager.flush();
//release memory
            manager.clear();
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
    public List<String> threadHoldQuery(Date startDate, Date endDate, int threshold) {

        List<String> ips = new ArrayList<>();

        // Create an EntityManager
        EntityManager manager = EntityManagerProvider.getEM();;
        EntityTransaction transaction = null;

        try {
            // Get a transaction
            transaction = manager.getTransaction();
            // Begin the transaction
            transaction.begin();

            // select distinct a.ip, count(a.id) from access_log a group by a.ip HAVING COUNT(a.id)>200;
            // select count(id) from access_log where ip='192.168.89.111';

            // Get a List of AccessLog
            StringBuffer q = new StringBuffer("SELECT a.ip FROM AccessLog a ");
            q.append("where a.accessDate >= :startDate and a.accessDate < :endDate ");
            q.append("GROUP BY a.ip HAVING COUNT(DISTINCT a.id) > :threshold ");
            ips = manager.createQuery(q.toString(), String.class)
            		.setParameter("startDate", startDate)
            		.setParameter("endDate", endDate)
            		.setParameter("threshold", Long.valueOf(threshold))
            		.getResultList();

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
        return ips;
    }

    /**
     * Delete the existing Student.
     * 
     * @param id
     */
    public void cleanUp() {
        // Create an EntityManager
        EntityManager manager = EntityManagerProvider.getEM();;
        EntityTransaction transaction = null;

        try {
            // Get a transaction
            transaction = manager.getTransaction();
            // Begin the transaction
            transaction.begin();

            // clean up with native queries .. still jpa compliant 
            manager.createNativeQuery("TRUNCATE access_log").executeUpdate();
            manager.createNativeQuery("DROP TABLE IF EXISTS access_log_filtered_copy").executeUpdate();

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
    
    public void copyFilterResults(Date startDate, Date endDate, int threshold) {

        List<String> ips = new ArrayList<>();

        // Create an EntityManager
        EntityManager manager = EntityManagerProvider.getEM();;
        EntityTransaction transaction = null;

        try {
            // Get a transaction
            transaction = manager.getTransaction();
            // Begin the transaction
            transaction.begin();

            // select distinct a.ip, count(a.id) from access_log a group by a.ip HAVING COUNT(a.id)>200;
            // select count(id) from access_log where ip='192.168.89.111';

            // Get a List of AccessLog
            // CREATE TABLE ab SELECT b.* FROM access_log b inner join 
            // ( select a.ip from access_log a  WHERE a.access_date >= '2017-01-01 13:00:00.0' 
            // and a.access_date < '2017-01-01 14:00:00.0' GROUP BY a.ip HAVING COUNT(DISTINCT a.id)> 1) c on b.ip=c.ip;


            
            StringBuffer q = new StringBuffer("CREATE TABLE access_log_filtered_copy ");
            q.append("SELECT b.* FROM access_log b inner join (select a.ip from access_log a ");
            q.append("WHERE a.access_date >= :startDate and a.access_date < :endDate ");
            q.append("GROUP BY a.ip HAVING COUNT(DISTINCT a.id)> :threshold ) c on b.ip=c.ip "
            		+ "where b.access_date >=:startDate and b.access_date < :endDate ");
            manager.createNativeQuery(q.toString())
            		.setParameter("startDate", startDate)
            		.setParameter("endDate", endDate)
            		.setParameter("threshold", Long.valueOf(threshold))
            		.executeUpdate();

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
    }

    /**
     * Update the existing AccessLog.
     * 
     * @param accessLog
     */
    public void merge(AccessLog accessLog) {
        // Create an EntityManager
        EntityManager manager = EntityManagerProvider.getEM();;
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

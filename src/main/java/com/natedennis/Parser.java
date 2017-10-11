package com.natedennis;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Hello world!
 *
 */
public class Parser {
	
    // Create an EntityManagerFactory when you start the application.
    public static final EntityManagerFactory ENTITY_MANAGER_FACTORY = Persistence
            .createEntityManagerFactory("Parser");
    
    
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
    }
}

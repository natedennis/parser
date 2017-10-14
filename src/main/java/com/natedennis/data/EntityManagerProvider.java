package com.natedennis.data;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityManagerProvider {

	private static final Logger logger = LoggerFactory.getLogger(EntityManagerProvider.class);

	private static final ThreadLocal<EntityManagerFactory> context = new ThreadLocal<EntityManagerFactory>() {
		@Override
		protected EntityManagerFactory initialValue() {
			logger.trace("set up the threadlocal entityManagerFactory default value");
			return Persistence.createEntityManagerFactory("Parser");
		}
	};

	public static void setEMF(EntityManagerFactory emf) {
		context.set(emf);
	}

	public static EntityManagerFactory getEMF() {
		EntityManagerFactory emf = context.get();
		return emf;
	}

	public static EntityManager getEM() {
		return context.get().createEntityManager();
	}

	public static void destory() {
		logger.trace("calling clean");
		context.remove();
	}

}

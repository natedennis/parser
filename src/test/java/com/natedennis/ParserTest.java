package com.natedennis;

import static org.junit.Assert.*;

import java.io.IOException;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.junit.Before;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.natedennis.data.EntityManagerProvider;
import com.natedennis.data.dao.AccessLogDAO;
import com.natedennis.data.enumeration.Duration;
import com.natedennis.util.ParseFileUtil;

import org.junit.*;

/**
 * Unit test for simple Parser.
 */
public class ParserTest {
	private final Logger logger = LoggerFactory.getLogger(ParserTest.class);

	private static final ParseFileUtil pfu = new ParseFileUtil();
	private static final AccessLogDAO dao = new AccessLogDAO();
	private static final String pattern = "yyyy-MM-dd.HH:mm:ss";// 2017-01-02.13:00:00

	
	@BeforeClass
	public static void runOnceBeforeClass() {
		EntityManagerProvider.setEMF(Persistence.createEntityManagerFactory("ParserJunit"));
	}

	@Before
	public void runBeforeEachMethod() {
		dao.cleanUp();
		String file = "src/test/resources/access.log";
		try {
			pfu.processAccessFile(file);
		} catch (IOException | ParseException e) {
			logger.error("error in parsing file: {} ",e);
		}
	}

	/**
	 * Rigourous Test :-)
	 * 
	 * @throws ParseException
	 * @throws IOException
	 */
	@Test
	public void testParserAccessLog() {
		// Create an EntityManager
		EntityManager manager = EntityManagerProvider.getEM();
		EntityTransaction transaction = null;
		Long count = 0L;
		transaction = manager.getTransaction();
		transaction.begin();
		count = (long) manager.createQuery("select count(a.id) from AccessLog a", Long.class).getSingleResult();
		transaction.commit();
		manager.close();
		assertTrue(count.equals(30L));
	}

	@Test
	public void testAggregationQueryHourTest1() {
		DateTime dateTime = DateTime.parse("2017-01-01.01:00:00", DateTimeFormat.forPattern(pattern));
		Date startDate = dateTime.toDate();
		DateTime endCal = new DateTime(startDate);
		Date endDate = endCal.plusHours(1).toDate();
		List<String> t = dao.threadHoldQuery(startDate, endDate, 2);
		assertTrue(t.size() == 1);
		assertTrue(t.get(0).equals("0.0.0.3"));
	}

	@Test
	public void testAggregationQueryHourTest2() {
		DateTime dateTime = DateTime.parse("2017-01-01.01:00:00", DateTimeFormat.forPattern(pattern));
		Date startDate = dateTime.toDate();
		DateTime endCal = new DateTime(startDate);
		Date endDate = endCal.plusHours(1).toDate();
		List<String> t = dao.threadHoldQuery(startDate, endDate, 0);
		assertTrue(t.size() == 3);
		assertTrue(t.contains("0.0.0.1") && t.contains("0.0.0.2") && t.contains("0.0.0.3"));
	}

	@Test
	public void testAggregationQueryHourTest3() {
		DateTime dateTime = DateTime.parse("2017-01-01.02:00:00", DateTimeFormat.forPattern(pattern));
		Date startDate = dateTime.toDate();
		DateTime endCal = new DateTime(startDate);
		Date endDate = endCal.plusHours(1).toDate();
		List<String> t = dao.threadHoldQuery(startDate, endDate, 0);
		assertTrue(t.size() == 0);
	}
	
	@Test
	public void testAggregationQueryDayTest1() {
		DateTime dateTime = DateTime.parse("2017-01-01.02:00:00", DateTimeFormat.forPattern(pattern));
		Date startDate = dateTime.toDate();
		DateTime endCal = new DateTime(startDate);
		Date endDate = endCal.plusDays(1).toDate();
		List<String> t = dao.threadHoldQuery(startDate, endDate, 2);
		assertTrue(t.size() == 1 && t.contains("0.0.0.3"));		
	}
	
	@Test
	public void testAggregationQueryDayTest2() {
		DateTime dateTime = DateTime.parse("2017-01-01.01:00:00", DateTimeFormat.forPattern(pattern));
		Date startDate = dateTime.toDate();
		DateTime endCal = new DateTime(startDate);
		Date endDate = endCal.plusDays(1).toDate();
		List<String> t = dao.threadHoldQuery(startDate, endDate, 2);
		assertTrue(t.size() == 1 && t.contains("0.0.0.3"));		
	}
	
	@Test
	public void testAggregationQueryDayTest3() {
		DateTime dateTime = DateTime.parse("2017-01-01.01:00:00", DateTimeFormat.forPattern(pattern));
		Date startDate = dateTime.toDate();
		DateTime endCal = new DateTime(startDate);
		Date endDate = endCal.plusDays(1).toDate();
		List<String> t = dao.threadHoldQuery(startDate, endDate, 1);
		assertTrue(t.size() == 2 &&  t.contains("0.0.0.2") && t.contains("0.0.0.3"));		
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void testCopyAccessLog() {
		EntityManager manager = EntityManagerProvider.getEM();
		EntityTransaction transaction = null;
		transaction = manager.getTransaction();
		transaction.begin();
		
		DateTime dateTime = DateTime.parse("2017-01-01.01:00:00", DateTimeFormat.forPattern(pattern));
		Date startDate = dateTime.toDate();
		DateTime endCal = new DateTime(startDate);
		Date endDate = endCal.plusDays(1).toDate();
		dao.copyFilterResults(startDate, endDate, 1);
		
		List<String> ips = manager.createNativeQuery("select distinct a.ip from access_log_filtered_copy a").getResultList();
		assertTrue(ips.size() == 2 &&  ips.contains("0.0.0.2") && ips.contains("0.0.0.3"));		

		Long countAccessLog = 
				(long) manager.createQuery("select count(a.id) from AccessLog a where a.ip in :ips "
						+ "and a.accessDate >= :startDate and a.accessDate < :endDate "
				)
				.setParameter("startDate", startDate)
				.setParameter("endDate", endDate)
				.setParameter("ips", ips)
				.getSingleResult();
		
		BigInteger countCopy = 
				 (BigInteger) manager.createNativeQuery("select count(a.id) from access_log_filtered_copy a"
				+ " where a.ip in (:ips)")
		.setParameter("ips",ips)
		.getSingleResult();
		
		
		transaction.commit();
		manager.close();
		assertTrue(countAccessLog.equals(countCopy.longValue()));
	}
	
}

package com.natedennis;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.natedennis.data.dao.AccessLogDAO;
import com.natedennis.data.enumeration.Duration;
import com.natedennis.util.ParseFileUtil;

/**
 * ﻿The goal is to write a parser in Java that parses web server access log
 * file, loads the log to MySQL and checks if a given IP makes more than a
 * certain number of requests for the given duration.
 */
public class Parser {
	

	private static final Logger logger = LoggerFactory.getLogger(Parser.class);

	public static void main(String[] args) {
		logger.info("Hello World!");
		try {
			String file = "target/classes/access.log";
			Date startDate;
			Date endDate;
			Duration duration;
			int threshold = 100;

			Options options = setupCLI();
			CommandLineParser parser = new DefaultParser();
			CommandLine cmdLine = parser.parse(options, args);

			if (cmdLine.hasOption("help")) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("sb server", options);
			} else {

				logger.debug("startDate : {}", cmdLine.hasOption("startDate")
						? ((String) cmdLine.getParsedOptionValue("startDate")) : "startDate not found");
				logger.debug("duration : {}", cmdLine.hasOption("duration")
						? ((String) cmdLine.getParsedOptionValue("duration")) : "duration not found");
				logger.debug("threshold : {}", cmdLine.hasOption("threshold")
						? ((Number) cmdLine.getParsedOptionValue("threshold")) : "threshold not found");

				if (cmdLine.hasOption("file")) {
					file = ((String) cmdLine.getParsedOptionValue("file"));
					if (!Files.exists(Paths.get(file))) {
						throw new IOException("File does not exist");
					}
				}

				// required 2017-01-02.13:00:00
				// format determined by the requirements document
				  String pattern = "yyyy-MM-dd.HH:mm:ss";
				  DateTime dateTime  = DateTime.parse((String) cmdLine.getParsedOptionValue("startDate"), 
				    		 DateTimeFormat.forPattern(pattern));;
				  startDate = dateTime.toDate();
//				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd.HH:mm:ss");
//				format.setTimeZone(TimeZone.getTimeZone( DateTimeZone.getDefault()));
//				startDate = format.parse(((String) cmdLine.getParsedOptionValue("startDate")));

				// required
				duration = Duration.valueOf(((String) cmdLine.getParsedOptionValue("duration")).toUpperCase());        
				
				DateTime endCal = new DateTime(startDate);

				if (duration.equals(Duration.HOURLY)) {
					endDate=endCal.plusHours(1).toDate();
				} else {
					endDate=endCal.plusDays(1).toDate();
				}

				// optional
				if (cmdLine.hasOption("threshold")) {
					threshold = ((Number) cmdLine.getParsedOptionValue("threshold")).intValue();
				}

				// TODO for larger projects should switch 
				// this to weld or openbeans and use cdi with apache delta spike
				// handles the file processing
				ParseFileUtil pfu = new ParseFileUtil();
				// dao for access log related stuff
				AccessLogDAO dao = new AccessLogDAO();

				logger.info("cleaning up any old data");
				dao.cleanUp();

				logger.info("processing access log");
				pfu.processAccessFile(file);
				
				logger.info("finding ips occuring more than {}, between the dates {} and {}",
						threshold, 
						startDate,
						endDate);

				List<String> ips = dao.threadHoldQuery(startDate, endDate, threshold);
				
				logger.info("******");
				ips.forEach(ip -> logger.info(ip));

				logger.info("copy records matching this criteria to access_log_filtered_copy");
				dao.copyFilterResults(startDate, endDate, threshold);
				logger.info("process complete");

			}

		} catch (ParseException | java.text.ParseException | IOException pe) {
			logger.error("exception:", pe);
		} finally {

		}
		System.exit(0);
	}

	/**
	 * set up cli options
	 * 
	 * @return
	 */
	private static Options setupCLI() {
		Option helpOption = Option.builder("h").longOpt("help").required(false).desc("shows this message").build();

		// default target/classes/access.log
		Option fileOption = Option.builder("f").longOpt("file").numberOfArgs(1).required(false).type(String.class)
				.desc("path to log file. default: target/classes/access.log").build();

		Option startDateOption = Option.builder("sD").longOpt("startDate").numberOfArgs(1).required(true)
				.type(String.class).desc("--startDate=2017-01-01.13:00:00  (start date of search) ").build();

		Option durationOption = Option.builder("dur").longOpt("duration").numberOfArgs(1).required(true)
				.type(String.class).desc("ex/ --duration=hourly (duration from startDate to search: hourly or daily)")
				.build();

		Option threshholdOption = Option.builder("th").longOpt("threshold").numberOfArgs(1).required(false)
				.type(Number.class)
				.desc("default 100 (a given IP makes more than this threshold number of requests for the given duration)")
				.build();

		Options options = new Options();
		options.addOption(helpOption);
		options.addOption(fileOption);
		options.addOption(startDateOption);
		options.addOption(durationOption);
		options.addOption(threshholdOption);

		return options;
	}
}

package com.natedennis;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.natedennis.data.dao.AccessLogDAO;
import com.natedennis.data.enumeration.Duration;

/**
 * Hello world!
 *
 */
public class Parser {

	// Create an EntityManagerFactory when you start the application.
	public static final EntityManagerFactory ENTITY_MANAGER_FACTORY = Persistence.createEntityManagerFactory("Parser");

	private static final Logger logger = LoggerFactory.getLogger(Parser.class);

	public static void main(String[] args) {
		logger.info("Hello World!");

		try {
			String file ="target/classes/access.log";
			Date startDate;
			Duration duration;
			int threshold = 100;

			
			Option helpOption = Option.builder("h").longOpt("help").required(false).desc("shows this message").build();

			//default target/classes/access.log
			Option fileOption = Option.builder("f").longOpt("file").numberOfArgs(1).required(false)
					.type(String.class)
					.desc("path to log file. default: target/classes/access.log").build();
			
			Option startDateOption = Option.builder("sD").longOpt("startDate").numberOfArgs(1).required(true)
					.type(String.class).desc("--startDate=2017-01-01.13:00:00  (start date of search) ").build();

			Option durationOption = Option.builder("dur").longOpt("duration").numberOfArgs(1).required(true)
					.type(String.class)
					.desc("ex/ --duration=hourly (duration from startDate to search: hourly or daily)").build();

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

			CommandLineParser parser = new DefaultParser();
			CommandLine cmdLine = parser.parse(options, args);

			if (cmdLine.hasOption("help")) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("sb server", options);
			} else {
				
				
				logger.info("startDate : {}", cmdLine.hasOption("startDate")?((String) cmdLine.getParsedOptionValue("startDate")):"startDate not found");
				logger.info("duration : {}", cmdLine.hasOption("duration")?((String) cmdLine.getParsedOptionValue("duration")):"duration not found");
				logger.info("threshold : {}", cmdLine.hasOption("threshold")?((Number) cmdLine.getParsedOptionValue("threshold")):"threshold not found");
				
				if(cmdLine.hasOption("file")) {
					file=((String) cmdLine.getParsedOptionValue("file"));
					if(!Files.exists(Paths.get(file))) { 
					    throw new IOException("File does not exist");
					}					
				}
				
				//required 2017-01-02.13:00:00
				startDate = new SimpleDateFormat("yyyy-MM-dd.HH:mm:ss").parse( ((String) cmdLine.getParsedOptionValue("startDate")) );  

			    //required
				duration = Duration.valueOf( ((String) cmdLine.getParsedOptionValue("duration")).toUpperCase());

				//optional
				if(cmdLine.hasOption("threshold")) {
					threshold=((Number) cmdLine.getParsedOptionValue("threshold")).intValue();
				}
				
				
				//handle the file processing
				ParseFileUtil pfu = new ParseFileUtil();
				pfu.processAccessFile(file);
				
				
			}
			

		} catch (ParseException | java.text.ParseException | IOException pe) {
			logger.error("exception:", pe);
		} finally {

		}
		System.exit(0);
	}
}

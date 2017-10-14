package com.natedennis.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.natedennis.data.dao.AccessLogDAO;
import com.natedennis.data.domain.AccessLog;

public class ParseFileUtil {

	private final static Charset ENCODING = StandardCharsets.UTF_8;

	private final Logger logger = LoggerFactory.getLogger(ParseFileUtil.class);

	private final int PAGE_SIZE = 999;

	public void processAccessFile(String inputFilePath) throws IOException, ParseException {
		List<AccessLog> inputList = new ArrayList<>();
		AccessLogDAO accessLogDAO = new AccessLogDAO();
		try (Scanner scanner = new Scanner(Paths.get(inputFilePath), ENCODING.name())) {
			while (scanner.hasNextLine()) {
				inputList.add(processLine(scanner.nextLine()));

				if (inputList.size() > PAGE_SIZE) {
					accessLogDAO.bulkPersist(inputList, PAGE_SIZE / 2);
					inputList = new ArrayList<>();
				}
			}
			if (inputList.size() > 0) {
				accessLogDAO.bulkPersist(inputList, PAGE_SIZE / 2);
				inputList = new ArrayList<>();
			}
		}
	}

	protected AccessLog processLine(String aLine) throws ParseException {
		// use a second Scanner to parse the content of each line
		Scanner scanner = new Scanner(aLine);
		scanner.useDelimiter("\\|");
		AccessLog a = new AccessLog();
		if (scanner.hasNext()) {
			String stringDate = scanner.next();
			a.setAccessDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse(((String) stringDate)));
		} else {
			logger.error("Empty or invalid line. Unable to process.");
		}
//TODO dont assume the line will be perfect
		if (scanner.hasNext()) {
			a.setIp(scanner.next());
		}
		if (scanner.hasNext()) {
			a.setRequest(scanner.next());
		}
		if (scanner.hasNext()) {
			a.setStatus(Integer.parseInt(scanner.next()));
		}
		if (scanner.hasNext()) {
			a.setUserAgent(scanner.next());
		}
		scanner.close();
		return a;
	}

}

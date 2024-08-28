package com.distocraft.dc5000.etl.gui.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.distocraft.dc5000.etl.gui.etl.FRHLogLineDetail;

public class FRHLogBrowser {

	private File logDir;

	private final SimpleDateFormat logSDF;

	private final SimpleDateFormat urlSDF;

	private final SimpleDateFormat yearSDF;

	private final SimpleDateFormat simpleSDF;

	private BufferedReader pivot_br = null;

	private long pivot_br_position = -1;
	
	private final String FLOW_LOG_DIR="/eniq/log/flow/";

	private final Log log = LogFactory.getLog(this.getClass()); // general
																// logger

	public FRHLogBrowser(final String path) {
		logDir = new File(path);
		logSDF = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
		urlSDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		yearSDF = new SimpleDateFormat("yyyy");
		simpleSDF = new SimpleDateFormat("yyyy-MM-dd");
	}

	public List<FRHLogLineDetail> parseFRHFolder(final String sstartTime, final String sendTime, final String techPack)
			throws LogException {
		try {

			log.debug("Parsing folder " + logDir);
			
			final Date startTime = urlSDF.parse(sstartTime);
			final Date endTime = urlSDF.parse(sendTime);

			// Add 2 second extra time for the endTime because the action
			// execution
			// may stop before the set execution ending is logged.
			final long extraTime = 2000; // 2000 = 2 seconds.
			endTime.setTime(endTime.getTime() + extraTime);
			// filter log files to archive
			FilenameFilter fileNameFilter = new PrefixFilter("controller");
			File[] allfiles = logDir.listFiles(fileNameFilter);
			Arrays.sort(allfiles, new Comparator<File>() {
				@Override
				public int compare(final File f1, final File f2) {
					return f1.getName().compareTo(f2.getName());
				}
			});

			final List<FRHLogLineDetail> parsedlist = new LinkedList<FRHLogLineDetail>();
			int matchedFiles = 0;

			for (int i = 0; i < allfiles.length; i++) {

				log.debug("Iterating at " + allfiles[i].getPath());

				final String fileName = allfiles[i].getName();

				try {
					String fileDate = getFileDate(fileName);
					if (!fileDate.equals("")) {
						fileDate = fileDate;
					} else {
						fileDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
					}
					final Date logFileDate = simpleSDF.parse(fileDate);
					final String logFileYear = yearSDF.format(logFileDate);
					final Date logFileStartTime = urlSDF.parse(simpleSDF.format(logFileDate) + " 00:00:00.000");
					final Date logFileEndTime = urlSDF.parse(simpleSDF.format(logFileDate) + " 23:59:59.999");
					final long logFileStart = logFileStartTime.getTime();
					final long logFileEnd = logFileEndTime.getTime();
					final long start = startTime.getTime();
					final long end = endTime.getTime();

					if ((logFileStart <= start && logFileEnd >= start) || (logFileStart <= end && logFileEnd >= end)
							|| (logFileStart >= start && logFileEnd <= end)) {

						matchedFiles++;

						log.debug("Reading log file: " + allfiles[i]);
						String id = getSessionId(allfiles[i], startTime, endTime, techPack);
						if (id != null) {
							logDir = new File(FLOW_LOG_DIR);
							String regex = "([^\\s]*)-([^\\s]*)";
							File[] flowFiles = listFilesMatching(logDir, regex);
							for (int j = 0; j < flowFiles.length; j++) {
								try {
									log.debug("iterating at flow log: "+flowFiles[j]);
									final List<FRHLogLineDetail> result = parseLogFile(flowFiles[j], logFileYear,
											logFileStartTime, startTime, endTime, techPack, id);
									parsedlist.addAll(sortListContents(result));
								} catch (Exception e) {
									log.warn("File " + flowFiles[j].getName() + " reading failed exceptionally.", e);
								}
							}
						}
					}

				} catch (Exception e) {
					log.warn("Mallformed logfile name " + fileName);
				}

			}

			if (matchedFiles <= 0) {
				throw new LogException("No matching log files found.");
			} else {
				log.info(matchedFiles + " matching log files found.");
			}
			return parsedlist;

		} catch (Exception e) {
			String specific_error = "Logs cannot be displayed in AdminUI during  set execution. Please see the logs in : "
					+ logDir.toString() + " .";
			throw new LogException("Log parsing failed exceptionally." + specific_error + ".");
		}
	}

	static class PrefixFilter implements FilenameFilter {
		private final String extension;

		public PrefixFilter(final String extension) {
			this.extension = extension;
		}

		@Override
		public boolean accept(File dir, String name) {
			// TODO Auto-generated method stub
			return name.startsWith(extension);
		}

	}

	public static File[] listFilesMatching(File root, String regex) {
		if (!root.isDirectory()) {
			throw new IllegalArgumentException(root + " is no directory.");
		}
		final Pattern p = Pattern.compile(regex); // careful: could also throw
													// an exception!
		return root.listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				// TODO Auto-generated method stub
				return p.matcher(pathname.getName()).matches();
			}
		});
	}

	private List<FRHLogLineDetail> parseLogFile(final File f, final String logFileYear, final Date fileStart,
			final Date startTime, final Date endTime, final String techPack, String sessionId)
			throws IOException, LogException {
		final List<FRHLogLineDetail> list = new LinkedList<FRHLogLineDetail>();

		FRHLogLineDetail lld = null;

		BufferedReader br = null;

		int count = 0;
		try {

			final long fileSize = f.length() + 1;
			long startPosition = 0;

			if (startTime.getTime() > fileStart.getTime()) {

				int steps = new Double((Math.log(5 / ((double) fileSize / (double) 1048576)) / Math.log(0.5)))
						.intValue();
				if (steps < 0) {
					steps = 0;
				}
				log.debug("File size is " + fileSize + " -> " + steps + " pivoting steps to be performed.");

				final long now = System.currentTimeMillis();

				final Map<String, Object> hm = new HashMap<String, Object>();
				hm.put("file", f);
				hm.put("targetPoint", startTime);
				hm.put("startPoint", fileStart);
				hm.put("startPosition", new Long(0));
				hm.put("endPoint", endTime);
				hm.put("endPosition", new Long((long) f.length()));

				try {
					for (int i = 0; i < steps; i++) {
						searchStartPosition(hm, logFileYear);
					}
				} finally {
					try {
						if (pivot_br != null) {
							pivot_br.close();
							pivot_br_position = -1;
						}
					} catch (Exception e) {
					}
				}

				log.debug("File pivoted " + steps + " times in " + (System.currentTimeMillis() - now) + " ms");

				startPosition = ((Long) hm.get("startPosition")).longValue();

			} else {
				log.debug("Pivoting cancelled. " + startTime.getTime() + " < " + fileStart.getTime());
			}

			final long now = System.currentTimeMillis();

			br = new BufferedReader(new FileReader(f));

			br.skip(startPosition);

			int linesread = 0;
			int linesaccepted = 0;

			String line = readLine(br);

			FRHLogLineDetail previousline = null;

			do {

				linesread++;

				if (startsWithTimestamp(line)) { // Start of log message

					if (previousline != null) {
						previousline.setMessage(previousline.getMessage().replaceAll("\\n", "<br>"));
						previousline.setMessage(previousline.getMessage().replaceAll("\\s", "&nbsp;"));

						list.add(previousline);
						previousline = null;
					}

					lld = parseDetails(line);
					log.debug("gotDetails: " + lld.getMessage());
					count++;

					if (lld == null) {
						continue;
					}
					if (lld.getLogTime().getTime() > endTime.getTime()) {
						break;
					}

					if (filter(lld, startTime, endTime, techPack)) {
						if ((line.contains(" " + techPack + " ") || line.contains("_" + techPack + " ")
								|| line.contains("_" + techPack + "_") || line.contains("_" + techPack + "-"))
								&& line.contains(" "+sessionId+" ")) {
							linesaccepted++;
							previousline = lld;
						}
					}

				} else { // Not start of log message

					if (previousline != null) {
						linesaccepted++;
						previousline.setMessage(previousline.getMessage() + "\n" + line);
					}

				}

			} while ((line = readLine(br)) != null);

			if (previousline != null) {
				previousline.setMessage(previousline.getMessage().replaceAll("\\n", "<br />"));
				previousline.setMessage(previousline.getMessage().replaceAll("\\s", "&nbsp;"));
				list.add(previousline);
			}

			log.info("File " + f.getName() + " successfully read in " + (System.currentTimeMillis() - now) + " ms. "
					+ linesaccepted + "/" + linesread + " lines accepted.");

		} catch (IOException e) {
			throw e;
		} finally {
			try {
				br.close();
			} catch (Exception e) {
			}
		}
		return list;
	}

	private void searchStartPosition(final Map<String, Object> hm, final String logFileYear) throws LogException {

		log.debug("Pivot start: " + ((Long) hm.get("startPosition")).longValue() + " .. "
				+ ((Long) hm.get("endPosition")).longValue());

		final long sspstart = System.currentTimeMillis();

		final long position = ((((Long) hm.get("endPosition")).longValue()
				- ((Long) hm.get("startPosition")).longValue()) / 2) + ((Long) hm.get("startPosition")).longValue();

		try {

			if (pivot_br == null || pivot_br_position > position) {
				pivot_br = new BufferedReader(new FileReader((File) hm.get("file")));
				pivot_br_position = 0;
			}

			final long preskip = System.currentTimeMillis();
			pivot_br.skip(position - pivot_br_position);
			pivot_br_position = position;
			log.info("Skip to " + position + " took " + (System.currentTimeMillis() - preskip) + " ms");

			final Date mid_date = parseTime(readPivotLine(), logFileYear);

			log.debug("Middle " + position + " " + logSDF.format(mid_date));

			if (mid_date.getTime() > ((Date) hm.get("targetPoint")).getTime()) {
				hm.put("endPosition", new Long(position));
				hm.put("endPoint", mid_date);
			} else {
				hm.put("startPosition", new Long(position));
				hm.put("startPoint", mid_date);

			}

			log.debug("Pivot end: " + ((Long) hm.get("startPosition")).longValue() + " .. "
					+ ((Long) hm.get("endPosition")).longValue());

		} catch (Exception e) {
			log.warn("File reading failed exceptionally.", e);
			throw new LogException("Reading log file failed exceptionally.");
		}

		log.info("Pivot step completed in " + (System.currentTimeMillis() - sspstart) + " ms");

	}

	public String getFileDate(String fileName) {
		String fileDate = fileName.substring(fileName.lastIndexOf("_") + 1, fileName.length());
		String parsePattern = "\\d{4}-\\d{2}-\\d{2}";

		Pattern pattern = Pattern.compile(parsePattern);
		Matcher match = pattern.matcher(fileDate);

		if (match.matches()) {
			fileDate = fileDate;
		} else {
			fileDate = "";
		}
		return fileDate;
	}

	public class LogException extends Exception {

		private static final long serialVersionUID = 1L;

		public LogException(final String msg) {
			super(msg);
		}
	};

	private String readPivotLine() {

		String line = null;
		while (true) {
			try {
				line = pivot_br.readLine();
			} catch (IOException e) {
				return null;
			}
			pivot_br_position += (line.length() + 2);
			if (startsWithTimestamp(line)) {
				break;
			}
		}
		return line;
	}

	private boolean startsWithTimestamp(final String line) {

		final String timestamppPattern = "\\d{2}.\\d{2}.\\d{4} \\d{2}:\\d{2}:\\d{2} .+";

		final Pattern parsePattern = Pattern.compile(timestamppPattern);
		final Matcher logLine = parsePattern.matcher(line);

		if (logLine.matches()) {
			return true;
		}
		return false;
	}

	/**
	 * Reads timestamp from the beginning of the line.
	 * 
	 * @throws ParseException
	 */
	private Date parseTime(final String line, final String logFileYear) throws ParseException {
		final int firstspace = line.indexOf(" ");
		final int secondspace = line.indexOf(" ", firstspace + 1);

		return logSDF.parse(logFileYear + "." + line.substring(0, secondspace));
	}

	private String readLine(final BufferedReader br) throws IOException {

		String line = null;

		line = br.readLine();

		return line;
	}

	protected FRHLogLineDetail parseDetails(String line) {
		// line = year + "." + line;

		try {

			final FRHLogLineDetail lld = new FRHLogLineDetail();

			// The line details can be:
			// Line example:
			// 2006.16.02 16:38:51 24 INFO
			// etl.DC_E_CPP.Install.DWHM_Install_DC_E_CPP.0.dwhm.VersionUpdate :
			// Executing for techpack DC_E_CPP

			int currentIndex = line.indexOf(" ");
			currentIndex = line.indexOf(" ", currentIndex + 1);
			String time = line.substring(0, currentIndex);
			lld.setLogTime(logSDF.parse(time));
			currentIndex = line.indexOf(" ", currentIndex + 1);
			int logLevelIndex = currentIndex + 1;
			currentIndex = line.indexOf(" ", currentIndex + 1);
			lld.setLogLevel(line.substring(logLevelIndex, currentIndex));
			int logDetailsIndex = currentIndex + 1;
			int details = line.indexOf("-", logDetailsIndex);
			String det = line.substring(details + 1, line.length());
			lld.setMessage(det);
			return lld;

		} catch (Exception e) {
			log.warn("Parsing: \"" + line + "\" failed", e);
			return null;
		}
	}

	private boolean filter(final FRHLogLineDetail lld, final Date startTime, final Date endTime,
			final String techPack) {
		if (lld == null) {
			return false;
		}
		if (lld.getLogTime().getTime() < startTime.getTime()) {
			return false;
		}

		if (lld.getLogTime().getTime() > endTime.getTime()) {
			return false;
		}
		return true;

	}

	protected List<FRHLogLineDetail> sortListContents(final List<FRHLogLineDetail> result) {
		Collections.sort(result, new Comparator<FRHLogLineDetail>() {

			@Override
			public int compare(final FRHLogLineDetail st1, final FRHLogLineDetail st2) {
				int returnval = 0;
				if (st1.getLogTime().getTime() > st2.getLogTime().getTime()) {
					returnval = 1;
				} else if (st1.getLogTime().getTime() < st2.getLogTime().getTime()) {
					returnval = -1;
				}

				return returnval;
			}
		});
		log.debug("sortListContents.size before returning=" + result.size());
		return result;
	}

	String getSessionId(File f, Date sTime, Date eTime, String tp) {
		String s = null;
		String result = null;
		String[] elements;
		try {
		String ssTime=logSDF.format(sTime);
		String esTime=logSDF.format(eTime);
		final String[] command = { "/bin/sh", "-c",
				"cat " + f + " |  awk '$0 >= \"" + ssTime + "\" && $0 <=\"" + esTime + "\"' | grep \"Session Id\" " };
		for (String x : command) {
			log.debug("command=" + x);
		}
		Process process;
		
			process = Runtime.getRuntime().exec(command);
			process.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

			while ((s = reader.readLine()) != null) {
				result = s;
				if (result.contains(": Session Id:")&&result.contains(tp+".")) {
					elements = result.split(":");
					result = elements[elements.length - 1].trim();
					break;
				} 
			}
		} catch (IOException e) {
			log.warn("IOException");

		} catch (InterruptedException e) {
			log.warn("InterruptedException");

		} catch (Exception e) {
			log.warn("Exception");
		}
		return result;
	}

}
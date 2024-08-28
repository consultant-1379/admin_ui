package com.distocraft.dc5000.etl.gui.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
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

import com.distocraft.dc5000.etl.gui.etl.LogLineDetail;

/**
 * Copyright &copy; Ericsson ltd. All rights reserved.<br>
 * File browser for log browsing. Class is used for DC5000 log browsing and reading lines with certain time and name
 * pairs.
 * 
 * @author Jaakko Melantie
 * @author Janne Berggren
 * @author Tuomas Lemminkainen
 */

public class LogBrowser { // NOPMD by eheijun on 03/06/11 08:39

  private final File logDir;

  private final SimpleDateFormat logSDF;

  private final SimpleDateFormat fileSDF;

  private final SimpleDateFormat urlSDF;

  private final SimpleDateFormat yearSDF;

  private final SimpleDateFormat simpleSDF;

  private BufferedReader pivot_br = null;

  private long pivot_br_position = -1;

  private final Log log = LogFactory.getLog(this.getClass()); // general logger

  /**
   * Constructs new instance for specified path.
   * 
   * @param path
   */
  public LogBrowser(final String path) {
    logDir = new File(path);

    logSDF = new SimpleDateFormat("yyyy.dd.MM HH:mm:ss");
    fileSDF = new SimpleDateFormat("yyyy_MM_dd");
    urlSDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    yearSDF = new SimpleDateFormat("yyyy");
    simpleSDF = new SimpleDateFormat("yyyy-MM-dd");

  }

  /**
   * Reads log files in a folder.
   * 
   * Start- and end time are in following format: yyyy-mm-dd hh:mm:ss.ms
   * 
   * @return list that contains rows from file
   * @exception LogException
   *              is thrown if parsoing failed
   */
  public List<LogLineDetail> parseFolder(final String sstartTime, final String sendTime, final String setName, // NOPMD by eheijun on 03/06/11 08:39
      final String techPack, final String setType) throws LogException {

    final long pfnow = System.currentTimeMillis();

    try {

      log.info("Parsing folder " + logDir);

      final Date startTime = urlSDF.parse(sstartTime);
      final Date endTime = urlSDF.parse(sendTime);

      // Add 2 second extra time for the endTime because the action execution
      // may stop before the set execution ending is logged.
      final long extraTime = 2000; // 2000 = 2 seconds.
      endTime.setTime(endTime.getTime() + extraTime);

      // filter log files to archive
      final FilenameFilter fileNameFilter = new SuffixFilter("log");
      final File[] allfiles = logDir.listFiles(fileNameFilter);

      Arrays.sort(allfiles, new Comparator<File>() {

        @Override
        public int compare(final File f1, final File f2) {
          return f1.getName().compareTo(f2.getName());
        }
      });

      // File list is sorted now

      final List<LogLineDetail> parsedlist = new LinkedList<LogLineDetail>();
      int matchedFiles = 0;

      for (int i = 0; i < allfiles.length; i++) {

        log.debug("Iterating at " + allfiles[i].getPath());

        final String fileName = allfiles[i].getName();

        try {
          final String fileDate = fileName.substring(fileName.lastIndexOf("-") + 1, fileName.lastIndexOf("."));
          final Date logFileDate = fileSDF.parse(fileDate);
          final String logFileYear = yearSDF.format(logFileDate);
          final Date logFileStartTime = urlSDF.parse(simpleSDF.format(logFileDate) + " 00:00:00");
          final Date logFileEndTime = urlSDF.parse(simpleSDF.format(logFileDate) + " 23:59:59");
          final long logFileStart = logFileStartTime.getTime();
          final long logFileEnd = logFileEndTime.getTime();
          final long start = startTime.getTime();
          final long end = endTime.getTime();

          if ((logFileStart <= start && logFileEnd >= start) || (logFileStart <= end && logFileEnd >= end)
              || (logFileStart >= start && logFileEnd <= end)) {

            matchedFiles++;

            log.info("Reading log file: " + allfiles[i]);

            try {
              final List<LogLineDetail> result = parseLogFile(allfiles[i], logFileYear, logFileStartTime, startTime,
                  endTime, setName, techPack, setType);
              parsedlist.addAll(sortListContents(result));
            } catch (Exception e) {
              log.warn("File " + allfiles[i].getName() + " reading failed exceptionally.", e);
              throw new LogException("Reading log file " + allfiles[i].getName() + " failed exceptionally.");
            }

          }

        } catch (Exception e) {
          log.warn("Mallformed logfile name " + fileName);
        }

      } // foreach file in logDir

      if (matchedFiles <= 0) {
        log.info("No matching log files found. Took " + (System.currentTimeMillis() - pfnow) + " ms");
        throw new LogException("No matching log files found. Took " + (System.currentTimeMillis() - pfnow) + " ms");
      } else {
        log.info(matchedFiles + " matching log files found. Took " + (System.currentTimeMillis() - pfnow) + " ms");
      }

      log.info("ParseFolder finished in");

      return parsedlist;

    } catch (Exception e) {
      String specific_error = "Logs cannot be displayed in AdminUI during  set execution. Please see the logs in : " + logDir.toString() + " .";
      throw new LogException("Log parsing failed exceptionally."+ specific_error +". Took " + (System.currentTimeMillis() - pfnow) + " ms");
    }

  }

  /**
   * filter log files before archiving
   */
  static class SuffixFilter implements FilenameFilter {

    private final String extension;

    public SuffixFilter(final String extension) {
      this.extension = extension;
    }

    @Override
    public boolean accept(final File dir, final String name) {
      return name.endsWith('.' + extension);
    }
  }

  /**
   * Reads a log file and returns list of relevant LogLineDetails.
   * 
   * @throws IOException
   *           is thrown if parsing of the file fails with IOError.
   * @throws LogException
   *           is thrown if parsing of the file fails.
   */
  private List<LogLineDetail> parseLogFile(final File f, final String logFileYear, final Date fileStart, // NOPMD by eheijun on 03/06/11 08:39
      final Date startTime, final Date endTime, final String setName, final String techPack, final String type)
      throws IOException, LogException {

    final List<LogLineDetail> list = new LinkedList<LogLineDetail>();

    BufferedReader br = null;

    try {

      final long fileSize = f.length() + 1;

      long startPosition = 0;

      if (startTime.getTime() > fileStart.getTime()) {

        int steps = new Double((Math.log(5 / ((double) fileSize / (double) 1048576)) / Math.log(0.5))).intValue();

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

        log.info("File pivoted " + steps + " times in " + (System.currentTimeMillis() - now) + " ms");

        startPosition = ((Long) hm.get("startPosition")).longValue();

      } else {
        log.info("Pivoting cancelled. " + startTime.getTime() + " < " + fileStart.getTime());
      }

      final long now = System.currentTimeMillis();

      br = new BufferedReader(new FileReader(f));

      br.skip(startPosition);

      int linesread = 0;
      int linesaccepted = 0;

      String line = readLine(br);

      LogLineDetail previousline = null;

      do {

        linesread++;

        if (startsWithTimestamp(line)) { // Start of log message

          if (previousline != null) {
            previousline.setMessage(previousline.getMessage().replaceAll("\\n", "<br>"));
            previousline.setMessage(previousline.getMessage().replaceAll("\\s", "&nbsp;"));

            list.add(previousline);
            previousline = null;
          }

          final LogLineDetail lld = parseDetails(line, logFileYear);

          if (lld == null) {
            continue;
          }
          if (lld.getLogTime().getTime() > endTime.getTime()) {
            break;
          }

          if (filter(lld, startTime, endTime, setName, techPack, type)) {
            linesaccepted++;
            previousline = lld;
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

  /**
   * Checks that log line meets all filter conditions.
   * 
   * @return true if meets false otherwise.
   */
  private boolean filter(final LogLineDetail lld, final Date startTime, final Date endTime, final String setName,
      final String techPack, final String type) {

    /*
     * log.debug("LogTime: " + urlSDF.format(lld.getLogTime()) + " ? [ " + urlSDF.format(startTime) + " ... " +
     * urlSDF.format(endTime)); log.debug("TechPack: " + lld.getTechPack() + " ? " + techPack); log.debug("SetName: = "
     * + lld.getSetName() + " ? " + setName); log.debug("SetType: = " + lld.getSetType() + " ? " + type);
     */

    if (lld == null) {
      return false;
    }
    if (lld.getLogTime().getTime() < startTime.getTime()) {
      return false;
    }

    if (lld.getLogTime().getTime() > endTime.getTime()) {
      return false;
    }

    if (!lld.getTechPack().equals(techPack)) {
      return false;
    }

    if (!lld.getSetName().equals(setName)) {
      return false;
    }

    if (!lld.getSetType().equals(type)) {
      return false;
    }

    return true;

  }

  /**
   * Hash content (File file, Date targetPoint, Date startPoint, Integer startPosition, Date endPoint, Integer
   * endPosition)
   * 
   * @throws LogException
   */
  private void searchStartPosition(final Map<String, Object> hm, final String logFileYear) throws LogException {

    log.debug("Pivot start: " + ((Long) hm.get("startPosition")).longValue() + " .. "
        + ((Long) hm.get("endPosition")).longValue());

    final long sspstart = System.currentTimeMillis();

    final long position = ((((Long) hm.get("endPosition")).longValue() - ((Long) hm.get("startPosition")).longValue()) / 2)
        + ((Long) hm.get("startPosition")).longValue();

    try {

      if (pivot_br == null || pivot_br_position > position) {
        pivot_br = new BufferedReader(new FileReader((File) hm.get("file")));
        pivot_br_position = 0;
      }

      final long preskip = System.currentTimeMillis();
      pivot_br.skip(position - pivot_br_position);
      pivot_br_position = position;
      log.debug("Skip to " + position + " took " + (System.currentTimeMillis() - preskip) + " ms");

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

    log.debug("Pivot step completed in " + (System.currentTimeMillis() - sspstart) + " ms");

  }

  /**
   * Reads one line for pivoting
   */
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

  /**
   * Reads next log line from reader. Skips all non timestamp starting lines. Used for searching times in the middle of
   * the file.
   */
  private String readLine(final BufferedReader br) throws IOException {

    String line = null;

    line = br.readLine();

    return line;
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

  /**
   * Reads log details from a line
   * 
   * @param line
   *          String to be parsed
   * @return LogLineDetail object for the line
   * @exception Exception
   *              is thrown if line is mallformed
   */
  protected LogLineDetail parseDetails(String line, final String year) {
    line = year + "." + line;

    try {

      final LogLineDetail lld = new LogLineDetail();

      // The line details can be:
      // Line example 1:
      // 2006.16.02 16:38:51 24 INFO
      // etl.DC_E_CPP.Install.DWHM_Install_DC_E_CPP.0.dwhm.VersionUpdate :
      // Executing for techpack DC_E_CPP
      // Or it can be line example 2:
      // 04.04 12:36:35 12 INFO
      // etl.DC_E_RBS.Support.DWHM_StorageTimeUpdate_DC_E_RBS : Logged failed
      // set execution

      // log.debug("line = " + line);

      int currentIndex = line.indexOf(" ");
      currentIndex = line.indexOf(" ", currentIndex + 1);
      // log.debug("logtime = " + logSDF.parse(line.substring(0,
      // currentIndex)));
      lld.setLogTime(logSDF.parse(line.substring(0, currentIndex))); // 2006.16.02
      // 16:38:46
      // =
      // logtime

      currentIndex = line.indexOf(" ", currentIndex + 1); // 23 = thread id.
      final int logLevelStartIndex = currentIndex + 1;
      currentIndex = line.indexOf(" ", currentIndex + 1); // INFO = loglevel
      // log.debug("loglevel = " + line.substring(logLevelStartIndex,
      // currentIndex));
      lld.setLogLevel(line.substring(logLevelStartIndex, currentIndex));

      final int typeDetailsStartIndex = currentIndex + 1;
      final int doubleDotIndex = line.indexOf(":", typeDetailsStartIndex);
      // log.debug("temp = " + line.substring(typeDetailsStartIndex,
      // doubleDotIndex));
      final String[] typeDetailsArray = line.substring(typeDetailsStartIndex, doubleDotIndex).split("\\.");

      // log.debug("Techpack = " + typeDetailsArray[1]);
      lld.setTechPack(typeDetailsArray[1]); // DC_E_CPP

      // log.debug("SetType = " + typeDetailsArray[2]);
      lld.setSetType(typeDetailsArray[2]); // Install

      // log.debug("SetName = " + typeDetailsArray[3].trim());
      lld.setSetName(typeDetailsArray[3].trim()); // DWHM_Install_DC_E_CPP

      if (typeDetailsArray.length > 4) {
        String typeDetailsString = "";

        for (int i = 4; i < typeDetailsArray.length; i++) {
          typeDetailsString += typeDetailsArray[i].trim() + ".";
        }

        final String typeDetails = typeDetailsString.substring(0, typeDetailsString.length() - 1);

        lld.setTypeDetails(typeDetails);
      }

      lld.setMessage(line.substring(doubleDotIndex + 2, line.length()));

      return lld;

    } catch (Exception e) {
      log.info("Parsing: \"" + line + "\" failed", e);
      return null;
    }
  }

  /**
   * Checks if specified line starts with timestamp
   * 
   * @param line
   * @return true if starts with timestamp false otherwise
   */
  private boolean startsWithTimestamp(final String line) {

    final String timestamppPattern = "\\d\\d\\.\\d\\d \\d\\d\\:\\d\\d\\:\\d\\d .+";

    final Pattern parsePattern = Pattern.compile(timestamppPattern);
    final Matcher logLine = parsePattern.matcher(line); // get a matcher object

    if (logLine.matches()) {
      return true;
    }
    return false;
  }

  public class LogException extends Exception {

    private static final long serialVersionUID = 1L;

    public LogException(final String msg) {
      super(msg);
    }
  };

  /**
   * Reads a license logfile.
   * 
   * @param logfile
   *          is a File object to the license logfile to be parsed through
   * @return Returns a List of LoginLineDetails from the logfile.
   * @throws IOException
   */
  public List<LogLineDetail> parseLicenseLogFile(final File logfile) throws IOException {

    final List<LogLineDetail> list = new LinkedList<LogLineDetail>();

    BufferedReader br = null;

    try {

      final long now = System.currentTimeMillis();

      br = new BufferedReader(new FileReader(logfile));

      int linesread = 0;
      int linesaccepted = 0;

      String line = readLine(br);

      LogLineDetail previousline = null;

      do {

        linesread++;

        if (startsWithTimestamp(line)) { // Start of log message

          if (previousline != null) {
            previousline.setMessage(previousline.getMessage().replaceAll("\\n", "<br>"));
            previousline.setMessage(previousline.getMessage().replaceAll("\\s", "&nbsp;"));

            list.add(previousline);
            previousline = null;
          }

          final String year = logfile.getName().substring((logfile.getName().indexOf("-") + 1),
              (logfile.getName().indexOf("-") + 5));

          final LogLineDetail lld = parseLicenseDetails(line, year);

          if (lld == null) {
            log.info("Parsing line details failed.");
            continue;
          }

          previousline = lld;

        } else { // Not start of log message

          log.info("Line not accepted because doesn't start with timestamp.");

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

      log.info("File " + logfile.getName() + " successfully read in " + (System.currentTimeMillis() - now) + " ms. "
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

  /**
   * Reads license log details from a line
   * 
   * @param line
   *          String to be parsed
   * @return LogLineDetail object for the line
   * @exception Exception
   *              is thrown if line is mallformed
   */
  private LogLineDetail parseLicenseDetails(String line, final String year) {
    line = year + "." + line;

    try {

      final LogLineDetail lld = new LogLineDetail();

      // The line details can be:
      // Line example 1:
      // 10.10 13:21:31 13 INFO licensing.cache.DefaultLicensingCache : License manager cache updated

      int currentIndex = line.indexOf(" ");
      currentIndex = line.indexOf(" ", currentIndex + 1);

      lld.setLogTime(logSDF.parse(line.substring(0, currentIndex))); // 2006.16.02 16:38:46 =
      // logtime

      currentIndex = line.indexOf(" ", currentIndex + 1); // 23 = thread id.
      final int logLevelStartIndex = currentIndex + 1;
      currentIndex = line.indexOf(" ", currentIndex + 1); // INFO = loglevel
      // log.debug("loglevel = " + line.substring(logLevelStartIndex,
      // currentIndex));
      lld.setLogLevel(line.substring(logLevelStartIndex, currentIndex));

      final int typeDetailsStartIndex = currentIndex + 1;
      final int doubleDotIndex = line.indexOf(":", typeDetailsStartIndex);

      lld.setMessage(line.substring(doubleDotIndex + 2, line.length()));

      return lld;

    } catch (Exception e) {
      log.info("Parsing: \"" + line + "\" failed", e);
      return null;
    }
  }

  /**
   * Sorts the lines read from log files based on the times . i.e sorts a List<LogLineDetail>
   * 
   * @param List
   *          <LogLineDetail>
   * 
   * @return sorted List<LogLineDetail>
   * @exception NA
   * 
   */

  protected List<LogLineDetail> sortListContents(final List<LogLineDetail> result) {
    Collections.sort(result, new Comparator<LogLineDetail>() {

      @Override
      public int compare(final LogLineDetail st1, final LogLineDetail st2) {
        int returnval = 0;
        if (st1.getLogTime().getTime() > st2.getLogTime().getTime()) {
          returnval = 1;
        } else if (st1.getLogTime().getTime() < st2.getLogTime().getTime()) {
          returnval = -1;
        }
        return returnval;
      }
    });
    return result;
  }

}

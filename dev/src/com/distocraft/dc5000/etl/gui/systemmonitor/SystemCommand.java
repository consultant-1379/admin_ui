package com.distocraft.dc5000.etl.gui.systemmonitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Copyright &copy; Distocraft ltd. All rights reserved.<br>
 * Class is for executing os specific system commands.
 * 
 * @author Antti Laurila
 *
 */
public class SystemCommand {

  public static final int SHOW_ALWAYS = -1;

  private Log log = LogFactory.getLog(this.getClass());

  private String shownCmd = null; // command shown at the screen

  // only one of the following is defined
  private String linkCmd = null; // contains link, if any

  private String shellCmd = null; // contains shell command, if any

  private String classCmd = null; // contains class, which is "spawed", if any

  private String page = null; // after command is executed, goto page <>, if null go back to monitor main page

  private int onlyShow = SHOW_ALWAYS; // show only in cases (?)

  private int exitValue = 0; // saved exit value of the shell command

  private SystemMonitorBase monitor = null; // this command belongs to monitor...

  public SystemCommand() {
    super();
  }

  /**
   * @param shownCmd command that is shown at the GUI
   * @param linkCmd link that is followrd, when command is requested
   * @param shellCmd shell cmd that is executed, when command is requested
   * @param classCmd class that is spawned, when command is requested
   * @param page page where to go, after command is executed, if null default monitor page is "called"
   * @param onlyShow interger, which can define the states when show this cmd
   */
  public SystemCommand(String shownCmd, String linkCmd, String shellCmd, String classCmd, String page, int onlyShow) {
    super();
    this.shownCmd = shownCmd;
    this.linkCmd = linkCmd;
    this.shellCmd = shellCmd;
    this.classCmd = classCmd;
    this.page = page;
    this.onlyShow = onlyShow;
  }

  /**
   * Set monitor that uses this cmd.
   * @param nMonitor
   */
  public final void setMonitor(SystemMonitorBase nMonitor) {
    monitor = nMonitor;
  }

  /**
   * 
   * @param classCmd
   */
  public final void setClassCmd(String classCmd) {
    this.classCmd = classCmd;
  }

  /**
   * 
   * @param linkCmd
   */
  public final void setLinkCmd(String linkCmd) {
    this.linkCmd = linkCmd;
  }

  /**
   * 
   * @param shellCmd
   */
  public final void setShellCmd(String shellCmd) {
    this.shellCmd = shellCmd;
  }

  /**
   * Execute shell cmd.
   * @return
   */
  public final String executeCmd() {
    // linkCmd should be handled by the link - so its not implemented now
    // the true implementation should be call the link
    if (linkCmd != null) {
      // TODO call some link
    }

    // is it shell cmd?
    if (shellCmd != null) {
      try {
        return runCmd(shellCmd);
      } catch (IOException e) {
        log.error("IOException", e);
      }
    }

    // or should we execute some class - not implemented
    if (classCmd != null) {
      // TODO create and execute some class
    }

    return null;
  }

  /**
   * Used to determine if we should show this cmd or not.
   * @param status
   * @return
   */
  public boolean shouldShow(int status) {
    return onlyShow == SHOW_ALWAYS || onlyShow == status;
  }

  /**
   * Get the actual value of the onlyshow.
   * @return
   */
  public int getOnlyShow() {
    return onlyShow;
  }

  /**
   * Set the value of onlyshow.
   * @param onlyShow
   */
  public void setOnlyShow(int onlyShow) {
    this.onlyShow = onlyShow;
  }

  /**
   * Page that is shown after execution.
   * @return
   */
  public String getPage() {
    if (page == null) {
      return null;
    }
    return page;
  }

  /**
   * 
   * @param page
   */
  public void setPage(String page) {
    this.page = page;
  }

  /**
   * get the string that is shown at the page
   * @return
   */
  public String getShownCmd() {
    return shownCmd;
  }

  /**
   * 
   * @param shownCmd
   */
  public void setShownCmd(String shownCmd) {
    this.shownCmd = shownCmd;
  }

  /**
   * This command is support for executing any system commands from GUI.
   * Use getExitValue() to get the exitValue of the system command.
   * 
   * @param cmd the command that is needed to run
   * @return returns the output of the completed command
   * @throws IOException
   */
  public final String runCmd(String cmd) throws IOException {
    StringBuffer result = new StringBuffer();

    //
    // We run something from the underlying OS - very non portable,
    // but there is no other way (me thinks)
    // create runtime environment and run the process
    Runtime rt = Runtime.getRuntime();
    //System.out.println("Command: '"+cmd+"'");
    Process process = rt.exec(cmd);

    // read what process wrote to the STDIN (immediate)
    BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
    String line;
    while ((line = in.readLine()) != null) {
      result.append(line).append("<br />");
    }

    // wait for process to end
    try {
      process.waitFor();
    } catch (InterruptedException e) {
      try {
        process.waitFor();
      } catch (InterruptedException e2) {
        // do we have a problem here?
      }
    }

    // and read whatever was left to STDIN
    while ((line = in.readLine()) != null) {
      result.append(line).append("<br />");
    }

    // close streams
    in.close();
    process.getErrorStream().close();
    process.getOutputStream().close();

    // save exit information of the process and return with output string
    exitValue = process.exitValue();
    //System.out.println("Exit value: "+exitValue);
    //System.out.println("Result: '"+result.toString()+"'");
    return result.toString();
  }

  /**
   * This command is support for executing any system commands from GUI.
   * Use getExitValue() to get the exitValue of the system command.
   * 
   * @param cmd the command that is needed to run
   * @return returns the output of the completed command
   * @throws IOException
   */
  public final String runCmdPlain(String cmd) throws IOException {
    StringBuffer result = new StringBuffer();

    //
    // We run something from the underlying OS - very non portable,
    // but there is no other way (me thinks)
    // create runtime environment and run the process
    Runtime rt = Runtime.getRuntime();
    this.log.info("SystemCommand.runCmdPlain running command: '" + cmd + "'");
    Process process = rt.exec(cmd);
    
    
    try {
      this.log.info("Waiting for the process to end.");
      process.waitFor();
    } catch (Exception e) {
      this.log.info("Exception during SystemCommand.runCmdPlain process waiting.");
    }
    
    // read what process wrote to the STDIN (immediate)
    BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
    String line;
    while ((line = in.readLine()) != null) {
      result.append(line);
      this.log.info("SystemCommand.runCmdPlain read line: " + line);
    }

    // wait for process to end
    try {
      process.waitFor();
    } catch (InterruptedException e) {
      try {
        process.waitFor();
      } catch (InterruptedException e2) {
        // do we have a problem here?
      }
    }

    // and read whatever was left to STDIN
    while ((line = in.readLine()) != null) {
      result.append(line).append("<br />");
    }

    // close streams
    in.close();
    process.getErrorStream().close();
    process.getOutputStream().close();

    String resultString = new String(result);
    resultString = resultString.replaceAll("  ", " ");

    //System.out.println("Result: '"+result.toString()+"'");
    return resultString;
  }
  
  /*
   * Added to run an array of String command
   */
  public final String runCmdMultiple(String[] cmd) {
	  StringBuffer result = new StringBuffer();
	  try {
		Runtime rt = Runtime.getRuntime();
	    this.log.info("SystemCommand.runCmdPlain running command: '" + cmd[2] + "'");
	    Process process = rt.exec(cmd);
	    this.log.info("Waiting for the process to end.");
	    process.waitFor();
	    BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
	    String line;
	    while ((line = in.readLine()) != null) {
	      result.append(line);
	      this.log.info("SystemCommand.runCmdMultiple read line: " + line);
	    }
	    process.waitFor();
	    // and read whatever was left to STDIN
	    while ((line = in.readLine()) != null) {
		      result.append(line).append("<br />");
		    }

		    // close streams
		    in.close();
		    process.getErrorStream().close();
		    process.getOutputStream().close();
	    
	  } catch(Exception e) {
		 this.log.warn("Process command didnt run as expected");
	  }
	  
	  String resultString = new String(result);
	  resultString = resultString.replaceAll("  ", " ");
	  return resultString;    
  }

  /**
   * @return returns the exit value of previous runCmd call (system command).
   */
  public int getExitValue() {
    return exitValue;
  }
}

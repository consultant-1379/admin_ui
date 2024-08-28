/**
 * 
 */
package com.distocraft.dc5000.etl.gui.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.logging.Log;


/**
 * @author eheijun
 *
 */
public class CommandRunner {
  
  /**
   * This command is support for executing any system commands from GUI. Use getExitValue() to get the exitValue of the
   * system command.
   * 
   * @param cmd
   *          the command that is needed to run
   * @return returns the output of the completed command
   * @throws IOException
   */
  public static final String runCmd(final String cmd, final Log log) throws IOException {
    final StringBuilder result = new StringBuilder();

    //
    // We run something from the underlying OS - very non portable,
    // but there is no other way (me thinks)
    // create runtime environment and run the process
    final Process process = Runtime.getRuntime().exec(cmd);

    // read what process wrote to the STDIN (immediate)
    final BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
    String line;
    while ((line = in.readLine()) != null) {
      result.append(line).append("<br />");
    }

    // wait for process to end
    try {
      process.waitFor();
    } catch (final InterruptedException e) {
      try {
        process.waitFor();
      } catch (final InterruptedException ie) {
        log.warn(ie);
      }
    }

    // and read whatever was left to STDIN
    while ((line = in.readLine()) != null) {
      result.append(line).append("<br />");
    }

    // cleanup
    in.close();
    process.getErrorStream().close();
    process.getOutputStream().close();

    // save exit information of the process and return with output string
    // exitValue = process.exitValue();
    return result.toString();
  }

  

}

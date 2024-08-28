package com.distocraft.dc5000.etl.gui.common;

import java.io.BufferedInputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Copyright &copy; Distocraft ltd. All rights reserved.<br>
 * Task runner class which handles writing to http-outputstream.
 * 
 * @author Matti Koljonen
 * 
 */
public class Task {

  private String name;

  private String action;

  private final Log log = LogFactory.getLog(this.getClass());

  /**
   * Default constructor
   */
  public Task() {
    name = null;
    action = null;
  }

  /**
   * To give a task a name but no action.
   * 
   * @param name
   *          name of the task
   */
  public Task(final String name) {
    this.name = name;
    action = null;
  }

  /**
   * To give a task name and action (URL).
   * 
   * @param name
   *          name of the task
   * @param action
   *          the URL that is used when task is run
   */
  public Task(final String name, final String action) {
    this.name = name;
    this.action = action;
  }

  /**
   * This method runs this task. Actuallly it just takes connection to url specified with action attribute.
   */
  public void run() {
    final int bufferSize = 256;
    try {
      final URL urlAddress = new URL(action);
      final URLConnection httpConnection = (URLConnection) urlAddress.openConnection();
      final BufferedInputStream input = new BufferedInputStream(httpConnection.getInputStream());

      httpConnection.connect();

      // read from url
      final byte[] data = new byte[bufferSize];
      input.read(data, 0, bufferSize);
      final String output = new String(data);
      input.close();

      // print first line to log
      final int delimNum = output.indexOf("\n");
      if (delimNum != -1) {
        log.info(name + " " + output.substring(0, delimNum));
      }
    } catch (Exception e) {
      log.error(e.getMessage());
    }
  }

  /**
   * @return
   */
  public String getAction() {
    return action;
  }

  /**
   * @return
   */
  public String getName() {
    return name;
  }

  /**
   * @param string
   */
  public void setAction(final String string) {
    action = string;
  }

  /**
   * @param string
   */
  public void setName(final String string) {
    name = string;
  }
}

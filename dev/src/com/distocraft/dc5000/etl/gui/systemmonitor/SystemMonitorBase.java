package com.distocraft.dc5000.etl.gui.systemmonitor;

import java.util.ArrayList;
import java.util.List;

import org.apache.velocity.context.Context;

/**
 * Copyright &copy; Distocraft ltd. All rights reserved.<br>
 * @deprecated
 * All monitors should implement this base class. Do not instantiate this class
 * directly, derive from it the actual monitors.
 * 
 * @author Antti Laurila
 */
public abstract class SystemMonitorBase {
	
	private long ID = 0;					// unique ID
	private List cmds = null;				// contains commands attached to this monitor
	private String name = null;				// the name of the monitor
	private String linkTo = null;			// where this monitor is linked to
	
	private static int nextID = 1;			// ID "pool"
	
	/**
	 * Set monitor without the link.
	 * @param nName name of the monitor
	 */
	public SystemMonitorBase(String nName) {
		name = nName;
		cmds = new ArrayList(10);
		ID = nextID++;
	}
	
	/**
	 * Set monitor with link name.
	 * @param nName name of the monitor
	 * @param nLinkTo where should "name" point to (the link)
	 */
	public SystemMonitorBase(String nName,String nLinkTo) {
		this(nName);
		linkTo = nLinkTo;
	}

	/**
	 * Name of the monitor. Shown at the UI-screen.
	 * @return the name of the monitor
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Returns list of commands that are available for this monitor.
	 * @return list of commands that are available
	 */
	public final List getCmds() {
		int j=cmds.size();
		List nCmds = new ArrayList(j);
		
		int status = getLastStatus();
		for(int i=0;i<j;i++) {
			SystemCommand sc = (SystemCommand) cmds.get(i);
			if(sc.shouldShow(status)) {
				nCmds.add(sc);
			}
		}
		
		return nCmds;
	}
	
	/**
	 * Add one command to this monitor.
	 */
	public final void addCmd(SystemCommand cmd) {
		cmd.setMonitor(this);	// this is the monitor of the command ("I am your controller")
		cmds.add(cmd);			// add the command to monitor list
	}
	
	/**
	 * Execute some command that is implemented by this monitor.
	 * @param shownCmdName the command that should be executed
	 * @return String that contains the page that should be shown, or null if we show monitor main page
	 */
	public final String executeCmd(Context ctx,String shownCmdName) {
		int j = cmds.size();
		
		for(int i=0;i<j;i++) {
			SystemCommand sc = (SystemCommand) cmds.get(i);
			if(sc.getShownCmd().equals(shownCmdName)) {
				ctx.put("result",sc.executeCmd());
				return sc.getPage();
			}
		}
		
		// odd, no such command, go to monitor main page (user patching?)
		return null; 
	}

	/**
	 * Execute the "status" check for something.
	 * @return status as an integer 
	 */
	public abstract int getStatus();
	
	/**
	 * 
	 * @return
	 */
	public abstract int getLastStatus();
	
	/**
	 * Get the color of the monitor as String (HTML color string, eg. #3333FF, #00FF00).
	 * This method returns the last value of the last status cmd execute.
	 * @return
	 */
	public abstract String getLastStatusColor();
	
	/**
	 * This method returns verbalic information about the last status call.
	 * @return string that might contain some status information
	 */
	public abstract String getLastStatusInformation();

	/**
	 * Get unique ID.
	 * @return
	 */
	public long getID() {
		return ID;
	}

	/**
	 * Link that is shown at the UI (name is the "link").
	 * @return
	 */
	public String getLinkTo() {
		return linkTo;
	}
}

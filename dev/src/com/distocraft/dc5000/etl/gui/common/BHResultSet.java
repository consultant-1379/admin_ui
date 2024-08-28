package com.distocraft.dc5000.etl.gui.common;

/**
 * Copyright &copy; Distocraft ltd. All rights reserved.<br>
 * This class is used by the ViewBHInformation -servlet.
 * 
 * @author Mark Stenback
 *
 */
public class BHResultSet 
{
	//=============================================================== GLOBAL VARIABLES
	/** Contains the value of <code>COUNT(date_id)</code>. */
	private final String count;
	
	/** Contains the value of <code>date_id</code>. */
	private final String dateID;
	
	/** Contains the value of <code>DIM_BHCLASS.DESCRIPTION</code>. */
	private final String desc;
	
	
	
	//=============================================================== CONSTRUCTORS
	/**
	 * @param count
	 * @param dateID
	 * @param desc
	 */
	public BHResultSet (final String count, final String dateID, final String desc)
	{
		this.count 	= count;
		this.dateID	= dateID;
		this.desc	= desc;
	}
	
	
	
	//=============================================================== GET -METHODS
	/**
	 * @return count 
	 */
	public String getCount ()
	{
		return this.count;
	}
	
	
	/**
	 * @return dateID
	 */
	public String getDateID ()
	{
		return this.dateID;
	}
	
	
	
	/**
	 * @return desc
	 */
	public String getDesc ()
	{
		return this.desc;
	}
}

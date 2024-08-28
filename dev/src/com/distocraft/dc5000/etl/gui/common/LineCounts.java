package com.distocraft.dc5000.etl.gui.common;

/**
 * Copyright &copy; Distocraft ltd. All rights reserved.<br>
 * This class is used by the DataRowSummary -servlet.
 * 
 * @author Mark Stenback
 */
public class LineCounts 
{
	//=============================================================== GLOBAL VARIABLES
	private int [] lineArray;
	private final int arraySize;
	private int arrayPosition;
	
	
	
	//=============================================================== CONSTRUCTOR
	/**
	 * @param size
	 */
	public LineCounts (final int size)
	{
		lineArray = new int [size];
		arraySize = size;
		arrayPosition = -1;
	}
	
	
	
	//=============================================================== PUBLIC METHODS
	/**
	 * @param line
	 * @param value
	 */
	public void setLine (final int line, final int value)
	{
		lineArray [line] = value;
	}
	
	
	
	/**
	 * 
	 * @return
	 */
	public int getNext ()
	{
		int returnValue = -1;
		
		if (arrayPosition <= arraySize)
		{
			arrayPosition++;
			
			returnValue = lineArray [arrayPosition];
		}
		
		return returnValue;
	}
	
	
	
	/**
	 * 
	 * @param line
	 * @return
	 */
	public int getLine (final int line)
	{
		return lineArray [line]; 
	}
	
	
	
	/**
	 * 
	 * @return
	 */
	public int [] getArray ()
	{
		return lineArray;
	}
	
	
	
	/**
	 * 
	 * @return
	 */
	public int size ()
	{
		return lineArray.length;
	}
}

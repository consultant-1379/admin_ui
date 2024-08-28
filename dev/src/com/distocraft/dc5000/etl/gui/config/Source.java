/*
 * Created on 30.7.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
 
package com.distocraft.dc5000.etl.gui.config;


/**
 * Copyright &copy; Distocraft ltd. All rights reserved.<br>
 * 
 * @author Jukka Karvanen, Jani Vesterinen
 * Class receives properties from properties file.
 * 
 * @author Matti Koljonen
 *
 */

public class Source {

	private String name;
	private Environment env;
	
	/**
	 * @param name
	 * @param realpath
	 */
	public Source(String name, String realpath) {
		this.name = name;
		env = Environment.getEnvWithoutDb(realpath);
	}
	

	/**
	 * 
	 * checks if property file exsist
	 * 
	 * @return true if Property exsist, else false;
	 */
	public boolean checkProperty()
	{
		
		return (env!=null);
		
	}

	/**
	 * @param key
	 * @return anyproperty
	 */
	public String getAnyProperty(String key) {
		return env.getProperty(key);
	}

	
	/**
	 * @param key
	 * @return property named as name.[foo]
	 */
	public String getProperty(String key) {
		return env.getProperty(name + "." + key);
	}
	
	/**
	 * @return name
	 */
	public String getName() 
	{
		return this.name;
	}

	
	
}

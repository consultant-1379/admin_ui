package com.distocraft.dc5000.etl.gui.systemmonitor;

public class RMIConnectException extends Throwable{
	private String _message ;
	
	public RMIConnectException(final String mess) {
		_message = mess ;
	}
	
	public RMIConnectException(){
		_message = "Unknown cause for exception." ;
	}
	
	public String getMessage(){
		return _message ;
	}
}

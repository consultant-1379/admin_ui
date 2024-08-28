package com.distocraft.dc5000.etl.gui.common;

public final class ENIQServiceStatusInfo {
	
	public enum ServiceHealth{
		Offline, Online, StatusYellow
	}
	
	public enum ConnState{
		LIMIT_OK , LIMIT_EXCEED	// Only for database
	}
	
	private static final String ENIQ_DWHDB = "ENIQ DWH" ;
	private static final String ENIQ_REPDB = "ENIQ REP" ;
	private static ServiceHealth engineHealth = ServiceHealth.Online;
	private static ServiceHealth lwpHelperHealth = ServiceHealth.Online;
	private static ServiceHealth flsHealth = ServiceHealth.Online;
	private static ServiceHealth schedulerHealth = ServiceHealth.Online;
	private static ServiceHealth eltDBHealth = ServiceHealth.Online;
	private static ServiceHealth repDBHealth = ServiceHealth.Online;
	private static ServiceHealth dwhDBHealth = ServiceHealth.Online;
	private static ServiceHealth licSerHealth = ServiceHealth.Online;
	private static ServiceHealth licManHealth = ServiceHealth.Online;
	private static ServiceHealth ldapHealth = ServiceHealth.Online;
	private static ConnState repDBConnState = ConnState.LIMIT_OK ;
	private static ConnState dwhDBConnState = ConnState.LIMIT_OK ;
	
	public static String getRepDBName(){
		return ENIQ_REPDB ;
	}
	
	public static String getDwhDBName(){
		return ENIQ_DWHDB ;
	}
	
	public static String getEtlDBName(){
		return ENIQ_REPDB ;
	}
	
	public static void setRepDBConnState(final ConnState state){
		repDBConnState = state ;
	}
	
	private static ConnState getRepDBConnState(){
		return  repDBConnState;
	}
	
	public static void setDwhDBConnState(final ConnState state){
		dwhDBConnState = state ;
	}
	
	private static ConnState getDwhDBConnState(){
		return  dwhDBConnState;
	}
	
	public static void setEngineHealth(final ServiceHealth ser){
		engineHealth = ser ;
	}
	
	private static ServiceHealth getEngineHealth(){
		return  engineHealth;
	}
	
	public static void setLwpHelperHealth(final ServiceHealth ser){
		lwpHelperHealth = ser ;
	}
	
	private static ServiceHealth getLwpHelperHealth(){
		return  lwpHelperHealth;
	}
	
	public static void setFlsHealth(final ServiceHealth ser){
		flsHealth = ser ;
	}
	
	private static ServiceHealth getFlsHealth(){
		return  flsHealth;
	}
	
	public static void setLdapHealth(final ServiceHealth ser){
		ldapHealth = ser ;
	}
	
	private static ServiceHealth getLdapHealth(){
		return  ldapHealth;
	}
	
	public static void setSchedulerHealth(final ServiceHealth ser){
		schedulerHealth = ser ;
	}
	
	private static ServiceHealth getSchedulerHealth(){
		return  schedulerHealth;
	}
	
	public static void setRepDBHealth(final ServiceHealth ser){
		repDBHealth = ser ;
	}
	
	private static ServiceHealth getRepDBHealth(){
		return  repDBHealth;
	}
	
	public static void setEtlDBHealth(final ServiceHealth ser){
		eltDBHealth = ser ;
	}
	
	private static ServiceHealth getEtlDBHealth(){
		return  eltDBHealth ;
	}
	
	public static void setdwhDBHealth(final ServiceHealth ser){
		dwhDBHealth = ser ;
	}	
	
	private static ServiceHealth getdwhDBHealth(){
		return  dwhDBHealth;
	}
	
	public static void setLicServerHealth(final ServiceHealth ser){
		licSerHealth = ser ;
	}
	
	private static ServiceHealth getLicServerHealth(){
		return  licSerHealth;
	}
	
	public static void setLicMangerHealth(final ServiceHealth ser){
		licManHealth = ser ;
	}
	
	private static ServiceHealth getLicMangerHealth(){
		return  licManHealth;
	}
	
	public static boolean isEngineOnline(){
		return (getEngineHealth() == ServiceHealth.Online) ;
	}
	
	public static boolean isEngineOffline(){
		return (getEngineHealth() == ServiceHealth.Offline) ;
	}
	
	public static boolean isLwpHelperOnline(){
		return (getLwpHelperHealth() == ServiceHealth.Online) ;
	}
	
	public static boolean isLwpHelperOffline(){
		return (getLwpHelperHealth() == ServiceHealth.Offline) ;
	}
	
	public static boolean isFlsOnline(){
		return (getFlsHealth() == ServiceHealth.Online) ;
	}
	
	public static boolean isFlsOffline(){
		return (getFlsHealth() == ServiceHealth.Offline) ;
	}
	
	public static boolean isFlsOnHold() {
		return (getFlsHealth() == ServiceHealth.StatusYellow) ;
	}
	
	public static boolean isLdapOnline(){
		return (getLdapHealth() == ServiceHealth.Online) ;
	}
	
	public static boolean isLdapOffline(){
		return (getLdapHealth() == ServiceHealth.Offline) ;
	}
	
	public static boolean isSchedulerOnline(){
		return (getSchedulerHealth() == ServiceHealth.Online) ;
	}
	
	public static boolean isSchedulerOffline(){
		return (getSchedulerHealth() == ServiceHealth.Offline) ;
	}
	
	public static boolean isLicServerOnline(){
		return (getLicServerHealth() == ServiceHealth.Online);
	}
	
	public static boolean isLicServerOffline(){
		return (getLicServerHealth() == ServiceHealth.Offline);
	}
	
	public static boolean isLicManagerOnline(){
		return (getLicMangerHealth() == ServiceHealth.Online);
	}
	
	public static boolean isLicManagerOffline(){
		return (getLicMangerHealth() == ServiceHealth.Offline);
	}
	
	public static boolean isEtlDBOnline(){
		return (getEtlDBHealth() == ServiceHealth.Online);
	}
	
	public static boolean isEtlDBOffline(){
		return (getEtlDBHealth() == ServiceHealth.Offline);
	}
	
	public static boolean isRepDBOnline(){
		return (getRepDBHealth() == ServiceHealth.Online);
	}
	
	public static boolean isRepDBOffline(){
		return (getRepDBHealth() == ServiceHealth.Offline);
	}
	
	public static boolean isRepDBConnLimitExceeded(){
		return (getRepDBConnState() == ConnState.LIMIT_EXCEED);
	}
	
	public static boolean isDwhDBOnline(){
		return (getdwhDBHealth() == ServiceHealth.Online);
	}
	
	public static boolean isDwhDBOffline(){
		return (getdwhDBHealth() == ServiceHealth.Offline);
	}
	
	public static boolean isDwhDBConnLimitExceeded(){
		return (getDwhDBConnState() == ConnState.LIMIT_EXCEED);
	}
	
}

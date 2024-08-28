--/***************************
--  TPActivation
--/***************************
create table TPActivation (
	TECHPACK_NAME varchar(30) not null,
	STATUS varchar(10) not null,
	VERSIONID varchar(128) ,
	TYPE varchar(10) not null,
    MODIFIED integer default 0 not null
);

alter table TPActivation
	add primary key (TECHPACK_NAME);
	
--/***************************
--  Versioning
--/***************************
create table Versioning (
	VERSIONID varchar(128) not null,
	DESCRIPTION varchar(50) ,
	STATUS numeric(9) ,
	TECHPACK_NAME varchar(30) not null,
	TECHPACK_VERSION varchar(32) ,
	TECHPACK_TYPE varchar(10) ,
	PRODUCT_NUMBER varchar(255) ,
	LOCKEDBY varchar(255) ,
	LOCKDATE timestamp ,
	BASEDEFINITION varchar(128) ,
	BASEVERSION varchar(16) ,
	INSTALLDESCRIPTION varchar(32000) ,
	UNIVERSENAME varchar(30) ,
	UNIVERSEEXTENSION varchar(16) ,
	ENIQ_LEVEL varchar(12) default '1.0' not null,
	LICENSENAME varchar(255) 
);

alter table Versioning
	add primary key (VERSIONID);

--/***************************
--  MeasurementType
--/***************************
create table MeasurementType (
	TYPEID varchar(255) not null,
	TYPECLASSID varchar(50) not null,
	TYPENAME varchar(255) not null,
	VENDORID varchar(50) ,
	FOLDERNAME varchar(50) ,
	DESCRIPTION varchar(32000) ,
	STATUS numeric(9) ,
	VERSIONID varchar(128) not null,
	OBJECTID varchar(255) not null,
	OBJECTNAME varchar(255) ,
	OBJECTVERSION integer ,
	OBJECTTYPE varchar(255) ,
	JOINABLE varchar(255) ,
	SIZING varchar(16) ,
	TOTALAGG integer ,
	ELEMENTBHSUPPORT integer ,
	RANKINGTABLE integer ,
	DELTACALCSUPPORT integer ,
	PLAINTABLE integer ,
	UNIVERSEEXTENSION varchar(4) ,
	VECTORSUPPORT integer ,
	DATAFORMATSUPPORT integer ,
	FOLLOWJOHN integer
);

alter table MeasurementType
	add primary key (TYPEID);
       
--/***************************
--  MeasurementKey
--/***************************
create table MeasurementKey (
	TYPEID varchar(255) not null,
	DATANAME varchar(128) not null,
	DESCRIPTION varchar(32000) ,
	ISELEMENT integer ,
	UNIQUEKEY integer ,
	COLNUMBER numeric(9) ,
	DATATYPE varchar(50) ,
	DATASIZE integer ,
	DATASCALE integer ,
	UNIQUEVALUE numeric(9) ,
	NULLABLE integer ,
	INDEXES varchar(20) ,
	INCLUDESQL integer ,
	UNIVOBJECT varchar(128) ,
	JOINABLE integer ,
	DATAID varchar(255) 
);

alter table MeasurementKey
    add primary key (TYPEID, DATANAME);


--/***************************
--  MeasurementTable
--/***************************
create table MeasurementTable (
	MTABLEID varchar(255) not null,
	TABLELEVEL varchar(50) not null,
	TYPEID varchar(255) ,
	BASETABLENAME varchar(255) ,
	DEFAULT_TEMPLATE varchar(50) ,
	PARTITIONPLAN varchar(128) 
);

alter table MeasurementTable
	add primary key (MTABLEID);
	
--/***************************
--  ReferenceTable
--/***************************
create table ReferenceTable (
	TYPEID varchar(255) not null,
	VERSIONID varchar(128) not null,
	TYPENAME varchar(255) ,
	OBJECTID varchar(255) ,
	OBJECTNAME varchar(255) ,
	OBJECTVERSION varchar(50) ,
	OBJECTTYPE varchar(255) ,
	DESCRIPTION varchar(32000) ,
	STATUS numeric(9) ,
	UPDATE_POLICY numeric(9) ,
	TABLE_TYPE varchar(12) ,
	DATAFORMATSUPPORT integer,
    BASEDEF integer default 0 not null 
);

alter table ReferenceTable
    add primary key (TYPEID);

--/***************************
--  Busyhour
--/***************************
create table Busyhour (
	VERSIONID varchar(128) not null,
	BHLEVEL varchar(32) not null,
	BHTYPE varchar(32) not null,
	BHCRITERIA varchar(32000) ,
	WHERECLAUSE varchar(32000) ,
	DESCRIPTION varchar(32000) ,
	TARGETVERSIONID varchar(128) not null,
	BHOBJECT varchar(32) not null,
	BHELEMENT integer not null,
	ENABLE integer null,
	AGGREGATIONTYPE varchar(128) default 'RANKBH_TIMELIMITED' not null,
	OFFSET integer null,
	WINDOWSIZE integer null,
	LOOKBACK integer null,
	P_THRESHOLD integer null,
	N_THRESHOLD integer null,
	CLAUSE varchar(32000) null,
	PLACEHOLDERTYPE varchar(128) null,
	GROUPING varchar(32) default 'None' not null,
	REACTIVATEVIEWS integer default 0 not null
);

alter table Busyhour 
	add primary key (VERSIONID, BHLEVEL, BHTYPE, TARGETVERSIONID, BHOBJECT);


--/***************************
--  BusyhourMapping
--/***************************
create table BusyhourMapping (
	VERSIONID varchar(128) not null,
	BHLEVEL varchar(32) not null,
	BHTYPE varchar(32) not null,
	TARGETVERSIONID varchar(128) not null,
	BHOBJECT varchar(32) not null,
	TYPEID varchar(255) not null,
	BHTARGETTYPE varchar(128) not null,
	BHTARGETLEVEL varchar(128) not null,
	ENABLE integer not null
);

alter table BusyhourMapping
	add primary key (VERSIONID, BHLEVEL, BHTYPE, TARGETVERSIONID, BHOBJECT, TYPEID);

--/***************************
--  BusyhourPlaceholders
--/***************************
create table BusyhourPlaceholders (
	VERSIONID varchar(128) not null,
	BHLEVEL varchar(32) not null,
	PRODUCTPLACEHOLDERS integer null,
	CUSTOMPLACEHOLDERS integer null
);

alter table BusyhourPlaceholders
	add primary key (VERSIONID, BHLEVEL);
  
--/***************************
--  BusyhourRankkeys
--/***************************
create table BusyhourRankkeys (
	VERSIONID varchar(128) not null,
	BHLEVEL varchar(32) not null,
	BHTYPE varchar(32) not null,
	KEYNAME varchar(128) not null,
	KEYVALUE varchar(128) not null,
	ORDERNBR numeric(9) not null,
	TARGETVERSIONID varchar(128) not null,
	BHOBJECT varchar(32) not null
);

alter table BusyhourRankkeys
	add primary key (VERSIONID, BHLEVEL, BHTYPE, KEYNAME, TARGETVERSIONID, BHOBJECT);

--/***************************
--  BusyhourSource
--/***************************
create table BusyhourSource (
	VERSIONID varchar(128) not null,
	BHLEVEL varchar(32) not null,
	BHTYPE varchar(32) not null,
	TYPENAME varchar(255) not null,
	TARGETVERSIONID varchar(128) not null,
	BHOBJECT varchar(32) not null
);

alter table BusyhourSource
	add primary key (VERSIONID, BHLEVEL, BHTYPE, TYPENAME, TARGETVERSIONID, BHOBJECT);
	
--/***************************
--  Aggregation
--/***************************
create table Aggregation (
	AGGREGATION varchar(255) not null,
	VERSIONID varchar(128) not null,
	AGGREGATIONSET varchar(100) ,
	AGGREGATIONGROUP varchar(100) ,
	REAGGREGATIONSET varchar(100) ,
	REAGGREGATIONGROUP varchar(100) ,
	GROUPORDER integer ,
	AGGREGATIONORDER integer ,
	AGGREGATIONTYPE varchar(50) ,
	AGGREGATIONSCOPE varchar(50) 
);

alter table Aggregation
    add primary key (AGGREGATION, VERSIONID);

--/***************************
--  AggregationRule
--/***************************
create table AggregationRule (
	AGGREGATION varchar(255) not null,
	VERSIONID varchar(128) not null,
	RULEID integer not null,
	TARGET_TYPE varchar(50) ,
	TARGET_LEVEL varchar(50) ,
	TARGET_TABLE varchar(255) ,
	TARGET_MTABLEID varchar(255) ,
	SOURCE_TYPE varchar(50) ,
	SOURCE_LEVEL varchar(50) ,
	SOURCE_TABLE varchar(255) ,
	SOURCE_MTABLEID varchar(255) ,
	RULETYPE varchar(50) not null,
	AGGREGATIONSCOPE varchar(50) ,
	BHTYPE varchar(50),
	ENABLE integer null
);

alter table AggregationRule
    add primary key (AGGREGATION, VERSIONID, RULEID, RULETYPE);

--/***************************
--  DWHTechPacks
--/***************************
create table DWHTechPacks (
	TECHPACK_NAME varchar(255) not null,
	VERSIONID varchar(128) not null,
	CREATIONDATE timestamp
);

alter table DWHTechPacks
    add primary key (TECHPACK_NAME);

--/***************************
--  TypeActivation
--/***************************
create table TypeActivation (
	TECHPACK_NAME varchar(255) not null,
	STATUS varchar(255) not null,
	TYPENAME varchar(255) not null,
	TABLELEVEL varchar(255) not null,
	STORAGETIME numeric(31) null,
	TYPE varchar(255) not null,
	PARTITIONPLAN varchar(255) null
);

alter table TypeActivation
    add primary key (TECHPACK_NAME, TYPENAME, TABLELEVEL);
       
--/***************************
--  Partitionplan
--/***************************
create table Partitionplan (
	PARTITIONPLAN varchar(255) not null,
	DEFAULTSTORAGETIME numeric(31) not null,
	DEFAULTPARTITIONSIZE numeric(31) not null,
	MAXSTORAGETIME numeric(31) not null,
	PARTITIONTYPE int
);

CREATE TABLE DWHType (
TECHPACK_NAME VARCHAR(31),
TYPENAME VARCHAR(31),
PARTITIONCOUNT INT
);

CREATE TABLE DWHPartition (
STORAGEID VARCHAR(100),
TABLENAME VARCHAR(100),
);

CREATE TABLE DWHColumn (
STORAGEID VARCHAR(100),
DATANAME VARCHAR(100)
);

CREATE TABLE ENIQ_EVENTS_ADMIN_PROPERTIES (
	PARAM_NAME varchar(100) not null,
	PARAM_VALUE varchar(1000) not null,
	DATE_MODIFIED timestamp,
	MODIFIED_BY varchar(100) not null
);
package com.distocraft.dc5000.etl.gui;

import ssc.rockfactory.RockFactory;

public enum RockFactoryType {
  ROCK_ETL_REP("rockEtlRep"), ROCK_ETL_REP_DBA("rockEtlRepDba"), ROCK_DWH("rockDwh"), ROCK_DWH_REP("rockDwhRep"), ROCK_DWH_DBA(
      "rockDwhDba");

  private final String name;

  private RockFactory rockFactory;

  private static final RockFactoryType[] types = { ROCK_ETL_REP, ROCK_ETL_REP_DBA, ROCK_DWH, ROCK_DWH_REP,
      ROCK_DWH_DBA };
  
  private RockFactoryType(final String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public static RockFactoryType[] getTypes() {
    return types;
  }

  public RockFactory getRockFactory() {
    return rockFactory;
  }

  public synchronized void setRockFactory(final RockFactory rockFactory) {
    this.rockFactory = rockFactory;
  }

  public static boolean allInitialised() {
    for (final RockFactoryType type : types) {
      if (type.getRockFactory() == null
          && (type.getName() == ROCK_ETL_REP.getName() || type.getName() == ROCK_ETL_REP_DBA.getName()
              || type.getName() == ROCK_DWH.getName() || type.getName() == ROCK_DWH_REP.getName() || type.getName() == ROCK_DWH_DBA
              .getName())) {
        return false;
      }
    }
    return true;
  }

}
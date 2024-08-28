#/bin/bash
# ********************************************************************
# Ericsson Radio Systems AB                                     SCRIPT
# ********************************************************************
#
#
# (c) Ericsson Radio Systems AB 2018 - All rights reserved.
#
# The copyright to the computer program(s) herein is the property
# of Ericsson Radio Systems AB, Sweden. The programs may be used 
# and/or copied only with the written permission from Ericsson Radio 
# Systems AB or in accordance with the terms and conditions stipulated 
# in the agreement/contract under which the program(s) have been 
# supplied.
#
# ********************************************************************
# Name    : disk_space.sh
# Date    : 13/07/2020(dummy) Last modified 25/04/2023
# Purpose : AdminUI uses this script to show disk space usage of the system
# Usage   : disk_space.sh
# ********************************************************************

OSTYPE=`uname -s`
if [ ${OSTYPE} = "HP-UX" ] ; then
  df -Pk
elif [ ${OSTYPE} = "SunOS" ]; then
  df -h
elif [ ${OSTYPE} = "Linux" ]; then
  df -h
else
  echo "Unknown O/S ${OSTYPE}"
fi

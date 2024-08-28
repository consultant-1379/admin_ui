#!/bin/bash
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
# Name    : inst_version.sh
# Date    : 13/07/2020(dummy) Last modified 25/04/2023
# Purpose : AdminUI uses this script to show installed ENIQ software version 
# Usage   : inst_version.sh
# ********************************************************************

. ${CONF_DIR}/niq.rc

echo "Installed ENIQ version:"
if test -f /eniq/admin/version/eniq_status 
then
	cat /eniq/admin/version/eniq_status
else
	echo "No version information available"
fi
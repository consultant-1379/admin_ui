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
# Name    : most_active_processes.sh
# Date    : 18/09/2020
# Purpose : Script to check most active processes
# Usage   : most_active_processes.sh
# ********************************************************************
ps -eo pid,ppid,cmd,%mem,%cpu --sort=-%mem | head
#!/usr/bin/env bash

##############################################################################
## Script for starting GearPump dashboard in Cloud Foundry
## (using java buildpack, zipDist option)
##
## Based on init script generated by gradle (gradle distZip).
##
## Expects following env variables to be set
## * GEARPUMP_MASTER - gearpump host address (host:port)
## * PORT - http port that dashboard is to be served (normally provided by CF)
##
## If USERNAME variable is set, gear.conf is rewritten to include the user
## and the password digest (based on PASSWORD variable) and enable security.
##############################################################################
##
## Copyright (c) 2015 Intel Corporation
##
## Licensed under the Apache License, Version 2.0 (the "License");
## you may not use this file except in compliance with the License.
## You may obtain a copy of the License at
##
## http://www.apache.org/licenses/LICENSE-2.0
##
## Unless required by applicable law or agreed to in writing, software
## distributed under the License is distributed on an "AS IS" BASIS,
## WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
## See the License for the specific language governing permissions and
## limitations under the License.
##############################################################################

# Add default JVM options here. You can also use JAVA_OPTS and DASHBOARD_OPTS to pass JVM options to this script.
DEFAULT_JVM_OPTS="-server"

APP_NAME="gearpump-dashboard"
APP_BASE_NAME=`basename "$0"`
PROG_VERSION=0.7.6

# Use the maximum available, or set MAX_FD != -1 to use that value.
MAX_FD="maximum"

warn ( ) {
    echo "$*"
}

die ( ) {
    echo
    echo "$*"
    echo
    exit 1
}

# OS specific support (must be 'true' or 'false').
cygwin=false
msys=false
darwin=false
case "`uname`" in
  CYGWIN* )
    cygwin=true
    ;;
  Darwin* )
    darwin=true
    ;;
  MINGW* )
    msys=true
    ;;
esac

# Attempt to set APP_HOME
# Resolve links: $0 may be a link
PRG="$0"
# Need this for relative symlinks.
while [ -h "$PRG" ] ; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '/.*' > /dev/null; then
        PRG="$link"
    else
        PRG=`dirname "$PRG"`"/$link"
    fi
done
SAVED="`pwd`"
cd "`dirname \"$PRG\"`/.." >/dev/null
APP_HOME="`pwd -P`"
cd "$SAVED" >/dev/null

CLASSPATH=TOCHANGE

# Determine the Java command to use to start the JVM.
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
        # IBM's JDK on AIX uses strange locations for the executables
        JAVACMD="$JAVA_HOME/jre/sh/java"
    else
        JAVACMD="$JAVA_HOME/bin/java"
    fi
    if [ ! -x "$JAVACMD" ] ; then
        die "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
    fi
else
    JAVACMD="java"
    which java >/dev/null 2>&1 || die "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
fi

# Increase the maximum file descriptors if we can.
if [ "$cygwin" = "false" -a "$darwin" = "false" ] ; then
    MAX_FD_LIMIT=`ulimit -H -n`
    if [ $? -eq 0 ] ; then
        if [ "$MAX_FD" = "maximum" -o "$MAX_FD" = "max" ] ; then
            MAX_FD="$MAX_FD_LIMIT"
        fi
        ulimit -n $MAX_FD
        if [ $? -ne 0 ] ; then
            warn "Could not set maximum file descriptor limit: $MAX_FD"
        fi
    else
        warn "Could not query maximum file descriptor limit: $MAX_FD_LIMIT"
    fi
fi

# Split up the JVM_OPTS And DASHBOARD_OPTS values into an array, following the shell quoting and substitution rules
function splitJvmOpts() {
    JVM_OPTS=("$@")
}


if [ -z "$GEARPUMP_MASTER" ] ; then
    die "ERROR: GEARPUMP_MASTER is not set!"
fi

DASHBOARD_OPTS="-Dgearpump.cluster.masters.0=$GEARPUMP_MASTER -Dgearpump.home=${APP_HOME} -Dprog.home=${APP_HOME} -Dprog.version=${PROG_VERSION}"
#  THESE OPTIONS WERE USED IN 'ORIGINAL' SERVICES SCRIPT BUT WE DON'T USE THEM.
# -Djava.rmi.server.hostname=localhost
echo "DASHBOARD_OPTS: $DASHBOARD_OPTS"

eval splitJvmOpts $DEFAULT_JVM_OPTS $JAVA_OPTS $DASHBOARD_OPTS


###########################
# Prepare config file

# change the port
sed -i "s/8090/${PORT}/g" $APP_HOME/lib/gear.conf

# check if USERNAME and PASSWORD are set
if [ -z "$USERNAME" ] ; then
    die "ERROR: USERNAME not set. Cannot configure security!"
fi
if [ -z "$PASSWORD" ] ; then
    die "ERROR: PASSWORD not set. Cannot configure security!"
fi

# generate digest for user's password
DIGEST=$(exec "$JAVACMD" -classpath "$CLASSPATH" io.gearpump.security.PasswordUtil -password $PASSWORD | tail -1)

toFind="\"admin\""
toReplace="\"$USERNAME\""
echo "$toFind -> $toReplace"
sed -i "s/$toFind/$toReplace/" $APP_HOME/lib/gear.conf

toFind="\"AeGxGOxlU8QENdOXejCeLxy+isrCv0TrS37HwA==\""
toReplace="\"$DIGEST\""
echo "$toFind -> $toReplace"
sed -i "s|$toFind|$toReplace|" $APP_HOME/lib/gear.conf

# enable authentication
sed -i "s/authentication-enabled = false/authentication-enabled = true/" $APP_HOME/lib/gear.conf
## TODO: can be moved to prepare.sh when we decide to enable security by default

###########################
# Run dshboard
# echo "$JAVACMD" "${JVM_OPTS[@]}" -classpath "$CLASSPATH" io.gearpump.services.main.Services "$@"
exec "$JAVACMD" "${JVM_OPTS[@]}" -classpath "$CLASSPATH" io.gearpump.services.main.Services "$@"
#!/usr/bin/env bash

##############################################################################
## prepare.sh GP_HOME DEST_DIR
##
##   GP_HOME - directory where binary distribution of GearPump is placed
##   DEST_DIR - directory where output files should be placed
##
## This script also expects 'dashboard.sh' and 'dashboard-manifest.yml'
## to be present in the directory it's ran from.
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
if [ "$#" -ne 2 ]; then
    echo "Illegal number of arguments. GP_HOME and DEST_DIR required (see source code)."
    echo "Example usage: ./prepare.sh gearpump-2.11-0.7.4 gearpump-dashboard"
    exit 1
fi

GP_HOME=$1
DEST_DIR=$2

#set -x

if [ ! -d "$DEST_DIR" ]; then
  mkdir $DEST_DIR
fi

mkdir $DEST_DIR/bin
mkdir $DEST_DIR/lib

# copy dependencies
cp $GP_HOME/conf/* $DEST_DIR/lib
cp -r $GP_HOME/dashboard/* $DEST_DIR/lib
cp $GP_HOME/lib/* $DEST_DIR/lib
cp $GP_HOME/lib/daemon/* $DEST_DIR/lib
cp $GP_HOME/lib/services/* $DEST_DIR/lib
cp $GP_HOME/VERSION $DEST_DIR/lib/
cp $GP_HOME/VERSION $DEST_DIR/

# compute classpath
CP_STRING=""
JAR_PREFIX=\$APP_HOME/lib

for filename in $DEST_DIR/lib/*.jar; do
	CP_STRING+=$JAR_PREFIX/$(basename $filename):
done

CP_STRING+=$JAR_PREFIX:$JAR_PREFIX/*
echo $CP_STRING

#copy starting script
cp dashboard.sh $DEST_DIR/bin/dashboard

#set execution permissions
chmod 766 $DEST_DIR/bin/dashboard

#change CLASSPATH
sed -i "s|CLASSPATH=TOCHANGE|CLASSPATH=${CP_STRING}|" "$DEST_DIR/bin/dashboard"

#copy manifest
cp dashboard-manifest.yml $DEST_DIR/manifest.yml

cd $DEST_DIR/
#make zip in target directory
mkdir target/
zip -r target/gearpump-dashboard.zip bin/ lib/ VERSION
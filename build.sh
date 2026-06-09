#!/bin/sh
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

# This script do a full build of Studio (including the MANIFEST generation and the P2 local repository construction)

# Building requires Java 25 or 17
if [ uname -o =="Darwin" ]; then
    if /usr/libexec/java_home -v 25 -a $(uname -m) -F 2>/dev/null; then
        export JAVA_HOME=$(/usr/libexec/java_home -v 25 -a $(uname -m) -F | head -n1)
    elif /usr/libexec/java_home -v 17 -a $(uname -m) -F 2>/dev/null; then
        export JAVA_HOME=$(/usr/libexec/java_home -v 17 -a $(uname -m) -F | head -n1)
    else
        echo "Native JDK 25 or 17 not found, trying to build with default JDK..."
    fi 
fi

mvn -f pom-first.xml clean install 
mvn -f pom.xml clean install -Djdk.xml.maxGeneralEntitySizeLimit=0 -Djdk.xml.totalEntitySizeLimit=0 -Djdk.xml.entityExpansionLimit=0

if [[ $1 =~ --datestamp ]] ; then
    for f in product/target/products/ApacheDirectoryStudio-*-SNAPSHOT-*; do 
        mv -v $f ${f/-SNAPSHOT/.v$(date +%Y%m%d)}; 
    done
fi

# build disk images for macOS
cd installers/macos/src/dmg/
# This creates unsigned DMGs, users might have to approve running/installing Studio
# add --sign <key> to codesign the app bundles
./createDMG.sh
cd -

# print build artifacts
ls -l product/target/products/ApacheDirectoryStudio-*
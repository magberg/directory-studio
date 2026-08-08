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

# Full Studio build: bootstrap .target (Tycho 5), MANIFEST/P2 first phase, then Tycho.

ROOT="$(cd "$(dirname "$0")" && pwd)"
"$ROOT/tools/bootstrap-target.sh"
mvn -f "$ROOT/pom-first.xml" clean install || exit $?

# Main TP pom disables default install; publish .target into local m2 so Tycho
# (and partial -pl builds) can resolve org.apache.directory.studio.eclipse-trgt-platform:target.
TARGET_FILE="$ROOT/eclipse-trgt-platform/org.apache.directory.studio.eclipse-trgt-platform.target"
if [ -f "$TARGET_FILE" ]; then
  mvn install:install-file \
    -Dfile="$TARGET_FILE" \
    -DgroupId=org.apache.directory.studio \
    -DartifactId=org.apache.directory.studio.eclipse-trgt-platform \
    -Dversion=3.0.0-SNAPSHOT \
    -Dpackaging=target \
    -DgeneratePom=false \
    -DpomFile="$ROOT/eclipse-trgt-platform/pom.xml" || exit $?
fi

mvn -f "$ROOT/pom.xml" clean install

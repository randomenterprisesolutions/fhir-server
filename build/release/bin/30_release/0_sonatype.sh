#!/usr/bin/env bash

set -eu -o pipefail

###############################################################################
# (C) Copyright IBM Corp. 2020, 2021
# (C) Copyright randomenterprisesolutions 2022, 2026
#
# SPDX-License-Identifier: Apache-2.0
###############################################################################

# Publishing to Maven Central has been removed.
# This project is no longer deployed to Sonatype OSSRH / Maven Central.
# To install artifacts locally, use:
#
#   mvn install -f fhir-examples -DskipTests
#   mvn install -f fhir-parent -DskipTests

echo "Maven Central publishing is not configured for this fork."
echo "Use 'mvn install' to install artifacts to your local repository."

# EOF
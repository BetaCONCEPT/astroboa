#!/bin/bash
mvn clean package
rm -f /opt/jboss-as-7.1.0.Final/standalone/deployments/astroboa.ear*
cp -v target/astroboa.ear /Users/savvas/Projects/eclipse/workspace-3.7.x/astroboa-jboss-bundle-distribution/target/astroboa-4.0.0-SNAPSHOT/astroboa-4.0.0-SNAPSHOT/torquebox-2.0.0.cr1/jboss/standalone/deployments

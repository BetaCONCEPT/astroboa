#!/bin/bash
mvn clean package
rm -f /opt/jboss-as-7.1.0.Final-SNAPSHOT/standalone/deployments/astroboa.ear*
cp target/astroboa.ear /opt/jboss-as-7.1.0.Final-SNAPSHOT/standalone/deployments/

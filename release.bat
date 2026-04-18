@echo off
mvn -T 1 -B -P+release clean release:clean release:prepare-with-pom release:perform
#!/usr/bin/env bash

gradle -b server/build.gradle clean build publishToMavenLocal
gradle -b fake-server/build.gradle bootRun
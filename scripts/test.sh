#!/usr/bin/env bash
# Compile everything and run the backend test suite (Linux / macOS).
set -e
cd "$(dirname "$0")/.."
mkdir -p out
javac -d out src/atm/*.java test/atm/BackendTests.java
java -cp out atm.BackendTests

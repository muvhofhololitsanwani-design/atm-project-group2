#!/usr/bin/env bash
# Compile and start the ATM (Linux / macOS). Needs JDK 17 or newer.
set -e
cd "$(dirname "$0")/.."
mkdir -p out
javac -d out src/atm/*.java
java -cp out atm.ATMApp

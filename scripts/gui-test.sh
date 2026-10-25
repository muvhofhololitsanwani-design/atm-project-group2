#!/usr/bin/env bash
# Run the robot-style GUI test (needs a display; on a server use: xvfb-run -a ./scripts/gui-test.sh).
set -e
cd "$(dirname "$0")/.."
mkdir -p out docs/screenshots
javac -d out src/atm/*.java test/atm/*.java
java -cp out atm.GuiWalkthrough

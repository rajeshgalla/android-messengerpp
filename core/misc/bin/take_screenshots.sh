#!/bin/bash

apk_file=$1

scale=0.51

names=(Android-2.3 Nexus-4 Nexus-7)
for name in ${names[*]}
do
     $ANDROID_HOME/tools/emulator -ports 5580,5581 -avd $name -scale $scale &
     sleep 10
     $ANDROID_HOME/tools/monkeyrunner ./take_screenshots.py ../screenshots-new $name $apk_file
     $ANDROID_HOME/platform-tools/adb -s emulator-5580 emu kill
     sleep 3
done
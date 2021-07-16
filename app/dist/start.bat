@echo off
adb push classes.dex /data/local/tmp
echo DSUDaemon is now running in the background on your, you can close the window now.
echo PS. If you want to kill the daemon do this: adb shell "kill $(cat /data/local/tmp/dsu_pid)"
echo PPS. Launching start.bat again will also kill the daemon
adb shell "nohup app_process -Djava.class.path=/data/local/tmp/classes.dex /system/bin dsudaemon.Main &"
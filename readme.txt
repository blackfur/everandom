adb install app-release.apk

adb devices
adb shell
cd /data/data
sqlite3 <db>
.help

adb shell netcfg
ip addr
ip addr show wlan0
ip route

apachectl restart
service mysql start

CREATE TABLE APP_NOTES(ID VARCHAR(16), TIMESTAMP VARCHAR(32), TXT VARCHAR(2048), STATUS VARCHAR(4));

* install
  http://192.168.128.101/phpnotes/dist/release/

emulator.sh -avd iwill
adb uninstall sample.note
./gradlew installRelease
adb shell am start -n sample.note/.MainActivity
./gradlew assemble

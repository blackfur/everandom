set -x

./gradlew assembleRelease
#docker cp /home/iwill/Desktop/dist 9b:/var/www/htdocs/phpnotes/
#sudo cp -rf /home/iwill/Desktop/dist /srv/http/phpnotes/
sudo cp -rf ./app/build/outputs/apk/release /srv/http/phpnotes/dist/

sudo systemctl restart httpd.service
#xdg-open http://localhost/phpnotes/qrcode.php &

timestamp=`date +"%s"`
xdg-open http://localhost/phpnotes/images/app-release.png?t=$timestamp &

#/bin/bash

result=`adb shell pm path com.example.todesk`
echo $result
if [[ -n "$result" ]];then
    echo "The APK has been installed"
else
    echo "The APK is not installed"
    result=`adb install app-release.apk`
    echo $result
    if [[ "$result" =~ "Success" ]];then
        echo "The APK is installed successfully"
    else
        echo "APK installation failed"
        exit
    fi
fi

array=(${result//:/ })
base_apk=${array[1]}
echo $base_apk
adb forward tcp:56789 tcp:56789
adb shell "export CLASSPATH=$base_apk;exec app_process /system/bin com.example.todesk.Main"
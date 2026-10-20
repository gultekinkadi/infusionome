#!/usr/bin/env bash
# Emülatörde uygulamayı açıp ekran görüntüsü alır. Çıktı: shots/
set -x
mkdir -p shots
PKG=com.hk.dropapp
adb wait-for-device
adb shell settings put global window_animation_scale 0
adb shell settings put global transition_animation_scale 0
adb shell settings put global animator_duration_scale 0
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell cmd locale set-app-locales $PKG --locales tr || true

shot() { sleep "${2:-3}"; adb exec-out screencap -p > "shots/$1.png"; }

center() { # resource-id -> "x y"
  adb shell uiautomator dump /sdcard/ui.xml >/dev/null
  adb exec-out cat /sdcard/ui.xml > /tmp/ui.xml
  python3 - "$1" <<'PY'
import re,sys
s=open('/tmp/ui.xml',encoding='utf-8').read()
m=re.search(r'resource-id="com\.hk\.dropapp:id/'+sys.argv[1]+r'"[^>]*bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"',s)
if m:
    a,b,c,d=map(int,m.groups()); print((a+c)//2,(b+d)//2)
PY
}
typein() { xy=$(center "$1"); [ -n "$xy" ] && adb shell input tap $xy && sleep 1 && adb shell input text "$2"; }

adb shell am start -n $PKG/.Activity.MainActivity
shot 1-ana-ekran-bos 6

typein main_sivi_hacmi 250
typein main_ilac_miktari 400
typein main_doz 5
typein main_kilo 70
adb shell input keyevent 4   # klavyeyi kapat
shot 2-ana-ekran-dolu 2

# Damla faktörü listesi açık hali
xy=$(center main_doz_birim); [ -n "$xy" ] && adb shell input tap $xy && shot 3-doz-birimleri 2 && adb shell input keyevent 4
sleep 1

xy=$(center main_calculate); adb shell input tap $xy
shot 4-sonuc 2
shot 5-sonuc-damla-anı 0.4

# Mikroset (60) ile normal hızda sonuç: geri dön, damla faktörünü 60 yap
adb shell input keyevent 4; sleep 2
xy=$(center main_drop); adb shell input tap $xy; sleep 2
adb shell uiautomator dump /sdcard/ui.xml >/dev/null; adb exec-out cat /sdcard/ui.xml > /tmp/ui.xml
xy=$(python3 -c "
import re;s=open('/tmp/ui.xml',encoding='utf-8').read()
m=re.search(r'text=\"60\"[^>]*bounds=\"\[(\d+),(\d+)\]\[(\d+),(\d+)\]\"',s)
print((int(m[1])+int(m[3]))//2,(int(m[2])+int(m[4]))//2) if m else print('')")
[ -n "$xy" ] && adb shell input tap $xy
sleep 1
xy=$(center main_calculate); adb shell input tap $xy
shot 6-sonuc-mikroset 2

# İngilizce + menü
adb shell input keyevent 4; sleep 1
adb shell input keyevent 82; shot 7-menu 2; adb shell input keyevent 4
adb shell cmd locale set-app-locales $PKG --locales en || true
adb shell am force-stop $PKG; adb shell am start -n $PKG/.Activity.MainActivity
shot 8-ingilizce 5
ls -la shots

#!/usr/bin/env bash
# يجلب مكتبة ics-openvpn ويحوّلها إلى مكتبة قابلة للتضمين
set -e
git clone --recursive https://github.com/schwabe/ics-openvpn.git
cd ics-openvpn/main
# تحويل الوحدة من application إلى library
sed -i 's/com.android.application/com.android.library/' build.gradle* || true
sed -i '/applicationId/d' build.gradle* || true
echo "تم. إن ظهرت أخطاء بناء راجع إعدادات flavors في ics-openvpn/main/build.gradle"

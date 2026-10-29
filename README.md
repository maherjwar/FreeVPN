# FreeVPN (بلا إعلانات)

1. شغّل `./setup.sh` لجلب مكتبة ics-openvpn.
2. افتح المجلد في Android Studio ثم Sync وRun.
3. التطبيق يجلب سيرفرات VPN Gate تلقائيًا ويعرضها حسب الدولة.

ملاحظة: سيرفرات VPN Gate يديرها متطوعون وقد تسجّل الحركة، فلا تستخدمها للمعاملات الحساسة.

## الحصول على APK بلا Android Studio
1. أنشئ مستودعًا على GitHub وارفع محتويات هذا المجلد (فرع main).
2. تبويب Actions ← Build APK ← Run workflow.
3. بعد انتهاء البناء نزّل الملف FreeVPN-apk من قسم Artifacts، ثم فك الضغط وثبّت ملف الـ APK.

#!/usr/bin/env bash
# يجلب ics-openvpn (إصدار v0.7.53 المتوافق مع Gradle 8.7) ويدمج واجهة FreeVPN فيه
set -e
cd "$(dirname "$0")"
if [ ! -d ics-openvpn ]; then
  git clone --depth 1 --branch v0.7.53 --recursive --shallow-submodules \
    https://github.com/schwabe/ics-openvpn.git ics-openvpn
fi
M=ics-openvpn/main

# 1) نسخ الكود
mkdir -p $M/src/skeleton/java/com/freevpn/app
cp sources/*.kt $M/src/skeleton/java/com/freevpn/app/

# 2) تسجيل الواجهة كشاشة رئيسية
if ! grep -q "com.freevpn.app.MainActivity" $M/src/skeleton/AndroidManifest.xml; then
  sed -i '/<\/application>/i\
        <activity\
            android:name="com.freevpn.app.MainActivity"\
            android:exported="true"\
            android:label="FreeVPN"\
            android:theme="@android:style/Theme.Material.Light.NoActionBar">\
            <intent-filter>\
                <action android:name="android.intent.action.MAIN" />\
                <category android:name="android.intent.category.LAUNCHER" />\
            </intent-filter>\
        </activity>' $M/src/skeleton/AndroidManifest.xml
fi

# 3) تفعيل Compose داخل وحدة main
if ! grep -q "compose-bom" $M/build.gradle.kts; then
  sed -i 's/^        buildConfig = true/        buildConfig = true\n        compose = true/' $M/build.gradle.kts
  sed -i '/^    kotlinOptions {/i\
    composeOptions {\
        kotlinCompilerExtensionVersion = "1.5.10"\
    }\
' $M/build.gradle.kts
  sed -i '/^    implementation(libs.androidx.core.ktx)/a\
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))\
    implementation("androidx.compose.material3:material3")\
    implementation("androidx.compose.ui:ui")\
    implementation("androidx.activity:activity-compose:1.9.1")\
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")\
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.4")\
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")' $M/build.gradle.kts
fi
echo "تم. افتح المجلد ics-openvpn في Android Studio واختر Build Variant: skeletonOvpn23Debug"

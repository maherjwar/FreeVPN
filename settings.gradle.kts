pluginManagement {
    repositories { google(); mavenCentral(); gradlePluginPortal() }
}
dependencyResolutionManagement {
    repositories { google(); mavenCentral() }
}
rootProject.name = "FreeVPN"
include(":app")

// مكتبة OpenVPN (يضيفها setup.sh)
include(":openvpn")
project(":openvpn").projectDir = file("ics-openvpn/main")

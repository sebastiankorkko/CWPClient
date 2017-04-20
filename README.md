# README #

### What is this repository for? ###

* CWP Client application
* Student: Sebastian Körkkö (2003063) - korkkosebastian@gmail.com

### How do I get set up? ###

* Summary of set up

1. Import this repository to Android Studio (tested on 2.3.1)
2. Build with Gradle (AndroidSDK 25.0.2, min 15)
3. Tested on embedded AVD emulator device:
     * Nexus 5X API 25 x86, 1080 x 1920: 420dpi, 
     * Target: Android 7.1.1(Google APIs), Size: 2GB

This repository has the gradle scripts included (use or don't).

No external dependencies apart from Android and JUnit (currently not used)

### Current features ###

* User interface is implemented to some extent:
     * Connect-button activates the tapping fragment
     * Tapping fragment reacts to updates and passes lineup/linedown-calls to controller
     * Preferences/settings implemented
     * Protocol changes shown on log (and by switching the light on the tapping image)
     * Main layout implemented with constraints
* Threading implemented (todo: locking & sephamores)
* Initial barebones for networking implemented (does not fully work atm)

### Under consideration ###

* Sliding interface does not make sense at the moment. I'm working on bringing the server properties over from the second page to the main page and to be together with the connect-button. This makes sense from UI-point of view.
# README #

### What is this repository for? ###

* CWP Client application
* Student: Sebastian Körkkö (2003063) - korkkosebastian@gmail.com

### How do I get set up? ###

* Summary of set up

1. Import this repository to Android Studio (tested on 2.3.1)
2. Build with Gradle (AndroidSDK 25.0.2, min 15)
3. Tested on embedded AVD emulator device running API 23 (Has issues on API 25)
    
This repository has the gradle scripts included (use or don't).

No external dependencies apart from Android and JUnit (currently not used)

### Current features ###

* User interface is implemented:
     * All required pieces are working and responsive
     * Preferences/settings implemented
     * Main layout implemented with CoordinatorLayout and fragments
* Service, service connection and notifications all working
* Threading implemented (receiving data)

### Issues ###

* Currently no bigger issues, but summary of my hurdles and minor problems:
     * Switched ConstraintLayout to CoordinatorLayout. Didn't fix the connectivity issue (originally I thought it was internal issue). Shame on me for not using enough debug-messages.
     * Issue with networking in API 25, have not had the time to fix it by implementing separate threading for sending data.
     * Random issue, where the top bar and the menu-button went missing when coming back to the app from the OS. Was unable to replicate the issue with consistency and may be present.
* I was running out of time so some aspects are lacking as I prioritized the functionality of the program over documentation (code commenting) and textual UI notifications.

### fix-review -notes ###

- Fixed all issues that were mentioned in the comments. It could be due to fixing few of the other things, but I was not able to replicate the issue with the changed protocol state-issue.
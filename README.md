# aCalDAV
CalDAV Sync Adapter for Android
forked from [gggard/AndroidCaldavSyncAdapter](https://github.com/gggard/AndroidCaldavSyncAdapater)
See wiki for more information and server compatibility list (https://github.com/gggard/AndroidCaldavSyncAdapater/wiki)

# Contents
Synchronize your Android phone with a CalDAV server like [Baïkal](http://baikal-server.com/)

# Please note
Currently, the active development is done in the fork [eenswi/AndroidCaldavSyncAdapter](https://github.com/ennswi/AndroidCaldavSyncAdapater)
Please report any issues there.

# Build
## Configure release
1. Create `gradle.properties` from template `_gradle.properties`
2. Modify `gradle.properties` with your settings

## Enable Checkstyle, PMD, Findbugs
Uncomment 

    #apply from: '../config/quality/quality.gradle'
    
from aCalDAV/build.gradle
Reports should be in `aCalDAV/build/reports`


## Building the project
Requirements: 
* Android SDK
* Gradle
* Android Studio (optional)

`apk` files should be placed in `aCalDAV/build/outputs/apk`

### Using Gradle
Navigate to the project root and run 

    'gradle build' for release

    'gradle packageDebug' for debug release
    
## Using AndroidStudio
Choose Import Project, choose ACalDAV Folder.
If you get an error about unregistered Git Root, select Add Git root.
More information on http://developer.android.com/sdk/installing/studio-build.html

## Acknowledgements
This projects uses some open source projects like:
* [backport-util-concurrent](http://sourceforge.net/projects/backport-jsr166/)
* [commons-codec](http://commons.apache.org/proper/commons-codec/)
* [commons-lang](http://commons.apache.org/proper/commons-lang/)
* [ical4j](http://build.mnode.org/projects/ical4j/)

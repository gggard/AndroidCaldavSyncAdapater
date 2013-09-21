# AndroidCaldavSyncAdapater

CalDAV Sync Adapter for Android

See wiki for more information and server compatibility list (https://github.com/gggard/AndroidCaldavSyncAdapater/wiki)

## Building the project

### Using eclipse
Choose: Import -> Android -> Existing Android Code Into Workspace

### Using ant
list available android sdk versions (targets):

    android list | grep -E '^id'

if you need to download more sdk versions:

    android

use one of the above "targets" below:

    cd CalDAVSyncAdapter
    android update project --path . --target android-18
    ant debug

check `bin/CalDAVSyncAdapter-debug.apk`

### Using Android Studio

Choose Import Project, choose CalDAVSyncAdapter Folder.

Select "Create project from existing sources". Next...

If you get an error about unregistered Git Root, select Add Git root.


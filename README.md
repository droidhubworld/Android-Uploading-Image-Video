# Android Uploading Image, Video
## Android Uploading image, video, files

### Screens
![1](https://user-images.githubusercontent.com/10918083/33470909-cb756658-d690-11e7-8a56-a0471befc6e8.png) ![2](https://user-images.githubusercontent.com/10918083/33470917-da136f5c-d690-11e7-9d88-eea9e33be1d6.png)

## Stap 1 : We need to app two permission in the manifest file to first one is Internet and second is read External Storage.

    <uses-permission android:name="android.permission.INTERNET"/>
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/> 
    
## Stap 2 : We need to and thes on bulid gradle

  packagingOptions {
        exclude 'META-INF/DEPENDENCIES'
        exclude 'META-INF/LICENSE'
        exclude 'META-INF/LICENSE.txt'
        exclude 'META-INF/license.txt'
        exclude 'META-INF/NOTICE'
        exclude 'META-INF/NOTICE.txt'
        exclude 'META-INF/notice.txt'
        exclude 'META-INF/ASL2.0'
    }
    
  compile group: 'org.apache.httpcomponents', name: 'httpmime', version: '4.3.1'

## Stap 3 : Now follow the code.

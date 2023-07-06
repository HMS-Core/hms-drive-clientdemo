# HMS Drivekit Clientdemo

[![License](https://img.shields.io/badge/Docs-hmsguides-brightgreen)](https://developer.huawei.com/consumer/cn/doc/development/HMS-Guides/drivekit-introduction)  ![Apache-2.0](https://img.shields.io/badge/license-Apache-blue)

English | [中文](https://github.com/HMS-Core/hms-drive-clientdemo/blob/master/README_ZH.md)

## Table of Contents

 * [Introduction](#introduction)
 * [Getting Started](#getting-started)
 * [Configuration](#configuration )
 * [Supported Environments](#supported-environments)
 * [Sample Code](#sample-code)
 * [License](#license)


## Introduction   
 This sample code encapsulates APIs of the HUAWEI Drive Kit SDK. It provides many sample programs for your reference or usage. You will learn how to use Drive Kit SDK.   
 The following describes packages of Java sample code.   
 
 app:   Sample code packages.   
 
 <img src="driveDemo.jpg" width = 30% height = 30%>

## Getting Started    
 For more development details, please refer to the following link:   
 https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/introduction-0000001050039630   

 Before using HUAWEI Drive Kit SDK sample code, check whether the JAVA environment has been installed.    
 
 Clone the sample code to local.    
 Take the Android Studio 3.2 version as an example. The steps to run the Drive service sample code are as follows:    
 1. You should create an app in AppGallery Connect, and obtain the file of agconnect-services.json and add to the project.   
 2. You should also generate a signing certificate fingerprint and add the certificate file to the project, and add configuration to build.gradle.   
 See the [Configuring App Information in AppGallery Connect](https://developer.huawei.com/consumer/en/doc/development/HMS-Guides/drivekit-devpreparations) guide to configure app in AppGallery Connect.   
 3. In Android Studio, select "File"->"Open". In the pop-up dialog box, enter the path where the sample code is stored locally, for example: "D:\HuaweiDriveSDK\samples\HuaweiDriveSample-4.0.3.300";
 4. Select the HuaweiDriveSample project to be opened, and then click "OK". In the pop-up dialog box, select "New Window" to open the project in a new window.  
 5. In Android Studio, click "Run", then select your device as the target and click "OK" to launch the sample application on your device.  
 
## Configuration
 To use functions provided by packages in examples, you do not need to set any related parameters in build.gradle and agconnect-services.json in the app package   
 The following describes parameters in build.gradle and agconnect-services.json   
 
 applicationId: ID of the application registered on the HUAWEI Developer.   
 app_id: App ID, which is obtained from app information.
 
 Devices to be tested: Huawei phones running EMUI 3.0 or later or non-Huawei phones running Android 8.1 or later.
 If multiple HMS Core services are used at the same time, the maximum value of each Kit must be used.
 
## Supported Environments   
 Java 1.8 or a later version is recommended.  
 Android Studio 3.2 version or a later version is recommended.   
 
 Your application should meet the following conditions:
 minSdkVersion 29 
 targetSdkVersion 33 
 compileSdkVersion 30 
 Gradle 3.5.4 and later.
 
## Sample Code  
 
 Currently, the core capabilities of Drive Kit include uploading, downloading, deleting, trashing, and searching for files in Drive as well as querying and monitoring file changes. 
 1. Obtain parameters in the Main function of each Java file in advance. For example, obtain the AT through the HMS SDK.   
 2. Call the required APIs. Some APIs depend on each other, for example:    

- Before calling `FILES.CREAEFILE`, you need to call `FILES.CREATE`.    
- Before calling `FILES.GET,FILES.SUBSCRIBE,FILES.COPY,FILES.UPDATE,FILES.UPDATECONTENT,CHANGES.SUBSCRIBE,COMMENTS.CREATE,COMMENTS.LIST`, you need to call `FILES.CREATE and FILES.CREATEFILE`.    
- Before calling `CHANGES.LIST`, you need to call `CHANGES.GETSTARTCURSOR`.    
- Before calling `CHANNELS.STOP`, you need to call `CHANGES.SUBSCRIBE`.    
- Before calling `COMMENTS.GET`, you need to call `COMMENTS.CREATE`.    
- Before calling `Replies.CREATE`, you need to call `COMMENTS.CREATE`.    

## Question or issues
If you want to evaluate more about HMS Core,
[r/HMSCore on Reddit](https://www.reddit.com/r/HuaweiDevelopers/) is for you to keep up with latest news about HMS Core, and to exchange insights with other developers.

If you have questions about how to use HMS samples, try the following options:
- [Stack Overflow](https://stackoverflow.com/questions/tagged/huawei-mobile-services) is the best place for any programming questions. Be sure to tag your question with 
`huawei-mobile-services`.
- [Huawei Developer Forum](https://forums.developer.huawei.com/forumPortal/en/home?fid=0101187876626530001) HMS Core Module is great for general questions, or seeking recommendations and opinions.

If you run into a bug in our samples, please submit an [issue](https://github.com/HMS-Core/hms-drive-clientdemo/issues) to the Repository. Even better you can submit a [Pull Request](https://github.com/HMS-Core/hms-drive-clientdemo/pulls) with a fix.

## License
 DriveKit SDK sample is licensed under the [Apache License, version 2.0](http://www.apache.org/licenses/LICENSE-2.0).  

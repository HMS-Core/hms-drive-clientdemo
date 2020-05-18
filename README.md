## HUAWEI DriveKitSDK-Java-sample


## Table of Contents

 * [Introduction](#introduction)
 * [Getting Started](#Getting Started)
 * [Configuration ](#configuration )
 * [Supported Environments](#supported-environments)
 * [Sample Code](# Sample Code)
 * [License](#license)


## Introduction   
 This sample code encapsulates APIs of the HUAWEI Drive Kit SDK. It provides many sample programs for your reference or usage.    
 The following describes packages of Java sample code.   
 
 app:   Sample code packages.   
 
## Getting Started    
 For more development details, please refer to the following link:   
 https://developer.huawei.com/consumer/en/doc/development/HMS-Guides/drivekit-introduction   

 Before using HUAWEI Drive Kit SDK sample code, check whether the JAVA environment has been installed.    
 
 Decompress the sample code package "HuaweiDriveSample-4.0.3.300.zip".    
 Take the Android Studio 3.2 version as an example. The steps to run the Drive service sample code are as follows:    
 1. You should create an app in AppGallery Connect, and obtain the file of agconnect-services.json and add to the project.   
 2. You should also generate a signing certificate fingerprint and add the certificate file to the project, and add configuration to build.gradle.   
 See the [Configuring App Information in AppGallery Connect](https://developer.huawei.com/consumer/en/doc/development/HMS-Guides/drivekit-devpreparations) guide to configure app in AppGallery Connect.   
 3. In Android Studio, select "File"->"Open". In the pop-up dialog box, enter the path where the sample code is stored locally, for example: "D:\HuaweiDriveSDK\samples\HuaweiDriveSample-4.0.3.300";
 4. Select the HuaweiDriveSample project to be opened, and then click "OK". In the pop-up dialog box, select "New Window" to open the project in a new window.  
 5. In Android Studio, click "Run", then select your device as the target and click "OK" to launch the sample application on your device.  


## Supported Environments   
 Java 1.8 or a later version is recommended.  
 Android Studio 3.2 version or a later version is recommended.   
 
## Configuration
 To use functions provided by packages in examples, you do not need to set any related parameters in build.gradle and agconnect-services.json in the app package   
 The following describes parameters in build.gradle and agconnect-services.json   
 
 applicationId: ID of the application registered on the HUAWEI Developer.   
 app_id: App ID, which is obtained from app information.
 
## Sample Code  
 
 Currently, the core capabilities of Drive Kit include uploading, downloading, deleting, trashing, and searching for files in Drive as well as querying and monitoring file changes. 
 1. Obtain parameters in the Main function of each Java file in advance. For example, obtain the AT through the HMS SDK.   
 2. Call the required APIs. Some APIs depend on each other, for example:    
    Before calling FILES.CREAEFILE, you need to call FILES.CREATE.    
    Before calling FILES.GET,FILES.SUBSCRIBE,FILES.COPY,FILES.UPDATE,FILES.UPDATECONTENT,CHANGES.SUBSCRIBE,COMMENTS.CREATE,COMMENTS.LIST ,you need to call FILES.CREATE and FILES.CREATEFILE
    Before calling CHANGES.LIST, you need to call CHANGES.GETSTARTCURSOR
    Before calling CHANNELS.STOP, you need to call CHANGES.SUBSCRIBE
	Before calling COMMENTS.GET, you need to call COMMENTS.CREATE
	Before calling Replies.CREATE, you need to call COMMENTS.CREATE

 
## License
 DriveKit SDK sample is licensed under the [Apache License, version 2.0](http://www.apache.org/licenses/LICENSE-2.0).  
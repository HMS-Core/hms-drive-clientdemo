# 华为云空间服务客户端示例代码

[![License](https://img.shields.io/badge/Docs-hmsguides-brightgreen)](https://developer.huawei.com/consumer/cn/doc/development/HMS-Guides/drivekit-introduction)  ![Apache-2.0](https://img.shields.io/badge/license-Apache-blue)
  
中文 | [English](https://github.com/HMS-Core/hms-drive-clientdemo/blob/master/README.md)

## Table of Contents

 * [简介](#简介)
 * [开发准备](#开发准备)
 * [配置](#配置)
 * [环境要求](#环境要求)
 * [示例代码](#示例代码)
 * [授权许可](#授权许可)

## 简介   
 该示例代码封装华为云空间服务SDK的接口，提供多个示例程序供你参考或使用。你将学习如何使用云空间服务SDK。  
 如下是Java示例代码包。
 
 app：示例代码包。
 
 <img src="driveDemo.jpg" width = 30% height = 30%>

## 开发准备   
 更多开发详情，请参见如下链接：
 https://developer.huawei.com/consumer/en/doc/development/HMS-Guides/drivekit-introduction   

 使用华为云空间服务SDK示例代码前，需检查Java环境是否已经安装。
 将示例代码复制到本地。
 以Android Studio 3.2为例。云空间服务示例代码的运行步骤如下：
 1.	在AppGallery Connect中创建应用，获取agconnect-services.json文件，并将文件添加到你的项目中。  
 2.	你还需生成签名证书指纹并将证书文件添加到你的项目中，然后在build.gradle文件中添加配置。  
 请参见“AppGallery Connect配置”在AppGallery Connect中配置应用信息。  
 3.	在Android Studio中，选择File > Open。在弹框中，输入示例代码的本地存储路径，例如D:\HuaweiDriveSDK\samples\HuaweiDriveSample-4.0.3.300。  
 4.	选中待打开的HuaweiDriveSample项目，然后点击“确认”。在弹框中，选择New Window在新窗口中打开项目。  
 5.	在Android Studio中，点击“Run”，选择你的设备作为目标设备，点击“确认”在你的设备上启动示例应用。  

## 配置
 build.gradle和agconnect-services.json文件中参数如下所示：  
 
 applicationId：在华为开发者联盟上注册的申请ID。  
 app_id：应用ID，从应用信息中获取。

## 环境要求   
 推荐使用Java 1.8及以上版本。  
 推荐使用Android Studio 3.2及以上版本。

## 示例代码  
 目前，云空间服务的核心能力包括文件上传、下载、删除、丢弃和查询，以及文件变更的查询和监控。
 1.	提前获取每个Java文件中主要功能的参数。例如，通过HMS SDK获取Access Token.
 2.	调用所需的接口。一些接口彼此存在依赖关系，例如：
  调用FILES.CREAEFILE接口前，你需要调用FILES.CREATE接口。  
  调用FILES.GET、FILES.SUBSCRIBE、FILES.COPY、FILES.UPDATE、FILES.UPDATECONTENT、CHANGES.SUBSCRIBE、COMMENTS.CREATE、COMMENTS.LIST接口前，你需要调用FILES.CREATE和FILES.CREATEFILE接口。  
  调用CHANNELS.STOP接口前，你需要调用CHANGES.SUBSCRIBE接口。  
  调用COMMENTS.GET接口前，你需要调用COMMENTS.CREATE接口。  
  调用Replies.CREATE接口前，你需要调用COMMENTS.CREATE接口。  

## 授权许可
 云空间服务SDK示例代码经过[Apache License, version 2.0](http://www.apache.org/licenses/LICENSE-2.0)授权许可.
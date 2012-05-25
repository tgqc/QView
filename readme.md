---
!!News

**QView v0.6 preview distribution is up!**
Improvements include accurate peer map model, new MQ network reports menu, various bug fixes.
To Download, select the download tab (https://github.com/tgqc/QView/downloads) and download 'qview_v0.6_preview.zip'. 
Unzip to to any directory, such as'Program Files'. kick off qview/bin/qview.exe to test drive.

---


![QView](https://github.com/tgqc/QView/raw/master/qview_icon.jpg)   QView 
=====


##Welcome to QView##



QView is an MQ network explorer, mapping tool and admin console.
It has some very powerful features, such as the ability to invoke a runmqc like interface on a qmgr node remotely at the click of a button.

[![QView explorer](https://github.com/tgqc/QView/raw/master/qview_screenshot.jpg)] 

###What does QView do?
Qview:

* Automagically **maps your MQ network**, including queue and channel objects.
* Displays an interactive and **printable tree view** of your network.
* Provides **configurable MONITORING** including queue depth and channel status, with sensible defaults.
* Provides a One-click **EDITABLE list of attributes** for each of your MQ objects.
* Provides a **‘runmqsc’ type interface** for each Queue Manger node at the click of a button.
* Provides 2 step **channel stop/start**, and **queue put and get disable/enable**.
* Will read and **parse messages** from and to a queue.
* Will **write back** or move read messages to any queue.

Much of the functionality could do with some polishing, and there is so much more we can do with the rich data model. 
See the roadmap section of the wiki for discussion on current effort and feature requests.

###QView is functional.

Some of these functions, such as reading and writing messages, are very rudimentary.

I have used the utility in my work as an MQ administrator, but it is still a bit rough around the edges. It is too large for one man alone to manage. There is great potential for this software, but WE NEED YOUR HELP to round out features and to log and address issues.

The utility is based on the Netbeans 6 platform, and to be honest, the implementation of platform components is still a bit hacky. Please help refactor changes if you know how things SHOULD be done.


###Getting started

####To use the tool
* Simply select the download tab (https://github.com/tgqc/QView/downloads) and download 'qview_explorer.zip'. 
* Unzip to to any directory, such as'Program Files'. kick off qview/bin/qview.exe to test drive.

Please report back on issues and feature requests.

####To make changes and contribute
* Download Git from http://git-scm.com/
* create and CD to a working directory
* type 'git init'
* type 'git remote add origin https://tgqc@github.com/tgqc/QView.git'
* type 'git pull origin master' ... the latest source will dowload.

Using Netbeans to code and test is not essential but recommended. Running the app from source is a one-click trick from the Netbeans IDE.

If you would like to be a regular contributer, let us know. mqsysadmin@gmail.com.

###Documentation and code commenting are sparse
The project was begun in a hurry. However the MVC structure is not too hard to navigate, and hopefully most code will be self explanatory. Please help out with documentation and code comments where you can, and email us if you need any help unthreading it.

###Too big?

The package is quite large, as it contains its own jre (1.5) for reasons of portability. This is probably not optimal. Drop the jre and use your own if you want a quicker download. If you think I should remove it, let us know.
The QView-suite folder is a drop-in netbeans project with a bunch of superfluous files. The guts of the project is in the QView-suite/MQProject folder, but as a Netbeans Platform application, the app is best navigated using the Netbeans IDE.

###A big thanks
* To Trung Nguyen for the use of and opportunity to disect his extensive original MQ PCF libraries.
* To the Netbeans project for their work on the platform (http://netbeans.org/features/platform/). A great way to get up and running quickly.
* To Chistophe Bouthier (http://christ.bouthier.free.fr/t) for his work on the hypertree library.


---
title: QView

description: QView MQ network explorer

platform: Java, Netbeans Platform

author: Tim Goodwill, mqsysadmin@gmail.com

tags: MQ, queue, MQ network, MQ administration, MQ explorer

created:  2006

uploaded: April 2012

---
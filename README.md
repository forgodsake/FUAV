# DroneKit-Android

![dronekit_python_logo](https://cloud.githubusercontent.com/assets/5368500/10805537/90dd4b14-7e22-11e5-9592-5925348a7df9.png)

[![Build Status](https://travis-ci.org/dronekit/dronekit-android.svg?branch=develop)](https://travis-ci.org/dronekit/dronekit-android)
[![Issue Stats](http://issuestats.com/github/dronekit/DroneKit-Android/badge/pr)](http://issuestats.com/github/dronekit/DroneKit-Android)
[![Issue Stats](http://issuestats.com/github/dronekit/DroneKit-Android/badge/issue)](http://issuestats.com/github/dronekit/DroneKit-Android) <a alt="Join the chat at https://gitter.im/dronekit/dronekit-android" href="https://gitter.im/dronekit/dronekit-android?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge"><img align="right" src="https://badges.gitter.im/Join%20Chat.svg"></img></a>


DroneKit-Android helps you create powerful Android apps for UAVs.


## Overview

[DroneKit-Android](http://android.dronekit.io/) is the implementation of [DroneKit](http://dronekit.io/) on Android.

The API provides interfaces that apps can use to control copters, planes, and ground vehicles. It is compatible with vehicles that communicate using the MAVLink protocol (including most vehicles made by 3DR and other members of the DroneCode foundation), and is validated against the open-source [ArduPilot flight control platform](ardupilot.com).

The project is made of two modules (most developers will use the Client library):
* [3DR Services App](https://github.com/DroidPlanner/DroneKit-Android/tree/develop/ServiceApp):
Provided as an apk through the [Google Play store](https://play.google.com/store/apps/details?id=com.fuav.android&hl=en), this is the layer performing direct
communication with the 3DR-powered vehicles.

* [DroneKit-Android Client library](http://android.dronekit.io):
Client library used by Android applications to leverage the functionality provided by the 3DR
Services layer.

## Getting Started

The [Getting Started](http://android.dronekit.io/getting_started.html) guide explains how to set up a DroneKit project in Android Studio, install the **3DRServices** app to your device, and start the autopilot simulator (SITL) for testing during development.

Once you've got DroneKit set up, the [First App: Hello Drone](http://android.dronekit.io/first_app.html) tutorial provides a step-by-step guide to creating a basic DroneKit-Android app and connecting it to a vehicle. 

### Examples

The [3DR Services](https://play.google.com/store/apps/details?id=com.fuav.android&hl=en) (Google Play Store) app displays a catalog of Android apps built with DroneKit.

Source code and documentation for some of these apps is linked below:
* [Tower](https://github.com/DroidPlanner/Tower)
* [Tower-Wear](https://github.com/DroidPlanner/tower-wear)
* [Tower-Pebble](https://github.com/DroidPlanner/dp-pebble) ([documentation here](http://android.dronekit.io/pebble_app.html))


## Resources

Project documentation is provided at http://android.dronekit.io/. This includes getting stated and tutorial material, and has has links to [examples](#examples) and the full [Javadoc](http://android.dronekit.io/javadoc/) API Reference.

The [DroneKit Forums](http://discuss.dronekit.io) are the best place to ask for technical support on how to use the library. You can also check out our [Gitter channel](https://gitter.im/dronekit/dronekit-android) though we prefer posts on the forums where possible.

* **Documentation:** http://android.dronekit.io/
  * **API Reference:** http://android.dronekit.io/javadoc/
  * **Examples:** http://android.dronekit.io/resources.html#example-apps
* **Forums:** [http://discuss.dronekit.io/](http://discuss.dronekit.io)
* **Gitter:** https://gitter.im/dronekit/dronekit-android (we prefer posts on the forums!)


## Users and contributors wanted!

We'd love your [feedback and suggestions](https://github.com/dronekit/dronekit-android/issues) about this API and are eager to evolve it to meet your needs, please feel free to create an issue to report bugs or feature requests.

We welcome all types of contributions but mostly contributions that would help us shrink our 
[issues list](https://github.com/dronekit/dronekit-android/issues).


## Licence

[DroneKit-Android Client library](http://android.dronekit.io) is made available under the permissive open source [Apache 2.0 License](https://github.com/dronekit/dronekit-android/blob/develop/ClientLib/LICENSE). 

[3DR Services App](https://github.com/DroidPlanner/DroneKit-Android/tree/develop/ServiceApp) is made available under the open source [GPL version 3](https://github.com/dronekit/dronekit-android/blob/develop/ServiceApp/LICENSE.md) licence.


***

Copyright 2015 3D Robotics, Inc.

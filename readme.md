# Tinge
Tinge is an Android app for controlling the Philips Hue lighting system.

Tinge brings the vibrancy of your lighting setup to your phone screen
with a crisp material design inspired UI, and features innovative ways
of adjusting the properties of multiple lights at once.


### Making Tinge Beautiful

Right from inception, Tinge is being engineered with aesthetics and user
experience at the forefront. It has been moulded around the material
design philosophy.

Core to this experience, the top toolbar has been designed as a vibrant
and responsive glass sheet, with a stained-glass effect to reflect the
user's hue ecosystem.

![](/documentation/assets/glass_toolbar_1.png)|![](/documentation/assets/glass_toolbar_2.png)|![](/documentation/assets/glass_toolbar_animation.gif)
| ------------- | ------------- | ------------- |


### Making Tinge Functional

One of the key usability features of Tinge is the ability to easily
shift the properties of several lights at the same time. 

Tinge features an overhaul of the standard Android SeekBar. This can be
used for adjusting the properties of single or multiple lights, with the
ability to merge and unmerge lights.

![](/documentation/assets/slider_moving_animation.gif)|![](/documentation/assets/slider_grouping_animation.gif)|![](/documentation/assets/slider_group_popup.png)
| ------------- | ------------- | ------------- |


### Making Tinge Powerful

Philips Hue allows the colours of smart lights to be wirelessly set. The
underlying API provides a lot more functionality than this though; with
the ability to setup complex rules, schedules and variables. This allows
for almost boundless custom functionality.

Current apps don't make use of this functionality to anywhere near its
full potential. Long term, Tinge will change that.

#### The grand plan

Ultimately, Tinge will provide an intuitive visual interface to unleash
the capabilities currently trapped beneath the hood of the Hue platform.

This would allow advanced users to view and define rules to achieve any
functionality they desire.

Moving on even further, Tinge will implement a visual flowchart that
shows the entire state of all rules and sensors on the hue platform.
This would analyse the entire system and build up a graph showing how
each component flows into one another.


#### Digging solid foundations

The precursor to implementing complex functionality like this is
architecturing solid foundations to build on.

This is being done by building a new alternative to the Philips hue API.

**The key high level architectural features of this are:**

* **Everything exposed by the API will be agnostic to the Hue system.**
  
  Lights, sensors, switches, hubs, etc are abstracted into interfaces
  which are exposed by the API.
  
  Within the API, all facets of communication with the physical lighting
  system are silently handled. Consumers of the API can then update
  properties directly as often as they want, without worrying about
  things such as rate limiting quotas on various platforms etc.
  
  This means that few changes would be required outside the API to
  implement alternate lighting systems.
  
* **Every property exposed by the API will be observable.**

  This allows parts of the app to update themselves as soon as the API
  becomes aware of changes, so that the UI updates itself as soon as a
  light is turned on, for example.
  
  All regular refreshing / polling code is handled inside the API, to
  check for updates that have happened outside the app.
  
  The API exposes aggregate observable properties / live events. For
  example, there is a per-group observable live event that gets fired
  whenever any light within that group is updated.
  
  
### Technologies used:

Tinge follows an MVVM pattern.

 - Within the Tinge API,
 [RxJava 2](https://github.com/ReactiveX/RxJava/tree/2.x),
 [Gson](https://github.com/google/gson) and
 [Retrofit](https://github.com/square/retrofit) are used to communicate
 with the Hue Bridge.
 [Android Observables](https://developer.android.com/reference/android/databinding/ObservableField)
 are then exposed for consumers of the API.

 - The ViewModel layer of Tinge then subscribes to Observables from the
 API, and transforms these into Observables appropriate for the View
 layer.

 - The View layer makes liberal use of
 [ConstraintLayout](https://developer.android.com/training/constraint-layout),
 and subscribes to Observables from the ViewModel layer using
 [Data Binding](https://developer.android.com/topic/libraries/data-binding).
 [BindingCollectionAdapter](https://github.com/evant/binding-collection-adapter)
 is used to data bind RecyclerViews to ObservableLists from the
 ViewModel.

 For testing, [Mockito](https://github.com/mockito/mockito) and
 [OkHttp MockWebServer](https://github.com/square/okhttp/tree/master/mockwebserver)
 are used (in addition to the standard stuff like JUnit).

[CircleCI](https://circleci.com/gh/time2key/tinge) is used for
continuous integration, which integrates with
[Firebase Test Lab](https://firebase.google.com/docs/test-lab) for
running UI and integration tests.

[![CircleCI](https://circleci.com/gh/time2key/tinge.svg?style=svg)](https://circleci.com/gh/time2key/tinge)
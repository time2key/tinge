# Tinge
Tinge is an Android app for controlling the Philips Hue lighting system.

Tinge brings the vibrancy of the user's current lighting setup to their
phone screen, and features innovative ways of adjusting the properties
of multiple lights at once.


### Making Tinge Beautiful

Right from inception, Tinge is being engineered with aesthetics and user
experience at the forefront. It has been moulded around the material
design philosophy. 

There are no branding colours visible on the user interface. The UI
completely reflects the colours of the current lighting setup.

Core to this experience, the top toolbar has been designed as a vibrant
and responsive glass sheet, with a stained-glass effect to reflect the
user's hue ecosystem.

![](/documentation/assets/glass_toolbar_1.png)|![](/documentation/assets/glass_toolbar_2.png)|![](/documentation/assets/glass_toolbar_animation.gif)
| ------------- | ------------- | ------------- |


### Making Tinge Functional

One of the key usability features of Tinge is the ability to easily
shift the properties of several lights at the same time. 

To do this, the number of touches and subscreens required to move from
one light to another has been minimised.

Tinge features an overhaul of the standard Android SeekBar. This can be
used with a single slider handle for controlling just one light, but it
can also be used for adjusting the properties of several lights at once.

![](/documentation/assets/slider_moving_animation.gif)|![](/documentation/assets/slider_grouping_animation.gif)|![](/documentation/assets/slider_group_popup.png)
| ------------- | ------------- | ------------- |


### Making Tinge Powerful

Philips Hue is a system that allows smart lights to be wirelessly set to
different colours. The underlying API provides a lot more functionality
than this though; with the ability to setup complex rules, schedules and
variables. This allows for almost boundless custom functionality.

Current apps don't make use of this functionality to anywhere near its
full potential. Long term, Tinge will change that.

#### The grand plan

Ultimately, Tinge will provide an intuitive visual interface to unleash
the capabilities currently trapped beneath the hood of the Hue platform.

This would then allow advanced users to view and define rules in a
completely free way, to achieve any functionality they desire.

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
  
**Technology wise:**
  
Internally, RxJava is used to manage and synchronise network calls.
Externally, Android observables are exposed for each property.

These exact technologies are subject to change as development on Tinge
progresses, but the concept of using observables will remain.


# Tinge
Tinge is an Android app for controlling the Philips Hue lighting system.

Currently in a pre release development stage, Tinge is being created
with the core design goals of being Beautiful, Functional and Powerful.

### Making Tinge Beautiful

Right from inception, Tinge is being engineered with aesthetics and user
experience at the forefront.

Material design is the philosophy of designing a user interface as if it
were a physical item composed of a series of overlapping surfaces. It is
the gold standard for UI / UX design across the Android ecosystem.
 
With Tinge being a light control app, I wanted the app by itself to be
completely colour-agnostic. This blank canvas would the be illuminated
with the current lighting setup.

Core to this experience, the top toolbar has been designed as a vibrant
and responsive glass sheet, with a stained-glass effect to reflect the
user's hue ecosystem.

![](/documentation/assets/glass_toolbar_1.png)|![](/documentation/assets/glass_toolbar_2.png)|![](/documentation/assets/glass_toolbar_animation.gif)
| ------------- | ------------- | ------------- |


### Making Tinge Functional

One of the key things I wanted to bring to Tinge is the ability to
easily shift the properties of several lights at the same time.

I also wanted to minimise the number of clicks and subscreens required
to move around from one light to another.

To do this, I have designed a custom SliderView implementation. This can
be used with a single slider handle for controlling just one light, but
it can also be used with multiple handles for adjusting properties of
several lights at once.

![](/documentation/assets/slider_moving_animation.gif)|![](/documentation/assets/slider_grouping_animation.gif)|![](/documentation/assets/slider_group_popup.png)
| ------------- | ------------- | ------------- |


### Making Tinge Powerful

Most people understand Philips Hue to be be a system for controlling
smart lights in quite a basic way. The underlying API provides a lot
more functionality than this though; with the ability to setup complex
rules, schedules and variables. This allows for almost boundless custom
functionality.

Very few apps make use of this functionality to anywhere near its full
potential. 

The official hue app does have 'Hue experiments' which allow some
complicated behaviours to be defined, such as animations and
multi-sensor rules. These are very brittle and non-customisable however.
They are also poorly integrated into the app, being hidden away in a
settings menu then loaded through a webview.

Available third party apps generally have even less functionality than
this. Those with complex functionality are similarly brittle,
non-customisable, and typically have poor UI.

#### The grand plan

Ultimately, I would like Tinge to provide an intuitive visual interface
to unleash the capabilities currently trapped beneath the hood of the
Hue platform.

This would then allow advanced users to view and define rules in a
completely free way, to achieve any functionality they desire.

Moving on even further, I am ultimately looking at implementing a visual
flowchart that shows the entire state of all rules and sensors on the
hue platform. This would analyse the entire system and build up a graph
showing how each component flows into one another.


#### Digging solid foundations

The precursor to implementing complex functionality like this is
architecturing solid foundations to build on.

This is being done by building a new alternative to the Philips hue api.

**The key high level architectural features of this will be:**

* **Everything exposed by the api will be agnostic to the Hue system.**
  
  There are a lot of alternatives to Philips Hue on the market, and long
  term it would make sense to support some of these.
  
  Lights, sensors, switches, hubs, etc will all be abstracted into
  interfaces which are exposed by the api.
  
  Within the api, all facets of communication with the physical lighting
  system will be silently handled. Consumers of the api can then update
  properties directly as often as they want, without worrying about
  things such as rate limiting quotas on various platforms etc.
  
  This should mean that few changes would be required outside the api to
  implement alternate lighting systems.
  
* **Every property exposed by the api will be observable.**

  This will allow parts of the app to update themselves as soon as the
  api becomes aware of changes, so that the ui updates itself as soon as
  a light is turned on, for example.
  
  All regular refreshing / polling code will be handled inside the api,
  to check for updates that have happened outside the app.
  
  The api will also expose aggregate observable properties / live
  events. For example, there will be an per-group observable live event
  that gets fired whenever any light within that group is updated.
  
**Technology wise:**
  
Internally, RxJava will be used to manage and synchronise network calls.
Externally, Android observables will be exposed for each property.

These exact technologies are subject to change as development on Tinge
progresses, but the concept of using observables will remain.


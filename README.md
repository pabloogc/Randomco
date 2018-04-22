#Randomco

This is a technical showcase of a [Flux](https://facebook.github.io/flux/]) 
application that fetches data from
https://randomuser.me/ displays it in a list and performs some basic
mutations on it.

## Code structure
The app is divided into Mini (my own Flux framework) the actual application.  

### Mini
The framework code lives under mini.*, from the Mini Architecutre. It's a minimal
implementation of Flux adapted for Android among
some other testing and extension utilities that I carry from project to project.  
Mini implementation is small personal project on it's own that some day will become
an actual library (if I find the time!).  

Since documenting Flux (or reactive architectures for that matter) is not
the point of this doc, I highly recommend reading both Flux And Redux documentation
to understand the motivation to bring those ideas into the Android world.  

Just a sneak peak of the benefits: Great logs, immutable-everything, no need to think
about orientation problems, screens are always in sync, extremely simple views 
(presenters are still a thing for complex views), fast and easy to test views, 
little boilerplate... In general, moving away from layered code and moving into reactive
everywhere.


### App
Actual application code lives under com.randomco.*. The application itself is just
an Activity that holds a Toolbar and a Fragment that displays the information
in a RecyclerView.  

**Note: I decided not to implement an additional detail view for the user since it didn't
have any technical implications other than making parcelable models and sending and 
intent to the new activity with whatever data (this pet project 
was already getting out of hand).

## Libraries

Both Mini and the application make heavy use of both RxJava an Dagger2. Since
these libraries can be considered quite complex on their own I am assuming the
reader has experience with both to make this doc brief.  

Image loading is done with Picasso for no particular reason other than simplicity.  
  
The rest is default Android support libraries.


## Testing

As a demo project, the point was to show techniques for testing different
components of the application instead of aiming for coverage. You can find
unit test for pure functions (mappers, filters), tests for Flux components
(store reducer functions), tests for view reactivity and tests for views
dispatching logic.  
Dependency injection combined with this strategies can cover 90% of a typical 
Flux application, the rest are the usual abstraction layers + mocks / fakes / whatever. 



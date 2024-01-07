![Architecture diagram](./docs/img/simple-clean-android-compose-architecture-v3.jpg)

# Simple Clean Architecture - Android Compose Example

### About

This is just an example of the Android Clean Architecture implementation. The main assumption of the
project is to be as simple as possible,
but also provide layers possible to be tested by unit tests.

The Clean Architecture approach was described by Robert C. Martin here: https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html.

### Main advantages of this architecture:

- viewModel and use case layers testable by unit test
- clear separation of data logic from the layout
- the whole logic is represented by simple data, making it easier to track it
- functional approach in order to simplify the code
- minimal view layer logic, reduced to Compose layout without data processing logic
- clear separation of concerns
- minimal and unified set of architecture components

### Practical/pragmatic remarks:

- it's hard to completely avoid references to Android classes in any part of code as sometimes there
  is just no effective way to reconcile
  the functional requirements (e.g. Parcelable data classes, libraries dependencies). As the main
  purpose is to write testable code,
  I'd consider it acceptable, as long as unit tests work and we don't need to call Android SDK
  methods during tests - please refer to:
  https://developer.android.com/training/testing/unit-testing/local-unit-tests#mocking-dependencies
  In other words: the purest structure we maintain, the less problems and bugs we have, but if we
  need to write own version of huge external library to achieve
  the Android SDK separation, maybe making exception would be a better choice.
- TODO: other remarks


### Simple Clean TDD Workflow

When using the project structure described above, it's also possible to easily implement the Test Driven Development approach and distribute tasks among team members more swiftly. Let's consider single screen workflow.

1. Developers start work on the project with a meeting where they discuss the new screen functionality and collaboratively establish main elements based on the acceptance criteria outlined in the task:
   - view model with empty methods and base ui state object
   - empty layout
   - use case interfaces
   - repository interfaces
   - set of empty unit and snapshot tests checking the previously mentioned elements
  
   If necessary, the components above can be determined by one person. However, collaborative agreements allow for a broader perspective on the project and capture a greater number of edge cases and possible issues. This results in more precise preliminary agreements and the better project awareness among the team members.
   
   Next, all those empty components are committed and pushed to the repository. We can use separate feature branch for this particular screen.


2. In the next step we should prepare single task for each component. We shouldn't split the single component implementation into several tasks. For instance, we should not split the implementation of a view model among multiple tasks, but we should rather separate the entire implementation of specific element.


3. The development starts - all unit and snapshot tests should be prepared first, before the main application components.


4. We proceed with main development and we create all the application components using the tests prepared in the previous step for verification. During the process, we refine details that may not have been uncovered during the preparation of basic elements and tests.


5. The final step is to assemble all the elements together. After that, most functions should work correctly, however some integration improvements may also be necessary. The tasks of refining elements are assigned according to the earlier assignment of work as much as possible.


### Workflow example:
Bob, Alice and John are developers working in a three-person team together. The team has been tasked with adding a new user authorization screen to the application. Alice will lead the project. Example workflow could look like this:
1. The team gathers to plan the work. During the meeting led by Alice, she creates the required base classes and adjusts the code as discussed with the team. These classes include:
   - simple view model with empty methods for login and password fields update and submit the form
   - data class for the ui state including properties for the layout
   - empty screen layout
   - user login use case interface for submit action
   
   Next, in the second stage of the meeting, the whole team examine together all the possible test cases for the view model, use case and layout. Alice creates test classes for the components (unit test classes for the view model and the use case and snapshot test class for the layout) and writes down all the agreed test cases aiming to describe each one in the function name in the clearest way possible.
   
   At the end of the meeting, Alice verifies the correctness of the agreed code and commits the changes to the common branch which will be the base for single component branches.


2. The work should start with writing automated tests. The most desirable solution is, when every team memeber creates the tests for the part of the code, he needs to make use of in his own task. Alice manages the task assingment in the following way:
   - John will create view model and Alice will write unit tests for it
   - Bob will create the use case and required repository implementations and John will write the unit tests for this use case
   - Alice will implement the compose layout and Bob will create the snapshot tests for this layout
   - unit tests will be created on the very beginning of the project and committed to the common repository branch to be available for all the team memebers before the main development
   - when all the components are ready, Alice will assemble it together and assign the tasks for bugfixes if needed


3. Alice creates unit tests for the view model. John creates unit tests for the use case. Bob creates the snapshot tests for the layout. Everyone commits their changes to the repository.


4. The work on actual code looks as follows:
   - Alice starts working on the layout and checks the results using snapshot tests
   - Bob starts working on the use case and the repository referring to the server service logging in users
   - John starts working on view model
   - meanwhile, some new business requirements appeared: in case of errors server service will respond with some user login validation messages which should be presented on the screen while submitting
   - Bob agrees with Alice the new feature and changes the snapshot tests
   - Bob agrees with John improvements on use case interface and data model
   - John changes unit tests for the use case and updates his code
   - John agrees with Alice the changes in the view model to present the validation
   - Alice changes unit tests for the view model and updates her code
   - after the components are finished, everyone commits their code to the common branch


5. Alice checks the new screen functionality:
   - during the checks it turned out, the user login validation messages are not displayed, so the team investigates the issue
   - it turned out, that one of the unit tests has a bug and is not checking the view model correctly, so Alice fixes the bug in unit test and then John fixes the view model
   - other minor issues are also found and fixed
   - the working project can be finished


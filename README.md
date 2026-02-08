Calendar (Parts 1, 2, & 3)
---
### Design changes from Part 2
- No changes were made to our original model, controller, or view classes from Part 1 & 2.
- Added a GUI interface and GUI view, since we are required to implement a GUI for this assignment.
- Added a new Features interface and GuiController which implements that interface, since the
difference between CLIs and GUIs is too big to be handled by the same controller.
 
### Design changes from Part 1
- Changed `CommandLineController`’s constructor to take in an `ICalendarManager` instead of an
`ICalendar`, as we now use the calendar manager object to keep track of the multiple calendars.
- Replaced `ICalendar` with `ICalendarManager` in the constructor of `AbstractCommandParser` 
since we aren’t using a single calendar anymore, but rather a calendar manager
that holds multiple calendars.
- Changed `CreateCommandParser`'s name to `CreateEventCommandParser` since we’re adding 
a `CreateCalendarCommandParser` due to the `create calendar` command.
- Changed `EditCommandParser`'s name to `EditEventCommandParser` since we’re adding
a `CreateCalendarCommandParser` due to the `edit calendar` command.
- Changed the constructor in `CreateEventCommandParser` to take in the next word after “create”
and verifying that the word after is “event”, since there’s now the possibility of creating
a calendar now.
- Changed the constructor in `EditEventCommandParser` to take in the next word after “edit”, 
since there’s now the additional possibility editing a calendar instead of just editing events.
- Changed the `QueryCommandParser`'s constructor to also take in the name of the current calendar,
since for printing events we also wish to print the current calendar being used.
- Added the `setTimezone` method in `CalendarEvent` since we need to keep track of 
timezones for events now.
- Moved the `createNewEventWithNewStartDate` method from `ISingleEvent` to `CalendarEvent` because
we need that method for `EventSeries` now.
- Changed all method signatures containing concrete classes to their interfaces, 
since we don’t wish to leak implementation details.

### Running the program
This program has three modes of running: through a GUI, interactive and headless.

The GUI mode allows the user to interact with the calendar through a graphical interface.
To quit the GUI, simply close the window.

Interactive mode allows
for the user to repeatedly type in commands and immediately see the results. To quit interactive
mode, simply type `exit`.

Headless mode allows for the user to provide the program a `.txt` file with a list
of commands, which are then executed sequentially. The last command must be `exit`.

To run this program, `cd` into the directory of the `calendar.jar` file, then either run:

```
java -jar calendar.jar
```
<sub>to run the GUI mode. </sub>

or
```
java -jar calendar.jar --mode interactive
```
or
```
java -jar calendar.jar --mode headless [file.txt]
```

### Working features of GUI mode
The GUI mode allows the user to display up to ten events starting from a date,
create single events, edit single events, create calendars, and switch between calendars.

The GUI has a calendar view that allows the user to select a date and see all events starting from 
that date (up to 10 events). For further details on how to use the GUI, please refer to the `USEME.md` file.

### Working features of interactive mode and headless mode
Note: `<dateStringTtimeString>` is in the form `YYYY-MM-DDThh:mm`, and `<dateString>`
is in the form `YYYY-MM-DD`. `<weekdays>` is a sequence of characters where each character
denotes a day of the week: `M` is Monday, `T` is Tuesday, `W` is Wednesday, `R` is Thursday,
`F` is Friday, `S` is Saturday, and `U` is Sunday.

All events have six properties: a subject, start date/time, end date/time, and optionally a
description, location (physical or online), and status (public or private).

<b>Create Calendar:</b>

<i>This command will create a new calendar with a unique name and timezone as specified by the user.
The expected timezone format is the IANA Time Zone Database.
In this format the timezone is specified as "area/location". Few examples include "America/New_York",
"Europe/Paris", "Asia/Kolkata", "Australia/Sydney", "Africa/Cairo", etc. The command is invalid if
entered a non-unique calendar name or an unsupported timezone. </i>

`create calendar --name <calName> --timezone area/location`

Example:
```
create calendar --name "Work Calendar" --timezone America/New_York
```
<sub>should create a new calendar with the name "Work Calendar" and the timezone set to "America/New_York".</sub>

<b>Edit Calendar:</b>

<i> This command is used to change/modify an existing property ( `name` or `timezone` ) of the calendar. 
The command is invalid if the property being changed is absent or
the value is invalid in the context of the property. </i>

`edit calendar --name <name-of-calendar> --property <property-name> <new-property-value>`

Example:
```
edit calendar --name "Work Calendar" --property timezone America/Los_Angeles
```
<sub>should change the timezone of the calendar named "Work Calendar" to "America/Los_Angeles".</sub>

<b>Use Calendar:</b>

<i>This command is used to switch the current calendar to the one specified by the user. </i>

`use calendar --name <name-of-calendar>`

Example:
```
use calendar --name "Work Calendar"
```
<sub>should switch the current calendar to the one named "Work Calendar".</sub>

<b>Copy Events:</b>

<i> The command is used to copy a specific event with the given name and start date/time from the 
current calendar to the target calendar to start at the specified date/time. The "to" date/time is 
assumed to be specified in the timezone of the target calendar. </i>

`copy event <eventSubject> from <dateStringTtimeString> to <targetCalendarName> at <dateStringTtimeString>`

Example:
```
copy event "Team Meeting" from 2023-10-01T10:00 to "Work Calendar" at 2023-10-02T10:00
```
<sub>should copy the event with the subject "Team Meeting" that starts on October 1st, 2023 at 10:00 AM </sub>

<i> This command has the same behavior as the copy event above, except it copies all events scheduled 
on that day. The times remain the same, except they are converted to the timezone of the target 
calendar (e.g. an event that starts at 2pm in the source calendar which is in EST would start at 
11am in the destination calendar which is in PST). </i>

`copy events on <dateString> --target <calendarName> to <dateString>`
Example:
```
copy events on 2023-10-01 --target "Work Calendar" to 2023-10-02
```
<sub>should copy all events scheduled on October 1st, 2023 from the current calendar
to the "Work Calendar" on October 2nd, 2023, with the timezone converted.</sub>

<i> The command has the same behavior as the other copy commands, except it copies all events 
scheduled in the specified date interval. The date string in the target calendar corresponds 
to the start of the interval. The endpoint dates of the interval are inclusive. </i>

`copy events between <dateString> and <dateString> --target <calendarName> to <dateString>`

Example:
```
copy events between 2023-10-01 and 2023-10-02 --target "Work Calendar" to 2023-10-03
```
<sub>should copy all events scheduled between October 1st, 2023 and October 2nd, 2023 from the current calendar
to the "Work Calendar" on October 3rd, 2023, with the timezone converted.</sub>


In both the copy events commands, if an event series partly overlaps with the specified range,
only those events in the series that overlap with the specified range should be copied, and their
status as part of a series should be retained in the destination calendar.



<b>Creating Events:</b>

<i>Create a single event with a specified timeframe:</i>
`create event <eventSubject> from <dateStringTtimeString> to <dateStringTtimeString>`

Example:

```
create event "Team Meeting" from 2023-10-01T10:00 to 2023-10-01T11:00
```
<sub>should create a single event with the subject "Team Meeting" that starts on October 1st, 2023 at 10:00 AM and ends at 11:00 AM.
</sub>


<i>Create an event series with a specified timeframe that repeats N times:</i>
`create event <eventSubject> from <dateStringTtimeString> to <dateStringTtimeString> repeats <weekdays> for <N> times`

Example:
```
create event "Team Meeting" from 2023-10-01T10:00 to 2023-10-01T11:00 repeats MTRF for 5 times
```
<sub>should create a series of events with the subject "Team Meeting" that starts on October 1st, 2023 at 10:00 AM and ends at 11:00 AM, repeating every Monday, Tuesday, Thursday,
and Friday for 5 occurrences.</sub>

<i>Create an event series with a specified timeframe that repeats until a specific date:</i>
`create event <eventSubject> from <dateStringTtimeString> to <dateStringTtimeString> repeats <weekdays> until <dateString>`

Example:
```
create event "Team Meeting" from 2023-10-01T10:00 to 2023-10-01T11:00 repeats MTRF until 2023-12-31
```
<sub>should create a series of events with the subject "Team Meeting" that starts on October 1st, 2023 at 10:00 AM and ends at 11:00 AM, repeating every Monday, Tuesday, Thursday,
until December 31st, 2023.</sub>

<b>Editing Events:</b>

<i>Change the property (subject/start/end/description/location/status) of the given event, irrespective
of whether it is part of a series or not:</i>
`edit event <property> <eventSubject> from <dateStringTtimeString> to <dateStringTtimeString> with <NewPropertyValue>`

Example:
```
edit event subject "Team Meeting" from 2023-10-01T10:00 to 2023-10-01T11:00 with Weekly Sync
```
<sub>should change the subject of the event with the subject "Team Meeting" that starts on October 1st, 2023 at 10:00 AM and ends at 11:00 AM to "Weekly Sync".</sub>

<i>Identify the event from the subject and start/end date/time and edit its property AND the properties
of the events in its series that start after:</i>
`edit events <property> <eventSubject> from <dateStringTtimeString> with <NewPropertyValue>`

Example:
```
edit events subject "Team Meeting" from 2023-10-01T10:00 with Weekly Sync
```
<sub>should change the subject of the event with the subject "Team Meeting" that starts on October 1st, 2023 at 10:00 AM and all subsequent events in the series to "Weekly Sync".</sub>

<i>Identify the event from the subject and start/end date/time and edit its property and ALL the
properties of the events in that event series:</i>
`edit series <property> <eventSubject> from <dateStringTtimeString> with <NewPropertyValue>`

Example:
```
edit series subject "Team Meeting" from 2023-10-01T10:00 with Weekly Sync
```
<sub>should change the subject of the event with the subject "Team Meeting" that starts on October 1st, 2023 at 10:00 AM and all events in the series to "Weekly Sync".</sub>

Note: for `edit events` and `edit series`, if the edited event is not part of a series then the
command has the same effect as `edit event`.

For how the edit commands behave, see the <b>Other notes</b> section.

<b>Querying Events:</b>

<i>Print a bulleted list of all events on that day along with their properties:</i>
`print events on <dateString>`

Example:
```
print events on 2023-10-01
```
<sub>should print a list of all events on October 1st, 2023, along with their properties.</sub>

<i>Print a bulleted list of all events in the given interval along with their properties:</i>
`print events from <dateStringTtimeString> to <dateStringTtimeString>`

Example:
```
print events from 2023-10-01T10:00 to 2023-10-01T11:00
```
<sub>should print a list of all events from October 1st, 2023 at 10:00 AM to 11:00 AM, along with their properties.</sub>

<i>Print whether the user is busy or available (if they have an event scheduled on the given time):</i>
`show status on <dateStringTtimeString>`

Example:
```
show status on 2023-10-01T10:00
```
<sub>should print whether the user is busy or available on October 1st, 2023 at 10:00 AM.</sub>

### Team contributions
<b>Part 1:</b> The work was spread pretty evenly across both team members. We started with pair 
programming for the initial model, and then split off after that to work on separate parts of the project
from there. 

Haoyu worked on the `create` and `query` part of the controller, the command line parsing,
and tests related to the controller.

Ruoyu worked on the `edit` part of the controller, the logic for our model,
and tests related to the model.

<b>Part 2:</b> We worked together on the logic for creating and using separate calendars with names
and timezones.

Then, Ruoyu worked on the logic for copying events over, and Haoyu worked on the parsing for the 
new commands on the controller. 

Afterward, Ruoyu worked on the tests for our new `Application` and 
`BetterCalendar` classes, while Haoyu worked on new tests for the `CommandLineController` and
integration tests.

<b>Part 3:</b> We worked together on the GUI design and implementation, as well as the new 
`GuiController`, `GuiView` and `Features` classes.

Haoyu worked on managing and switching between multiple calendars, logic of displaying events, 
as well as the mock tests for the GUI controller.

Ruoyu worked on the logic of editing events, as well as the mock classes for the GUI controller.

### Other notes
<b>VERY IMPORTANT NOTE:</b>
- For assignment 4, we got points taken off since the behavior of editing an event's starting date
  was "unconventional". When one edited the start date/time of an event, the end date/time also got
  adjusted accordingly so the event kept the same duration. For example, if an event went from `10:00
  to 11:00`, and we edited the start date/time to be `9:00`, the end date time would be changed to
  `10:00`. We did it this way since we were consistently told to refer to Google Calendar (which
  handles changing start date/time logic this way) for any calendar behavior that was not explicitly
  mentioned in the assignment—and this part of the logic was inexplicit enough for us to do so.
<br></br>
- Our regrade request for this deduction got <b>accepted</b>, meaning the instructor found it 
  <b>logical</b> for editing the start date/time of an event to also change the end date/time to
  keep the same duration. Hence, our GUI implementation of editing an event's start date/time also
  does change said event's end date/time to keep the same duration.

<b>General Behavior of the program:</b>

* There will never be any event in the calendar that has the same subject and start date/time.
* Events in a series will always have the same start time.
* Events in a series won't be over one day long.
* Event subjects should be enclosed in quotes.
* All other behavior mentioned in the assignment description.

<b>Behavior of the Event Commands:</b>

* if an event is not found the program will print an error message and not change anything.
* if multiple events are found meeting the edit scope, (Event have same start date but different EndDate)the program will print an error message and not change anything
* For all edit command, if the event is not part of a series, the program will only change the property that event.
* changing the start date/time will also change the end date/time to be the same length as the original event.
* changing the end date time will <b>not</b> change the start date/time.
  (following logic of the assignment and google calendar)
* if at any time the event edited have the same subject and start date/time as another event in the calendar, the program will print an error message and not change anything.

Edit Event command:

* For events in a series, if the start date/time is changed, or if the endDateTime is no longer on the same day as the start date/time, 
the program will make the event into a single event. And the Calendar will contain the original series with the event removed as well as the updated single event.
* Any other property changed will not remove the event from the series (only changing the event).

Edit Events command:

* For event in a series, if the start date/time is the changed the event and all event after will become its own series.
and the original series with the unchanged events will still be in the calendar.
* For event in a series, if the start date is changed to a later date, the event startDate will be set to the closest weekday that 
the series occurs on (the next weekDay in the series from the date that the user inputs), and the rest of the events 
in the series will be back shifted accordingly.
* For event in a series, if the end date/time is changed makes the event longer than a day the program will throw an error message and not change anything.
if it is shorter than a day, the program will change the end date/time of the event and all events after it in the series.
* Any other property changed will not remove the event from the series, it will change the property of the event and all events afterward.

Edit Series command:
* For event in a series, changing the start date of the whole series is <b>not</b> allowed, the program will throw an error message and not change anything.
* For event in a series, changing the start<b> time</b> of the event will change the start time of all events in the series.
* For event in a series, changing the end date/time if the new end date/time makes the event longer than a day it will throw an error message and not change anything.
* the event the command calls on will not be removed from series.
* Any other property changed will not remove the event from the series, it will change the property of the event and all events in the series.

For Single Event:
All edit commands will have the same effect as the first command and program will change the event and not remove it from the calendar.

<b>Behavior of the Copy Commands:</b>

* For the `copy events` command, it will only copy events that are in the specified date range, it will not copy events that are not completely in the range. 
  (Single events that are not completely within the range will not be copied).
* When copying events, if the event is part of a series, it will only maintain the series status if the event lands on the same weekday as the original event.
  (if a series event is copied to a different weekday, it will be copied as a single event).
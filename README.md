Plot your daily productivity (quotient of 24h) and calculate your median productivity over the recorded timespan, prompting you to hurry if you haven't yet beat it today :) Useful and motivating if you track every action using Toggl!

![Screenshot of the program](https://github.com/heinzelotto/togglplot/blob/master/screenshot.png)

(Popup with details on hover, the blue colored node is the median (unique since the amount of nodes displayed is odd :] ))

Takes your Toggl API-key to authenticate, using (a modified/fixed version) of the useful [JToggl](https://github.com/bbaumgartner/jtoggl), which has been packaged.

Hardcoded things (that should be variable and accessible in a user menu):
* Timespan of entries displayed: 185d
* Entries are downloaded and saved in log.csv, past days before today are not synchronized again, even if the data on Toggl changes
* Ignored time entries are all those that have the activity string "lol", which is what I use. Those entries are excluded from all productivity analysis

run with `togglplot api-key` or `sbt "run 9876xxx-api-key-xxx6789"` (this somehow needs quotes, otherwise sbt will confuse the api key parameter with java arguments or something)

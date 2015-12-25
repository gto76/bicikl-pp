Bicikl++
=========

#### Public bicycle availability Android app for Ljubljana


<img src="/doc/Screenshot_2015-02-07-07-13-34.png" height="400">
<img src="/doc/Screenshot_2015-02-07-07-16-15.png" height="400">
<img src="/doc/Screenshot_2015-02-07-07-20-09.png" height="400">

App enables users of public bicycle system in Ljubljana to find the quickest routs from current location to selected destination. Besides showing the current availability of the bikes, the app also stores the data for later analysis.

Screens of the app 
------------------
* **Map** - The home screen of the app, that shows the Google map of Ljubljana. On it there is a pin for every station. With a longer click (hold) the destination gets selected, and the quickest route is drawn on a map. The route consists only of the stations that have available bikes/spaces.
* **Stations** - screen shows the list of all the stations, ordered by the distance from current location.
* **Station** - shows the details of a station and it's availability history. In a background, there's a picture of the station taken from the street view API.
* **Paths** - This screen shows a list of the quickest routes from current location to the selected destination. The routes are colored according to the availability of the stations. In the background is a picture of the selected destination.
* **Options** - enables configuration of cycling speed and the number of acceptable free spaces.


# SecurityApp
How to configure the app
1. Setup your Google Maps API Key a tutorial how to get yours can be found here:
  1.1 get the key
  https://developers.google.com/maps/documentation/android-sdk/get-api-key?hl=de
  1.2 setup your key into the AndroidManifest.xml file of your project
  https://developers.google.com/maps/documentation/android-sdk/config?hl=de

2. Change the IP adress accordingly to your backend in the project Network->HttpClient
private final static String serverUrl = "http://<IP>:<PORT>";

3. Start backend server and now you can launch the app

A little bit about the code implementation
The main role in this project plays the Google Maps SDK. Therefore I have created 3 classes, which will provide you with the neccessary functionalities for each activity.

GoMap_HeatMap - shows you the map with heatmap tiles, if you zoom every event will get a marker on the map, the markers are clickable and will bring you the the detail view of the event

GoMap_NewEvent - has the ability to set your position with a marker on the map

GoMap_Detail - for the detail view of an event, basically just shows you a zoomed in view on the map to the location of the event.

The networking part is implement using the AsyncHttpClient library (https://github.com/codepath/CPAsyncHttpClient).

Caution changing backend servers with already initialised app:
During the first start of the app an account will be generated in the background and saved into the Apps shared preference file. If you change the backend server, therefore you have to delete the apps preference file first, io that it will generate a new account on the new backend server. This file can be found in the devices storage under: data->data->com.peasec.security.app->shared_prefs->PEASEC_SharedPref.xml

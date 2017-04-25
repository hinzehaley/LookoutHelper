# LookoutHelper
Android mobile application for helping wildfire lookouts pinpoint fire locations

This application is still being developed, so locations should be confirmed by the wildfire lookout before being reported
as a fire location.

# Summary

Wildfire lookouts have tools for finding the azimuth (or horizontal angle where north is 0 degrees, south is 180 degrees, etc.) and the vertical angle
of a wildfire spotted in the distance. Using this information, the lookout has to use landmarks and topography maps to estimate the exact location of the fire.
This application simplifies that process by allowing the user to input the azimuth and vertical angle of a wildfire and then suggesting possible locations,
 both with legal descriptions and latitude and longitude. On initial start-up of the application, the user enters the location of the lookout
  they are working at in addition to its elevation and the principal meridian that legal land descriptions should be based off of. This information
  is used in conjunction with the azimuth and vertical angle to determine wildfire location.
  
  A feature for locating wildfires with a second azimuth from a cross lookout is also available on this application.


# Installation
Clone the repository and open it in Android Studio

Generate a Google Maps API key by following the directions in this [link](https://developers.google.com/maps/documentation/android-api/signup). 
Place it in a file titled google_maps_api.xml in res/values. The format of the file should be as follows:
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!--
    Once you have your key (it starts with "AIza"), replace the "google_maps_key"
    string in this file.
    -->
    <string name="google_maps_key" translatable="false" templateMergeStrategy="preserve">
        Your Key Here
    </string>
</resources> 
```

Generate a Google Elevation API key by clicking "Get A Key" at this [link](https://developers.google.com/maps/documentation/elevation/get-api-key). 
Place it in a file titled elevation_api_key.xml in res/values. The format of the file should be as follows:
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
  <string name="google_elevation_key" translatable="false" templateMergeStrategy="preserve">
    Your Key Here
  </string>
</resources>
```

Click the Run button at the top of Android Studio to run the application on an Android device or emulator!


 # How It Works
 
 Using the latitude, longitude, and elevation of the wildfire lookout in conjunction with the azimuth and the vertical angle of the fire from the lookout
 allows for the creation of right triangles where the hypotenuse is the line following the azimuth and vertical angle out from the lookout, the adjacent
  side is the line following the azimuth out from the lookout with a vertical angle of 0, and the opposite side is a side at
  at any point along these two lines that intersects the opposite side at a right angle. Using this information, it is possible to calculate the elevation
  of any point along the hypotenuse.
  
 The point at which the hypotenuse intersects the surface of the earth is the wildfire location, so using Google's Elevation API, one can check
 the actual elevation of the Earth's surface at sample points along the hypotenuse and then compare that to the target elevation (or the elevation calculated for that same point using the
 method described above). When a point is found where these elevations are the same, the application returns this point
  as a suggested fire location. In most cases, this application finds up to four possible points that are potential fire locations and then
  places markers on a map that are color-coded depending on how likely it is that the given location is correct. The user can then select one of these points
  as the wildfire location.
  
# Libraries
This application uses the Google Maps API, the Google Elevation API, and Volley for sending requests to the Google Elevation API

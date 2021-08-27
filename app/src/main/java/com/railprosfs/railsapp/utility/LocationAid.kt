package com.railprosfs.railsapp.utility

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.*
import java.io.Closeable

/*
    To use this class you will want to check/ask for permissions (and maybe if the play store is there).
    Then when you create the class, it starts up the periodic update of the cached location.  You can
    either poll (getCachedLocation) for it or provide a callback (onLocationStateListener) to trigger
    when updates come in.
    Finally, you should always call "close" once done to turn off the location updates.

    This class requires either of two permissions: ACCESS_FINE_LOCATION or ACCESS_COURSE_LOCATION.
    This class requires the adding google play services to the dependencies:
    implementation 'com.google.android.gms:play-services-location:x.x.x'
    Check the permissions before instantiating this class for best results.
    For more location examples, see https://github.com/android/location-samples
    For Google Docs on Location see https://developer.android.com/training/location

    Someday might want to add *Settings* information to this class, but requires an Activity context.
        // Docs (seem to) say there needs to be some initial call before settings are available.
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(activity, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                boolean hasGPS = locationSettingsResponse.getLocationSettingsStates().isGpsUsable();
                boolean hasCELL = locationSettingsResponse.getLocationSettingsStates().isNetworkLocationUsable();
            }
        });

 */
class LocationAid(mContext: Context, interval: Long = RETRY_INTERVAL, limit: Long = RETRY_LIMIT) : Closeable {
    /*
        The permission and service verification functions are static,
        since they need to be used prior to instantiating this class.
     */
    companion object {

        // If the permission is not ok, will need to request it.
        fun checkPermissions(context: Context, permission: String = Manifest.permission.ACCESS_FINE_LOCATION): Boolean =
            ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

        // The education check is used by Google to suggest more information be given to the user.
        fun checkEducation(activity: Activity, permission: String = Manifest.permission.ACCESS_FINE_LOCATION): Boolean =
            shouldShowRequestPermissionRationale(activity, permission)

        /*
            In the calling Activity, add this callback to see if the user allowed permissions.
            override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray)
            check for grantResults[0] == PackageManager.PERMISSION_GRANTED
         */
        fun askPermission(activity: Activity,
                          permission: String = Manifest.permission.ACCESS_FINE_LOCATION,
                          rtnCode: Int) {
            ActivityCompat.requestPermissions(activity, arrayOf(permission), rtnCode)
        }

        // Good indication if location services are going to work.
        fun isLocationEnabled(activity: Activity): Boolean {
            val locationManager = activity.getSystemService(LOCATION_SERVICE) as LocationManager
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                    || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        }

        /*
            Check if Play Store services is installed (required to use this class).
            https://developers.google.com/android/guides/setup
         */
        fun isPlayStoreInstalled(context: Context): Boolean = checkPlayStoreApi(context) == SUCCESS_API

        // Added this method in case need to know why, e.g. for logging.
        fun checkPlayStoreApi(context: Context): String =
            when (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)) {
                ConnectionResult.SUCCESS -> SUCCESS_API
                ConnectionResult.SERVICE_MISSING -> "SERVICE_MISSING"
                ConnectionResult.SERVICE_UPDATING -> "SERVICE_UPDATING"
                ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED -> "SERVICE_VERSION_UPDATE_REQUIRED"
                ConnectionResult.SERVICE_DISABLED -> "SERVICE_DISABLED"
                ConnectionResult.SERVICE_INVALID -> "SERVICE_INVALID"
                else -> "UNKNOWN"
            }

        const val RETRY_INTERVAL: Long = 20000      // About how often the location is checked.
        const val RETRY_LIMIT: Long = 10000         // At most, location checked this often.
        private const val SUCCESS_API: String = "SUCCESS"   // Self documenting.
    }

    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private val mLocationMonitor: LocationMonitor = LocationMonitor()
    private var mRecentLocation: Location? = null

    // Implement this interface in the caller to get callbacks on location updates.
    interface LocationListener {
        fun locationFound(locationFound: Location, locations: MutableList<Location>)
    }

    init {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext)
        val locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = interval
        locationRequest.fastestInterval = limit
        setupLocationRequest(locationRequest)
    }

    override fun close() {
        mFusedLocationClient?.removeLocationUpdates(mLocationMonitor)
    }

    fun getCachedLocation(): Location? {
        return mRecentLocation
    }

    /*
        This method allows caller to listen for location changes.
     */
    fun onLocationStateListener(listener: LocationListener) {
        mLocationMonitor.setOnLocationListener(listener)
    }

    @SuppressLint("MissingPermission")
    private fun setupLocationRequest(locationRequest: LocationRequest){
        mFusedLocationClient?.requestLocationUpdates(locationRequest, mLocationMonitor, Looper.getMainLooper())
    }

    inner class LocationMonitor : LocationCallback() {
        private var locationListener: LocationListener? = null
        fun setOnLocationListener(listener: LocationListener?) {
            locationListener = listener
        }

        override fun onLocationResult(locationResult: LocationResult?) {
            if(locationResult != null) {
                mRecentLocation = locationResult.lastLocation
                locationListener?.locationFound(locationResult.lastLocation, locationResult.locations)
            }
        }

        override fun onLocationAvailability(locationAvailability: LocationAvailability?) {
            super.onLocationAvailability(locationAvailability)
        }
    }
}
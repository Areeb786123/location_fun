package com.areeb.location_fun

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.areeb.location_fun.databinding.ActivityMainBinding
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!
    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 100
        private const val TAG = "locationFun"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(_binding!!.root)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getCurrentLocation()
    }

    fun isLocationEnable(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER,
        )
    }

    private fun checkNetworkPermission(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ),
            LOCATION_PERMISSION_REQUEST_CODE,
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            } else {
                Log.e(TAG, "some error occur")
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getCurrentLocation() {
        if (checkNetworkPermission()) {
            if (isLocationEnable()) {
                val locationRequest = LocationRequest()
                    .setInterval(10000) // Update location every 10 seconds
                    .setFastestInterval(5000) // Get the latest location available within 5 seconds
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

                mFusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    object : LocationCallback() {
                        override fun onLocationResult(p0: LocationResult) {
                            val location: Location? = p0?.lastLocation
                            if (location != null) {
                                val geoCoder = Geocoder(this@MainActivity, Locale.getDefault())
                                val list: MutableList<Address>? = geoCoder.getFromLocation(location.latitude, location.longitude, 1)

                                Log.e(
                                    TAG,
                                    "latitude ${location.latitude} + longitude ${location.longitude}",
                                )
                                binding.locationTextView.text =
                                    "${list?.get(0)?.locality} + ${list?.get(0)?.subLocality} + ${list?.get(0)?.countryName}"

                                Log.e(
                                    TAG,
                                    "${list?.get(0)?.locality} + ${list?.get(0)?.subLocality} + ${list?.get(0)?.countryName}",
                                )
                            } else {
                                Toast.makeText(this@MainActivity, "result comes null", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    Looper.myLooper(),
                )
            } else {
                Toast.makeText(this, "Please turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermission()
        }
    }

    override fun onResume() {
        super.onResume()
        getCurrentLocation()
    }

    fun getLastKnownLocation() {
        if (checkNetworkPermission()) {
            if (isLocationEnable()) {
                mFusedLocationClient.lastLocation
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            val geoCoder = Geocoder(this, Locale.getDefault())
                            val list: MutableList<Address>? =
                                geoCoder.getFromLocation(location.latitude, location.longitude, 1)
                            Log.e(
                                TAG,
                                "latitude ${location.latitude} + longitude ${location.longitude}",
                            )
                            binding.locationTextView.text =
                                "${list?.get(0)?.locality} + ${list?.get(0)?.subLocality} + ${
                                    list?.get(0)?.countryName
                                }"

                            Log.e(
                                TAG,
                                "${list?.get(0)?.locality} + ${list?.get(0)?.subLocality} + ${
                                    list?.get(0)?.countryName
                                }",
                            )
                        } else {
                            Toast.makeText(this, "Location is not available", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.e(TAG, "Error getting location", exception)
                    }
            } else {
                Toast.makeText(this, "Please turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermission()
        }
    }

    private fun getCurrentLocationNew() {
        val locationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(10000)
            .setFastestInterval(5000)
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
        task.addOnSuccessListener(this) {
            if (checkNetworkPermission()) {
                if (isLocationEnable()) {
                    mFusedLocationClient.requestLocationUpdates(
                        locationRequest,
                        object : LocationCallback() {
                            override fun onLocationResult(p0: LocationResult) {
                                p0 ?: return
                                for (location in p0.locations) {
                                    val geoCoder = Geocoder(
                                        this@MainActivity,
                                        Locale.getDefault(),
                                    )
                                    val list: MutableList<Address>? =
                                        geoCoder.getFromLocation(
                                            location.latitude,
                                            location.longitude,
                                            1,
                                        )
                                    Log.e(
                                        TAG,
                                        "latitude ${location.latitude} + longitude ${location.longitude}",
                                    )
                                    binding.locationTextView.text =
                                        "${list?.get(0)?.locality} + ${list?.get(0)?.subLocality} + ${
                                            list?.get(0)?.countryName
                                        }"
                                    mFusedLocationClient.removeLocationUpdates(this)
                                }
                            }
                        },
                        Looper.getMainLooper(),
                    )
                } else {
                    Toast.makeText(this, "Please turn on location", Toast.LENGTH_LONG).show()
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                }
            } else {
                requestPermission()
            }
        }
        task.addOnFailureListener(this) {
            Log.e(TAG, "error")
        }
    }
}

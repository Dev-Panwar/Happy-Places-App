package dev.panwar.happyplaces.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import dev.panwar.happyplaces.R
import dev.panwar.happyplaces.databinding.ActivityMapBinding
import dev.panwar.happyplaces.models.HappyPlaceModel

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private var binding: ActivityMapBinding?=null
    private var mHappyPlaceDetail: HappyPlaceModel?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding?.root)

//        retrieving the Happy Place model details we passed to this activity from HAPPy place detail activity when clicked on btn view on map.
        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)){
             mHappyPlaceDetail=intent.getParcelableExtra(MainActivity.EXTRA_PLACE_DETAILS) as? HappyPlaceModel
        }
//        if our mHappyPlaceDetail!=null then we set toolbar title as location of model
        if (mHappyPlaceDetail!=null){
            setSupportActionBar(binding?.toolbarMap)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title=mHappyPlaceDetail!!.title

            binding?.toolbarMap?.setNavigationOnClickListener {
                onBackPressed()
            }
        }

        //        creating object that support Map Fragment
        val supportMapFragment: SupportMapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
//        to add the below line we need to inherit that interface OnMapReadyCallback and implement it's function
         supportMapFragment.getMapAsync(this)
    }
// auto generated function by  interface OnMapReadyCallback
    override fun onMapReady(googleMap: GoogleMap) {
        val position=LatLng(mHappyPlaceDetail!!.latitute,mHappyPlaceDetail!!.longitude)
//         val position is position of our happy place on map
//     to add marker on Map....i.e Pin
      googleMap.addMarker(MarkerOptions().position(position).title(mHappyPlaceDetail!!.location))

//    now adding zoom to location functionality/animation instead of showing complete map
    val newLatLngZoom = CameraUpdateFactory.newLatLngZoom(position, 15f)
    googleMap.animateCamera(newLatLngZoom)

    }
}
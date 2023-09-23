package dev.panwar.happyplaces.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.gms.location.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import dev.panwar.happyplaces.R
import dev.panwar.happyplaces.database.DatabaseHandler
import dev.panwar.happyplaces.databinding.ActivityAddHappyPlaceBinding
import dev.panwar.happyplaces.models.HappyPlaceModel
import dev.panwar.happyplaces.utils.GetAddressFromLatLng
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class AddHappyPlace : AppCompatActivity(), View.OnClickListener {

    private var binding:ActivityAddHappyPlaceBinding?=null
    private var cal:Calendar=Calendar.getInstance()
    private  lateinit var dateSetListener: DatePickerDialog.OnDateSetListener

    private var saveImageToInternalStorage: Uri? = null

    private var mLatitude: Double = 0.0 // A variable which will hold the latitude value.
    private var mLongitude: Double = 0.0 // A variable which will hold the longitude value.

    // TODO (Step 6: A variable for data model class in which we will receive the details to edit.)
    // START
    private var mHappyPlaceDetails: HappyPlaceModel? = null
    // END

//    step 1 for getting current location
    private lateinit var mFusedLocationClient: FusedLocationProviderClient


    companion object{
        private const val GALLERY=1
        private const val CAMERA=2
        private const val IMAGE_DIRECTORY="HappyPlacesImages"
        private const val PLACES_AUTO_COMPLETE_REQUEST_CODE=3
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddHappyPlaceBinding.inflate(layoutInflater)
        setContentView(binding?.root)
//         Setting up the action bar using the toolbar and making enable the home back button and also adding the click of it.)
        setSupportActionBar(binding?.toolbarAddPlace) // Use the toolbar to set the action bar.
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // This is to use the home back button.
        // Setting the click event to the back button
        binding?.toolbarAddPlace?.setNavigationOnClickListener {
            onBackPressed()
        }
        // END

//        for Places(Google maps) api calling
        if(!Places.isInitialized()){
            Places.initialize(this@AddHappyPlace, resources.getString(R.string.google_maps_api_key))
        }

        // TODO (Step 7: Assign the details to the variable of data model class which we have created above the details which we will receive through intent.)
        // START
        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)) {
            mHappyPlaceDetails =
                intent.getParcelableExtra(MainActivity.EXTRA_PLACE_DETAILS) as? HappyPlaceModel
        }
        // END

//        step 2 for getting current location
        mFusedLocationClient=LocationServices.getFusedLocationProviderClient(this)

//setting up the date set listener
        dateSetListener=DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->

            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH,month)
            cal.set(Calendar.DAY_OF_MONTH,dayOfMonth)
//            as here we have selected the date in calender successfully so putting in etdate by calling updateDateInView()...this will put date in
            updateDateInView()
        }

        updateDateInView() // Here the calender instance what we have created before will give us the current date which is formatted in the format in function...calender.getInstance...setted the current date to the cal
        // TODO (Step 8: Filling the existing details to the UI components to edit.)
        // START
        if (mHappyPlaceDetails != null) {
            supportActionBar?.title = "Edit Happy Place"

            binding?.etTitle?.setText(mHappyPlaceDetails!!.title)
            binding?.etDescription?.setText(mHappyPlaceDetails!!.description)
            binding?.etDate?.setText(mHappyPlaceDetails!!.date)
            binding?.etLocation?.setText(mHappyPlaceDetails!!.location)
            mLatitude = mHappyPlaceDetails!!.latitute
            mLongitude = mHappyPlaceDetails!!.longitude

            saveImageToInternalStorage = Uri.parse(mHappyPlaceDetails!!.image)

            binding?.ivPlaceImage?.setImageURI(saveImageToInternalStorage)

           binding?.btnSave?.text = "UPDATE"
        }
        // END

// way to call on click function...as our class is inheriting onclickListener and we are passing reference
        binding?.etDate?.setOnClickListener(this)
        binding?.tvAddImage?.setOnClickListener(this)
        binding?.btnSave?.setOnClickListener(this)
        binding?.etLocation?.setOnClickListener(this)
        binding?.tvSelectCurrentLocation?.setOnClickListener(this)
    }

//    to check location turned on or not
    private fun isLocationEnabled():Boolean{
        val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
//        checking if the location is turned on or not
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

//    Step 3 for finding current location of user...Read documentation for more https://developer.android.com/training/location/change-location-settings#:~:text=Get%20current%20location%20settings,-Once%20you%20have&text=Task%20task%20%3D%20client,code%20from%20the%20LocationSettingsResponse%20object.
//    we will use all these functions https://developers.google.com/android/reference/com/google/android/gms/location/LocationRequest.html
  @SuppressLint("MissingPermission") //we are supressing the the error that we haven't checked the location enabled or not...but we are sure that location is already enabled as we call this function in on click tv Selected current location where we already checked location granted
    private fun requestNewLocationData(){
        var mLocationRequest=LocationRequest()
        mLocationRequest.priority= LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval=1000
//    how many times we need to find location once we click on select current location
        mLocationRequest.numUpdates=1

    mFusedLocationClient.requestLocationUpdates(mLocationRequest,mLocationCallback, Looper.myLooper())
    }

//    step 4 for finding current location of user
    private val mLocationCallback = object : LocationCallback(){
    override fun onLocationResult(locationResult: LocationResult) {
        val mLastLocation: Location?=locationResult!!.lastLocation
        mLatitude=mLastLocation!!.latitude
        mLongitude=mLastLocation!!.longitude
        Log.i("Current latitute", "$mLatitude")
        Log.i("Current longitude", "$mLongitude")
//  object of GetAddressFromLatLng() class we created
        val addressTask=GetAddressFromLatLng(this@AddHappyPlace,mLatitude,mLongitude)
//        calling the setAddressListener function of GetAddressFromLatLng Class
        addressTask.setAddressListener(object : GetAddressFromLatLng.AddressListener{
            override fun onAddressFound(address: String?) {
              binding?.etLocation?.setText(address)
            }

            override fun onError() {
               Log.e("Get Address", "Something Went wrong")
            }

        })
//        calling the getAddressListener function of GetAddressFromLatLng Class
        addressTask.getAddressListener()
    }
    }

//New way...instead of writing onclick for every id we use View.OnclickListener all on click events will be handled here...just add  View.OnclickListener as clas inheriting it
    override fun onClick(v: View?) {
       when(v!!.id){
           R.id.et_date ->{
//               for showing date picker dialog
               DatePickerDialog(this@AddHappyPlace,dateSetListener,cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH)).show()
           }
           R.id.tv_add_image ->{
//               this dialog will show when we click on add image
                val pictureDialog=AlertDialog.Builder(this)
               pictureDialog.setTitle("Select Action")
//               items of Alert dialog
               val pictureDialogItems= arrayOf("Select Photo from Gallery","Capture Photo from Camera")
               pictureDialog.setItems(pictureDialogItems){
                   _, which->
                   when(which){
                       0-> choosePhotoFromGallery()
                       1-> takePhotoFromCamera()
                   }
               }
               pictureDialog.show()
           }

           R.id.btn_save -> {

               when {
                   binding?.etTitle?.text.isNullOrEmpty() -> {
                       Toast.makeText(this, "Please enter title", Toast.LENGTH_SHORT).show()
                   }
                   binding?.etDescription?.text.isNullOrEmpty() -> {
                       Toast.makeText(this, "Please enter description", Toast.LENGTH_SHORT)
                           .show()
                   }
                   binding?.etLocation?.text.isNullOrEmpty() -> {
                       Toast.makeText(this, "Please select location", Toast.LENGTH_SHORT)
                           .show()
                   }
                   saveImageToInternalStorage == null -> {
                       Toast.makeText(this, "Please add image", Toast.LENGTH_SHORT).show()
                   }
                   else -> {

                       // Assigning all the values to data model class.
                       val happyPlaceModel = HappyPlaceModel(
//                           line 159 because if we edit happy place, id should be same...if we write just 0 then it auto increment because id is primary key...So to make id of edited happy place same we do this
                           if (mHappyPlaceDetails==null) 0 else mHappyPlaceDetails!!.id,
                           binding?.etTitle?.text.toString(),
                           saveImageToInternalStorage.toString(),
                           binding?.etDescription?.text.toString(),
                           binding?.etDate?.text.toString(),
                           binding?.etLocation?.text.toString(),
                           mLatitude,
                           mLongitude
                       )

                       // Here we initialize the database handler class.
                       val dbHandler = DatabaseHandler(this)
//                     if mHappyPlace is null then only we add new entry to prevent duplicates or edited entries
                       if(mHappyPlaceDetails==null){
                           val addHappyPlace = dbHandler.addHappyPlace(happyPlaceModel)

                           if (addHappyPlace > 0) {
//                           setting result ok for this activity so that onActivityResult fun will call in mainActivity from where this intent is called
                               setResult(Activity.RESULT_OK)
                               finish();//finishing activity
                           }
                       }
                       else{
                           val updateHappyPlace = dbHandler.updateHappyPlace(happyPlaceModel)

                           if (updateHappyPlace > 0) {
//                           setting result ok for this activity so that onActivityResult fun will call in mainActivity from where this intent is called
                               setResult(Activity.RESULT_OK)
                               finish();//finishing activity
                           }
                       }
                   }
               }
           }

           R.id.et_location->{
               try {
//               this is the list of field which has to be passed
                   val fields= listOf(Place.Field.ID,Place.Field.NAME,Place.Field.LAT_LNG,Place.Field.ADDRESS)

//                   Start the autoComplete intent with a Unique Request code...this Auto Complete is feature from google p;aces dependency
                   val intent=Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).build(this)
                   startActivityForResult(intent, PLACES_AUTO_COMPLETE_REQUEST_CODE)


               }catch (e:Exception){
                   e.printStackTrace()
               }
           }

           R.id.tv_select_current_location->{
//               if Location is not enabled we send user to Location intent inside system to turn it on
               if (!isLocationEnabled()){
                   Toast.makeText(this,"Your Location Provider is Turned Off. Please turn it On",Toast.LENGTH_SHORT).show()
                   val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                   startActivity(intent)
               }else{
                   Dexter.withActivity(this).withPermissions(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION)
                       .withListener(object : MultiplePermissionsListener{
                           override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                               if (report!!.areAllPermissionsGranted()){
//                                   Toast.makeText(this@AddHappyPlace,"Location permission granted. Now you can see the current Location", Toast.LENGTH_SHORT).show()
                                   requestNewLocationData()
                               }
                           }

                           override fun onPermissionRationaleShouldBeShown(
                               permissions: MutableList<PermissionRequest>,
                               token: PermissionToken
                           ) {
                                showRationaleDialogueForPermissions()
                           }

                       }).onSameThread().check()
               }
           }
       }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode==Activity.RESULT_OK){
            if (requestCode== GALLERY){
                 if (data!=null){
//                     data.data gives us the address...URI(Uniform Resource Identifier)
                     val contentUri=data.data
                     try {
//                         getting the bitmap from our URI
//                         this.contentResolver: The contentResolver is a system-level class that acts as a bridge between your app and the underlying Android content providers. It's used to perform various operations related to accessing and manipulating data stored on the device.

                         val selectedImageBitmap=MediaStore.Images.Media.getBitmap(this.contentResolver,contentUri)
//                         this saves the address of image stored in internal storage which is returned by fun saveImageToInternalStorage
                        saveImageToInternalStorage = saveImageToInternalStorage(selectedImageBitmap)
                         Log.e("Saved Image","Path :: $saveImageToInternalStorage")


                         binding?.ivPlaceImage?.setImageBitmap(selectedImageBitmap)
                     }
                     catch (e: IOException){
                         e.printStackTrace()
                         Toast.makeText(this@AddHappyPlace,"failed to load image from Gallery",Toast.LENGTH_LONG).show()
                     }
                 }
            }
            else if (requestCode== CAMERA){
//                to make captured data from camera as Bitmap....converted/typeCasted to bitmap
                val thumbnail:Bitmap=data!!.extras!!.get("data") as Bitmap

                saveImageToInternalStorage = saveImageToInternalStorage(thumbnail)
                Log.e("Saved Image","Path :: $saveImageToInternalStorage")

                binding?.ivPlaceImage?.setImageBitmap(thumbnail)
            }
            else if (requestCode == PLACES_AUTO_COMPLETE_REQUEST_CODE){
//                 fetching the selected place
//                Log.e("status", "Status of AutoComplete ${Autocomplete.getStatusFromIntent(data)}")
                val place:Place=Autocomplete.getPlaceFromIntent(data!!)
                binding?.etLocation?.setText(place.address)
                mLatitude= place.latLng!!.latitude
                mLongitude= place.latLng!!.longitude
            }
        }
    }

    private fun takePhotoFromCamera(){
        Dexter.withActivity(this@AddHappyPlace).withPermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.CAMERA
        ).withListener(object : MultiplePermissionsListener{

            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
//                checking if all permission were Granted
                if(report!!.areAllPermissionsGranted()){
//                    for capturing image
                    val cameraIntent=Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//                   Camera IS constant companion object we made for the Result code or we can say identify the request made in onActivityResult....start activity for result means we start activity and get result...this auto calls the inbuilt override function  onActivityResult
                    startActivityForResult(cameraIntent, CAMERA)
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                permissions: MutableList<PermissionRequest>,
                token: PermissionToken) {
                showRationaleDialogueForPermissions()
            }
        }).onSameThread().check()
    }

    private fun choosePhotoFromGallery() {
        Dexter.withActivity(this@AddHappyPlace).withPermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).withListener(object : MultiplePermissionsListener{

            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
//                checking if all permission were Granted
                if(report!!.areAllPermissionsGranted()){
//                    action pick means we will pick something from the intent
                  val galleryIntent=Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
//                    GALLERY IS constant companion object we made for the Result code or we can say identify the request made in onActivityResult....start activity for result means we start activity and get result...this auto calls the inbuilt override function  onActivityResult
                    startActivityForResult(galleryIntent, GALLERY)
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                permissions: MutableList<PermissionRequest>,
                token: PermissionToken) {
                showRationaleDialogueForPermissions()
            }
        }).onSameThread().check()

    }


    private fun showRationaleDialogueForPermissions() {
        AlertDialog.Builder(this@AddHappyPlace).setMessage("It looks Like you have turned of Permission required for this feature. It can be enabled under Application settings"
        ).setPositiveButton("Go To Settings"){_,_->
// making user go to settings to manually give the permission
//            automatically going to the APP Settings
            try {
                val intent=Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri=Uri.fromParts("package",packageName,null)
                intent.data=uri
                startActivity(intent)
            }
            catch (e: ActivityNotFoundException){
                e.printStackTrace()
            }

        }.setNegativeButton("Cancel"){dialog,_ ->
           dialog.dismiss()
        }.show()
    }

    private fun updateDateInView(){
//        formatting the date
        val myFormat="dd.MM.yyyy"
        val sdf=SimpleDateFormat(myFormat, Locale.getDefault())
//        setting the date in etDate input box it formats the cal time to the format given to sdf
        binding?.etDate?.setText(sdf.format(cal.time).toString())

    }

    private fun saveImageToInternalStorage(bitmap:Bitmap):Uri{
//        getting the context of the application
        val wrapper=ContextWrapper(applicationContext)
//        every App has its directory on Device Storage and we are Accessing it by this way...Context Mode private means only this app can access thisa directory
        var file=wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)
//         now we have directory now we are using it to create unique file which will store image...UUID.randomUUID() generates random user id
        file=File(file,"${UUID.randomUUID()}.jpg")
//        now we have file...we need FileOutputStream to store it

        try {
//             we are saving image to our phone...means we are trying to output an image to our phone so we need to use output Stream
            val stream:OutputStream=FileOutputStream(file)
//            finally saving the the image after compression
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream)

        }
        catch (e: IOException){
            e.printStackTrace()
        }
//        returning the absolute path of file that we have created
        return Uri.parse(file.absolutePath)
    }
}
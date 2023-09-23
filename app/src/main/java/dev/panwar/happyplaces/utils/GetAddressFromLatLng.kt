package dev.panwar.happyplaces.utils

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.AsyncTask
import java.util.*

class GetAddressFromLatLng(context:Context, private val latitude:Double, private val longitude: Double): AsyncTask<Void,String,String>() {


//    class that converts latitude longitude to address...GEOCODER
    private var geocoder: Geocoder = Geocoder(context, Locale.getDefault())
    private lateinit var mAddressListener: AddressListener //object of interface we created below

    override fun doInBackground(vararg params: Void?): String {

        try {
            //        maxResult=1 means we chose any one address from the given latitude and longitude as there can be several addresses at same latitude and longitude available
//        getting list of address from given lat and long
            val addressList : List<Address>?= geocoder.getFromLocation(latitude,longitude, 1)

            if (addressList!=null && addressList.isNotEmpty()){
//            as we have selected maxResult 1...we have one one location in address list so addressList[0]
                val address:Address=addressList[0]
//            String builder because String is immutable...and StringBuilder is mutable
                val sb=StringBuilder()
//            as a address has many properties like plot no, street no, locality, landmark, city, state, country, lat, long, etc to traverse over all these we loop over these and save all these in a string with a space between each entry
                for (i in 0..address.maxAddressLineIndex){
                    sb.append(address.getAddressLine(i)).append(" ")
                }
//            deleting the last space " "
                sb.deleteCharAt(sb.length-1)
                return sb.toString()
            }
        }catch (e: Exception){
            e.printStackTrace()
        }

        return ""
    }
// auto called after execution of do in Background function....if we override on PreExecute than it execute before do in background
    override fun onPostExecute(resultString: String?) {
        if (resultString==null){
//            function of AddressListener interface
            mAddressListener.onError()
        }else{
//            function of AddressListener interface
            mAddressListener.onAddressFound(resultString)
        }
        super.onPostExecute(resultString)
    }
//setter for our Address Listener
    fun setAddressListener(addressListener: AddressListener){
//        putting addressListener to our Address Listener
        mAddressListener=addressListener
    }

//    getter for our Address Listener
    fun getAddressListener(){
//    this is asyncTask function...that execute this Async task
        execute()
    }

    interface AddressListener{
        fun onAddressFound(address:String?)
        fun onError()
    }
}
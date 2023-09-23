package dev.panwar.happyplaces.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.panwar.happyplaces.adapters.HappyPlacesAdapter
import dev.panwar.happyplaces.database.DatabaseHandler
import dev.panwar.happyplaces.databinding.ActivityMainBinding
import dev.panwar.happyplaces.models.HappyPlaceModel
import dev.panwar.happyplaces.utils.SwipeToDeleteCallback
import dev.panwar.happyplaces.utils.SwipeToEditCallback

class MainActivity : AppCompatActivity() {

    private var binding:ActivityMainBinding?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.fabAddHappyPlace?.setOnClickListener {
            val intent= Intent(this, AddHappyPlace::class.java)
            startActivityForResult(intent,ADD_PLACE_ACTIVITY_REQUEST_CODE)
        }
        getHappyPlacesListFromLocalDB()


    }

    //  Calling an function which have created for getting list of inserted data from local database. And the list of values are printed in the log.)
    // START
    /**
     * A function to get the list of happy place from local database.
     */
    private fun getHappyPlacesListFromLocalDB() {

        val dbHandler = DatabaseHandler(this)

        val getHappyPlacesList = dbHandler.getHappyPlacesList()

        //  Calling an function which have created for getting list of inserted data from local database
        //  and passing the list to recyclerview to populate in UI.)
        // START
        if (getHappyPlacesList.size > 0) {
            binding?.rvHappyPlaces?.visibility = View.VISIBLE
            binding?.tvNoRecordsAvailable?.visibility = View.GONE
            setupHappyPlacesRecyclerView(getHappyPlacesList)
        } else {
            binding?.rvHappyPlaces?.visibility = View.GONE
            binding?.tvNoRecordsAvailable?.visibility = View.VISIBLE
        }
        // END
    }
    // END

    // Creating a function for setting up the recyclerview to UI.)
    // START
    /**
     * A function to populate the recyclerview to the UI.
     */
    private fun setupHappyPlacesRecyclerView(happyPlacesList: ArrayList<HappyPlaceModel>) {

        binding?.rvHappyPlaces?.layoutManager = LinearLayoutManager(this)
        binding?.rvHappyPlaces?.setHasFixedSize(true)

        val placesAdapter = HappyPlacesAdapter(this, happyPlacesList)
        binding?.rvHappyPlaces?.adapter = placesAdapter

//        step 4...when we click on places adapter...created object of Onclick listener interface ..implementing OnClick function
        placesAdapter.setOnClickListener(object : HappyPlacesAdapter.OnClickListener{
            override fun onClick(position: Int, model: HappyPlaceModel) {
                val intent=Intent(this@MainActivity,HappyPlaceDetailActivity::class.java)
//                sending object
                intent.putExtra(EXTRA_PLACE_DETAILS,model)
                startActivity(intent)
//                step 5 in happy places adapter
            }
        })

        // TODO(Step 3: Bind the edit feature class to recyclerview)
        // START
        val editSwipeHandler = object : SwipeToEditCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // TODO (Step 5: Call the adapter function when it is swiped)
                // START
                val adapter = binding?.rvHappyPlaces?.adapter as HappyPlacesAdapter
                adapter.notifyEditItem(
                    this@MainActivity,
                    viewHolder.adapterPosition,
                    ADD_PLACE_ACTIVITY_REQUEST_CODE
                )
                // END
            }
        }
        val editItemTouchHelper = ItemTouchHelper(editSwipeHandler)
        editItemTouchHelper.attachToRecyclerView(binding?.rvHappyPlaces)
        // END

        //  Bind the delete feature class to recyclerview)
        // START
        val deleteSwipeHandler = object : SwipeToDeleteCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // TODO (Step 5: Call the adapter function when it is swiped)
                // START
                val adapter = binding?.rvHappyPlaces?.adapter as HappyPlacesAdapter
//                remove at function in Adapter
                adapter.removeAt(viewHolder.adapterPosition)
//                Loading the updated list again
                getHappyPlacesListFromLocalDB()
            }
        }
        val deleteItemTouchHelper = ItemTouchHelper(deleteSwipeHandler)
        deleteItemTouchHelper.attachToRecyclerView(binding?.rvHappyPlaces)
        // END
    }
    // END

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode== ADD_PLACE_ACTIVITY_REQUEST_CODE){
            if(resultCode==Activity.RESULT_OK){
                getHappyPlacesListFromLocalDB()
            }else{
                Log.e("Activity","cancelled or back Pressed")
            }
        }
    }

    companion object{
        var ADD_PLACE_ACTIVITY_REQUEST_CODE=1
        var EXTRA_PLACE_DETAILS="extra_place_details"
    }
}
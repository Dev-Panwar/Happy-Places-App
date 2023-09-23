package dev.panwar.happyplaces.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.panwar.happyplaces.activities.AddHappyPlace
import dev.panwar.happyplaces.activities.MainActivity
import dev.panwar.happyplaces.database.DatabaseHandler
import dev.panwar.happyplaces.databinding.ItemHappyPlaceBinding
import dev.panwar.happyplaces.models.HappyPlaceModel

//used recycler view with view binding
// Creating an adapter class for binding it to the recyclerview in the new package which is adapters.)
// START
class HappyPlacesAdapter(
    private val context: Context,
    private val list: ArrayList<HappyPlaceModel>
) : RecyclerView.Adapter<HappyPlacesAdapter.MyViewHolder>() {


//  Step 2 for on click functionality in Recycler view.. onClick Listener of type OnclickListener interface
     private var onCLickListener: OnClickListener?=null

    /**
     * Inflates the item views which is designed in xml layout file
     *
     * Create a new {@link MyViewHolder} and initializes some private fields to be used by RecyclerView.
     */


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        // Use View Binding to inflate the layout and create an instance of ItemHappyPlaceBinding
        val binding = ItemHappyPlaceBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(binding)
    }

    /**
     * Binds each item in the ArrayList to a view
     *
     * Called when RecyclerView needs a new {@link MyViewHolder} of the given type to represent
     * an item.
     */
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model = list[position]
        val binding = holder.binding

        // Use the binding object to directly access views in the layout
        binding.ivPlaceImage.setImageURI(Uri.parse(model.image))
        binding.tvTitle.text = model.title
        binding.tvDescription.text = model.description

//        step 5 for adding on click in recycler view
         holder.itemView.setOnClickListener{
             if(onCLickListener!=null){
                 onCLickListener!!.onClick(position,model)
             }
         }
    }

    // TODO (Step 4: Create a function to edit the happy place details which is inserted earlier and pass the details through intent.)
    // START
    /**
     * A function to edit the added happy place detail and pass the existing details through intent.
     */
    fun notifyEditItem(activity: Activity, position: Int, requestCode: Int) {
        val intent = Intent(context, AddHappyPlace::class.java)
        intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS, list[position])
//        as this function will be called from Main activity..So activity.startActivityForResult
        activity.startActivityForResult(
            intent,
            requestCode
        ) // Activity is started with requestCode

        notifyItemChanged(position) // Notify any registered observers that the item at position has changed.
    }
    // END

    fun removeAt(position: Int){
        val dbHandler=DatabaseHandler(context)
        val isDeleted=dbHandler.deleteHappyPlace(list[position])
        if (isDeleted>0){
//            removeAt function of list
            list.removeAt(position)
//            Notifying the adapter
            notifyItemRemoved(position)
        }
    }

    /**
     * Gets the number of items in the list
     */

    override fun getItemCount(): Int {
        return list.size
    }

//    for on click functionality in recycler view...we created an interface that needs to be implemented as this class is already
//    inheriting Recycler view Adapter so Cannot inherit OnCLickListener
//    step 1
    interface OnClickListener{
        fun onClick(position: Int, model: HappyPlaceModel)
    }
//  step 3 for implementing recycler view....step 4 in main activity
    fun setOnClickListener(onClickListener: OnClickListener){
        this.onCLickListener=onClickListener
    }

    /**
     * A ViewHolder describes an item view and metadata about its place within the RecyclerView.
     */
    inner class MyViewHolder(val binding: ItemHappyPlaceBinding) : RecyclerView.ViewHolder(binding.root)
}
// END

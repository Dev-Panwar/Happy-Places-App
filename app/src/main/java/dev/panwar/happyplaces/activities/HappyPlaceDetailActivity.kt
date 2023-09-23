package dev.panwar.happyplaces.activities

import android.content.Intent
import android.net.Uri
import dev.panwar.happyplaces.databinding.ActivityAddHappyPlaceBinding
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import dev.panwar.happyplaces.databinding.ActivityHappyPlaceDetailBinding
import dev.panwar.happyplaces.models.HappyPlaceModel

class HappyPlaceDetailActivity : AppCompatActivity() {

    private var binding:ActivityHappyPlaceDetailBinding?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityHappyPlaceDetailBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        var happyPlaceDetailModel: HappyPlaceModel?=null
//    retrieving whole object
        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)){
            happyPlaceDetailModel=intent.getParcelableExtra(MainActivity.EXTRA_PLACE_DETAILS) as? HappyPlaceModel
        }

        if (happyPlaceDetailModel!=null){
//            setting the Title of Toolbar of detail activity....shows title of happy place clicked
            setSupportActionBar(binding?.toolbarHappyPlaceDetail)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title=happyPlaceDetailModel.title

            binding?.toolbarHappyPlaceDetail?.setNavigationOnClickListener {
                onBackPressed()
            }
// set Image URI because in data class Image is stored as String i.e link or Path
            binding?.ivPlaceImage?.setImageURI(Uri.parse(happyPlaceDetailModel.image))
            binding?.tvTitle?.text=happyPlaceDetailModel.title
            binding?.tvDescription?.text=happyPlaceDetailModel.description
            binding?.tvDate?.text=happyPlaceDetailModel.date
            binding?.tvLocation?.text=happyPlaceDetailModel.location

            binding?.btnViewOnMap?.setOnClickListener {
                val intent=Intent(this, MapActivity::class.java)
//                sending happyPlaceDetail model....i.e Object of happy Place model.... MainActivity.EXTRA_PLACE_DETAILS is const val object
                intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS, happyPlaceDetailModel)
                startActivity(intent)
            }
        }

    }
}
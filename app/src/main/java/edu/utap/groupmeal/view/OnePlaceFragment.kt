package edu.utap.groupmeal.view

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import edu.utap.groupmeal.R
import edu.utap.groupmeal.databinding.FragmentOnePlaceBinding
import com.bumptech.glide.Glide
import kotlin.math.min

class OnePlaceFragment : Fragment(R.layout.fragment_one_place) {
    private val viewModel: GooglePlacesViewModel by activityViewModels()
    private val args: OnePlaceFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentOnePlaceBinding.bind(view)

        viewModel.hideActionBarFavorites()

        viewModel.fetchOnePlace(args.place.place_id)
        viewModel.observeOnePlace().observe(viewLifecycleOwner){ thePlace ->
            thePlace.place_id = args.place.place_id
            binding.onePlaceName.text = thePlace.name

            if (thePlace.opening_hours?.open_now == true) {
                binding.openNow.text = "OPEN"
                binding.openNow.setTextColor(Color.parseColor("#0cb03b"))
            } else {
                binding.openNow.text = "CLOSED"
                binding.openNow.setTextColor(Color.parseColor("#d31e13"))
            }
            binding.onePlaceWebsite.text = "Website"
            binding.onePlaceWebsite.setOnClickListener {
                val url = thePlace.website
                if (!url.isNullOrBlank()) {
                    val finalUrl = if (url.startsWith("http")) url else "https://$url"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(finalUrl))
                    startActivity(intent)
                }
            }
            binding.openingHours.text = thePlace.opening_hours?.weekday_text?.joinToString("\n")
            binding.onePlaceAddress.text = thePlace.formatted_address
            binding.onePlacePhoneNumber.text = thePlace.international_phone_number

            binding.onePlaceAddress.setOnClickListener {
                val mapsUrl = thePlace.url
                if (!mapsUrl.isNullOrBlank()) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(mapsUrl))
                    startActivity(intent)
                }
            }

            binding.btnInvite.setOnClickListener {
                val action = OnePlaceFragmentDirections
                    .actionOnePlaceFragmentToCreateInviteFragment(thePlace)
                findNavController().navigate(action)
            }

            val onePlaceImages = listOf(binding.onePlaceImage1, binding.onePlaceImage2, binding.onePlaceImage3)

            if (thePlace.photos.isNullOrEmpty()) {
                for (onePlaceImage in onePlaceImages) {
                    onePlaceImage.visibility = View.GONE
                }
            } else {
                for (i in 0 until min(onePlaceImages.size, thePlace.photos.size)) {
                    val onePlaceImage = onePlaceImages[i]
                    onePlaceImage.visibility = View.VISIBLE
                    val photo = thePlace.photos[i]
                    val imageUrl = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=${photo.width}&photoreference=${photo.photo_reference}&key=AIzaSyDZb5T5praR793eAhVr4ylMjD0Xlb12bec"

                    Glide.with(this)
                        .load(imageUrl)
                        .into(onePlaceImage)
                }
            }
        }
    }
}
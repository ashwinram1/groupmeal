package edu.utap.groupmeal.view.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import edu.utap.groupmeal.R
import edu.utap.groupmeal.api.GooglePlace
import edu.utap.groupmeal.databinding.RowPlaceBinding
import edu.utap.groupmeal.view.GooglePlacesViewModel

class PlaceRowAdapter(private val viewModel: GooglePlacesViewModel,
                      private val navigateToOnePlace: (GooglePlace)->Unit )
    : ListAdapter<GooglePlace, PlaceRowAdapter.VH>(PlaceDiff()) {

    inner class VH(val rowPlaceBinding: RowPlaceBinding)
        : RecyclerView.ViewHolder(rowPlaceBinding.root) {
        init {
            rowPlaceBinding.name.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    navigateToOnePlace(getItem(position))
                }
            }

            rowPlaceBinding.address.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    navigateToOnePlace(getItem(position))
                }
            }

            rowPlaceBinding.rowFav.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val place = getItem(position)
                    if (viewModel.placeIsFavorite(place)) {
                        rowPlaceBinding.rowFav.setImageResource(R.drawable.ic_favorite_border_black_24dp)
                    } else {
                        rowPlaceBinding.rowFav.setImageResource(R.drawable.ic_favorite_black_24dp)
                    }
                    viewModel.togglePlaceFavorite(place)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = RowPlaceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val binding = holder.rowPlaceBinding
        val place = getItem(position)
        binding.name.text = place.name
        binding.address.text = place.vicinity

        if (place.price_level == null) {
            binding.priceLevel.text = "N/A"
        } else {
            binding.priceLevel.text = "${place.price_level} / 4"
        }

        if (place.rating == null) {
            binding.rating.text = "N/A"
        } else {
            binding.rating.text = "${place.rating} (${place.user_ratings_total})"
        }

        if (viewModel.placeIsFavorite(place)) {
            binding.rowFav.setImageResource(R.drawable.ic_favorite_black_24dp)
        } else {
            binding.rowFav.setImageResource(R.drawable.ic_favorite_border_black_24dp)
        }

        if (place.opening_hours?.open_now == true) {
            binding.openNow.text = "OPEN"
            binding.openNow.setTextColor(Color.parseColor("#0cb03b"))
        } else {
            binding.openNow.text = "CLOSED"
            binding.openNow.setTextColor(Color.parseColor("#d31e13"))
        }
    }

    class PlaceDiff : DiffUtil.ItemCallback<GooglePlace>() {
        override fun areItemsTheSame(oldItem: GooglePlace, newItem: GooglePlace): Boolean {
            return oldItem.place_id == newItem.place_id
        }
        override fun areContentsTheSame(oldItem: GooglePlace, newItem: GooglePlace): Boolean {
            return oldItem.name == newItem.name &&
                    oldItem.rating == newItem.rating
        }
    }
}


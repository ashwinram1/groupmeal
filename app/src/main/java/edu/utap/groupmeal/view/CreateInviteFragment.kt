package edu.utap.groupmeal.view

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import edu.utap.groupmeal.MainViewModel
import edu.utap.groupmeal.R
import edu.utap.groupmeal.databinding.FragmentCreateInviteBinding
import edu.utap.groupmeal.model.Status
import java.util.*
import kotlin.collections.HashMap

class CreateInviteFragment : Fragment(R.layout.fragment_create_invite) {
    private val args: CreateInviteFragmentArgs by navArgs()
    private lateinit var friendsList: List<DocumentReference>
    private val viewModel: MainViewModel by activityViewModels()
    private val addedFriends = HashMap<String, Status>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentCreateInviteBinding.bind(view)

        val place = args.placeArg

        binding.invitePlaceName.text = place.name
        binding.invitePlaceAddress.text = place.formatted_address
        binding.invitePlaceAddress.setOnClickListener {
            val mapsUrl = place.url
            if (!mapsUrl.isNullOrBlank()) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(mapsUrl)))
            }
        }

        binding.invitePlaceUrl.text = "Website"
        binding.invitePlaceUrl.setOnClickListener {
            val url = place.website
            if (!url.isNullOrBlank()) {
                val finalUrl = if (url.startsWith("http")) url else "https://$url"
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(finalUrl)))
            }
        }

        val calendar = Calendar.getInstance()
        binding.pickDateTime.setOnClickListener {
            val dateListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
                val timeListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                    calendar.set(year, month, day, hour, minute)
                    binding.selectedDateTime.text = calendar.time.toString()
                }
                TimePickerDialog(
                    requireContext(),
                    timeListener,
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    false
                ).show()
            }

            DatePickerDialog(
                requireContext(),
                dateListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).apply {
                datePicker.minDate = System.currentTimeMillis()
            }.show()
        }

        friendsList = viewModel.getFriendsDocRef()

        binding.addFriendButton.setOnClickListener {
            val friendEmail = binding.friendEmailEnter.text.toString().trim()
            if (friendEmail.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a friend's email to add first.", Toast.LENGTH_SHORT).show()
            } else {
                val match = friendsList.find { it.id == friendEmail }
                if (match != null) {
                    addedFriends[match.path] = Status.UNKNOWN
                    binding.addedFriends.text = addedFriends.keys.joinToString("\n") { path ->
                        path.substringAfterLast("/")
                    }
                    Toast.makeText(requireContext(), "$friendEmail added to invite!", Toast.LENGTH_SHORT).show()
                } else {
                    Snackbar.make(binding.root, "$friendEmail was not found among your friends. They need to be added as your friend before you can add them to invites.", Snackbar.LENGTH_LONG).show()
                }
            }
        }

        binding.sendInviteButton.setOnClickListener {
            if (binding.selectedDateTime.text == "No date/time selected") {
                Toast.makeText(requireContext(), "You must select a date/time for this invite first.", Toast.LENGTH_SHORT).show()
            } else if (addedFriends.isEmpty()) {
                Toast.makeText(requireContext(), "You must add friends to this invite first.", Toast.LENGTH_SHORT).show()
            } else {
                // Add yourself as accepted
                addedFriends[viewModel.getCurrentUserRef().path] = Status.ACCEPTED

                viewModel.sendInvite(
                    Timestamp(calendar.time),
                    place.formatted_address ?: "",
                    place.website ?: "",
                    addedFriends
                )
                Toast.makeText(requireContext(), "Invite Sent!", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }
    }
}

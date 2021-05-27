package com.dayaonweb.quoter.view.ui.profile

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.dayaonweb.quoter.R
import com.dayaonweb.quoter.databinding.FragmentProfileBinding
import com.dayaonweb.quoter.enums.Status
import com.dayaonweb.quoter.extensions.isVisible
import com.dayaonweb.quoter.extensions.loadImageUri
import com.dayaonweb.quoter.extensions.snack

class Profile : Fragment() {

    private var binding: FragmentProfileBinding? = null
    private val viewModel by viewModels<ProfileViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false)
        viewModel.fetchUserDetails()
        viewModel.user.observe({ lifecycle }) { user ->
            binding?.ivUserProfilePicture?.loadImageUri(user.profilePicture)
            binding?.btnUpdate?.isVisible(user.name.isNotEmpty())
            binding?.tilUsername?.editText?.setText(user.name)
        }
        viewModel.status.observe(viewLifecycleOwner,{ status ->
            when (status) {
                Status.READ_FAIL -> view?.snack("Unable to fetch data. Please try again later!")
                Status.UPDATE_FAIL -> view?.snack("Unable to update. Please try again later!")
                Status.UPDATE_SUCCESS -> view?.snack("Updated!")
                Status.IN_PROGRESS -> view?.snack("Updating...")
                else -> return@observe
            }
        })

        return binding?.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.apply {
            ivUserProfilePicture.setOnClickListener {
                // Handle image pick + image capture
                view.snack("Coming soon!")
            }
            btnSuggestion.setOnClickListener {
                // Show form dialog
            }
            btnUpdate.setOnClickListener {
                viewModel.updateUserName(binding?.tilUsername)
            }
        }
    }


    override fun onResume() {
        super.onResume()
        val toolbar = (activity as AppCompatActivity).supportActionBar
        toolbar?.apply {
            title = "Profile"
            subtitle = "Edit profile"
            setBackgroundDrawable(
                GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    intArrayOf(
                        context?.getColor(R.color.purple_700)!!,
                        context?.getColor(R.color.purple_500)!!
                    )
                )
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}
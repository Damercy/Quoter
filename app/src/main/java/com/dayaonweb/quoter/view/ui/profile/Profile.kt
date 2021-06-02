package com.dayaonweb.quoter.view.ui.profile

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.github.dhaval2404.imagepicker.ImagePicker

private const val TAG = "Profile"

class Profile : Fragment() {

    private var binding: FragmentProfileBinding? = null
    private val viewModel by viewModels<ProfileViewModel>()
    private val startForProfileImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            viewModel.handleProfileImageResult(result)
        }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false)
        binding?.root?.context?.let { viewModel.fetchUserDetails(it) }
        viewModel.user.observe({ lifecycle }) { user ->
            binding?.btnUpdate?.isVisible(user.name.isNotEmpty())
            binding?.tilUsername?.editText?.setText(user.name)
            binding?.ivUserProfilePicture?.loadImageUri(user.profilePicture,R.drawable.ic_profile)
        }
        viewModel.status.observe(viewLifecycleOwner, { status ->

            when (status) {
                Status.READ_FAIL -> {
                    toggleIndicator(false)
                    view?.snack("Unable to fetch data. Please try again later!")
                }
                Status.UPDATE_FAIL -> {
                    toggleIndicator(false)
                    view?.snack("Unable to update. Please try again later!")
                }
                Status.UPDATE_SUCCESS -> {
                    toggleIndicator(false)
                    view?.snack("Updated!")
                }
                Status.IN_PROGRESS -> toggleIndicator(true)
                else -> return@observe
            }
        })
        viewModel.progressAmount.observe(viewLifecycleOwner, { progressAmount ->
            if(progressAmount>0)
                binding?.uploadIndicator?.setProgressCompat(progressAmount, true)
        })
        return binding?.root
    }

    private fun toggleIndicator(visible: Boolean) {
        with(binding?.uploadIndicator) {
            this?.isVisible(visible)
            if (!visible)
                this?.isIndeterminate = true
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.apply {
            ivUserProfilePicture.setOnClickListener {
                handleProfilePictureIntent()
            }
            btnSuggestion.setOnClickListener {
                // Show form dialog
            }
            btnUpdate.setOnClickListener {
                viewModel.updateUserName(binding?.tilUsername)
            }
        }
    }

    private fun handleProfilePictureIntent() {
        ImagePicker.with(this)
            .cropSquare()
            .compress(1024)
            .createIntent { intent ->
                startForProfileImageResult.launch(intent)
            }
    }


    override fun onResume() {
        super.onResume()
        val toolbar = (activity as AppCompatActivity).supportActionBar
        toolbar?.apply {
            show()
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
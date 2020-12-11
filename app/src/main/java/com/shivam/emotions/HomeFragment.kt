package com.shivam.emotions

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.shivam.emotions.databinding.FragmentHomeBinding


class HomeFragment : BaseFragment() {
    private lateinit var homeBinding: FragmentHomeBinding
    private var shouldNavigate = true


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeBinding = FragmentHomeBinding.inflate(inflater, container, false)
        return homeBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showInputShareDialog()


        val receiveImageIntent = requireActivity().intent
        if (receiveImageIntent.action == Intent.ACTION_SEND && shouldNavigate) {

            val receivedImageURI =
                receiveImageIntent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri

            findNavController().navigate(
                HomeFragmentDirections.actionHomeFragmentToImageFragment(
                    receivedImageURI.toString()
                )
            )


        }


        homeBinding.btnVideo.setOnClickListener {
//            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToPermissionFragment())
        }

        homeBinding.btnImage.setOnClickListener {
            findNavController().navigate(
                HomeFragmentDirections.actionHomeFragmentToImageFragment(
                    null
                )
            )
        }
    }

    override fun onResume() {
        super.onResume()
        shouldNavigate = false
    }

    private fun showInputShareDialog() {
        val imageDialog =
            MaterialAlertDialogBuilder(requireContext(), R.style.AboutUsAlertDialogStyle)
        imageDialog.setTitle("Did you know ?")

        val dialogLayout = layoutInflater.inflate(R.layout.image_input_share, null)
        imageDialog.setView(dialogLayout)

        imageDialog.setNegativeButton("Cool") { dialog, which ->
            dialog.dismiss()
        }
        imageDialog.setMessage("You can share images from other apps and analyse the emotions of people present in them directly in our app")
        imageDialog.show()


    }

}
package com.example.audiorecorderandimagepicker.image

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.example.audiorecorderandimagepicker.audiorecorder.loadFromUri
import com.example.audiorecorderandimagepicker.audiorecorder.loadFromUrl
import com.example.audiorecorderandimagepicker.audiorecorder.onThrottledClick
import com.example.audiorecorderandimagepicker.databinding.DialogFragmentImageViewerBinding

class ImageViewerDialog : DialogFragment() {

    companion object {

        const val TAG = "ImageViewerDialog"

        private const val KEY_IMAGE = "IMAGE"
        private const val KEY_LOAD_FROM = "KEY_LOAD_FROM"

        fun newInstance(image: String, keyLoadFrom: LoadFrom): ImageViewerDialog {
            val args = Bundle()
            args.putString(KEY_IMAGE, image)
            args.putInt(KEY_LOAD_FROM, keyLoadFrom.ordinal)
            val fragment = ImageViewerDialog()
            fragment.arguments = args
            return fragment
        }

        enum class LoadFrom {
            URI, LINK
        }

    }

    lateinit var binding: DialogFragmentImageViewerBinding

    private var image: String = ""
    private var loadFrom: Int = LoadFrom.URI.ordinal

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        arguments?.let {
            image = it.getString(KEY_IMAGE, "")
            loadFrom = it.getInt(KEY_LOAD_FROM, LoadFrom.URI.ordinal)
        }
        binding = DialogFragmentImageViewerBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        when (loadFrom) {
            LoadFrom.URI.ordinal -> binding.imvImage.loadFromUri(image)
            LoadFrom.LINK.ordinal -> binding.imvImage.loadFromUrl(image)
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        dialog?.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        }
    }

    private fun setupClickListeners() {
        binding.btnClose.onThrottledClick { dismiss() }
    }

}
package com.example.audiorecorderandimagepicker.audiorecorder.player

import android.annotation.SuppressLint
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.Color
import android.graphics.PorterDuff
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.example.audiorecorderandimagepicker.R
import com.example.audiorecorderandimagepicker.audiorecorder.isNull
import com.example.audiorecorderandimagepicker.databinding.DialogFragmentAudioPlayerBinding

class AudioPlayerDialogFragment :
    DialogFragment() {

    companion object {

        const val TAG = "AudioPlayerDialogFragment"

        private const val KEY_AUDIO = "AUDIO"
        private const val KEY_LOAD_FROM = "LOAD_FROM"

        fun newInstance(
            image: String,
            keyLoadFrom: LoadFrom,
        ): AudioPlayerDialogFragment {
            val args = Bundle()
            args.putString(KEY_AUDIO, image)
            args.putInt(KEY_LOAD_FROM, keyLoadFrom.ordinal)
            val fragment = AudioPlayerDialogFragment()
            fragment.arguments = args
            return fragment
        }

        enum class LoadFrom {
            URI, LINK
        }

    }

    lateinit var binding: DialogFragmentAudioPlayerBinding


    var mediaPlayer: MediaPlayer? = MediaPlayer()

    private var audio: String = ""
    private var loadFrom: Int = LoadFrom.URI.ordinal


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        arguments?.let {
            audio = it.getString(KEY_AUDIO, "")
            loadFrom = it.getInt(KEY_LOAD_FROM, LoadFrom.URI.ordinal)
        }
        binding = DialogFragmentAudioPlayerBinding.inflate(layoutInflater)
        initPlayer()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        customize()
        setupClickListeners()
    }

    private fun customize() {
       val primaryColor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
           requireContext().getColor(R.color.purple_200)
       } else {
         Color.parseColor("#FFBB86FC")
       }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            binding.slider.progressDrawable.colorFilter =
                BlendModeColorFilter(primaryColor, BlendMode.SRC_ATOP)
            binding.slider.thumb.colorFilter = BlendModeColorFilter(primaryColor, BlendMode.SRC_IN)
        } else {
            binding.slider.progressDrawable.setColorFilter(primaryColor, PorterDuff.Mode.SRC_ATOP)
            binding.slider.thumb.setColorFilter(primaryColor, PorterDuff.Mode.SRC_IN);
        }
        binding.button.setColorFilter(primaryColor)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        /* dialog?.window?.apply {
             setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
             clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
         }*/
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupClickListeners() {
        binding.button.setOnClickListener { playSong() }
        binding.slider.setOnTouchListener { _, _ -> true }
    }


    /*override fun onStopTrackingTouch(seekBar: SeekBar) {
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {}

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        if (mediaPlayer != null && fromUser) {
            *//* mSeekbarUpdateHandler.removeCallbacks(mUpdateSeekbar)
             mediaPlayer?.seekTo(progress * 1000)
             mSeekbarUpdateHandler.postDelayed(mUpdateSeekbar, 0)*//*
        }
    }*/


    private fun initPlayer() {
        try {
            binding.slider.progress = 0
            if (mediaPlayer.isNull()) mediaPlayer = MediaPlayer()
            mediaPlayer?.setOnCompletionListener {
                binding.button.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_play_circle_filled
                    )
                )
            }
            binding.button.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_pause_circle_filled
                )
            )
            mediaPlayer?.setAudioAttributes(
                AudioAttributes
                    .Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )

            when (loadFrom) {
                LoadFrom.URI.ordinal -> mediaPlayer?.setDataSource(
                    requireContext(),
                    Uri.parse(audio)
                )
                LoadFrom.LINK.ordinal -> mediaPlayer?.setDataSource(audio)
            }
            mediaPlayer?.prepare()
            //mediaPlayer?.setVolume(0.5f, 0.5f)
            mediaPlayer?.isLooping = false
            binding.slider.max = mediaPlayer?.duration ?: 0
            mediaPlayer?.start()
            mSeekbarUpdateHandler.postDelayed(mUpdateSeekbar, 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun playSong() {
        try {
            if (mediaPlayer.isNull()) {
                initPlayer()
            } else {
                if (mediaPlayer?.isPlaying == true) {
                    mediaPlayer?.pause()
                    binding.button.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.ic_play_circle_filled
                        )
                    )
                    mSeekbarUpdateHandler.removeCallbacks(mUpdateSeekbar)
                } else {
                    binding.button.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.ic_pause_circle_filled
                        )
                    )
                    mediaPlayer?.start()
                    mSeekbarUpdateHandler.postDelayed(mUpdateSeekbar, 0)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val mSeekbarUpdateHandler: Handler = Handler(Looper.getMainLooper())
    private val mUpdateSeekbar: Runnable = object : Runnable {
        override fun run() {
            binding.slider.progress = mediaPlayer?.currentPosition ?: 0
            mSeekbarUpdateHandler.postDelayed(this, 25)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        clearMediaPlayer()
    }

    private fun clearMediaPlayer() {
        mSeekbarUpdateHandler.removeCallbacks(mUpdateSeekbar)
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
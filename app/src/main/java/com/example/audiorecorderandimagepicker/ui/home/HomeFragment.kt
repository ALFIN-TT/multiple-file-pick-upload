package com.example.audiorecorderandimagepicker.ui.home

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.audiorecorderandimagepicker.BuildConfig
import com.example.audiorecorderandimagepicker.R
import com.example.audiorecorderandimagepicker.audiorecorder.*
import com.example.audiorecorderandimagepicker.audiorecorder.player.AudioPlayerDialogFragment
import com.example.audiorecorderandimagepicker.audiorecorder.record.AudioPlayer
import com.example.audiorecorderandimagepicker.audiorecorder.record.RecorderState
import com.example.audiorecorderandimagepicker.audiorecorder.record.WaveRecorder
import com.example.audiorecorderandimagepicker.databinding.FragmentHomeBinding
import com.example.audiorecorderandimagepicker.image.ImageViewerDialog
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class HomeFragment : Fragment(), AudioRecordButton.OnRecordButtonClickListener,
    AttachmentAdapter.ImageAdapterListener {

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
        private const val TAKE_IMAGE_REQUEST = 0
        private val TAG: String = HomeFragment::class.java.simpleName
        private const val AUTHORITY = "${BuildConfig.APPLICATION_ID}.fileprovider"
        private const val REQUEST_CODE_RECORD_AUDIO = 2
        const val AUDIO_FILE_FORMAT = ".wav"

    }


    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    var dialogView: View? = null
    var captureBtn: Button? = null
    var galleryBtn: Button? = null
    var inflater: LayoutInflater? = null
    var dialog: AlertDialog? = null
    var currentPhotoPath = ""
    var selectedPosition = 0
    lateinit var viewModel: HomeViewModel


    private val _permissions = arrayOf(Manifest.permission.RECORD_AUDIO)
    private lateinit var waveRecorder: WaveRecorder
    private var audioPlayer: AudioPlayer? = null
    private var recordedTime: Long = 0
    private var cachedAudioFile: String? = null

    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            // returns boolean representind whether the
            // permission is granted or not
            if (isGranted) {
                // permission granted continue the normal workflow of app
                Log.i("DEBUG", "permission granted")
                captureImage()
            } else {
                // if permission denied then check whether never ask
                // again is selected or not by making use of
                !ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(), Manifest.permission.CAMERA
                )
                Log.i("DEBUG", "permission denied")
            }
        }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecording()

        binding.btnSend.onThrottledClick {
            /*  val fragment: Fragment = ConfirmOrderFragment.newInstance()
              replaceFragment(fragment)*/
        }

        binding.btnPicImage.onThrottledClick {
            getImageChooser()
        }
    }


    private fun getImageChooser() {
        val builder = AlertDialog.Builder(
            requireActivity()
        )
        inflater = requireActivity().layoutInflater
        dialogView = inflater?.inflate(R.layout.layout_image_chooser, null, false)
        builder.setView(dialogView)
        captureBtn = dialogView?.findViewById<Button>(R.id.btnCapture)
        galleryBtn = dialogView?.findViewById<Button>(R.id.btnGallery)
        captureBtn?.onThrottledClick {
            requestPermission.launch(Manifest.permission.CAMERA)
        }
        galleryBtn?.onThrottledClick {
            pickImages()
        }
        dialog = builder.create()
        dialog?.show()
    }

    private fun pickImages() {
        val galleryIntent = Intent(Intent.ACTION_GET_CONTENT)
        galleryIntent.type = "image/*"
        // Create the File where the photo should go
        var photoFile: File? = null
        try {
            photoFile = createImageFile()
        } catch (e: Exception) {
            // Error occurred while creating the File
            Log.e(TAG, "Error occurred while creating file is: $e")
        }
        // Continue only if the File was successfully created
        if (photoFile != null) {
            val photoURI = FileProvider.getUriForFile(
                requireActivity(),
                AUTHORITY,
                photoFile
            )
            galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            galleryIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST)
        }
        dialog?.dismiss()
    }

    private fun captureImage() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // if (intent.resolveActivity(requireActivity().packageManager) != null) {
        // Create the File where the photo should go

        var photoFile: File? = null
        try {
            photoFile = createImageFile()
        } catch (e: Exception) {
            // Error occurred while creating the File
            Log.e(TAG, "Error occured while creating file is: $e")
        }
        // Continue only if the File was successfully created
        if (photoFile != null) {
            val photoURI = FileProvider.getUriForFile(
                requireActivity(),
                AUTHORITY,
                photoFile
            )
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            startActivityForResult(intent, TAKE_IMAGE_REQUEST)
        }
        // }
        dialog?.dismiss()
    }


    @Throws(IOException::class)
    private fun createImageFile(): File? {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",  /* suffix */
            storageDir /* directory */
        )
        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.absolutePath
        return image
    }


    fun saveBitmapToFile(file: File) {
        try {

            // BitmapFactory options to downsize the image
            val o = BitmapFactory.Options()
            o.inJustDecodeBounds = true
            o.inSampleSize = 6
            // factor of downsizing the image
            var inputStream = FileInputStream(file)
            //Bitmap selectedBitmap = null;
            BitmapFactory.decodeStream(inputStream, null, o)
            inputStream.close()

            // The new size we want to scale to
            val REQUIRED_SIZE = 75

            // Find the correct scale value. It should be the power of 2.
            var scale = 1
            while (o.outWidth / scale / 2 >= REQUIRED_SIZE &&
                o.outHeight / scale / 2 >= REQUIRED_SIZE
            ) {
                scale *= 2
            }
            val o2 = BitmapFactory.Options()
            o2.inSampleSize = scale
            inputStream = FileInputStream(file)
            val selectedBitmap = BitmapFactory.decodeStream(inputStream, null, o2)
            inputStream.close()

            //rotate back the picture if it gets rotated automatically, in some devices it may happen
            val exifInterface = ExifInterface(currentPhotoPath)
            val orientation: Int = exifInterface.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED
            )
            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                ExifInterface.ORIENTATION_NORMAL -> matrix.postRotate(0f)
                else -> {}
            }
            val rotatedBitmap = Bitmap.createBitmap(
                selectedBitmap!!, 0, 0, selectedBitmap.width,
                selectedBitmap.height, matrix, true
            )

            // here i override the original image file
            file.createNewFile()
            val outputStream = FileOutputStream(file)
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream)
            Picasso.get().load(File(currentPhotoPath)).placeholder(R.drawable.plus_button_512)
                .error(R.drawable.plus_button_512).into(binding.selectedImage)
            viewModel.attachments.add(
                AttachmentAdapter.Attachment(
                    currentPhotoPath,
                    AttachmentType.IMAGE
                )
            )
            showUserResult()
        } catch (e: java.lang.Exception) {
            Log.e(TAG, "Error is: $e")
        }
    }

    private fun showUserResult() {
        if (viewModel.attachments.size > 0) {
            Picasso.get().load(File(viewModel.attachments[selectedPosition].path))
                .into(binding.selectedImage)
            binding.photoRV.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            binding.photoRV.adapter = AttachmentAdapter(viewModel.attachments, this)
            binding.photoRV.visibility = View.VISIBLE
        } else {
            binding.photoRV.visibility = View.GONE
        }
    }


    override fun onClickRemove(position: Int) {
        val file = File(viewModel.attachments[position].path)
        if (file.exists()) file.delete()
        viewModel.attachments.removeAt(position)
        if (viewModel.attachments.isNotEmpty()) {
            binding.photoRV.adapter = AttachmentAdapter(viewModel.attachments, this)
            if (position == selectedPosition) {
                selectedPosition++
                if (selectedPosition <= viewModel.attachments.size - 1) {
                    Picasso.get().load(File(viewModel.attachments[selectedPosition].path))
                        .into(binding.selectedImage)
                } else {
                    selectedPosition = 0
                    Picasso.get().load(File(viewModel.attachments[selectedPosition].path))
                        .into(binding.selectedImage)
                }
            }
        } else {
            binding.photoRV.visibility = View.GONE
        }
    }

    override fun onClickAttachment(position: Int) {
        if (viewModel.attachments[position].type == AttachmentType.AUDIO) {
            /* if (audioPlayer.isNull()) {
                 audioPlayer = AudioPlayer(requireContext())
             }
             audioPlayer?.stop()
             audioPlayer?.play(Uri.parse(viewModel.attachments[position].path))*/
            AudioPlayerDialogFragment.newInstance(
                viewModel.attachments[position].path,
                AudioPlayerDialogFragment.Companion.LoadFrom.URI,
            ).show(childFragmentManager, AudioPlayerDialogFragment.TAG)
        } else {
            selectedPosition = position
            ImageViewerDialog.newInstance(
                viewModel.attachments[position].path,
                ImageViewerDialog.Companion.LoadFrom.URI
            ).show(childFragmentManager, ImageViewerDialog.TAG)
            binding.selectedImage.loadFromUri(viewModel.attachments[position].path)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            TAKE_IMAGE_REQUEST -> if (resultCode == Activity.RESULT_OK) {
                saveBitmapToFile(File(currentPhotoPath))
            } else if (resultCode == Activity.RESULT_CANCELED) {
                currentPhotoPath = ""
            }
            PICK_IMAGE_REQUEST -> if (resultCode == Activity.RESULT_OK && data != null && data.data != null) {
                Log.e(TAG, "Data is: " + data.data)
                try {
                    val inputStream = requireActivity().contentResolver.openInputStream(
                        data.data!!
                    )
                    val file = File(currentPhotoPath)
                    var fo: FileOutputStream? = null
                    try {
                        fo = FileOutputStream(file)
                        val buffer = ByteArray(1024)
                        var length: Int
                        while (inputStream!!.read(buffer).also { length = it } != -1) {
                            fo.write(buffer, 0, length)
                        }
                    } catch (e: java.lang.Exception) {
                        Log.e(TAG, "Error is: $e")
                    } finally {
                        fo!!.close()
                        inputStream!!.close()
                    }
                    saveBitmapToFile(file)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } else if (resultCode == Activity.RESULT_OK && data != null && data.clipData != null) {
                Log.e(TAG, "Data is: " + data.clipData!!.itemCount)
                var i = 0
                while (i < data.clipData!!.itemCount) {
                    Log.e(TAG, "Uri in multiselect is: " + data.clipData!!.getItemAt(i).uri)
                    try {
                        if (i != 0) createImageFile()
                        val inputStream = requireActivity().contentResolver.openInputStream(
                            data.clipData!!.getItemAt(i).uri
                        )
                        val file = File(currentPhotoPath)
                        var fo: FileOutputStream? = null
                        try {
                            fo = FileOutputStream(file)
                            val buffer = ByteArray(1024)
                            var length: Int
                            while (inputStream!!.read(buffer).also { length = it } != -1) {
                                fo.write(buffer, 0, length)
                            }
                        } catch (e: java.lang.Exception) {
                            Log.e(TAG, "Exception Occurred is: $e")
                        } finally {
                            fo!!.close()
                            inputStream!!.close()
                        }
                        saveBitmapToFile(file)
                    } catch (e: IOException) {
                        Log.e(TAG, "Exception Occurred is: $e")
                    }
                    i++
                }
            }
            else -> {}
        }
    }


    private fun initRecording() {
        binding.btnRecord.isSoundEffectsEnabled = false
        binding.btnRecord.isClickable = true
        binding.btnRecord.registerOnRecordButtonClickListener(this)
        waveRecorder = WaveRecorder(createAudioFileName())
        waveRecorder.onStateChangeListener = {
            when (it) {
                RecorderState.RECORDING -> binding.btnRecord.startScale()
                RecorderState.STOP -> binding.btnRecord.stopScale()
                RecorderState.PAUSE -> {
                }
            }
        }
        waveRecorder.onTimeElapsed = {
            recordedTime = it//caching recording time.
            Log.e("TAG", "onCreate: time elapsed $recordedTime")
            //timeTextView.text = formatTimeUnit(it * 1000)
        }

    }

    private fun createAudioFileName() = run {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date())
        val root = requireContext().cacheDir?.absolutePath
        val audioPath = "$root/audio"
        val dir = File(audioPath)
        if (!dir.exists()) dir.mkdirs()
        "$audioPath/${timeStamp}_audioFile${AUDIO_FILE_FORMAT}"
    }


    /***
     * find recorder time and delete audio file if length is too short.
     */
    private fun handleShortRecording() {
        if (recordedTime < 1) {
            Log.e("TAG", "onCreate: short audio need to delete  $recordedTime")
            try {
                cachedAudioFile?.let { deleteFile(it) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            cachedAudioFile = null//clr previous recorded file.
            if (requireContext().hasPermissions(_permissions)) requireContext().toast("Audio too short")
        } else {
            viewModel.attachments.add(
                AttachmentAdapter.Attachment(
                    cachedAudioFile ?: "",
                    AttachmentType.AUDIO
                )
            )
            binding.photoRV.adapter = AttachmentAdapter(viewModel.attachments, this)
            binding.photoRV.visibility = View.VISIBLE
        }
    }


    /***
     * delete audio file for memory management.
     */
    private fun deleteAudioFile() {
        try {
            val root = requireContext().cacheDir?.absolutePath
            val audioPath = "$root/audio"
            deleteRecursive(File(audioPath))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    /***
     * recording start.
     */
    override fun onClickStart(audioRecordButton: AudioRecordButton, event: MotionEvent?) {
        checkPermissions()
    }


    /***
     * recording finish.
     */
    override fun onClickEnd(audioRecordButton: AudioRecordButton, event: MotionEvent?) {
        try {
            waveRecorder.stopRecording()
            cachedAudioFile = waveRecorder.filePath
            viewModel.cachedAudioUri = Uri.parse(waveRecorder.filePath)
            handleShortRecording()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun checkPermissions() {
        if (!requireContext().hasPermissions(_permissions)) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                _permissions,
                REQUEST_CODE_RECORD_AUDIO
            )
        } else {
            try {
                waveRecorder.filePath = createAudioFileName()
                waveRecorder.startRecording()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_RECORD_AUDIO) {
            if (!requireContext().hasPermissions(_permissions)) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    _permissions,
                    REQUEST_CODE_RECORD_AUDIO
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        deleteAudioFile()
        _binding = null
    }
}
package com.example.mysampleapp

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.mysampleapp.databinding.FragmentQuestion3Binding
import java.io.File
import java.io.IOException

class Question3 : Fragment() {
    private lateinit var binding: FragmentQuestion3Binding
    private lateinit var viewModel: FormViewModel

    private var selfieImagePath: String? = null
    private lateinit var takePictureLauncher: ActivityResultLauncher<Intent>

    private val CAMERA_PERMISSION_CODE = 100

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentQuestion3Binding.inflate(layoutInflater)
        viewModel = ViewModelProvider(requireActivity())[FormViewModel::class.java]

        setupCameraLauncher()

        binding.captureImage.setOnClickListener {
            if (checkPermissions()) {
                captureSelfie()
            }
        }

        binding.q3Next.setOnClickListener {
            if (selfieImagePath == null) {
                Toast.makeText(requireContext(), "Please capture a selfie first", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.selfiePath = selfieImagePath
                findNavController().navigate(R.id.action_question3_to_submit)
            }
        }

        return binding.root
    }

    private fun checkPermissions(): Boolean {
        val cameraPermission = ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA)
        return if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
            false
        } else {
            true
        }
    }

    private fun setupCameraLauncher() {
        takePictureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                selfieImagePath?.let {
                    val file = File(it)
                    if (file.exists()) {
                        val bitmap = BitmapFactory.decodeFile(it)
                        binding.pickImage.setImageBitmap(bitmap)
                        Toast.makeText(requireContext(), "Selfie captured successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Failed to capture selfie (file not found)", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Log.w("SelfieCapture", "Camera operation was canceled by the user.")
                Toast.makeText(requireContext(), "Camera operation was canceled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun captureSelfie() {
        try {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val imageFile = createImageFile()

            if (imageFile != null) {
                selfieImagePath = imageFile.absolutePath
                val photoURI = FileProvider.getUriForFile(
                    requireContext(),
                    "${requireContext().packageName}.fileprovider",
                    imageFile
                )

                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

                takePictureLauncher.launch(cameraIntent)
            } else {
                Toast.makeText(requireContext(), "Unable to create image file", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("SelfieCapture", "Error launching camera: ${e.message}")
            Toast.makeText(requireContext(), "Error launching camera", Toast.LENGTH_SHORT).show()
        }
    }


    private fun createImageFile(): File? {
        return try {
            val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            if (storageDir?.exists() == false) {
                storageDir.mkdirs()
            }
            File.createTempFile("selfie_", ".jpg", storageDir).apply {
                Log.d("SelfieCapture", "Image file created at: $absolutePath")
            }
        } catch (e: IOException) {
            Log.e("SelfieCapture", "Error creating image file: ${e.message}")
            null
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                captureSelfie()
            } else {
                Toast.makeText(requireContext(), "Camera permission is required to capture a selfie", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

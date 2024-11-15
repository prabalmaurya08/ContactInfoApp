package com.example.mysampleapp

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.mysampleapp.databinding.FragmentSubmitBinding
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Submit : Fragment(), LocationListener {
    private lateinit var viewModel: FormViewModel
    private lateinit var binding: FragmentSubmitBinding
    private lateinit var locationManager: LocationManager
    private var latitude: Double? = null
    private var longitude: Double? = null
    private var mediaRecorder: MediaRecorder? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSubmitBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(requireActivity())[FormViewModel::class.java]

        val submitButton: Button = binding.submitBtn

        // Request location permission
        requestLocationPermission()

        submitButton.setOnClickListener {
            if (validateInput()) {
//                // Show the progress bar and hide the submit button
//                binding.progressBar.visibility = View.VISIBLE
//                binding.submitBtn.visibility = View.INVISIBLE
                binding.tv1.visibility=View.VISIBLE

                // Stop any ongoing audio recording
                stopAudioRecording()

                // Request location
                getLocation()
             //   saveFormData()
            } else {
                Toast.makeText(requireContext(), "Validation Error! Please fill all required fields.", Toast.LENGTH_SHORT).show()
            }
        }
        return binding.root
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                100
            )
        }
    }

    private fun getLocation() {
        try {
            locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    0,
                    0f,
                    this
                )
            }
        } catch (e: Exception) {
            Log.e("SubmitFragment", "Error getting location: ${e.message}")
            saveFormData()  // Proceed to save data even if location is not fetched
        }
    }

    override fun onLocationChanged(location: Location) {
        latitude = location.latitude
        longitude = location.longitude
        locationManager.removeUpdates(this)

        // After obtaining the location, save the form data
        saveFormData()
    }

    private fun stopAudioRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
        } catch (e: Exception) {
            Log.e("SubmitFragment", "Error stopping audio recording: ${e.message}")
        }
    }

    private fun validateInput(): Boolean {
        return viewModel.age.toString().isNotEmpty()
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun saveFormData() {
        val gender = viewModel.gender ?: 0
        val age = viewModel.age?.toInt() ?: 0
        val selfiePath = viewModel.selfiePath ?: "Not provided"
        val audioPath = viewModel.audioFilePath ?: "Not recorded"
        val gps = if (latitude != null && longitude != null) {
            "$latitude,$longitude"
        } else {
            "Location not available"
        }
        val submitTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        val formData = JSONObject().apply {
            put("Q1", gender)
            put("Q2", age)
            put("Q3", selfiePath)
            put("recording", audioPath)
            put("gps", gps)
            put("submit_time", submitTime)
        }

        // Use Coroutines for background file writing
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val jsonFile = File(requireContext().filesDir, "form_data.json")
                val jsonArray = if (jsonFile.exists()) {
                    JSONArray(jsonFile.readText())
                } else {
                    JSONArray()
                }

                jsonArray.put(formData)

                // Write the data to the file
                FileOutputStream(jsonFile).use { it.write(jsonArray.toString().toByteArray()) }

                // After saving data, update UI on the main thread
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Form data saved successfully!", Toast.LENGTH_SHORT).show()

                    // Navigate to the result fragment after saving the data
                    findNavController().navigate(R.id.action_submit_to_result)

//                    // Hide progress bar and show submit button again
//                    binding.progressBar.visibility = View.GONE
//                    binding.submitBtn.visibility = View.VISIBLE

                }
            } catch (e: Exception) {
                Log.e("SubmitFragment", "Error saving form data: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error saving form data", Toast.LENGTH_SHORT).show()

                    // Hide progress bar and show submit button again
                    binding.progressBar.visibility = View.GONE
                    binding.submitBtn.visibility = View.VISIBLE
                }
            }
        }
    }
}

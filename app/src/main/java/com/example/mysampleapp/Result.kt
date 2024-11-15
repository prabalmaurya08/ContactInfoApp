package com.example.mysampleapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.mysampleapp.databinding.FragmentResultBinding
import org.json.JSONArray
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class Result : Fragment() {
    private lateinit var binding: FragmentResultBinding
    private lateinit var tableLayout: TableLayout

    // For Notification
    private val notificationManager by lazy {
        requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    private val notificationId = 1
    private val channelId = "download_channel"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentResultBinding.inflate(inflater, container, false)
        // Initialize TableLayout
        tableLayout = binding.tableLayout

        // Display Results
        displayResults()

        // Set the onClick listener for the download button
        binding.downloadButton.setOnClickListener {
            onDownloadClicked()
        }

        binding.returnButton.setOnClickListener {
            findNavController().navigate(R.id.action_result_to_question1)
        }

        // Create Notification Channel for Android O and above
        createNotificationChannel()

        return binding.root
    }

    private fun displayResults() {
        val jsonFile = File(requireContext().filesDir, "form_data.json")

        // Check if the file exists and handle case where there is no data
        if (!jsonFile.exists()) {
            Toast.makeText(requireContext(), "No data found!", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            // Parse JSON file into JSONArray
            val jsonArray = JSONArray(jsonFile.readText())

            // Iterate through the data and add rows to the TableLayout
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)

                // Create a new row for the table
                val row = TableRow(requireContext())

                // Data to be displayed in each row
                val gender = jsonObject.getInt("Q1").toString()
                val age = jsonObject.getInt("Q2").toString()
                val selfie = jsonObject.getString("Q3")
                val audio = jsonObject.getString("recording")
                val gps = jsonObject.getString("gps")
                val time = jsonObject.getString("submit_time")

                // Create text views for each column in the row
                val columns = arrayOf(gender, age, selfie, audio, gps, time)
                for (column in columns) {
                    val textView = TextView(requireContext()).apply {
                        text = column
                        setPadding(16, 8, 16, 8)
                    }
                    row.addView(textView)
                }

                // Add the row to the table layout
                tableLayout.addView(row)
            }

        } catch (e: Exception) {
            Log.e("ResultFragment", "Error reading or parsing JSON", e)
            Toast.makeText(requireContext(), "Error displaying results", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createNotificationChannel() {
        // For devices running Android Oreo (API level 26) and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Download Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for file download status"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun saveFileToDownloads(file: File) {
        // Show notification that download has started
        showNotification("Download in Progress", "Your file is being downloaded.")

        try {
            val downloadsDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            val destinationFile = File(downloadsDir, "form_data_downloaded.json")

            // Copy the file from internal storage to external storage
            file.inputStream().use { input ->
                FileOutputStream(destinationFile).use { output ->
                    input.copyTo(output)
                }
            }

            // Show success notification
            showNotification("Download Complete", "Your file has been downloaded successfully.", destinationFile)
            Toast.makeText(requireContext(), "File downloaded to Downloads folder", Toast.LENGTH_SHORT).show()

        } catch (e: IOException) {
            showNotification("Download Failed", "An error occurred: ${e.message}")
            Log.e("ResultFragment", "Download error: ${e.message}", e)
            e.printStackTrace()
        }
    }

    private fun showNotification(title: String, message: String, file: File? = null) {
        // Create the PendingIntent to open the downloaded file
        val openFileIntent = if (file != null) {
            val uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider", // Ensure the correct authority
                file
            )

            Intent(Intent.ACTION_VIEW).apply {
                data = uri
                type = "application/json" // Update this based on the file type (e.g., "application/pdf" for PDFs)
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }.let { intent ->
                PendingIntent.getActivity(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            }
        } else {
            null
        }

        // Build the notification
        val notification = NotificationCompat.Builder(requireContext(), channelId)
            .setSmallIcon(android.R.drawable.ic_menu_save)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .apply {
                // Set the PendingIntent to open the file when the notification is clicked
                openFileIntent?.let {
                    setContentIntent(it)
                }
            }
            .build()

        // Show the notification
        notificationManager.notify(notificationId, notification)
    }

    private fun onDownloadClicked() {
        // Logic to download the answers as a JSON or any other format
        val jsonFile = File(requireContext().filesDir, "form_data.json")

        if (jsonFile.exists()) {
            // Start downloading process (or share the file)
            Toast.makeText(requireContext(), "Downloading...", Toast.LENGTH_SHORT).show()

            // Call saveFileToDownloads() to handle file saving
            saveFileToDownloads(jsonFile)

        } else {
            Toast.makeText(requireContext(), "No data to download", Toast.LENGTH_SHORT).show()
        }
    }
}

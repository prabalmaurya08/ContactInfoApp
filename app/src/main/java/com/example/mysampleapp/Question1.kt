package com.example.mysampleapp

import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController

import com.example.mysampleapp.databinding.FragmentQuestion1Binding
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream


class Question1 : Fragment() {
    private lateinit var binding: FragmentQuestion1Binding
    private lateinit var viewModel: FormViewModel
    private lateinit var mediaRecorder: MediaRecorder
    private var audioFilePath: String = ""

    private lateinit var spinner: Spinner
    private val REQUEST_RECORD_AUDIO_PERMISSION = 200
    private val permissions = arrayOf(
        android.Manifest.permission.RECORD_AUDIO,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        android.Manifest.permission.READ_EXTERNAL_STORAGE
    )

    private fun requestPermissions() {
        requestPermissions(permissions, REQUEST_RECORD_AUDIO_PERMISSION)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                setupAudioRecording()
            } else {
                Toast.makeText(requireContext(), "Permissions denied", Toast.LENGTH_SHORT).show()
            }
        }
    }





    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding=FragmentQuestion1Binding.inflate(layoutInflater)
        viewModel = ViewModelProvider(requireActivity())[FormViewModel::class.java]
        spinner=binding.genderSpinner
        setUpSpinner()


        //  findNavController().navigate(R.id.action_question1_to_question2)
       //  setupAudioRecording()
        binding.q1Next.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions()
            } else {
                setupAudioRecording()
            }
            onNextClicked()
        }
        return binding.root
    }





    private fun onNextClicked() {
        val selectedPosition = spinner.selectedItemPosition
        // Map the selection to gender ID
        val genderId = when (selectedPosition) {
            1 -> 1 // Male
            2 -> 2 // Female
            3 -> 3 // Other
            else -> 0 // Not selected
        }

        // Save the gender ID to the ViewModel
        if (genderId == 0) {
            Toast.makeText(requireContext(), "Please select a gender", Toast.LENGTH_SHORT).show()
        } else {
            viewModel.gender = genderId
              findNavController().navigate(R.id.action_question1_to_question2)


        }

    }





    private fun setUpSpinner(){
        // Define the gender options with corresponding IDs
        val genderOptions = listOf("Select Gender", "Male", "Female", "Other")
        val adapter = ArrayAdapter(
            requireContext(),
            androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
            genderOptions
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }



    private fun setupAudioRecording() {
        try {
            audioFilePath = "${requireContext().externalCacheDir?.absolutePath}/audio_record.3gp"
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(audioFilePath)
                prepare()
                start()
            }
            viewModel.audioFilePath = audioFilePath
            Toast.makeText(requireContext(), "Recording started", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Failed to start recording", Toast.LENGTH_SHORT).show()
        }
    }








    //source code to convert
    fun convert3gpToWav(inputPath: String, outputPath: String) {
        try {
            val inputFile = File(inputPath)
            val outputFile = File(outputPath)

            // Read the raw PCM data from the .3gp file
            val fis = FileInputStream(inputFile)
            val fos = FileOutputStream(outputFile)

            // Write the WAV header
            writeWavHeader(fos, fis.available())

            // Copy the PCM data
            fis.copyTo(fos)
            fis.close()
            fos.close()

           // Log.d("AudioConversion", "Conversion to WAV completed: $outputPath")
        } catch (e: Exception) {
           // Log.e("AudioConversion", "Error converting to WAV: ${e.message}")
        }
    }

    private fun writeWavHeader(outputStream: FileOutputStream, dataLength: Int) {
        // WAV header format
        val header = ByteArray(44)

        // "RIFF" chunk descriptor
        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()

        // File size (36 + data length)
        val fileSize = 36 + dataLength
        header[4] = (fileSize and 0xff).toByte()
        header[5] = ((fileSize shr 8) and 0xff).toByte()
        header[6] = ((fileSize shr 16) and 0xff).toByte()
        header[7] = ((fileSize shr 24) and 0xff).toByte()

        // "WAVE" format
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()

        // "fmt " sub-chunk
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()

        // Sub-chunk size (16 for PCM)
        header[16] = 16
        header[17] = 0
        header[18] = 0
        header[19] = 0

        // Audio format (1 = PCM)
        header[20] = 1
        header[21] = 0

        // Number of channels (1 = mono)
        header[22] = 1
        header[23] = 0

        // Sample rate (e.g., 44100 Hz)
        val sampleRate = 44100
        header[24] = (sampleRate and 0xff).toByte()
        header[25] = ((sampleRate shr 8) and 0xff).toByte()
        header[26] = ((sampleRate shr 16) and 0xff).toByte()
        header[27] = ((sampleRate shr 24) and 0xff).toByte()

        // Byte rate (SampleRate * NumChannels * BitsPerSample/8)
        val byteRate = sampleRate * 2
        header[28] = (byteRate and 0xff).toByte()
        header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte()
        header[31] = ((byteRate shr 24) and 0xff).toByte()

        // Block align (NumChannels * BitsPerSample/8)
        header[32] = 2
        header[33] = 0

        // Bits per sample (16 bits)
        header[34] = 16
        header[35] = 0

        // "data" sub-chunk
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()

        // Data size
        header[40] = (dataLength and 0xff).toByte()
        header[41] = ((dataLength shr 8) and 0xff).toByte()
        header[42] = ((dataLength shr 16) and 0xff).toByte()
        header[43] = ((dataLength shr 24) and 0xff).toByte()

        outputStream.write(header, 0, 44)
    }


}
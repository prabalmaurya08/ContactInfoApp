package com.example.mysampleapp


import android.content.Intent

import android.os.Bundle

import android.text.InputType
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText

import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModelProvider

import androidx.navigation.fragment.findNavController
import com.example.mysampleapp.databinding.FragmentQuestion2Binding



class Question2 : Fragment() {
    private lateinit var binding: FragmentQuestion2Binding
    private lateinit var ageEditText: EditText
    private lateinit var viewModel: FormViewModel

    private var selfieImagePath: String? = null
    private lateinit var takePictureLauncher: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding=FragmentQuestion2Binding.inflate(layoutInflater)
        viewModel= ViewModelProvider(requireActivity())[FormViewModel::class.java]
        setUpageEditText()
        return binding.root
    }
    private fun setUpageEditText() {
        ageEditText = binding.age
        ageEditText.inputType = InputType.TYPE_CLASS_NUMBER
        binding.q2Next.setOnClickListener {
            val ageText = ageEditText.text.toString()
            if (ageText.isNotEmpty()) {
                val age = ageText.toInt()
                viewModel.age = age
                findNavController().navigate(R.id.action_question2_to_question3)

            }

        }
    }


}
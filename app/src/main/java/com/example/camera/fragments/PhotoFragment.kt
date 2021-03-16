package com.example.camera.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.camera.R
import com.example.camera.utils.CommonUtils
import com.example.camera.utils.CommonUtils.PIC_URI
import kotlinx.android.synthetic.main.fragment_photo.*
import java.io.File

class PhotoFragment : Fragment(), View.OnClickListener {
    private var uri : String = ""
    private lateinit var uriObject : Uri
    private var imageFile = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        uri = arguments?.get(PIC_URI) as String

        uriObject = Uri.parse(uri)
        imageFile = uriObject.lastPathSegment.toString()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_photo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        deleteButton.setOnClickListener(this)
        saveButton.setOnClickListener(this)
        Glide.with(this).load(uriObject).into(photo_view)

    }

    override fun onClick(v: View?) {
        if ( v== null) return

        when (v) {
            deleteButton -> deletePicAndOpenCamera()
            saveButton -> goToGallery()
        }
    }

    private fun deletePicAndOpenCamera(){
        deletePic()

        findNavController().navigate(R.id.action_photoFragment_to_camera)
    }

    private fun goToGallery(){
        findNavController().navigate(R.id.action_photoFragment_to_photos_list)
    }

    private fun deletePic(){

        val uriForDeleteFunction = "${CommonUtils.baseUri}${imageFile}"
        File(uriForDeleteFunction).delete()
        Toast.makeText(context, "Foto eliminata con successo", Toast.LENGTH_SHORT).show()
    }
}
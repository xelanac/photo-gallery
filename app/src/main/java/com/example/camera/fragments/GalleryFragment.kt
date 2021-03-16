package com.example.camera.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.camera.R
import com.example.camera.adapters.GalleryAdapter
import com.example.camera.interfaces.ClickEventView
import com.example.camera.utils.CommonUtils
import com.example.camera.utils.CommonUtils.PIC_URI
import kotlinx.android.synthetic.main.fragment_gallery.*
import java.io.File

class GalleryFragment : Fragment(), View.OnClickListener, ClickEventView {

    private var photoList = listOf<File>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_gallery, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        floating_action_button.setOnClickListener(this)
        recyclerview_gallery.setHasFixedSize(true)

        if (File(CommonUtils.baseUri).listFiles() == null) {
            info_empty_txt.text = resources.getString(R.string.empty_gallery)
        } else {
            info_empty_txt.text = ""
            photoList = CommonUtils.getPics(CommonUtils.baseUri)!!

            val galleryAdapter = GalleryAdapter(photoList, requireContext(), this)
            recyclerview_gallery.adapter = galleryAdapter
            recyclerview_gallery.layoutManager = LinearLayoutManager(context)
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            floating_action_button -> findNavController().navigate(R.id.action_photos_list_to_camera)
        }
    }

    override fun clickOnItemInList(pic: String) {

        findNavController().navigate(R.id.action_photos_list_to_photoDetailFragment, bundleOf(PIC_URI to pic))
    }
}
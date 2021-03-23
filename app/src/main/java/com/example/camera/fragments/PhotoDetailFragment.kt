package com.example.camera.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.RotateAnimation
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.camera.R
import com.example.camera.dialogs.EditTextDialogFragment
import com.example.camera.interfaces.EditTextDialog
import com.example.camera.utils.CommonUtils.BASE_URI
import com.example.camera.utils.CommonUtils.PIC_URI
import com.example.camera.utils.Compass
import com.example.camera.utils.SOTWFormatter
import com.google.android.material.bottomnavigation.BottomNavigationView
import ja.burhanrashid52.photoeditor.PhotoEditor
import kotlinx.android.synthetic.main.fab_layout.*
import kotlinx.android.synthetic.main.fragment_photo_detail.*

class PhotoDetailFragment : Fragment(), View.OnClickListener, EditTextDialog,
    BottomNavigationView.OnNavigationItemSelectedListener, PhotoEditor.OnSaveListener,
    Compass.CompassListener {

    private val fromBottom: Animation by lazy { AnimationUtils.loadAnimation(
        requireContext(),
        R.anim.from_bottom_anim
    ) }
    private val toBottom: Animation by lazy { AnimationUtils.loadAnimation(
        requireContext(),
        R.anim.to_bottom_anim
    ) }

    private lateinit var pictureUri : Uri
    private lateinit var editor : PhotoEditor
    private var clicked: Boolean = false
    private var colorText = 0
    private val stickerWidth = 450
    private val stickerHeight = 90
    private var unidirectionalArrow = R.drawable.unidirectional_arrow
    private var bidirectionalArrow = R.drawable.bidirectional_arrow
    private var line = R.drawable.line
    private var compass: Compass? = null
    private var sotwFormatter: SOTWFormatter? = null
    private var currentAzimuth = 0f
    private var imgTitle = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        colorText = requireContext().getColor(R.color.black)
        sotwFormatter = SOTWFormatter(requireContext())

        arguments?.getString(PIC_URI)?.apply {
            pictureUri = Uri.parse("$BASE_URI$this")
        }

        imgTitle = arguments?.getString(PIC_URI)!!

        compass = Compass(requireContext())
        compass?.setListener(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_photo_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        photoEditorView.source.setImageURI(pictureUri)

        editor = PhotoEditor
            .Builder(requireContext(), photoEditorView)
            .build()

        fab.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.black))

        bottomNavigationView.setOnNavigationItemSelectedListener(this)
        fab.setOnClickListener(this)
        fab_1.setOnClickListener(this)
        fab_2.setOnClickListener(this)
        fab_3.setOnClickListener(this)
        fab_4.setOnClickListener(this)
        fab_5.setOnClickListener(this)
        fab_6.setOnClickListener(this)

    }

    override fun onStart() {
        super.onStart()
        compass?.start()
    }

    override fun onStop() {
        super.onStop()
        compass?.stop()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_photo_detail, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_save -> {
                if (ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) requestPermissions(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_WRITE_PERMISSION
                )
                else {
                        editor.saveAsFile(pictureUri.toString(), this)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun saveImage(pic: String){
        val uriPic = Uri.parse("file:///$pic")
        val bitmap = changeUriToBitmap(uriPic)
        MediaStore.Images.Media.insertImage(requireContext().contentResolver,
            bitmap,
            imgTitle,
            "empty description")
    }

    private fun changeUriToBitmap(uri: Uri) = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_WRITE_PERMISSION -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    editor.saveAsFile(pictureUri.toString(), this)
            }
        }
    }

    override fun onSuccess(imagePath: String) {
        saveImage(pictureUri.toString())
        findNavController().popBackStack()
        Toast.makeText(requireContext(), "Foto salvata con successo", Toast.LENGTH_SHORT).show()
    }

    override fun onFailure(exception: Exception) {
        Toast.makeText(requireContext(), exception.message.toString(), Toast.LENGTH_SHORT).show()
    }

    private fun editWithLine(editor: PhotoEditor){
        val arrow = ContextCompat.getDrawable(requireContext(), line)
        val bitmap = arrow?.toBitmap(stickerWidth, stickerHeight, Bitmap.Config.ARGB_8888)
        editor.setOpacity(0)
        editor.addImage(bitmap)
    }

    private fun editWithUnidirectionalArrow(editor: PhotoEditor){
        val arrow = ContextCompat.getDrawable(requireContext(), unidirectionalArrow)
        val bitmap = arrow?.toBitmap(stickerWidth, stickerHeight, Bitmap.Config.ARGB_8888)
        editor.setOpacity(0)
        editor.addImage(bitmap)
    }

    private fun editWithBidirectionalArrow(editor: PhotoEditor){
        val arrow = ContextCompat.getDrawable(requireContext(), bidirectionalArrow)
        val bitmap = arrow?.toBitmap(stickerWidth, stickerHeight, Bitmap.Config.ARGB_8888)
        editor.setOpacity(0)
        editor.addImage(bitmap)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_text -> openEditTextDialog()
            R.id.action_line -> editWithLine(editor)
            R.id.action_arrow -> editWithUnidirectionalArrow(editor)
            R.id.action_bidirectional_arrow -> editWithBidirectionalArrow(editor)
        }
        return true
    }

    private fun openEditTextDialog(){
        val dialogEditText = EditTextDialogFragment.newInstance(this)
        dialogEditText.show(childFragmentManager, "edit text dialog")
    }

    override fun editTextOnPic(text: String) {
        if(text.isEmpty()){
                Toast.makeText(
                requireContext(),
                "Testo vuoto! Impossibile inserire la nota.",
                Toast.LENGTH_SHORT)
                .show()
        } else {
            editor.addText(text, colorText)
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            fab -> onFabButtonClicked()
            fab_1 -> setBlackColor()
            fab_2 -> setBlueColor()
            fab_3 -> setPurpleColor()
            fab_4 -> setRedColor()
            fab_5 -> setYellowColor()
            fab_6 -> setGreenColor()
        }
    }

    private fun onFabButtonClicked() {
        setFabsVisibility(clicked)
        setFabsAnimation(clicked)
        clicked = !clicked
    }

    private fun setFabsVisibility(clicked: Boolean) {
        val visibility = if (clicked) View.INVISIBLE else View.VISIBLE
        fab_1.visibility = visibility
        fab_2.visibility = visibility
        fab_3.visibility = visibility
        fab_4.visibility = visibility
        fab_5.visibility = visibility
        fab_6.visibility = visibility
    }

    private fun setFabsAnimation(clicked: Boolean) {
        val anim = if (clicked) toBottom else fromBottom
        fab_1.startAnimation(anim)
        fab_2.startAnimation(anim)
        fab_3.startAnimation(anim)
        fab_4.startAnimation(anim)
        fab_5.startAnimation(anim)
        fab_6.startAnimation(anim)
    }

    private fun setRedColor(){
        refreshStickers(R.drawable.line_red,
                        R.drawable.unidirectional_arrow_red,
                        R.drawable.bidirectional_arrow_red,
                        R.color.photo_brush_red)

        onFabButtonClicked()
    }

    private fun setGreenColor(){
        refreshStickers(R.drawable.line_green,
                        R.drawable.unidirectional_arrow_green,
                        R.drawable.bidirectional_arrow_green,
                        R.color.photo_brush_green)

        onFabButtonClicked()
    }

    private fun setBlueColor(){
        refreshStickers(R.drawable.line_blue,
                        R.drawable.unidirectional_arrow_blue,
                        R.drawable.bidirectional_arrow_blue,
                        R.color.photo_brush_blue)

        onFabButtonClicked()
    }

    private fun setYellowColor(){
        refreshStickers(R.drawable.line_yellow,
                        R.drawable.unidirectional_arrow_yellow,
                        R.drawable.bidirectional_arrow_yellow,
                        R.color.photo_brush_yellow)

        onFabButtonClicked()
    }

    private fun setBlackColor(){
        refreshStickers(R.drawable.line,
                        R.drawable.unidirectional_arrow,
                        R.drawable.bidirectional_arrow,
                        R.color.black)

        onFabButtonClicked()
    }

    private fun setPurpleColor(){
        refreshStickers(R.drawable.line_purple,
                        R.drawable.unidirectional_arrow_purple,
                        R.drawable.bidirectional_arrow_purple,
                        R.color.photo_deep_purple)

        onFabButtonClicked()
    }

    private fun refreshStickers(line: Int, unidirArrow: Int, bidirArrow: Int, color: Int){
        this.line = line
        this.unidirectionalArrow = unidirArrow
        this.bidirectionalArrow = bidirArrow
        this.colorText = requireContext().getColor(color)
        this.fab.backgroundTintList = ColorStateList.valueOf(resources.getColor(color))
    }

    companion object {
        const val REQUEST_WRITE_PERMISSION = 1001
    }


    override fun onNewAzimuth(azimuth: Float) {
        adjustArrow(azimuth)
    }

    private fun adjustArrow(azimuth: Float) {
        Log.d(
            "adjustArrow",
            "will set rotation from " + currentAzimuth + " to "
                    + azimuth
        )
        val an: Animation = RotateAnimation(
            -currentAzimuth, -azimuth,
            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
            0.5f
        )
        currentAzimuth = azimuth
        an.duration = 500
        an.repeatCount = 0
        an.fillAfter = true
        img_compass.startAnimation(an)
    }
}
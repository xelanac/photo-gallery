package com.example.camera.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context.SENSOR_SERVICE
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.hardware.*
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
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
import com.google.android.material.bottomnavigation.BottomNavigationView
import ja.burhanrashid52.photoeditor.PhotoEditor
import kotlinx.android.synthetic.main.fab_layout.*
import kotlinx.android.synthetic.main.fragment_photo_detail.*
import kotlin.math.roundToInt


class PhotoDetailFragment : Fragment(), View.OnClickListener, EditTextDialog,
    BottomNavigationView.OnNavigationItemSelectedListener, PhotoEditor.OnSaveListener,
    SensorEventListener {

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
    private var imgTitle = ""
    
    private var sensorManager: SensorManager? = null
    private lateinit var accelerometer : Sensor
    private lateinit var magnetometer : Sensor
    private var lastAccelerometer = FloatArray(3)
    private var lastMagnetometer = FloatArray(3)
    private var rotationMatrix = FloatArray(9)
    private var orientation = FloatArray(3)
    private var isLastAccelerometerArrayCopied = false
    private var isLastMagnetometerArrayCopied = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        colorText = requireContext().getColor(R.color.black)

        arguments?.getString(PIC_URI)?.apply {
            pictureUri = Uri.parse("$BASE_URI$this")
        }

        imgTitle = arguments?.getString(PIC_URI)!!

        initCompassServices()
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

    private fun initCompassServices() {
        sensorManager = requireActivity().getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometer = sensorManager!!.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
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
        Toast.makeText(requireContext(), R.string.photo_successfully_saved, Toast.LENGTH_SHORT).show()
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
                R.string.empty_text,
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

    override fun onResume() {
        super.onResume()

        sensorManager?.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager?.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()

        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor == accelerometer) {
            System.arraycopy(event.values, 0, lastAccelerometer, 0, event.values.size )
            isLastAccelerometerArrayCopied = true
        } else if (event?.sensor == magnetometer) {
            System.arraycopy(event.values, 0, lastMagnetometer, 0, event.values.size)
            isLastMagnetometerArrayCopied = true
        }

        if(isLastAccelerometerArrayCopied && isLastMagnetometerArrayCopied) {
            SensorManager.getRotationMatrix(
                rotationMatrix,
                null,
                lastAccelerometer,
                lastMagnetometer
            )
            SensorManager.getOrientation(rotationMatrix, orientation)

            val degrees = (Math.toDegrees(orientation[0].toDouble()) + 360.0) % 360.0
            val angle = ((degrees * 100).roundToInt() / 100).toFloat()

            img_compass.rotation = -angle

            val direction = getDirection(angle.toDouble())

            val textDirection = "$angle° $direction"
            txt_direction.text = textDirection
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    companion object {
        const val REQUEST_WRITE_PERMISSION = 1001
    }

    private fun getDirection(angle: Double): String {
        var direction = ""

        if (angle >= 350 || angle <= 10)
            direction = "N"
        if (angle < 350 && angle > 280)
            direction = "NW"
        if (angle <= 280 && angle > 260)
            direction = "W"
        if (angle <= 260 && angle > 190)
            direction = "SW"
        if (angle <= 190 && angle > 170)
            direction = "S"
        if (angle <= 170 && angle > 100)
            direction = "SE"
        if (angle <= 100 && angle > 80)
            direction = "E"
        if (angle <= 80 && angle > 10)
            direction = "NE"

        return direction
    }
}
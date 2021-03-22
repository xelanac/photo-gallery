package com.example.camera.dialogs

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.Fragment
import com.example.camera.R
import com.example.camera.interfaces.EditTextDialog
import kotlinx.android.synthetic.main.fragment_edit_text.*

class EditTextDialogFragment: AppCompatDialogFragment() {

    private var editTextDialog: EditTextDialog? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context)
        val inflater = requireActivity().layoutInflater;

        val view = inflater.inflate(R.layout.fragment_edit_text, null)

        builder.setView(view)
            .setTitle(R.string.title_edit_text_dialog)
            .setNegativeButton(R.string.delete,
            DialogInterface.OnClickListener{ dialog, which ->
                //TODO uscire dal dialog e non salvare il testo
            })
            .setPositiveButton(R.string.save,
            DialogInterface.OnClickListener { dialog, which ->
                val textInput = view.findViewById<EditText>(R.id.text)
                val textEntered = textInput.text.toString()

                editTextDialog?.editTextOnPic(textEntered)
            })

       return builder.create()
    }

    companion object {
        fun newInstance(_editTextDialogListener: EditTextDialog) = EditTextDialogFragment().apply {
            editTextDialog = _editTextDialogListener
        }
    }
}
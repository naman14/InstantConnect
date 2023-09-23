package com.naman14.instantconnect

import android.app.Dialog
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.naman14.instantconnect.databinding.DialogEditAliasBinding
import com.naman14.instantconnect.databinding.DialogSendMessageBinding

class SendMessageDialog(val callback: (alias: String) -> Unit): BottomSheetDialogFragment() {

    private var mBehavior: BottomSheetBehavior<*>? = null

    private lateinit var binding: DialogSendMessageBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog


        binding = DialogSendMessageBinding.inflate(layoutInflater)

        val v = binding.root

        dialog.setContentView(v)

        mBehavior = BottomSheetBehavior.from(v.parent as View)

        binding.btnSave.setOnClickListener {
            val alias = binding.etAmount.text.toString()
            callback(alias)
            dismiss()
        }

        return dialog

    }

    override fun onStart() {
        super.onStart()
        mBehavior!!.state = BottomSheetBehavior.STATE_EXPANDED
    }
}
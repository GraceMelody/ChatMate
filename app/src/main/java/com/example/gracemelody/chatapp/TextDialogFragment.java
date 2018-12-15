package com.example.gracemelody.chatapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.widget.EditText;
import android.widget.TextView;

public class TextDialogFragment extends DialogFragment {
    public interface TextDialogFragmentListener {
        void onPositiveClick(TextDialogFragment dialog);
    }

    final TextDialogFragment textDialogFragment = this;
    TextDialogFragmentListener textDialogFragmentListener;
    Dialog dialog;
    String title = "";

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(R.layout.text_dialog_fragment);
        builder.setTitle(title);
        builder.setPositiveButton("Join Channel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (textDialogFragmentListener != null) {
                    textDialogFragmentListener.onPositiveClick(textDialogFragment);
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        dialog = builder.create();
        return dialog;
    }

    public void setTextDialogFragmentListener(TextDialogFragmentListener l) {
        textDialogFragmentListener = l;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        EditText channelText = dialog.findViewById(R.id.txtChannel);
        return channelText.getText().toString();
    }
}

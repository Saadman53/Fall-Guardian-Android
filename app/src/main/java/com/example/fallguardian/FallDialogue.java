package com.example.fallguardian;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.bumptech.glide.Glide;

public class FallDialogue extends AppCompatDialogFragment {


    String title, message;



    public FallDialogue(String title, String message) {
        this.title = title;
        this.message = message;

    }



    private FallDialogueListener fallDialogueListener;
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title)
                .setMessage(message)
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        fallDialogueListener.applyText("0");
                    }
                })
                .setNeutralButton("Yes, Don't send SMS", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        fallDialogueListener.applyText("1");
                    }
                })
                .setPositiveButton("YES, Send SMS", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        fallDialogueListener.applyText("2");
                    }
                });
        AlertDialog alert = builder.create();
        return alert;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try{
            fallDialogueListener = (FallDialogueListener) context;
        }
        catch (Exception e){
            throw new ClassCastException(context.toString()+" must implement FallDialogueListener "+e);
        }
    }

    public interface FallDialogueListener{
        void applyText(String fall);
    }
}

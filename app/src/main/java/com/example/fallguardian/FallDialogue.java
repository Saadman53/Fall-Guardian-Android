package com.example.fallguardian;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
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

    //"Fall Detected!"
    //"Oh no! Have you fallen?"



    private FallDialogueListener fallDialogueListener;
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

//        Glide.with(this)
//                .load(R.drawable.)
//                .into(imageView);

        LayoutInflater factory = LayoutInflater.from(getActivity());
        View view = factory.inflate(R.layout.sample, null);
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
        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try{
            fallDialogueListener = (FallDialogueListener) context;
        }
        catch (Exception e){
            throw new ClassCastException(context.toString()+" must implement FallDialogueListener");
        }
    }

    public interface FallDialogueListener{
        void applyText(String fall);
    }
}

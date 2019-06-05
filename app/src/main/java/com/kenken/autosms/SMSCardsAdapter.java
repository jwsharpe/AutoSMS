package com.kenken.autosms;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.kenken.autosms.R;

import java.util.ArrayList;

/**
 * Changes our SMSCard object into a view
 */

public class SMSCardsAdapter extends ArrayAdapter<SMSCard> {
    public SMSCardsAdapter(Context context, ArrayList<SMSCard> smsCards) {
        super(context, 0, smsCards);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        final SMSCard card = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_sms_card, parent, false);
       }

       // ImageView accentCircle = convertView.findViewById(R.id.accent_shape);
        ImageView accent = convertView.findViewById(R.id.accent_shape);
        TextView title = convertView.findViewById(R.id.card_title);
        TextView targetNumber = convertView.findViewById(R.id.card_phone);
        Button toggleButton = convertView.findViewById(R.id.toggle_button);

        // Populate the data into the template view using the data object
        accent.setColorFilter(card.materialColor);
        title.setText(card.title);
        targetNumber.setText(card.phone);
        toggleButton.setText("On");


        // Return the completed view to render on screen
        return convertView;

    }
}
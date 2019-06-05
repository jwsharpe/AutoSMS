package com.kenken.autosms;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.example.kenken.autosms.R;
import com.google.gson.Gson;

import java.util.ArrayList;

/**
 * AutoSMS is designed to automatically reply via SMS to contacts who reach out to the user through
 * texting (MMS, SMS or Data). The user can either set a wildcard, that is an automatic response
 * to any unknown number, or a specific SMS Card, that is an automatic response to a user
 * specified number. Of course, the user is required to have a message prepared for the system
 * to send out.
 *
 * Guidelines for Material Design
 * https://material.io
 *
 * Helped with my SMS recieve/send research. Although it is currently a bit old the
 * information is still very relevant.
 * http://codetheory.in/android-sms/
 *
 * @author James Sharpe
 * @date 12/11/2017
 *
 */
public class MainActivity extends AppCompatActivity {

    //Saves the permissions.
    int MY_PERMISSIONS_SEND_SMS;
    int MY_PERMISSIONS_RECEIVE_SMS;

    //Contains all the CardViews or CardItems along with the SMSCard objects
    ListView cardContainer;
    //This is the cardContainer adapter responsible for converting SMSCards into CardViews
    SMSCardsAdapter cardsAdapter;

    //preferences allows me to access data from this MainActivity and the SMSReceiver activity
    SharedPreferences preferences;

    //Uses google gson format to serialize the SMSCards in shared preferences as a json.
    Gson gson = new Gson();

    /**
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /**
         * I set the theme in the manifest to be the splash theme, instead of running another instance.
         * This is done because I do not want the splash screen to appear on the screen longer than needed.
         * By setting the theme at the beginning, while the application is loading the splash theme is
         * held only up until the application has finished loading and the onCreate method in the
         * MainActivity is called.
         */
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /**
         * These two methods requests the users permissions at the beginning of the application.
         *
         * Because the permissions are currently in an android "group" only one dialog is shown
         * and asked for.
         */
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.SEND_SMS},
                MY_PERMISSIONS_SEND_SMS);
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECEIVE_SMS},
                MY_PERMISSIONS_RECEIVE_SMS);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addService();
            }
        });

        preferences = getSharedPreferences("SMSShared", MODE_PRIVATE);
        cardsAdapter = new SMSCardsAdapter(this, new ArrayList<SMSCard>());

        cardContainer = findViewById(R.id.card_container);
        cardContainer.setAdapter(cardsAdapter);
    }

    @Override
    public void finish() {
        super.finish();
        updatePreferences();
    }

    /**
     * FAB method
     * The addService method builds a dialog menu and allows the user to create a SMSCard and
     * cardItem to look at.
     */
    public void addService() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.card_dialog, null);
        dialogBuilder.setView(dialogView);

        final EditText editTitle = dialogView.findViewById(R.id.edit_title);
        final EditText editPhone = dialogView.findViewById(R.id.edit_phone);
        editPhone.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        final EditText editMessage = dialogView.findViewById(R.id.edit_message);

        dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String titleText = editTitle.getText().toString();
                String phoneText = editPhone.getText().toString();
                //Regex Conversion
                String phoneNumber = phoneText.replaceAll("[^0-9*]", "");
                String messageText = editMessage.getText().toString();

                //This checks to make sure the dialog box is filled in correctly.
                if(titleText.length() * messageText.length() != 0 && (phoneNumber.length() == 10 || phoneText.equals("***"))) {
                    cardsAdapter.add(new SMSCard(R.color.colorAccent, titleText, phoneNumber, messageText));
                    updatePreferences();
                }
            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Empty
            }
        });

        //Shows the dialog
        dialogBuilder.create().show();
    }

    /**
     * The view method creates a snackbar and allows the user to view the card message or delete
     * the message.
     */
    public void viewMessage(View card) {
        final SMSCard smsCard = findSMSCardByView(card);

        Snackbar snackbar = Snackbar
                .make(findViewById(R.id.card_container), "Auto Response: '" + smsCard.message+"'", Snackbar.LENGTH_LONG)
                .setAction("Delete Card", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Snackbar.make(findViewById(R.id.card_container), "Card Deleted", Snackbar.LENGTH_LONG).show();
                        cardsAdapter.remove(smsCard);
                        updatePreferences();
                    }
                });
        snackbar.show();
    }

    /**
     * This is a helper method that allows me to use the relativeview of a card in order to find
     * the CardObject
     */
    private SMSCard findSMSCardByView(View view) {
        ViewGroup row = (ViewGroup) view;
        TextView textView = row.findViewById(R.id.card_title);

        for (int i = 0; i < cardsAdapter.getCount(); i++)
            if(cardsAdapter.getItem(i).title.equals(textView.getText().toString()))
                return cardsAdapter.getItem(i);

        return null;
    }

    /**
     * Update preferences clears the preferences and readds all the cards.
     * This comes down to me not having enough time to figure out a better method for finding
     * unique cards.
     */
    protected void updatePreferences()
    {
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        for (int i = 0; i < cardsAdapter.getCount(); i++) {
            String json = gson.toJson(cardsAdapter.getItem(i));
            editor.putString(json, cardsAdapter.getItem(i).title);
        }
        editor.apply();
    }

    public void toggleService(View toggleButton) {
        SMSCard card = findSMSCardByView((View) toggleButton.getParent());
        Button button = (Button) toggleButton;
        if(card.isOn) {
            card.isOn = false;
            button.setText("Off");
            button.setElevation(-8);
            button.setTextColor(getResources().getColor(R.color.black));
        } else {
            card.isOn = true;
            button.setText("On");
            button.setElevation(0);
            button.setTextColor(getResources().getColor(R.color.colorAccent));
        }
        updatePreferences();
    }
}

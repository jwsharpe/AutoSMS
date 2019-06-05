package com.kenken.autosms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;

/**
 * SMSReceiver extends BroadcastReceiver and is defined in the manifest to call onReceive when
 * an SMS is recieved on a device. The SMSReceiver pulls out the phone number of the contact and
 * then opens up the applications shared preferences in order to compare that number with the
 * SMSCard lists. If the phone numbers match, an automatic response it set out.
 *
 * @author James Sharpe
 * @date 12/11/2017
 */

public class SMSReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        String phoneNumber = "";
        SharedPreferences preferences =
                context.getSharedPreferences("SMSShared", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        ArrayList<SMSCard> cards = new ArrayList<SMSCard>();
        for (String json : preferences.getAll().keySet()) {
            SMSCard card = gson.fromJson(json, SMSCard.class);
            cards.add(card);
        }

        if(bundle != null) {
            /**
             * This partiation finds the phone number in the PDU.
             * The PDU is the Protocol Distribution Unit, it contains all information about a
             * message being sent to a users phone. Timestamp, SMS, Sender, ect...
             */
            Object[] pdus = (Object[]) bundle.get("pdus");
            final SmsMessage[] messages = new SmsMessage[pdus.length];
            for (int i = 0; i < pdus.length; i++) {
                String format = bundle.getString("format");
                messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i], format);
            }
            phoneNumber = messages[0].getOriginatingAddress();

            /**
             * This partition creates an smsManager class, and sends out a text message
             * Only if
             * The phone number matches or there is a wildcard and
             * The wildcard or smscard is activated
             */
            SmsManager smsManager = SmsManager.getDefault();
            Boolean isOnList = false;
            Boolean isWildcardOnList = false;
            String wildCardMessage = "";
            for (int i = 0; i < cards.size(); i++) {
                if(("+1" + cards.get(i).phone).equals(phoneNumber) && cards.get(i).isOn) {
                    smsManager.sendTextMessage(phoneNumber, null,
                            cards.get(i).message, null, null);
                    isOnList = true;
                }
                if(cards.get(i).phone.equals("***") && cards.get(i).isOn)
                {
                    isWildcardOnList = true;
                    wildCardMessage = cards.get(i).message;
                }
            }
            if(!isOnList && isWildcardOnList)
                smsManager.sendTextMessage(phoneNumber, null,
                        wildCardMessage, null, null);

        }
    }
}


package com.kenken.autosms;

/**
 * SMSCard is a standard object without the use of getters and setters. This is done as an exploration
 * of writing code without them.
 *
 * The SMSCard object holds the materialColor accent, title, phone, body message, and toggle switch
 * for the SMSViews. This is the object that gets thrown in the SMSAdapter class.
 *
 * @author James Sharpe
 * @date 12/11/2017
 */

public class SMSCard {
    int materialColor;
    String title;
    String phone;
    String message;
    Boolean isOn;

    public SMSCard(int materialColor, String title, String targetNumber, String targetMessage) {
        this.materialColor = materialColor;
        this.title = title;
        this.phone = targetNumber;
        this.message = targetMessage;
        isOn = true;
    }
}

package com.railprosfs.railsapp.ui_support;

public class PhoneNumberSupport {

    public String convertPhoneNumber(String input) {
        if(input == null) return "";
        int s = input.length();
        if(s < 7 || s > 11) return input;
        return reformatString(input);
    }

    //Reformat string like so: 1(320)241-1232
    private String reformatString(String input) {
        if(input == null) return "";
        String[] digits = input.split("");
        StringBuilder reformat = new StringBuilder();
        for(int i = input.length(); i >= 0; --i) {
            if(reformat.length() == 4) {
                reformat.append("-");
            }
            else if(reformat.length() == 8) {
                reformat.append(" )");
            }
            reformat.append(digits[i]);
            if(reformat.length() == 13) {
                reformat.append("(");
            }
        }

        if(input.length() == 7 || input.length() == 10)
            return reformat.reverse().toString();
        else
            return input;
    }

    public String switchToNumber(String input) {
        if(input == null) return "";
        String mInput = input;
        mInput = mInput.replaceAll("-", "");
        mInput = mInput.replaceAll("\\s+", "");
        mInput = mInput.replaceAll("[()]", "");
        return mInput;
    }
}

package com.example.speach2text;

import java.util.Calendar;

public class Function {
    static String wishMe() {
        String s = "";
        Calendar c = Calendar.getInstance();
        int time = c.get(Calendar.HOUR_OF_DAY);

        if (time >= 6 && time < 12) {
            s = "Good Morning Sir";
        } else if (time >= 12 && time < 16) {
            s = "Good Afternoon Sir";
        } else if (time >= 16 && time < 22) {
            s = "Good Evening Sir";
        } else if (time >= 22 || time < 6) {
            s = "Good Night Sir";
        }
        return s;
    }
}

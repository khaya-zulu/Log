package com.knexno.sudo.log.Utils;

import android.content.Context;
import android.widget.Toast;

public class Util {

    private Util(){}

    public static void showToast(Context context, int msg){
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }
}

package com.knexno.sudo.log.Utils;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.knexno.sudo.log.R;

import static com.knexno.sudo.log.Utils.Contract.USER_UID;

public class CustomDialog extends Dialog implements View.OnClickListener {

    private Context context;
    private String action;
    private TextView titleText, messageText, btnPositive, btnNegative;
    private String entryUid = null;

    public static final String AC_DISCARD = "discard";
    public static final String AC_DELETE = "delete";
    public static final String AC_LOG_OUT = "logOut";

    public CustomDialog(@NonNull Context context, String action) {
        super(context);
        this.context = context;
        this.action = action;
    }

    public CustomDialog(@NonNull Context context, String action, String entryUid) {
        super(context);
        this.context = context;
        this.action = action;
        this.entryUid = entryUid;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_custom);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        titleText = findViewById(R.id.dialog_title);
        messageText = findViewById(R.id.dialog_message);
        btnPositive = findViewById(R.id.btn_positive);
        btnNegative = findViewById(R.id.btn_negative);
    }

    public void setTitle(int title){
        titleText.setText(title);
    }

    public void setMessage(int message){
        messageText.setText(message);
    }

    public void setPositive(int positive){
        btnPositive.setText(positive);
    }

    public void setNegative(int negative){
        btnNegative.setText(negative);
    }

    public void actionLogOut(){
        AuthUI.getInstance().signOut(context);
    }

    public void actionDiscard(){
        if (getOwnerActivity() != null){
            getOwnerActivity().finish();
        }
    }

    public void actionDel(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(USER_UID).child(entryUid);
        reference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (!task.isSuccessful()){
                    Util.showToast(context, R.string.deleting_error);
                }
            }
        });

        actionDiscard();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_positive:
                if (action.equals(AC_DISCARD)){
                    actionDiscard();
                }else if (action.equals(AC_DELETE)){
                    actionDel();
                }else {
                    actionLogOut();
                }
                break;
            case R.id.btn_negative:
                dismiss();
                break;
        }
    }
}

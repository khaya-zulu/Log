package com.knexno.sudo.log;

import android.annotation.SuppressLint;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.knexno.sudo.log.RecyclerView.Entry;
import com.knexno.sudo.log.Utils.CustomDialog;
import com.knexno.sudo.log.Utils.Util;
import com.knexno.sudo.log.databinding.ActivityEditBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import static com.knexno.sudo.log.Utils.Contract.USER_UID;

public class AddActivity extends AppCompatActivity {

    //General
    private final Context CONTEXT = AddActivity.this;

    //Ui
    private ActivityEditBinding binding;
    private MenuItem itemDone;
    private Boolean isEdited = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        setTitle(R.string.add_entry);

        if (getActionBar() != null){
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit);
        binding.dateText.setText(getCurrentDate());

        setupOnEdit();
    }

    public void addEntry(){
        final String uuid = UUID.randomUUID().toString().replace("-", "");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(USER_UID).child(uuid);

        String heading = binding.headingText.getText().toString().trim();
        String body = binding.bodyText.getText().toString().trim();
        Entry newEntry = new Entry(uuid, heading, body, getCurrentDate());

        reference.setValue(newEntry).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    finish();
                }else {
                    Util.showToast(CONTEXT, R.string.adding_error);
                }
            }
        });
    }

    public String getCurrentDate(){
        Date today = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
        return dateFormat.format(today);
    }

    @SuppressLint("ClickableViewAccessibility")
    public void setupOnEdit(){
        View.OnTouchListener touchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                itemDone.setEnabled(true);
                if (!isEdited){
                    isEdited = true;
                }
                return true;
            }
        };
        binding.headingText.setOnTouchListener(touchListener);
        binding.bodyText.setOnTouchListener(touchListener);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (isEdited){
            discardDialog();
        }else {
            finish();
        }

    }

    public void discardDialog(){
        CustomDialog customDialog = new CustomDialog(CONTEXT, CustomDialog.AC_DISCARD);

        customDialog.setTitle(R.string.discard_title);
        customDialog.setMessage(R.string.discard_message);
        customDialog.setNegative(R.string.cancel);
        customDialog.setPositive(R.string.discard_positive);

        customDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add, menu);

        itemDone = menu.getItem(R.id.action_done);
        itemDone.setEnabled(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        addEntry();

        return super.onOptionsItemSelected(item);
    }
}

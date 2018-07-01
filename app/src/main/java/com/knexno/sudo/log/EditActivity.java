package com.knexno.sudo.log;

import android.annotation.SuppressLint;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.knexno.sudo.log.RecyclerView.Entry;
import com.knexno.sudo.log.Utils.CustomDialog;
import com.knexno.sudo.log.Utils.Util;
import com.knexno.sudo.log.databinding.ActivityEditBinding;

import static com.knexno.sudo.log.Utils.Contract.INTENT_ENTRY_UID;
import static com.knexno.sudo.log.Utils.Contract.USER_UID;

public class EditActivity extends AppCompatActivity {

    //General
    private final Context CONTEXT = EditActivity.this;

    //Firebase database
    private DatabaseReference reference;
    private ValueEventListener referenceListener;
    private Entry entry;
    private String entryUid;

    //Ui
    private Boolean isEdited = false;
    private Boolean isSaved = false;
    private ActivityEditBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        setTitle(R.string.edit_entry);

        if (getActionBar() != null){
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        setupDatabaseWithUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        reference.addListenerForSingleValueEvent(referenceListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        reference.removeEventListener(referenceListener);
    }

    public void setupDatabaseWithUI(){
        entryUid = getIntent().getStringExtra(INTENT_ENTRY_UID);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit);

        reference = FirebaseDatabase.getInstance().getReference(USER_UID).child(entryUid);
        referenceListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                entry = dataSnapshot.getValue(Entry.class);
                binding.setEntry(entry);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Util.showToast(CONTEXT, R.string.database_error);

            }
        };

        reference.addListenerForSingleValueEvent(referenceListener);
        setupOnEdit();
    }

    @SuppressLint("ClickableViewAccessibility")
    public void setupOnEdit(){
        View.OnTouchListener touchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!isEdited){
                    isEdited = true;
                }
                return true;
            }
        };
        binding.headingText.setOnTouchListener(touchListener);
        binding.bodyText.setOnTouchListener(touchListener);
    }

    public void updateEntry(Entry entry){
        reference.setValue(entry);
        Util.showToast(CONTEXT, R.string.entry_saved);
        isSaved = true;
    }

    public void deleteEntry(){
        CustomDialog customDialog = new CustomDialog(CONTEXT, CustomDialog.AC_DELETE, entryUid);

        customDialog.setTitle(R.string.delete_title);
        customDialog.setMessage(R.string.delete_message);
        customDialog.setNegative(R.string.cancel);
        customDialog.setPositive(R.string.delete_positive);

        customDialog.show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (isEdited){
            if (isSaved){
                finish();
            }else {
                discardDialog();
            }
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
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_save:
                updateEntry(binding.getEntry());
                break;
            case R.id.action_delete:
                deleteEntry();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}

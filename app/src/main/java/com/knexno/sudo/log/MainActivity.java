package com.knexno.sudo.log;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.knexno.sudo.log.RecyclerView.Entry;
import com.knexno.sudo.log.RecyclerView.EntryAdapter;
import com.knexno.sudo.log.Utils.CustomDialog;
import com.knexno.sudo.log.Utils.Util;

import java.util.Collections;

import static com.knexno.sudo.log.Utils.Contract.ENTRY_HEADING;
import static com.knexno.sudo.log.Utils.Contract.USER_UID;

public class MainActivity extends AppCompatActivity {

    //general
    private final Context CONTEXT = MainActivity.this;

    //Auth
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseAuth firebaseAuth;
    private static final int RC_SIGN_IN = 123;

    //RecyclerView
    private RecyclerView recyclerView;
    private EntryAdapter adapter;
    private EntryAdapter searchAdapter;

    //Empty View
    private RelativeLayout emptyView;
    private TextView emptyViewMessage;
    private CardView emptyViewCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();

        populateView();

        if (getActionBar() != null){
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        Boolean isConnected = checkConnectivity();
        if (isConnected){
            assignAuthListener();
            firebaseAuth.addAuthStateListener(authStateListener);
        }else {
            Util.showToast(CONTEXT, R.string.no_connection);
            setupEmptyView();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        onSignedOut();
    }

    public void populateView(){
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(CONTEXT));

        assert USER_UID != null;
        Query query = FirebaseDatabase.getInstance().getReference(USER_UID);

        FirebaseRecyclerOptions<Entry> options =
                new FirebaseRecyclerOptions.Builder<Entry>().setQuery(query, Entry.class)
                        .build();

        adapter = new EntryAdapter(options);
        recyclerView.setAdapter(adapter);
        setupEmptyView();
    }

    public void setupEmptyView(){
        if (emptyView == null || emptyViewMessage == null){
            emptyView = findViewById(R.id.empty_view);
            emptyViewMessage = findViewById(R.id.empty_view_message);
            emptyViewCard = findViewById(R.id.empty_view_card);
        }

        if (adapter.getItemCount() == 0){
            Boolean isConnected = checkConnectivity();

            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);

            if (isConnected){
                emptyViewMessage.setText(R.string.add_new_entry);
                startActivity(new Intent(CONTEXT, AddActivity.class));
                emptyViewCard.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(CONTEXT, AddActivity.class));
                    }
                });
                emptyViewCard.setBackgroundResource(R.drawable.list_item_selector);
            }else {
                emptyViewMessage.setText(R.string.no_connection);
            }
        }else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
            emptyViewCard.setOnClickListener(null);
        }
    }

    public void onSignedOut(){
        adapter.stopListening();
    }

    public void onSignIn(FirebaseUser user){
        USER_UID = user.getUid();
        adapter.startListening();
    }

    public void assignAuthListener(){
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null){
                    onSignIn(user);
                }else {
                    onSignedOut();
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(true)
                                    .setAvailableProviders(Collections.singletonList(
                                            new AuthUI.IdpConfig.GoogleBuilder().build()))
                                    .build(),
                            RC_SIGN_IN);
                }

            }
        };
    }

    public Boolean checkConnectivity(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN){
            if (resultCode == RESULT_OK){
                Util.showToast(CONTEXT, R.string.welcome);
            }else {
                finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        CustomDialog dialog = new CustomDialog(CONTEXT, CustomDialog.AC_LOG_OUT);

        dialog.setTitle(R.string.logout_title);
        dialog.setMessage(R.string.logout_message);
        dialog.setNegative(R.string.cancel);
        dialog.setPositive(R.string.logout_positive);

        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) menuItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Query searchQuery = FirebaseDatabase.getInstance().getReference(USER_UID).orderByChild(ENTRY_HEADING).startAt(newText)
                        .endAt(newText + "\uf8ff");

                if (TextUtils.isEmpty(newText)){
                    recyclerView.setAdapter(adapter);
                    searchAdapter = null;
                }else {
                    searchAdapter = null;
                    FirebaseRecyclerOptions<Entry> options =
                            new FirebaseRecyclerOptions.Builder<Entry>().setQuery(searchQuery, Entry.class)
                                    .build();

                    searchAdapter = new EntryAdapter(options);
                    recyclerView.setAdapter(searchAdapter);
                }
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_add:
                startActivity(new Intent(CONTEXT, AddActivity.class));
                break;
            case R.id.action_search:
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
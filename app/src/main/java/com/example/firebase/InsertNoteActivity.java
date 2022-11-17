package com.example.firebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class InsertNoteActivity extends AppCompatActivity implements View.OnClickListener{
    private TextView tvEmail;
    private TextView tvUid;
    private EditText etTitle;
    private EditText etDesc;

    FirebaseAuth mAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    Note note;

    GoogleSignInClient gsc;
    GoogleSignInOptions gso;
    GoogleSignInAccount gsa;

    NoteAdapter noteAdapter;
    ArrayList<Note> noteList;
    RecyclerView rv_notes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert_note);

        tvEmail = findViewById(R.id.tv_email);
        tvUid = findViewById(R.id.tv_uid);
        Button btnKeluar = findViewById(R.id.btn_keluar);
        etTitle = findViewById(R.id.et_title);
        etDesc = findViewById(R.id.et_description);
        Button btnSubmit = findViewById(R.id.btn_submit);

        mAuth = FirebaseAuth.getInstance();

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        note = new Note();

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        gsc = GoogleSignIn.getClient(this, gso);

        gsa = GoogleSignIn.getLastSignedInAccount(this);

        btnSubmit.setOnClickListener(this);
        btnKeluar.setOnClickListener(this);

        rv_notes = findViewById(R.id.rv_notes);
//        ViewCompat.setNestedScrollingEnabled(rv_notes, false);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        rv_notes.setLayoutManager(layoutManager);
        rv_notes.setItemAnimator(new DefaultItemAnimator());

        databaseReference.child("notes").child(Objects.requireNonNull(mAuth.getUid())).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                noteList = new ArrayList<>();
                for(DataSnapshot dSnapshot : snapshot.getChildren()){
                    Note note = dSnapshot.getValue(Note.class);
                    note.setKey(dSnapshot.getKey());
                    noteList.add(note);
                }
                noteAdapter = new NoteAdapter(noteList, InsertNoteActivity.this);
                rv_notes.setAdapter(noteAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null){
            tvEmail.setText(currentUser.getEmail());
            tvUid.setText(currentUser.getUid());
        } else if (gsa != null) {
            tvEmail.setText(gsa.getEmail());
            tvUid.setText(gsa.getDisplayName());
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_keluar:
                logOut();
                break;
            case R.id.btn_submit:
                submitData();
                break;
        }
    }

    public void logOut(){
        mAuth.signOut();
        gsc.signOut();
        Intent intent = new Intent(InsertNoteActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // makesure user cant go back
        startActivity(intent);
    }

    public void submitData(){
        if (!validateForm()){
            return;
        }

        String title = etTitle.getText().toString();
        String desc = etDesc.getText().toString();
        Note baru = new Note(title, desc);
        databaseReference
                .child("notes")
                .child(Objects.requireNonNull(mAuth.getUid()))
                .push()
                .setValue(baru)
                .addOnSuccessListener(this, unused -> Toast.makeText(InsertNoteActivity.this, "Add data", Toast.LENGTH_SHORT).show()).addOnFailureListener(this, e -> Toast.makeText(InsertNoteActivity.this, "Failed to Add data", Toast.LENGTH_SHORT).show());
    }

    private boolean validateForm() {
        boolean result = true;
        if (TextUtils.isEmpty(etTitle.getText().toString())) {
            etTitle.setError("Required");
            result = false;
        } else {
            etTitle.setError(null);
        }
        if (TextUtils.isEmpty(etDesc.getText().toString())) {
            etDesc.setError("Required");
            result = false;
        } else {
            etDesc.setError(null);

        }
        return result;
    }
}
package com.ruslanmancavolkov.parkingvelo;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ruslanmancavolkov.parkingvelo.models.Parcs;

public class ParcActivity extends AppCompatActivity {

    private Button btnSubmit;
    private EditText name, capacity, creationDate;
    private Switch shared;
    private Parcs parc;
    private DatabaseReference ref;
    private static final String FIREBASE_DB = "https://parking-velo.firebaseio.com/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parc);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        btnSubmit = findViewById(R.id.btn_submit);
        name = findViewById(R.id.name);
        capacity = findViewById(R.id.capacity);
        creationDate = findViewById(R.id.creation_date);
        shared = (Switch)findViewById(R.id.shared);

        Intent i = getIntent();
        parc = (Parcs)i.getSerializableExtra("parc");

        name.setText(parc.getN());
        capacity.setText(String.valueOf(parc.getCp()));
        creationDate.setText(parc.getDc());
        shared.setChecked(parc.getS());
        ref = FirebaseDatabase.getInstance(FirebaseApp.getInstance()).getReferenceFromUrl(FIREBASE_DB);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference parcsRef = ref.child("parcs");
                parcsRef.child(parc.getId()).child("n").setValue(name.getText().toString());
                parcsRef.child(parc.getId()).child("s").setValue(shared.isChecked());
                parcsRef.child(parc.getId()).child("cp").setValue(Integer.parseInt(capacity.getText().toString()));
                startActivity(new Intent(ParcActivity.this, ParcsActivity.class));
            }
        });
    }
}

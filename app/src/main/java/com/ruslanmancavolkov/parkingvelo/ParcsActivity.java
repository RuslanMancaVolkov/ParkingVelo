package com.ruslanmancavolkov.parkingvelo;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ruslanmancavolkov.parkingvelo.adapters.ParcsListAdapter;
import com.ruslanmancavolkov.parkingvelo.adapters.ParcsListViewAdapter;
import com.ruslanmancavolkov.parkingvelo.helpers.RecyclerParcsTouchHelper;
import com.ruslanmancavolkov.parkingvelo.helpers.RecyclerParcsTouchHelperListener;
import com.ruslanmancavolkov.parkingvelo.models.Parcs;
import com.ruslanmancavolkov.parkingvelo.models.ParcsLocations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ParcsActivity extends AppCompatActivity implements RecyclerParcsTouchHelperListener {

    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;
    ArrayList<Parcs> parcs;
    private ParcsListViewAdapter parcsListViewAdapter;
    ListView lvParcs;
    private static final String FIREBASE_DB = "https://parking-velo.firebaseio.com/";
    private DatabaseReference ref;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private ParcsListAdapter adapter;
    private CoordinatorLayout rootLayout;
    private GeoFire geoFire;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parcs);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        auth = FirebaseAuth.getInstance();

        //get current user
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(ParcsActivity.this, LoginActivity.class));
                    finish();
                }
            }
        };

        parcs = new ArrayList<Parcs>();

        ref = FirebaseDatabase.getInstance(FirebaseApp.getInstance()).getReferenceFromUrl(FIREBASE_DB);
        String uid = auth.getCurrentUser().getUid();

        recyclerView = (RecyclerView)findViewById(R.id.recycler_view);
        rootLayout = (CoordinatorLayout)findViewById(R.id.rootLayout);
        adapter = new ParcsListAdapter(this, parcs);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(adapter);
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback
                = new RecyclerParcsTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);


        Query query = ref.child("parcs").orderByChild("u").equalTo(uid);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                progressBar.setVisibility(View.GONE);
                parcs.clear();
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    Parcs parc = postSnapshot.getValue(Parcs.class);
                    parc.setId(postSnapshot.getKey());
                    parcs.add(parc);
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // ...
            }
        });


    }

    @Override
    public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof ParcsListAdapter.MyViewHolder){
            final Parcs parc = parcs.get(viewHolder.getAdapterPosition());
            final Parcs deletedItem = parcs.get(viewHolder.getAdapterPosition());

            // On récupère les données géographiques avant de supprimer
            ref.child("parcs_locations").child(parc.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    final ParcsLocations location = dataSnapshot.getValue(ParcsLocations.class);

                    ref.child("parcs").child(parc.getId()).removeValue();
                    ref.child("parcs_locations").child(parc.getId()).removeValue();
                    final int deleteIndex = viewHolder.getAdapterPosition();

                    adapter.removeItem(deleteIndex);

                    Snackbar snackBar = Snackbar.make(rootLayout, parc.getN() + " " + getString(R.string.delete_parc), Snackbar.LENGTH_LONG);
                    snackBar.setAction(getString(R.string.undo), new View.OnClickListener(){

                        @Override
                        public void onClick(View v) {

                            DatabaseReference parcsRef = ref.child("parcs");
                            String parcKey = parc.getId();
                            // Ne pas insérer l'identifiant de l'objet pour alléger la quantité de données stockées
                            parcsRef.child(parcKey).setValue(new Parcs (parc));

                            geoFire = new GeoFire(ref.child("parcs_locations"));
                            geoFire.setLocation(parcKey, new GeoLocation(location.l.get(0), location.l.get(1)));

                            adapter.restoreItem(deletedItem, deleteIndex);
                        }
                    });

                    snackBar.setActionTextColor(Color.YELLOW);
                    snackBar.show();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // ...
                }
            });


        }
    }
}

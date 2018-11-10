package com.appspot.airpeepee.airpeepee.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class db {

    public Toilet toilet;
    public List<Toilet> mtoilet;

    public String plz;

    public db(){
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("/toilet");

        // First Query all Toilette with name "City Toilette"
        Query mQueryRef = ref.orderByChild("name").equalTo("City Toilette");

        mQueryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                System.out.println(dataSnapshot.getChildrenCount()); // just 136 toilet with name "City Toilette"

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // Attach a listener to read the data at our posts reference
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                System.out.println(dataSnapshot.getChildrenCount());

              //  toilet = dataSnapshot.getValue(Toilet.class);
              //  System.out.println(toilet);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });







    };

    public db(String toilet, String plz) {

        // Get a reference to our toilets
        final FirebaseDatabase firedb = FirebaseDatabase.getInstance();
        DatabaseReference toiletRef = firedb.getReference("/toilet");

        /* Attach a listener to read the data at our posts reference */
        toiletRef.addValueEventListener(new

              ValueEventListener() {
                  @Override
                  public void onDataChange(DataSnapshot dataSnapshot) {
                      Toilet toilet = dataSnapshot.getValue(Toilet.class);

                      //Log.d("toilet:", String.valueOf(toilet));
                      System.out.println(toilet);
                      //System.out.println(dataSnapshot.getKey()); //das ist die Toilet collection (table toilet)
                      //System.out.println(dataSnapshot.getValue());  // das ist das Value von das Table. In diesem Fall - das ganze JSON

                      //Log.d("db:", toile.toString());
                  }

                  @Override
                  public void onCancelled(DatabaseError databaseError) {
                      System.out.println("The read failed: " + databaseError.getCode());
                  }
              });

    }

}

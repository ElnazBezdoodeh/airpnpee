package com.appspot.airpeepee.airpeepee.model;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.appspot.airpeepee.airpeepee.AddActivity;
import com.appspot.airpeepee.airpeepee.MainActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseException;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class db {

    private static DatabaseReference toiletRef ;
    private static StorageReference storageRef;


    public db(){

        //db ref
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        //db storage ref
        final FirebaseStorage databaseStorage = FirebaseStorage.getInstance();

        toiletRef = database.getReference("/toilet");
        storageRef = databaseStorage.getReference();

        // Attach a SINGLE READ listener to read the data at our posts reference
        toiletRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // declare the list inside onDataChange because the function fires asynchronously
                List<Toilet> toiletList = new ArrayList<>();

                for(DataSnapshot toiletSnapshot : dataSnapshot.getChildren()) {
                    String id = (String) toiletSnapshot.getKey();
                    String fee = (String) toiletSnapshot.child("fee").getValue();
                    String name = (String) toiletSnapshot.child("name").getValue();
                    String openingHours = (String) toiletSnapshot.child("opening_hours").getValue();
                    String plz = (String) toiletSnapshot.child("plz").getValue();
                    String street = (String) toiletSnapshot.child("street").getValue();
                    String streetNo = (String) toiletSnapshot.child("street_number").getValue();
                    String wheelchair = (String) toiletSnapshot.child("wheelchair").getValue();
                    double locationLat = (double) toiletSnapshot.child("location").child("lat").getValue();
                    double locationLon = (double) toiletSnapshot.child("location").child("lon").getValue();
                    boolean isPrivate = Boolean.parseBoolean(toiletSnapshot.child("isPrivate").getValue().toString());

                    Toilet temp =new Toilet(id, fee, locationLat, locationLon, name, openingHours, plz, street, streetNo, wheelchair,isPrivate);

                    toiletList.add(temp);
                }

                DataHolder.getInstance().setData(toiletList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }

        });

    }

    public static boolean addToilet(Toilet toilet)
    {
        try {
            toiletRef.child(toilet.id).push();
            toiletRef.child(toilet.id).child("name").setValue(toilet.getName());
            toiletRef.child(toilet.id).child("fee").setValue(toilet.isFee());
            toiletRef.child(toilet.id).child("description").setValue(toilet.getDescription());
            toiletRef.child(toilet.id).child("isPrivate").setValue(toilet.isPrivate());
            toiletRef.child(toilet.id).child("location").child("lat").setValue(toilet.getLocationLat());
            toiletRef.child(toilet.id).child("location").child("lon").setValue(toilet.getLocationLon());
            toiletRef.child(toilet.id).child("opening_hours").setValue(toilet.getOpeninghours());
            toiletRef.child(toilet.id).child("wheelchair").setValue(toilet.isWheelchair());
            toiletRef.child(toilet.id).child("ratingTotal").setValue(toilet.getTotalRating());
            toiletRef.child(toilet.id).child("street").setValue(toilet.getStreet());
            toiletRef.child(toilet.id).child("plz").setValue(toilet.getPlz());
            toiletRef.child(toilet.id).child("photoUrl").setValue(toilet.getPhotoUrl());
            return true;
        }
        catch (Exception e)
        {
            return false;
        }

    }

    public static String uploadImage(Uri filePath, final Context context) {

        String bucketResult = "";

        if(filePath != null)
        {
            StorageReference ref = storageRef.child("images/"+ UUID.randomUUID().toString());

            //final ProgressDialog progressDialog = new ProgressDialog(context);
            //progressDialog.setTitle("Uploading...");
            //progressDialog.show();

            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //progressDialog.dismiss();
                            Toast.makeText(context, "Successfully uploaded photo!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //progressDialog.dismiss();
                            Toast.makeText(context, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            //progressDialog.setMessage("Uploaded "+(int)progress+"%");
                        }
                    });
            bucketResult = ref.toString();

        }

        return bucketResult;
    }


}

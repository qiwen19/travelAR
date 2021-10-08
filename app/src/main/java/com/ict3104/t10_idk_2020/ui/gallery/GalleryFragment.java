package com.ict3104.t10_idk_2020.ui.gallery;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.ict3104.t10_idk_2020.R;

import static android.content.ContentValues.TAG;

public class GalleryFragment extends Fragment {

    private GalleryViewModel galleryViewModel;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseAuth mAuth;

    private String sortName = "asc";
    private String sortLocation = "asc";
    private String sortRating = "asc";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        galleryViewModel =
                ViewModelProviders.of(this).get(GalleryViewModel.class);
        return inflater.inflate(R.layout.fragment_gallery, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

        View v = getView();
        final TableLayout reportTable = v.findViewById(R.id.reportTable);
        final TableRow.LayoutParams params = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT);
        params.weight = 1.0f;

        // Firestore get featurelist data
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        final CollectionReference featuresCollection = db.collection("Features");
//        for (int i = 0; i < 25; i++) {
//            final Feature feature = new Feature();
//            feature.setAge("23");
//            feature.setCommuteMethod("Walking");
//            feature.setGender("Male");
//            feature.setName("Stairs");
//            feature.setRating("5");
//            feature.setLocation("Yio Chu Kang MRT");
//            feature.setFeedback("Ohhhh nice stairsss");
//            featuresCollection.document().set(feature);
//        }

        final Query.Direction[] dir = new Query.Direction[1];
        v.findViewById(R.id.FeatureNameHeader).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Log.d(TAG, "Feature sort by name");
                reportTable.removeAllViews();
                if (sortName.equals("asc")) {
                    dir[0] = Query.Direction.DESCENDING;
                    Log.d(TAG, "Feature sort by name DESCENDING");
                    sortName = "desc";
                }
                else {
                    dir[0] = Query.Direction.ASCENDING;
                    Log.d(TAG, "Feature sort by name asc");

                    sortName = "asc";
                }
                buildTable(reportTable, "name", params, featuresCollection, dir[0]);
            }
        });

        v.findViewById(R.id.FeatureLocationHeader).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Log.d(TAG, "Feature sort by location");
                reportTable.removeAllViews();
                if (sortLocation == "asc") {
                    dir[0] = Query.Direction.DESCENDING;
                    sortLocation = "desc";
                    Log.d(TAG, "Feature sort by location DESCENDING");

                }
                else {
                    dir[0] = Query.Direction.ASCENDING;
                    sortLocation = "asc";
                    Log.d(TAG, "Feature sort by location asc");

                }
                buildTable(reportTable, "location", params, featuresCollection, dir[0]);
            }
        });

        v.findViewById(R.id.FeatureRatingHeader).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Log.d(TAG, "Feature sort by rating");
                //cleanTable(reportTable);
                reportTable.removeAllViews();
                if (sortRating == "asc") {
                    dir[0] = Query.Direction.DESCENDING;
                    sortRating = "desc";
                    Log.d(TAG, "Feature sort by rating DESCENDING");
                }
                else {
                    dir[0] = Query.Direction.ASCENDING;
                    sortRating = "asc";
                    Log.d(TAG, "Feature sort by rating asc");
                }
                buildTable(reportTable, "rating", params, featuresCollection, dir[0]);
            }
        });

        //Default
        buildTable(reportTable, "name", params, featuresCollection, Query.Direction.ASCENDING);
    }

    private  void buildTable(final TableLayout reportTable, String sortBy, final TableRow.LayoutParams params, CollectionReference featuresCollection, Query.Direction direction){
        final View v = getView();
        // All the detail in card view
        final Button closeSelected = v.findViewById(R.id.closeButton);
        final Button locationButton = v.findViewById(R.id.locationButton);
        final CardView reportDetailsCard = v.findViewById(R.id.reportDetailsCard);
        final TextView textViewFeatureNames = v.findViewById(R.id.textViewFeatureNames);
        final TextView textViewFeatureRating = v.findViewById(R.id.textViewFeatureRating);
        final TextView textViewAge = v.findViewById(R.id.textViewAge);
        final TextView textViewGender = v.findViewById(R.id.textViewGender);
        final TextView textViewCommute = v.findViewById(R.id.textViewCommute);
        final TextView textViewFeedback = v.findViewById(R.id.textViewFeatureFeedback);


        featuresCollection.orderBy(sortBy, direction)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (final QueryDocumentSnapshot document : task.getResult()) {
                               // Log.d(TAG, document.getId() + " => " + document.getData());
                                final TableRow tbrow = new TableRow(getContext());
                                tbrow.setClickable(true);
                                tbrow.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        textViewFeatureNames.setText(String.format("Feature: %s", document.get("name")));
                                        textViewFeatureRating.setText(String.format("Rating: %s", document.get("rating")));
                                        textViewAge.setText(String.format("Age: %s", document.get("age")));
                                        textViewGender.setText(String.format("Gender: %s", document.get("gender")));
                                        textViewCommute.setText(String.format("Commute Method: %s", document.get("commuteMethod")));
                                        textViewFeedback.setText(String.format("Feedback: %s", document.get("feedback")));
                                        reportDetailsCard.setVisibility(View.VISIBLE);

                                        closeSelected.setOnClickListener(new View.OnClickListener() {
                                            public void onClick(View v) {
                                                reportDetailsCard.setVisibility(View.INVISIBLE);
                                                tbrow.setBackgroundResource(0);

                                            }
                                        });
                                        locationButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

                                                String LatLng = String.format("%s", document.get("location"));
                                                String feature = String.format("%s", document.get("name"));
                                                String rating = String.format("%s", document.get("rating"));

                                                String location = String.format("%s, feature: %s, rating: %s", document.get("name"), feature, rating);
                                                Toast.makeText(getActivity(), location, Toast.LENGTH_LONG).show();

                                                String[] latLng = LatLng.split(",");
                                                double latitude = Double.parseDouble(latLng[0]);
                                                double longitude = Double.parseDouble(latLng[1]);
                                                LatLng latLngLocation = new LatLng(latitude, longitude);

                                                Uri gmmIntentUri = Uri.parse("geo:<" + latitude  + ">,<" + longitude + ">?q=<" + latitude  + ">,<" + longitude + ">(" + feature + ")");
                                               // Uri gmmIntentUri = Uri.parse(String.format("geo:%s", LatLng));
                                                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                                                mapIntent.setPackage("com.google.android.apps.maps");
                                                if (mapIntent.resolveActivity(getContext().getPackageManager()) != null) {
                                                    startActivity(mapIntent);
                                                }
                                                //lat:1.335752, long:103.92229
                                            }
                                        });
                                    }
                                });

                                TextView t1v = new TextView(getContext());
                                t1v.setText(document.get("name").toString());
                                t1v.setAllCaps(true);
                                t1v.setTextSize(12);
                                t1v.setLayoutParams(params);
                                t1v.setGravity(Gravity.CENTER);
                                t1v.setPadding(0, 0, 0, 50);
                                tbrow.addView(t1v);

                                TextView t2v = new TextView(getContext());
                                t2v.setText(document.get("location").toString());
                                t2v.setAllCaps(true);
                                t2v.setTextSize(12);
                                t2v.setLayoutParams(params);
                                t2v.setGravity(Gravity.CENTER);
                                t2v.setPadding(0, 0, 0, 50);

                                tbrow.addView(t2v);

                                TextView t3v = new TextView(getContext());
                                t3v.setText(document.get("rating").toString());
                                t3v.setAllCaps(true);
                                t3v.setTextSize(12);
                                t3v.setLayoutParams(params);
                                t3v.setGravity(Gravity.CENTER);
                                t3v.setPadding(0, 0, 0, 50);
                                tbrow.addView(t3v);

                                reportTable.addView(tbrow);
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
}


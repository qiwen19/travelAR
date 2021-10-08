package com.ict3104.t10_idk_2020;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class PlaceNode extends Node {
    Context context;
    PlaceToken place;

    private ViewRenderable placeRenderable = null;
    private TextView textViewPlace = null, textViewPlaceDesc = null;
    private Button ratingBtn = null, submitBtn = null;
    private RatingBar ratingBar = null;
    private EditText txtFeedBack = null;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseAuth mAuth;

    public PlaceNode(Context c, PlaceToken p){
        this.context = c;
        this.place = p;
    }

    @Override
    public void onActivate(){
        super.onActivate();

        if(getScene() == null){
            return;
        }

        if(placeRenderable != null){
            return;
        }

        ViewRenderable.builder()
                .setView(context, R.layout.place_view)
                .build()
                .thenAccept(
                (renderable) -> {
                    setRenderable(renderable);
                    placeRenderable = renderable;

                    textViewPlace = renderable.getView().findViewById(R.id.placeName);
                    textViewPlace.setText(place.name);
                    textViewPlaceDesc = renderable.getView().findViewById(R.id.placeDesc);
                    textViewPlaceDesc.setText(place.desc);
                    ratingBtn = renderable.getView().findViewById(R.id.btnRate);

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                    AlertDialog alert = alertDialogBuilder.create();
                    alert.setTitle(place.name + " Feedback");
                    alert.setView(View.inflate(context, R.layout.rate_dialog, null));

                    ratingBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //Activity act = (Activity) context;
                            alert.show();

                            submitBtn =(Button) alert.findViewById(R.id.btnSubmit);
                            submitBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    //Get User Details
                                    mAuth = FirebaseAuth.getInstance();
                                    String uid = mAuth.getCurrentUser().getUid();
                                    db = FirebaseFirestore.getInstance();
                                    final DocumentReference docRef = db.collection("Account").document(uid);

                                    Map<String, Object> feedback = new HashMap<>();
                                    String commuteMethod = "Walking";

                                    //Get user information.
                                    docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                DocumentSnapshot document = task.getResult();
                                                if (document.exists()) {
                                                    Log.d("ANJ", "DocumentSnapshot data: " + document.getData());
                                                    // Get user data
                                                    String gender = document.getString("gender");
                                                    String dob = document.getString("dob");
                                                    //Calculate user age
                                                    DateTimeFormatter format = DateTimeFormatter.ofPattern("d/MM/yyyy");
                                                    LocalDate birthDate = LocalDate.parse(dob, format);
                                                    LocalDate currentDate =  LocalDate.now();
                                                    int age = calculateAge(birthDate, currentDate);

                                                    ratingBar = (RatingBar) alert.findViewById(R.id.feedbackRating);
                                                    txtFeedBack = (EditText) alert.findViewById(R.id.editTextFeedback);
                                                    String rating = Float.toString(ratingBar.getRating());
                                                    String txtFeedback = txtFeedBack.getText().toString();
                                                    String latlng = Double.toString(place.latlng.latitude) + ", "
                                                            + Double.toString(place.latlng.longitude);

                                                    //Store to database features collection.
                                                    feedback.put("gender", gender);
                                                    feedback.put("age", age);
                                                    feedback.put("commuteMethod", commuteMethod);
                                                    feedback.put("rating", rating);
                                                    feedback.put("feedback", txtFeedback);
                                                    feedback.put("name", place.name);
                                                    feedback.put("location", latlng);
                                                    db.collection("Features").add(feedback);
                                                } else {
                                                    Log.d("PlaceNode", "No such document");
                                                }
                                            } else {
                                                Log.d("PlaceNode", "get failed with ", task.getException());
                                            }
                                        }
                                    });
                                    //TODO: Get this node and remove from parent so that user cant feedback on the same node again
                                    alert.dismiss();
                                }
                            });
                        }
                    });
                });
    }

    public static int calculateAge(LocalDate birthDate, LocalDate currentDate){
        if((birthDate != null) && (currentDate != null)){
            return Period.between(birthDate, currentDate).getYears();
        }
        else{
            return 0;
        }
    }

    public void showInfoWindow(){
        //Show text
        if(textViewPlaceDesc.getVisibility() == View.VISIBLE || ratingBtn.getVisibility() == View.VISIBLE){
            textViewPlaceDesc.setVisibility(View.GONE);
            ratingBtn.setVisibility(View.GONE);
        }
        else{
            textViewPlaceDesc.setVisibility(View.VISIBLE);
            ratingBtn.setVisibility(View.VISIBLE);
        }

        //Hide text for other nodes
        if(this.getParent().getChildren() != null){
            for(int i = 0; i < this.getParent().getChildren().size(); i++){
                //Check if the node is not the Sphere Obj.
                if(this.getParent().getChildren().get(i).getClass() == ObjNode.class){
                    continue;
                }

                PlaceNode child = (PlaceNode)this.getParent().getChildren().get(i);
                if(child != this && child.getClass() == this.getClass()){
                    child.textViewPlaceDesc.setVisibility(View.GONE);
                    child.ratingBtn.setVisibility(View.GONE);
                }
            }
        }
    }
}

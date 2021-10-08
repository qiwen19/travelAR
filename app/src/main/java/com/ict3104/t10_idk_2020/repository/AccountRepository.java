package com.ict3104.t10_idk_2020.repository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.ict3104.t10_idk_2020.model.Account;

public class AccountRepository{

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference accountCollection = db.collection("Account");

    int userCount = 0;
    public void isUserExist(String email, String password, final IntegerCallBack callBack){
        accountCollection
                .whereEqualTo("email", email)
                .whereEqualTo("password", password)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                userCount++;
                            }
                            callBack.onCallBack(userCount);
                        }else {
                            Log.d("AccountRepository", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    public void insertAccount(String uid,Account account, final BooleanCallBack callBack){
        Log.d("RESULTS", uid);
        accountCollection.document(uid).set(account);
        callBack.onCallBack(true);
    }

    int count = 0;
    public void isAccountExist(String email, final IntegerCallBack callBack){
        accountCollection
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                count++;
                            }
                            callBack.onCallBack(count);
                        }else {
                            Log.d("AccountRepository", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

}

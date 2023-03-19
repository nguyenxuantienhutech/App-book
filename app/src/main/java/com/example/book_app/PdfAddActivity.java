package com.example.book_app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.book_app.databinding.ActivityPdfAddBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class PdfAddActivity extends AppCompatActivity {
    private ActivityPdfAddBinding binding;
    private FirebaseAuth firebaseAuth;
    private Uri uriPdf = null;
    private ArrayList<ModelCategory> categoryList;
    private static final String TAG = "ADD_PDF_TAG";
    private static final int PDF_PICK_CODE = 1000;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        //tag for debugging
        loadCategories();

        //back btn
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        //select category
        binding.categoryTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                categoryPickDialog();
            }
        });
        //submit
        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });
        //attach
        binding.attachBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pdfPickIntent();
            }
        });
    }
    private  String title = "", description ="", category = "";
    private void validateData() {
        //step 1: validate
        Log.d(TAG, "validateData: validating data...");
        //get data
        title = binding.titleEt.getText().toString().trim();
        description = binding.descriptionEt.getText().toString().trim();
        category = binding.categoryTv.getText().toString().trim();
        //validate
        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(description) || TextUtils.isEmpty(category) || uriPdf == null){
            Toast.makeText(this, "Please do not to empty", Toast.LENGTH_SHORT).show();
        }
        else{
            uploadToStorage();
        }
    }

    private void uploadToStorage() {
        //step 2: upload to storage
        Log.d(TAG, "uploadToStorage: Uploadint to storage");
        progressDialog.setMessage("Uploading Pdf...");
        progressDialog.show();
        long timestamp = System.currentTimeMillis();
        //path of pdf in firebase storage
        String filePathAndName = "Books/" + timestamp;
        //Storage references
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
        storageReference.putFile(uriPdf)
            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d(TAG, "onSuccess: PDF uploaded to storage...");
                Log.d(TAG, "onSuccess: getting pdf url");
                //get url
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
//                while(!uriTask.isSuccessful()){
//                    String uploadPdfUrl = ""+uriTask.getResult();
//                    //upload to firebase db
//                    uploadInfoToDb(uploadPdfUrl,timestamp);
//                }
                uriTask.addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful()){
                            String uploadPdfUrl = ""+task.getResult();
                            //upload to firebase db
                            uploadInfoToDb(uploadPdfUrl,timestamp);
                        } else {
                            progressDialog.dismiss();
                            Log.d(TAG, "onComplete: Could not get download url");
                            Toast.makeText(PdfAddActivity.this, "Could not get download url", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Log.d(TAG, "onFailure: Upload pdf failed"+e.getMessage());
                Toast.makeText(PdfAddActivity.this, "Upload Pdf failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadInfoToDb(String uploadPdfUrl, long timestamp) {
        //step 3: upload to realtime db
        Log.d(TAG, "uploadInfoToDb: Uploading to DB...");
        progressDialog.setMessage("Uploading pdf info...");
        String uid = firebaseAuth.getUid();
        //setup data to upload
        HashMap<String ,Object> hashMap = new HashMap<>();
        hashMap.put("uid", ""+uid);
        hashMap.put("id", ""+timestamp);
        hashMap.put("title", ""+title);
        hashMap.put("description", ""+description);
        hashMap.put("category", ""+category);
        hashMap.put("url", ""+uploadPdfUrl);
        hashMap.put("timestamp", timestamp);
        //db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(""+timestamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                progressDialog.dismiss();
                Log.d(TAG, "onSuccess: Success to upload database...");
                Toast.makeText(PdfAddActivity.this, "Success to upload database", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Log.d(TAG, "onFailure: Upload to database failed"+e.getMessage());
                Toast.makeText(PdfAddActivity.this, "Upload to database failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCategories() {
        Log.d(TAG, "loadCategories: Loading pdf category");
        categoryList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryList.clear();
                for(DataSnapshot ds : snapshot.getChildren()){
                    //get data
                    ModelCategory model = ds.getValue(ModelCategory.class);
                    //add to list
                    categoryList.add(model);
                    Log.d(TAG, "onDataChange: "+model.getCategory());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void categoryPickDialog() {
        //load category from firebase
        Log.d(TAG, "categoryPickDialog: showing category pdf");
        //get String array cate from array list
        String [] categoriesArray = new String[categoryList.size()];
        for (int i = 0; i < categoryList.size(); i++){
            categoriesArray[i] = categoryList.get(i).getCategory();
        }
        //alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick category").setItems(categoriesArray, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //handle item click
                //get clicked item from list
                String category = categoriesArray[which];
                //set category to text view
                binding.categoryTv.setText(category);
                Log.d(TAG, "onClick: Selected category: "+ category);
            }
        }).show();
    }

    private void pdfPickIntent() {
        Log.d(TAG, "pdfPickIntent: starting pdf pick intent");
        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select PDF"), PDF_PICK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == PDF_PICK_CODE){
                Log.d(TAG, "onActivityResult: PDF Picked");
                uriPdf = data.getData();
                Log.d(TAG, "onActivityResult: URI: "+uriPdf);

            }
        }
        else{
            Log.d(TAG, "onActivityResult: Cancel pick pdf");
            Toast.makeText(this, "Cancel pick pdf", Toast.LENGTH_SHORT).show();
        }
    }
}
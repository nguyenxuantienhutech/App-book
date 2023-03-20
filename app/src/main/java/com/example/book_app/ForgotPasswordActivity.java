package com.example.book_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.example.book_app.databinding.ActivityForgotPasswordBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ForgotPasswordActivity extends AppCompatActivity {
    private ActivityForgotPasswordBinding binding;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.statusTv.setVisibility(View.GONE);
        binding.statusBtn.setVisibility(View.GONE);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);
        //back
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        //reset password
        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });
    }

    private void validateData() {
        String email = binding.emailEt.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            statusCheck("Email empty!", R.drawable.ic_error);
        } else if (!isValidEmail(email)) {
            statusCheck("Invalid Email!", R.drawable.ic_error);
        } else {
            existEmail(email, new OnEmailCheckListener() {
                @Override
                public void onEmailCheck(boolean exist) {
                    if (!exist) {
                        statusCheck("Email is not registered", R.drawable.ic_error);
                    } else {
                        resetPassword();
                    }
                }
            });
        }
    }
    //check email exist
    public void existEmail(String email, OnEmailCheckListener listener) {
        email = binding.emailEt.getText().toString().trim();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        Task<SignInMethodQueryResult> task = mAuth.fetchSignInMethodsForEmail(email);

        task.addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
            @Override
            public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                if (task.isSuccessful()) {
                    SignInMethodQueryResult result = task.getResult();
                    List<String> signInMethods = result.getSignInMethods();
                    if (signInMethods != null && !signInMethods.isEmpty()) {
                        listener.onEmailCheck(true);
                    } else {
                        listener.onEmailCheck(false);
                    }
                } else {
                    listener.onEmailCheck(false);
                }
            }
        });
    }

    public interface OnEmailCheckListener {
        void onEmailCheck(boolean exist);
    }

    //check email true
    public boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+"[a-zA-Z0-9_+&*-]+)*@" +"(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
    //set status
    private void statusCheck(String text, int icon) {
        //show notification in 3s
        binding.statusTv.setVisibility(View.VISIBLE);
        binding.statusBtn.setVisibility(View.VISIBLE);
        binding.statusBtn.setImageResource(icon);
        binding.statusTv.setText(text);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                binding.statusTv.setVisibility(View.GONE);
                binding.statusBtn.setVisibility(View.GONE);
            }
        }, 3000);
    }
    //reset password
    private void resetPassword() {
        progressDialog.setMessage("Email sending...");
        progressDialog.show();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        //get text email input
        String emailAddress = binding.emailEt.getText().toString().trim();
        auth.sendPasswordResetEmail(emailAddress)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            progressDialog.dismiss();
                            binding.statusTv.setVisibility(View.VISIBLE);
                            binding.statusBtn.setVisibility(View.VISIBLE);
                            statusCheck("Check your email to reset password.",R.drawable.ic_done);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    binding.statusTv.setVisibility(View.GONE);
                                    binding.statusBtn.setVisibility(View.GONE);
                                }
                            }, 3000);
                        }
                        else{
                            progressDialog.dismiss();
                            binding.statusTv.setVisibility(View.VISIBLE);
                            binding.statusBtn.setVisibility(View.VISIBLE);
                            binding.statusBtn.setImageResource(R.drawable.ic_error);
                            binding.statusTv.setText("Please make sure the email you entered is correct");
                        }
                    }
                });
    }
}
package com.kpstv.youtube;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jakewharton.processphoenix.ProcessPhoenix;
import com.kpstv.youtube.helper.BillingClient;
import com.kpstv.youtube.helper.BillingUtils;
import com.kpstv.youtube.utils.YTutils;
import com.razorpay.PaymentData;
import com.razorpay.PaymentResultWithDataListener;
import com.razorpay.RazorpayClient;

public class PurchaseActivity extends AppCompatActivity implements PaymentResultWithDataListener {

    private static final int RC_SIGN_IN = 103;
    private Button mBuybutton; BillingClient client;
    private static final String TAG = "PurchaseActivity";
    GoogleApiClient mGoogleSignInClient;
    private FirebaseAuth mAuth; FirebaseDatabase database;
    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase);

        initViews();

        database = FirebaseDatabase.getInstance();

        database.getReference("is_payment_disable").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    if ((Boolean) dataSnapshot.getValue()) {
                        doOnPaymentDisable();
                    }
                }catch (Exception ignored){
                    doOnPaymentDisable();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle("Purchase");

        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getResources().getString(R.string.default_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = new GoogleApiClient.Builder(getApplicationContext())
                .enableAutoManage(this,connectionResult -> {

        }).addApi(Auth.GOOGLE_SIGN_IN_API, gso).build();
                /*.enableAutoManmage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                    }
                }).addApi(Auth.GOOGLE_SIGN_IN_API, gso).build();*/

        mBuybutton.setOnClickListener(view -> {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser!=null) {
                makePurchase( currentUser.getUid());
            }else {
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleSignInClient);
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Toast.makeText(this, "Failed to sign in, Error: "+e.getStatusCode(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
                Log.e(TAG, "signInResult:failed code=" + e.getStatusCode());
            }
        }
        if (requestCode == 104) {
            if (resultCode==1) {
                boolean isPaid = data.getBooleanExtra("payment",false);
                if (isPaid) {
                    BillingUtils.setSuccess(data.getStringExtra("client"),
                            FirebaseDatabase.getInstance().getReference("orders"));
                    doOnSuccess();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }



    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {
        Log.d("firebaseAuth", "firebaseAuthWithGoogle:" + acct.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        makePurchase(user.getUid());
                    } else {
                        Log.e("GoogleSignFailed",task.getException().getMessage()+"");
                        Toast.makeText(getApplicationContext(),"Sign in failed!",Toast.LENGTH_SHORT).show();
                    }
                });
    }


    @SuppressLint("StaticFieldLeak")
    void makePurchase(String uid) {

        client = new BillingClient(PurchaseActivity.this)
                .getInstance(AppInterface.razorpayKEYID, AppInterface.razorpayKEYSECRET);
        client.setDatabase(database.getReference("orders"));
        client.setCurrentOrder(new BillingClient.OrderRequest(
                "premium_ytplayer", 7000, "INR",
                "YTPlayer Pro", uid
        ));
        client.verify(new BillingClient.VerificationListener() {
            @Override
            public void onVerficationSuccess(RazorpayClient razorpayClient, String paymentId) {
                Toast.makeText(PurchaseActivity.this, "Payment already done for this order!"
                        , Toast.LENGTH_SHORT).show();
                doOnSuccess();
            }

            @Override
            public void onVerficationFailed(RazorpayClient razorpayClient, int code) {
                if (code==1) {
                    View v = getLayoutInflater().inflate(R.layout.alert_payment_gateway,null);
                    ImageView razorpay = v.findViewById(R.id.razorpayButton);
                    ImageView paypal = v.findViewById(R.id.paypalButton);
                    Button cancelButton = v.findViewById(R.id.cancelButton);

                    cancelButton.setOnClickListener(view -> alertDialog.dismiss());

                    razorpay.setOnClickListener(view -> {
                        alertDialog.dismiss();
                        client.quickCheckout();
                    });

                    paypal.setOnClickListener(view -> {
                        alertDialog.dismiss();
                        Intent intent = new Intent(PurchaseActivity.this,PaypalActivity.class);
                        intent.putExtra("uid",uid);
                        startActivityForResult(intent,104);
                    });

                    alertDialog = new AlertDialog.Builder(PurchaseActivity.this)
                            .setView(v)
                            .create();
                    alertDialog.show();
                }else
                    Toast.makeText(PurchaseActivity.this, "Error in fetching payment details!"
                            , Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    void doOnPaymentDisable() {
        alertDialog = new AlertDialog.Builder(this)
                .setTitle("Payment Disabled")
                .setCancelable(false)
                .setMessage("Due to some issues or request, in-app-purchase is disabled currently.\nKindly wait until issue is fixed!")
                .setPositiveButton("OK",(dialogInterface, i) -> {
                    finish();
                })
                .setNeutralButton("Contact",(dialogInterface, i) -> {
                    YTutils.StartURLIntent("mailto:developerkp16@gmail.com",MainActivity.activity);
                    finish();
                })
                .create();
        alertDialog.show();
    }

    private void initViews() {
        mBuybutton = findViewById(R.id.buyButton);
    }

    public void contactClick(View view) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL,new String[]{"developerkp16@gmail.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT,"YTPlayer: Payment Error");
        try {
            startActivity(Intent.createChooser(intent, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "No email clients found.", Toast.LENGTH_SHORT).show();
        }
    }

    AlertDialog alertDialog;
    void doOnSuccess() {

        SharedPreferences preferences = getSharedPreferences("appSettings",MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("pref_purchase",true);
        editor.apply();

        View v = getLayoutInflater().inflate(R.layout.alert_purchase_complete,null);
        alertDialog = new AlertDialog.Builder(this)
                .setView(v)
                .setCancelable(false)
                .setPositiveButton("Restart",(dialogInterface, i) -> {
                    ProcessPhoenix.triggerRebirth(PurchaseActivity.this);
                })
                .create();
        alertDialog.show();
    }

    @Override
    public void onPaymentSuccess(String s, PaymentData paymentData) {
        int code = client.processSuccess(s,paymentData);
        if (code == 0) {
            Toast.makeText(this, "Payment Completed!", Toast.LENGTH_SHORT).show();
            doOnSuccess();
        }else if (code == 2) Toast.makeText(this, "Error: Payment Failed!", Toast.LENGTH_SHORT).show();
        else if (code == 1)
            Toast.makeText(this, "Payment not authorized, Contact me!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPaymentError(int code, String s, PaymentData paymentData) {
        Toast.makeText(this, "Payment error/cancelled (code "+code+")", Toast.LENGTH_SHORT).show();
    }
}

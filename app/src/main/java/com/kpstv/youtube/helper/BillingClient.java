package com.kpstv.youtube.helper;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.razorpay.Checkout;
import com.razorpay.Order;
import com.razorpay.Payment;
import com.razorpay.PaymentData;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

import org.json.JSONException;
import org.json.JSONObject;

/** Verification Error codes:
 *  -------------------------
 *  1 - Error payment not done yet...
 *  2 - Error in fetching payment data...
 *
 *  Process success payment:
 *  ------------------------
 *  0 - Payment success
 *  1 - Payment not authorized
 *  2 - Undefined error
 *  3 - Payment captured failed
 */


public class BillingClient {

    private String keyId;
    private String keySecret;
    private static RazorpayClient razorpay;

    private static final String TAG = "BillingClient";
    private static Activity activity;
    private static DatabaseReference reference;
    private static OrderRequest orderRequest;

    public BillingClient(Activity activity) {
        this.activity = activity;
    }

    public static class OrderRequest {
        String orderName, currency, description, email;
        int amount;

        public OrderRequest(String orderName, int amount,
                            String currency, String description, String email) {
            this.orderName = orderName;
            this.currency = currency;
            this.description = description;
            this.email = email;
            this.amount = amount;
        }

        public String getOrderName() {
            return orderName;
        }

        public String getCurrency() {
            return currency;
        }

        public String getDescription() {
            return description;
        }

        public String getUID() {
            return email;
        }

        public int getAmount() {
            return amount;
        }
    }

    public void setCurrentOrder(OrderRequest request) {
        orderRequest = request;
    }

    public void setDatabase(DatabaseReference dbRef) {
        reference = dbRef;
    }

    public BillingClient getInstance(String keyId, String keySecret) {
        this.keyId = keyId;
        this.keySecret = keySecret;
        try {
            razorpay = new RazorpayClient(keyId, keySecret);
            return this;
        } catch (RazorpayException e) {
            return null;
        }
    }

    public int processSuccess(String Id, PaymentData data) {
        try {
            Payment pD = razorpay.Payments.fetch(Id);
            JSONObject object = pD.toJson();
            if (object.getString("status").equals("authorized")) {
                JSONObject captureRequest = new JSONObject();
                captureRequest.put("amount",orderRequest.getAmount() );
                captureRequest.put("currency", orderRequest.getCurrency());
                Payment payment = razorpay.Payments.capture(Id,captureRequest);
                JSONObject obj = payment.toJson();
                if (obj.getString("status").equals("captured")) {
                    try {
                        reference.child(orderRequest.getUID()).setValue(Id);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return 0;
                }else return 3;
            }else {
                return 1;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return 2;
    }

    public void verify(VerificationListener listener) {
        listener.execute();
    }

    public static abstract class VerificationListener extends AsyncTask<Void, Void, Void> {
        private boolean verified=false;
        private boolean processComplete=false;
        ProgressDialog dialog = new ProgressDialog(activity);
        String Id;
        @Override
        protected void onPreExecute() {
            dialog.setMessage("Processing...");
            dialog.setCancelable(false);
            dialog.show();
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        try {
                            if (dataSnapshot.child(orderRequest.getUID()).exists()) {
                                verified=true;
                                Id = (String) dataSnapshot.child(orderRequest.getUID()).getValue();
                            }
                        } catch (Exception e) {
                            onVerficationFailed(razorpay,2);
                        }
                        processComplete=true;
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        onVerficationFailed(razorpay,2);
                        processComplete = true;
                    }
                });
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            while (!processComplete);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dialog.dismiss();
            if (verified)
                onVerficationSuccess(razorpay,Id);
            else onVerficationFailed(razorpay,1);
        }

        public abstract void onVerficationSuccess(RazorpayClient razorpayClient, String paymentId);
        public abstract void onVerficationFailed(RazorpayClient razorpayClient,int code);
    }

    public void quickCheckout() {
        /** Get order id first */
        try {
            JSONObject request = new JSONObject();
            request.put("amount", orderRequest.getAmount());
            request.put("currency", orderRequest.getCurrency());
            request.put("receipt", orderRequest.getOrderName());
            request.put("payment_capture", false);

            Order order = razorpay.Orders.create(request);
            JSONObject object = order.toJson();
            if (object.getString("status").equals("created")) {
                String orderId = object.getString("id");

                /** Launch checkout form */

                Checkout checkout = new Checkout();
                JSONObject options = new JSONObject();
                options.put("name", "KP'S TV Inc.");

                options.put("description", orderRequest.getDescription());
                options.put("order_id", orderId);
                options.put("currency", "INR");

                checkout.open(activity, options);
            }else {
                Log.e(TAG, "Failed to create order");
            }
        } catch (RazorpayException e) {
            // Handle Exception
            System.out.println(e.getMessage());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}


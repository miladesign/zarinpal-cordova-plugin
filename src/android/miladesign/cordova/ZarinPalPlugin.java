package miladesign.cordova;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.zarinpal.ewallets.purchase.OnCallbackRequestPaymentListener;
import com.zarinpal.ewallets.purchase.OnCallbackVerificationPaymentListener;
import com.zarinpal.ewallets.purchase.PaymentRequest;
import com.zarinpal.ewallets.purchase.ZarinPal;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

public class ZarinPalPlugin extends CordovaPlugin {
	private static final String LOG_TAG = "ZarinPal";
	private static CallbackContext callbackVerification, callbackPayment = null;
	private static Activity mActivity = null;
	public CordovaInterface cordova = null;
	private String merchantId, schema;
	private boolean sandbox = false;
	private Intent mIntent;

	@Override
	public void initialize(CordovaInterface initCordova, CordovaWebView WebView) {
		Log.e (LOG_TAG, "initialize");
		cordova = initCordova;
		super.initialize (cordova, webView);
	}
	
	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		if ("initialize".equals(action)) {
			initialize(args, callbackContext);
			return true;
	    }
		if ("startPayment".equals(action)) {
			startPayment(args, callbackContext);
			return true;
	    }
		if ("verificationPayment".equals(action)) {
			verificationPayment(callbackContext);
			return true;
	    }
		if ("showPayment".equals(action)) {
			showPayment(callbackContext);
			return true;
	    }
	    return false;
	}
	
	@Override
	public void onNewIntent(Intent intent) {
		cordova.getActivity().setIntent(intent);
		super.onNewIntent(intent);
    }
	
	private void initialize(JSONArray args, CallbackContext callbackContext) throws JSONException {
		merchantId = args.getString(0);
		sandbox = args.getBoolean(1);
		schema = "cordovazp://app";
		mActivity = cordova.getActivity();
		callbackContext.success();
	}
	
	private void showPayment(final CallbackContext callbackContext) {
		cordova.getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (mIntent != null) {
					mActivity.startActivity(mIntent);
					callbackContext.success();
				} else {
					callbackContext.error("Intent is null");
				}
			}
		});
	}
	
	private void verificationPayment(CallbackContext callbackContext) {
		callbackVerification = callbackContext;
		final Intent intent = cordova.getActivity().getIntent();
		cordova.getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (intent.getData() != null) {
					ZarinPal.getPurchase(mActivity).verificationPayment(intent.getData(), new OnCallbackVerificationPaymentListener() {
			            @Override
			            public void onCallbackResultVerificationPayment(boolean success, String refID, PaymentRequest paymentRequest) {
			            	JSONObject result = new JSONObject();
			            	try {
								result.put("Success", success);
								result.put("RefID", refID);
							} catch (JSONException e) {
								e.printStackTrace();
							}
			    			PluginResult pr = new PluginResult(PluginResult.Status.OK, result);
			    			pr.setKeepCallback(true);
			    			callbackVerification.sendPluginResult(pr);
			            }
			        });
				} else {
					callbackVerification.error("Intent is null!");
				}
			}
		});
	}
	
	private void startPayment(JSONArray args, CallbackContext callbackContext) throws JSONException {
		callbackPayment = callbackContext;
		final boolean autoStart = args.getBoolean(0);
		final int amount = args.getInt(1);
		final String description = args.getString(2);
		final String email = args.getString(3);
		final String mobile = args.getString(4);
		cordova.getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				PaymentRequest payment;
				if (sandbox) {
					payment = ZarinPal.getSandboxPaymentRequest();
				} else {
					payment = ZarinPal.getPaymentRequest();
				}
		        payment.setMerchantID(merchantId);
		        payment.setAmount(amount);
		        payment.setDescription(description);
		        payment.setCallbackURL(schema);
		        if ((mobile != "") && (mobile.length() == 11) && (mobile.startsWith("0"))) {
		        	payment.setMobile(mobile);
		        }
		        if ((email != "") && (isValidEmail(email))) {
		        	payment.setEmail(email);
		        }
		        
		        ZarinPal.getPurchase(mActivity).startPayment(payment, new OnCallbackRequestPaymentListener() {
		            @Override
		            public void onCallbackResultPaymentRequest(int status, String authority, Uri paymentGatewayUri, Intent intent) {
		            	mIntent = intent;
	            		JSONObject result = new JSONObject();
		            	try {
							result.put("Status", status);
							result.put("Authority", authority);
						} catch (JSONException e) {
							e.printStackTrace();
						}
		    			PluginResult pr = new PluginResult(PluginResult.Status.OK, result);
		    			pr.setKeepCallback(true);
		    			callbackPayment.sendPluginResult(pr);
		            	if (autoStart) {
		            		mActivity.startActivity(intent);
		            	}
		            }
		        });
			}
		});
	}
	
	private boolean isValidEmail(CharSequence target) {
	    if (target == null) 
	        return false;
	    return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
	}
}

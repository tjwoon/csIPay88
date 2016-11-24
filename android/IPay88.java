// This code can only handle one payment at any given moment - no concurrent payments
// are allowed!

package org.cloudsky.cordovaPlugins;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ipay.Ipay;
import com.ipay.IpayPayment;
import com.ipay.IpayResultDelegate;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.Manifest;
import java.util.Locale;
import java.io.Serializable;

public class IPay88 extends CordovaPlugin {
    
    // Configuration. CUSTOMISE THIS ACCORDING TO YOUR NEEDS! ----------

    public static int IPAY88_ACT_REQUEST_CODE = 88;
    public static int REQUEST_PERMISSION_REQUEST_CODE = 87;


    // Constants

    private static final String PHONE = Manifest.permission.READ_PHONE_STATE;


    // iPay88 results receiver -----------------------------------------

    static public class ResultDelegate implements IpayResultDelegate, Serializable {
        
        private static final long serialVersionUID = 5963066398271211659L;
        
        public final static int STATUS_OK = 1;
        public final static int STATUS_FAILED = 2;
        public final static int STATUS_CANCELED = 0;
        
        public void onPaymentSucceeded (String transId, String refNo, String amount, String remarks, String auth)
        {
            IPay88.r_status = STATUS_OK;
            IPay88.r_transactionId = transId;
            IPay88.r_referenceNo = refNo;
            IPay88.r_amount = amount;
            IPay88.r_remarks = remarks;
            IPay88.r_authCode = auth;
        }
        
        public void onPaymentFailed (String transId, String refNo, String amount, String remarks, String err)
        {
            IPay88.r_status = STATUS_FAILED;
            IPay88.r_transactionId = transId;
            IPay88.r_referenceNo = refNo;
            IPay88.r_amount = amount;
            IPay88.r_remarks = remarks;
            IPay88.r_err = err;
        }

        public void onPaymentCanceled (String transId, String refNo, String amount, String remarks, String errDesc)
        {
            IPay88.r_status = STATUS_CANCELED;
            IPay88.r_transactionId = transId;
            IPay88.r_referenceNo = refNo;
            IPay88.r_amount = amount;
            IPay88.r_remarks = remarks;
            IPay88.r_err = "canceled";
        }

        public void onRequeryResult (String merchantCode, String refNo, String amount, String result)
        {
            // TODO warning, this is a stub to satisfy superclass interface
            // requirements. We do not yet have any meaningful support for
            // requery in this Cordova library yet.
        }
    }

    
    // State -----------------------------------------------------------

    // Class variables to transfer results from IpayResultDelegate.
    public static boolean isInProgress = false;
    public static int     r_status;
    public static String  r_transactionId;
    public static String  r_referenceNo;
    public static String  r_amount;
    public static String  r_remarks;
    public static String  r_authCode;
    public static String  r_err;

    private ResultDelegate iPayDelegate;
    private CallbackContext cordovaCallbackContext;
    
    private JSONObject iPayArgObj;


    // Methods ---------------------------------------------------------
    
    @Override
    public void onRequestPermissionResult (
        int requestCode,
        String[] permissions,
        int[] grantResults
    )
    throws JSONException
    {
        for(int r:grantResults)
        {
            if(r == PackageManager.PERMISSION_DENIED)
            {
                cordovaCallbackContext.error("Permission denied");
                return;
            }
        }

        if(requestCode == REQUEST_PERMISSION_REQUEST_CODE) {
            payViaIPay88();
        }
    }

    @Override
    public void onActivityResult (int requestCode, int resultCode, Intent intent)
    {
        if(requestCode == IPAY88_ACT_REQUEST_CODE) {
            try {
                JSONObject resp = new JSONObject();
                resp.put("transactionId", r_transactionId);
                resp.put("referenceNo", r_referenceNo);
                resp.put("amount", r_amount);
                resp.put("remarks", r_remarks);
                switch(r_status) {
                    case ResultDelegate.STATUS_OK:
                        resp.put("authCode", r_authCode);
                        cordovaCallbackContext.success(resp);
                        break;
                    case ResultDelegate.STATUS_FAILED:
                    case ResultDelegate.STATUS_CANCELED:
                        resp.put("err", r_err);
                        cordovaCallbackContext.error(resp);
                        break;
                    default:
                        cordovaCallbackContext.error("Unexpected result from iPay88 plugin.");
                }
            } catch (Exception e) {
                cordovaCallbackContext.error("Unexpected failure in iPay88 plugin: "+e.getMessage());
            } finally {
                isInProgress = false;
            }
        }
    }
    
    @Override
    public boolean execute (String action, JSONArray args, CallbackContext callbackContext)
    throws JSONException
    {
        if(action.equals("makepayment")) {
            if(isInProgress) {
                callbackContext.error("Another payment is in progress!");
            } else {
                cordovaCallbackContext = callbackContext;
                iPayArgObj = args.getJSONObject(0);

                if(!cordova.hasPermission(PHONE)) {
                    cordova.requestPermission(this, REQUEST_PERMISSION_REQUEST_CODE, PHONE);
                } else {
                    payViaIPay88();
                }
            }
            
            return true;
        } else {
            return false;
        }
    }

    private void initCanceledResult ()
    {
        r_status = ResultDelegate.STATUS_CANCELED;
        r_transactionId = "";
        r_referenceNo = "";
        r_amount = "";
        r_remarks = "";
        r_authCode = "";
        r_err = "canceled";
    }

    private void payViaIPay88 ()
    {
        int amount;
        String name, email, phone, refNo, currency, country,
               description, remark, paymentId, lang, merchantKey, merchantCode,
               backendPostUrl;

        initCanceledResult(); // Default the result to "canceled", as ResultDelegate is not called on backbutton exit.

        try {
            amount = iPayArgObj.getInt("amount");
            name = iPayArgObj.getString("name");
            email = iPayArgObj.getString("email");
            phone = iPayArgObj.getString("phone");
            refNo = iPayArgObj.getString("refNo");
            currency = iPayArgObj.getString("currency");
            country = iPayArgObj.getString("country");
            description = iPayArgObj.getString("description");
            remark = iPayArgObj.getString("remark");
            paymentId = iPayArgObj.getString("paymentId");
            lang = iPayArgObj.getString("lang");
            merchantKey = iPayArgObj.getString("merchantKey");
            merchantCode = iPayArgObj.getString("merchantCode");
            backendPostUrl = iPayArgObj.getString("backendPostUrl");
        } catch (Exception e) {
            cordovaCallbackContext.error("Required parameter missing or invalid. "+e.getMessage());
            return;
        }

        // iPay object.
        IpayPayment payment = new IpayPayment();
        payment.setMerchantKey(merchantKey);
        payment.setMerchantCode(merchantCode);
        payment.setAmount(String.format(Locale.US, "%.2f", Integer.valueOf(amount).floatValue()/100.0)); // http://developer.android.com/reference/java/util/Locale.html#default_locale
        payment.setUserName(name);
        payment.setUserEmail(email);
        payment.setUserContact(phone);
        payment.setRefNo(refNo);
        payment.setCurrency(currency);
        payment.setCountry(country);
        payment.setProdDesc(description);
        payment.setRemark(remark);
        payment.setPaymentId(paymentId);
        payment.setLang(lang);
        payment.setBackendPostURL(backendPostUrl);
        
        // Create and save the iPay88 results delegate.
        iPayDelegate = new ResultDelegate(); 

        // iPay88 intent.
        Intent checkoutIntent = Ipay.getInstance().checkout(
            payment,
            cordova.getActivity(),
            iPayDelegate
        );

        // Launch the iPay88 activity.
        // 1st arg to startActivityForResult() must be null, otherwise all WebViews get pause
        // leading to stuck iPay88 activity??
        isInProgress = true;
        cordova.startActivityForResult(null, checkoutIntent, IPAY88_ACT_REQUEST_CODE);
        cordova.setActivityResultCallback(this);
    }
}

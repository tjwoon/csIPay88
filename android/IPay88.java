// This code can only handle one payment at any given moment - no concurrent payments
// are allowed!

package org.cloudsky.cordovaPlugins;

import org.apache.cordova.api.CordovaInterface;
import org.apache.cordova.api.CordovaPlugin;
import org.apache.cordova.api.CallbackContext;
import org.apache.cordova.api.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ipay.Ipay;
import com.ipay.IpayPayment;
import com.ipay.IpayResultDelegate;

import android.content.Intent;
import java.util.Locale;
import java.io.Serializable;

public class IPay88 extends CordovaPlugin {
    
    // Configuration. CUSTOMISE THIS ACCORDING TO YOUR NEEDS! ----------

    public static int IPAY88_ACT_REQUEST_CODE = 88;
    

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
    private cordovaCallbackContext cordovaCallbackContext;


    // Methods ---------------------------------------------------------
    
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
    
    // Execute()
    // Scenarios:
    // - MAKEPAYMENT
    //   action = "makepayment"
    //   args = [
    //     {
    //       amount: int amount to charge (in cents)
    //       name: "payee name"
    //       email: "payee email address"
    //       phone: "payee phone number"
    //       refNo: "reference number for this transaction"
    //       currency: "MYR" | etc...
    //       lang: "ISO-8859-1" | "UTF-8" | etc...
    //       country: iPay88 gateway country - "MY" | "PH" | etc...
    //       description: "description of the product"
    //       remark: "remarks for the transaction"
    //       paymentId: "ipay payment id"
    //       merchantKey: "ipay merchant key"
    //       merchantCode: "ipay merchant code"
    //     }
    //   ]
    //   
    //   Return:
    //   - success(successObject) // Successful. See SUCCESS OBJECT.
    //   - error(failureObject)  // Failure or cancellation. See FAILURE OBJECT.
    //   - error("misc error message") // Misc failure
    //   
    //   Success Object = {
    //     transactionId: transId,
    //     referenceNo: refNo,
    //     amount: amount,
    //     remarks: remarks,
    //     authCode: auth,
    //   }
    //   
    //   Failure Object = "unexpected error string" or {
    //     transactionId: transId,
    //     referenceNo: refNo,
    //     amount: amount,
    //     remarks: remarks,
    //     err: error message, // "canceled" if user canceled.
    //   }
    @Override
    public boolean execute (String action, JSONArray args, CallbackContext callbackContext)
    throws JSONException
    {
        if(action.equals("makepayment")) {
            if(isInProgress) {
                callbackContext.error("Another payment is in progress!");
            } else {
                JSONObject argObj;
                
                isInProgress = true;
                cordovaCallbackContext = callbackContext;
                initCanceledResult(); // Default the result to "canceled", as ResultDelegate is not called on backbutton exit.

                argObj = args.getJSONObject(0);
                payViaIPay88(argObj);
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

    private void payViaIPay88 (JSONObject args, CallbackContext callbackContext)
    throws JSONException
    {
        int amount;
        String name, email, phone, refNo, currency, country,
               description, remark, paymentId, lang, merchantKey, merchantCode;

        amount = arg.getInt("amount");
        name = arg.getString("name");
        email = arg.getString("email");
        phone = arg.getString("phone");
        refNo = arg.getString("refNo");
        currency = arg.getString("currency");
        country = arg.getString("country");
        description = arg.getString("description");
        remark = arg.getString("remark");
        paymentId = arg.getString("paymentId");
        lang = arg.getString("lang");
        merchantKey = arg.getString("merchantKey");
        merchantCode = arg.getString("merchantCode");

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
        
        // Create and save the iPay88 results delegate.
        iPayDelegate = new ResultDelegate(); 

        // iPay88 intent.
        Intent checkoutIntent = Ipay.getInstance().checkout(
            payment,
            cordova.getActivity(),
            iPayDelegate
        );

        // Launch the iPay88 activity.
        cordova.setActivityResultCallback(this);
        cordova.startActivityForResult(this, checkoutIntent, IPAY88_ACT_REQUEST_CODE);
    }
}

CsIPay88
========

Cordova Library for integrating with the iPay88 payment gateway's mobile SDK.

This plugin will automatically request for READ_PHONE_STATE permission on
Android, if it is not already granted (iPay88 SDK requires it).


Installation
------------

`cordova plugin add cordova-plugin-ipay88`

Usage
-----

```javascript
cloudSky.iPay88.makePayment(
    {
        amount: 123 // int amount to charge in cents. 123 = 1.23
        name: "payee name"
        email: "payee email address"
        phone: "payee phone number"
        refNo: "reference number for this transaction"
        currency: "MYR" | ...
        lang: "ISO-8859-1" | "UTF-8" | ...
        country: "MY" | "PH" | ... // iPay88 gateway country
        description: "description of the product"
        remark: "remarks for the transaction"
        paymentId: "ipay payment id"
        merchantKey: "ipay merchant key"
        merchantCode: "ipay merchant code"
        backendPostUrl: "http://..." // The URL which iPay will call from their
            // servers upon successful payment.
    },
    function (resp) {
        // Success callback
        // resp = {
        //     transactionId: transId,
        //     referenceNo: refNo,
        //     amount: amount,
        //     remarks: remarks,
        //     authCode: auth,
        // }
    },
    function (err) {
        // Failure callback
        // err = "some error string" OR
        // err = {
        //     transactionId: transId,
        //     referenceNo: refNo,
        //     amount: amount,
        //     remarks: remarks,
        //     err: error message, // "canceled" if user canceled the payment.
        // }
    }
)
```

### Special Android notes

Android may kill your Cordova activity while your user is in the iPay88 activity.
When this happens, payment results will NOT be sent to the JavaScript callback
which you passed to the other methods (above). Instead, it will be sent to your
app with the "resume" Cordova event.

```javacript
document.addEventListener("resume", function (event) {
    /* Event:
        {
            action: "resume",
            pendingResult: {
                pluginServiceName: "IPay88",
                pluginStatus: "OK", // or "Error", etc.
                result: ... // same result that would have been passed to
                    // your normal callback function.
            }
        }
    */
}, false)
```

See also:

- https://cordova.apache.org/docs/en/latest/guide/platforms/android/index.html#lifecycle-guide
- https://cordova.apache.org/docs/en/latest/guide/platforms/android/plugin.html#launching-other-activities

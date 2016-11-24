CsIPay88
========

Cordova Library for integrating with the iPay88 payment gateway's mobile SDK.

Installation
------------

`cordova plugin add org.cloudsky.cordovaplugins.ipay88`

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

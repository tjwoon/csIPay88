'use strict';

var argscheck = require('cordova/argscheck'),
    exec      = require('cordova/exec');

function IPay88 () {};

IPay88.prototype = {

    // MAKEPAYMENT
    // params = [
    //   {
    //     amount: int amount to charge (in cents)
    //     name: "payee name"
    //     email: "payee email address"
    //     phone: "payee phone number"
    //     refNo: "reference number for this transaction"
    //     currency: "MYR" | etc...
    //     lang: "ISO-8859-1" | "UTF-8" | etc...
    //     country: iPay88 gateway country - "MY" | "PH" | etc...
    //     description: "description of the product"
    //     remark: "remarks for the transaction"
    //     paymentId: "ipay payment id"
    //     merchantKey: "ipay merchant key"
    //     merchantCode: "ipay merchant code"
    //   }
    // ]
    // 
    // Return:
    // - success(successObject) // Successful. See SUCCESS OBJECT.
    // - error(failureObject)  // Failure or cancellation. See FAILURE OBJECT.
    // - error("misc error message") // Misc failure
    // 
    // Success Object = {
    //   transactionId: transId,
    //   referenceNo: refNo,
    //   amount: amount,
    //   remarks: remarks,
    //   authCode: auth,
    // }
    // 
    // Failure Object = "unexpected error string" or {
    //   transactionId: transId,
    //   referenceNo: refNo,
    //   amount: amount,
    //   remarks: remarks,
    //   err: error message, // "canceled" if user canceled.
    // }
    makePayment: function (params, success, error)
    {
        argscheck.checkArgs('ofF', 'CsIPay88.makePayment', arguments);
        exec(success, error, 'CsIPay88', 'makepayment', [params]);
    },

};

module.exports = new IPay88();

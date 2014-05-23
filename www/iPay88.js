'use strict';

var argscheck = require('cordova/argscheck'),
    exec      = require('cordova/exec');

function IPay88 () {};

IPay88.prototype = {

    makePayment: function (params, success, error)
    {
        argscheck.checkArgs('ofF', 'iPay88.makePayment', arguments);
        exec(success, error, 'IPay88', 'makepayment', [params]);
    },

};

module.exports = new IPay88();

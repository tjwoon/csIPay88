/***

Tested against iPay88 iOS SDK v1.0.1

***/


#import <Cordova/CDV.h>
#import "Ipay.h"
#import "IpayPayment.h"

@interface CsIPay88 : CDVPlugin <PaymentResultDelegate>

- (void) makepayment: (CDVInvokedUrlCommand*)command;

@end

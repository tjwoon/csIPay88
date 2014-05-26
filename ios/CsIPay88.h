#import <Cordova/CDV.h>
#import "Ipay.h"
#import "IpayPayment.h"

@interface CsIPay88 : CDVPlugin <PaymentResultDelegate>

- (void) makepayment: (CDVInvokedUrlCommand*)command;

@end

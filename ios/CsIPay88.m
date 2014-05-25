#import "CsIPay88.h"

@implementation CsIPay88

UIView *paymentView;
NSString *callbackId;
bool paymentInProgress;

// Helper
- (void)sendResult: (CDVPluginResult*)result
{
  paymentInProgress = false;
  [self.commandDelegate sendPluginResult:result callbackId:callbackId];
  [paymentView release];
  [callbackId release];
}

// makepayment entry point
- (void)makepayment: (CDVInvokedUrlCommand*)command
{
  callbackId = [command callbackId];
  [callbackId retain];

  if(paymentInProgress) {
    [self sendResult: [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"A payment is already in progress."]];
    return;
  }
  paymentInProgress = true;

  NSDictionary *args = (NSDictionary*) [command argumentAtIndex:0 withDefault:nil andClass:[NSDictionary class]];
  if(args == nil) {
    [self sendResult: [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Argument must be an object."]];
    return;
  }
  
  // Mandatory arguments
  NSNumber *amount = [args objectForKey:@"amount"];
  NSString *name = [args objectForKey:@"name"];
  NSString *email = [args objectForKey:@"email"];
  NSString *phone = [args objectForKey:@"phone"];
  NSString *refNo = [args objectForKey:@"refNo"];
  NSString *currency = [args objectForKey:@"currency"];
  NSString *country = [args objectForKey:@"country"];
  NSString *description = [args objectForKey:@"description"];
  NSString *remark = [args objectForKey:@"remark"];
  NSString *paymentId = [args objectForKey:@"paymentId"];
  NSString *lang = [args objectForKey:@"lang"];
  NSString *merchantKey = [args objectForKey:@"merchantKey"];
  NSString *merchantCode = [args objectForKey:@"merchantCode"];
  if(amount == nil || name == nil || email == nil || phone == nil || refNo == nil
    || currency == nil || country == nil || description == nil || remark == nil || paymentId == nil
    || lang == nil || merchantKey == nil || merchantCode == nil
  ) {
    [self sendResult: [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Required arguments are missing."]];
    return;
  }
  
  NSString *formattedAmount = [NSString stringWithFormat:@"%.2f", [amount doubleValue]/100.0];
  
  // Setup iPay88 payment object.
  IpayPayment *payment = [[IpayPayment alloc] init];
  [payment setPaymentId:paymentId];
  [payment setMerchantKey:merchantKey];
  [payment setMerchantCode:merchantCode];
  [payment setRefNo:refNo];
  [payment setAmount:formattedAmount];
  [payment setCurrency:currency];
  [payment setProdDesc:description];
  [payment setUserName:name];
  [payment setUserEmail:email];
  [payment setUserContact:phone];
  [payment setRemark:remark];
  [payment setLang:lang];
  [payment setCountry:country];
  
  // Create iPay88 View.
  Ipay *paymentsdk = [[Ipay alloc] init];
  paymentsdk.delegate = self;
  paymentView = [paymentsdk checkout:payment];
  [paymentView retain];
  
  // Transfer control to iPay88 View.
  [self.webView addSubview:paymentView];
}


/** iPay88 Result Delegate **/

- (void)paymentSuccess:(NSString *)refNo withTransId:(NSString *)transId withAmount:(NSString *)amount withRemark:(NSString *)remark withAuthCode:(NSString *)authCode
{
  [paymentView removeFromSuperview];
  NSArray *keys = [NSArray arrayWithObjects:@"transactionId", @"referenceNo", @"amount", @"remarks", @"authCode", nil];
  NSArray *objects = [NSArray arrayWithObjects:transId, refNo, amount, remark, authCode, nil];
  NSDictionary *result = [NSDictionary dictionaryWithObjects:objects forKeys:keys];
  [self sendResult: [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:result]];
}

- (void)paymentFailed:(NSString *)refNo withTransId:(NSString *)transId withAmount:(NSString *)amount withRemark:(NSString *)remark withErrDesc:(NSString *)errDesc
{
  [paymentView removeFromSuperview];
  NSArray *keys = [NSArray arrayWithObjects:@"transactionId", @"referenceNo", @"amount", @"remarks", @"err", nil];
  NSArray *objects = [NSArray arrayWithObjects:transId, refNo, amount, remark, errDesc, nil];
  NSDictionary *result = [NSDictionary dictionaryWithObjects:objects forKeys:keys];
  [self sendResult: [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:result]];
}

- (void)paymentCancelled:(NSString *)refNo withTransId:(NSString *)transId withAmount:(NSString *)amount withRemark:(NSString *)remark withErrDesc:(NSString *)errDesc
{
  [paymentView removeFromSuperview];
  NSArray *keys = [NSArray arrayWithObjects:@"transactionId", @"referenceNo", @"amount", @"remarks", @"err", nil];
  NSArray *objects = [NSArray arrayWithObjects:transId, refNo, amount, remark, @"canceled", nil];
  NSDictionary *result = [NSDictionary dictionaryWithObjects:objects forKeys:keys];
  [self sendResult: [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:result]];
}

@end

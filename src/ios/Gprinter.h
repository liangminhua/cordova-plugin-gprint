#import <Cordova/CDV.h>
#import "TscCommand.h"
#import "BLKWrite.h"
#import "EscCommand.h"

@interface Gprinter : CDVPlugin {
  // Member variables go here.
}

- (void)initService:(CDVInvokedUrlCommand*)command;
- (void)openPort:(CDVInvokedUrlCommand*)command;
- (void)closePort:(CDVInvokedUrlCommand*)command;
- (void)printTestPage:(CDVInvokedUrlCommand*)command;
- (void)queryPrinterStatus:(CDVInvokedUrlCommand*)command;
- (void)getPrinterCommandType:(CDVInvokedUrlCommand*)command;
- (void)getCommand:(CDVInvokedUrlCommand*)command;
- (void)getTscCommand:(CDVInvokedUrlCommand*)command;
- (void)sendEscCommand:(CDVInvokedUrlCommand*)command;
- (void)sendTscCommand:(CDVInvokedUrlCommand*)command;

@end
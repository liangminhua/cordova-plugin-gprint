/********* Gprinter.m Cordova Plugin Implementation *******/
#import "Gprinter.h"
#import <Cordova/CDV.h>

@implementation Gprinter

- (void)initService:(CDVInvokedUrlCommand*)command
{
    CDVPluginResult* pluginResult = nil;
    //NSString* echo = [command.arguments objectAtIndex:0];

    if (true) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"haode"];
    } else {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
    }

    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}
-(void)openPort:(CDVInvokedUrlCommand *)command{
    CDVPluginResult* pluginResult=nil;
    
}
-(void)closePort:(CDVInvokedUrlCommand *)command{
    CDVPluginResult* pluginResult=nil;
    
}
-(void)printTestPage:(CDVInvokedUrlCommand *)command{
    CDVPluginResult* pluginResult=nil;
    
}
-(void)queryPrinterStatus:(CDVInvokedUrlCommand *)command{
    CDVPluginResult* pluginResult=nil;
    
}
-(void)getPrinterCommandType:(CDVInvokedUrlCommand *)command{
    CDVPluginResult* pluginResult=nil;
    
}
-(void)getCommand:(CDVInvokedUrlCommand *)command{
    CDVPluginResult* pluginResult=nil;
    
}
-(void)getTscCommand:(CDVInvokedUrlCommand *)command{
    CDVPluginResult* pluginResult=nil;
    
}
-(void)sendEscCommand:(CDVInvokedUrlCommand *)command{
    CDVPluginResult* pluginResult=nil;
    
}
-(void)sendTscCommand:(CDVInvokedUrlCommand *)command{
    CDVPluginResult* pluginResult=nil;
    
}


@end

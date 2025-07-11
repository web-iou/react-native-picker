//
//  RCTBEEPickerManager.m
//  RCTBEEPickerManager
//
//  Created by MFHJ-DZ-001-417 on 16/9/6.
//  Copyright © 2016年 MFHJ-DZ-001-417. All rights reserved.
//

#import "RCTBEEPickerManager.h"
#import "BzwPicker.h"
#import <React/RCTEventDispatcher.h>

@interface RCTBEEPickerManager()

@property(nonatomic,strong)BzwPicker *pick;
@property(nonatomic,assign)float height;
@property(nonatomic,weak)UIWindow * window;

@end

@implementation RCTBEEPickerManager

@synthesize bridge = _bridge;

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}

+ (BOOL)requiresMainQueueSetup
{
    return YES;
}

RCT_EXPORT_MODULE();

RCT_EXPORT_METHOD(_init:(NSDictionary *)indic){

    dispatch_async(dispatch_get_main_queue(), ^{
        [[UIApplication sharedApplication].keyWindow endEditing:YES];
    });

    self.window = [UIApplication sharedApplication].keyWindow;

    NSString *pickerConfirmBtnText=indic[@"pickerConfirmBtnText"];
    NSString *pickerCancelBtnText=indic[@"pickerCancelBtnText"];
    NSString *pickerTitleText=indic[@"pickerTitleText"];
    NSArray *pickerConfirmBtnColor=indic[@"pickerConfirmBtnColor"];
    NSArray *pickerCancelBtnColor=indic[@"pickerCancelBtnColor"];
    NSArray *pickerTitleColor=indic[@"pickerTitleColor"];
    NSArray *pickerToolBarBg=indic[@"pickerToolBarBg"];
    NSArray *pickerBg=indic[@"pickerBg"];
    NSArray *selectArry=indic[@"selectedValue"];
    NSArray *weightArry=indic[@"wheelFlex"];
    NSString *pickerToolBarFontSize=[NSString stringWithFormat:@"%@",indic[@"pickerToolBarFontSize"]];
    NSString *pickerFontSize=[NSString stringWithFormat:@"%@",indic[@"pickerFontSize"]];
    NSString *pickerFontFamily=[NSString stringWithFormat:@"%@",indic[@"pickerFontFamily"]];
    NSArray *pickerFontColor=indic[@"pickerFontColor"];
    NSString *pickerRowHeight=indic[@"pickerRowHeight"];
    id pickerData=indic[@"pickerData"];

    NSMutableDictionary *dataDic=[[NSMutableDictionary alloc]init];

    dataDic[@"pickerData"]=pickerData;

    [self.window.subviews enumerateObjectsUsingBlock:^(__kindof UIView * _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {

        if ([obj isKindOfClass:[BzwPicker class]]) {
            dispatch_async(dispatch_get_main_queue(), ^{

                [obj removeFromSuperview];
            });
        }

    }];

    self.height=300;
    
    self.pick=[[BzwPicker alloc]initWithFrame:CGRectMake(0, SCREEN_HEIGHT, SCREEN_WIDTH, self.height) dic:dataDic leftStr:pickerCancelBtnText centerStr:pickerTitleText rightStr:pickerConfirmBtnText topbgColor:pickerToolBarBg bottombgColor:pickerBg leftbtnbgColor:pickerCancelBtnColor rightbtnbgColor:pickerConfirmBtnColor centerbtnColor:pickerTitleColor selectValueArry:selectArry weightArry:weightArry pickerToolBarFontSize:pickerToolBarFontSize pickerFontSize:pickerFontSize pickerFontColor:pickerFontColor  pickerRowHeight: pickerRowHeight pickerFontFamily:pickerFontFamily];
    
    _pick.bolock=^(NSDictionary *backinfoArry){

        dispatch_async(dispatch_get_main_queue(), ^{

            [self.bridge.eventDispatcher sendAppEventWithName:@"pickerEvent" body:backinfoArry];
        });
    };

    dispatch_async(dispatch_get_main_queue(), ^{

        [self.window addSubview:_pick];
    });

}

RCT_EXPORT_METHOD(show){
    if (self.pick) {
        [self.pick showPickerWithAnimation];
    }return;
}

RCT_EXPORT_METHOD(hide){

    if (self.pick) {
        [self.pick hidePickerWithAnimation];
    }

    return;
}

RCT_EXPORT_METHOD(select: (NSArray*)data){

    if (self.pick) {
        dispatch_async(dispatch_get_main_queue(), ^{
            _pick.selectValueArry = data;
            [_pick selectRow];
        });
    }return;
}

RCT_EXPORT_METHOD(isPickerShow:(RCTResponseSenderBlock)getBack){

    if (self.pick) {

        CGFloat pickY=_pick.pickerContainer.frame.origin.y;

        if (pickY==SCREEN_HEIGHT) {

            getBack(@[@NO]);
        }else
        {
            getBack(@[@YES]);
        }
    }else{
        getBack(@[@"picker不存在"]);
    }
}

@end

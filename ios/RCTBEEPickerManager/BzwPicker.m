//
//  BzwPicker.m
//  PickerView
//
//  Created by Bao on 15/12/14.
//  Copyright © 2015年 Microlink. All rights reserved.
//

#import "BzwPicker.h"
#define linSpace 5

@implementation BzwPicker

-(instancetype)initWithFrame:(CGRect)frame dic:(NSDictionary *)dic leftStr:(NSString *)leftStr centerStr:(NSString *)centerStr rightStr:(NSString *)rightStr topbgColor:(NSArray *)topbgColor bottombgColor:(NSArray *)bottombgColor leftbtnbgColor:(NSArray *)leftbtnbgColor rightbtnbgColor:(NSArray *)rightbtnbgColor centerbtnColor:(NSArray *)centerbtnColor selectValueArry:(NSArray *)selectValueArry  weightArry:(NSArray *)weightArry
       pickerToolBarFontSize:(NSString *)pickerToolBarFontSize  pickerFontSize:(NSString *)pickerFontSize  pickerFontColor:(NSArray *)pickerFontColor pickerRowHeight:(NSString *)pickerRowHeight pickerFontFamily:(NSString *)pickerFontFamily emptyText:(NSString *)emptyText

{
    self = [super initWithFrame:CGRectMake(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT)];
    if (self)
    {
        self.userInteractionEnabled=NO;
        self.backArry=[[NSMutableArray alloc]init];
        self.provinceArray=[[NSMutableArray alloc]init];
        self.cityArray=[[NSMutableArray alloc]init];
        self.selectValueArry=selectValueArry;
        self.weightArry=weightArry;
        self.pickerDic=dic;
        self.leftStr=leftStr;
        self.rightStr=rightStr;
        self.centStr=centerStr;
        self.pickerToolBarFontSize=pickerToolBarFontSize;
        self.pickerFontSize=pickerFontSize;
        self.pickerFontFamily=pickerFontFamily;
        self.pickerFontColor=pickerFontColor;
        self.pickerRowHeight=pickerRowHeight;
        self.emptyText = emptyText ?: @"NO DATA"; // 默认值为"NO DATA"
        
        // 创建modal背景遮罩
        [self createModalBackground];
        
        [self getStyle];
        
        // 检查数据是否为空
        if ([self isPickerDataEmpty]) {
            // 如果数据为空，显示空状态
            dispatch_async(dispatch_get_main_queue(), ^{
                [self createEmptyStateViewWithFrame:frame
                                        topbgColor:topbgColor
                                      bottombgColor:bottombgColor
                                      leftbtnbgColor:leftbtnbgColor
                                     rightbtnbgColor:rightbtnbgColor
                                      centerbtnColor:centerbtnColor];
            });
        } else {
            // 如果数据不为空，正常显示picker
            [self getnumStyle];
            dispatch_async(dispatch_get_main_queue(), ^{
                [self makeuiWith:topbgColor With:bottombgColor With:leftbtnbgColor With:rightbtnbgColor With:centerbtnColor withFrame:frame];
                [self selectRow];
            });
        }
    }
    return self;
}
// 创建modal背景遮罩
-(void)createModalBackground
{
    self.modalBackgroundView = [[UIView alloc] initWithFrame:self.bounds];
    self.modalBackgroundView.backgroundColor = [UIColor colorWithRed:0 green:0 blue:0 alpha:0.5];
    self.modalBackgroundView.alpha = 0.0; // 初始状态为透明
    [self addSubview:self.modalBackgroundView];
    
    // 添加点击手势，点击背景关闭picker
    UITapGestureRecognizer *tapGesture = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(backgroundTapped)];
    [self.modalBackgroundView addGestureRecognizer:tapGesture];
}

// 点击背景关闭picker
-(void)backgroundTapped
{
    [self cancleAction];
}

-(void)makeuiWith:(NSArray *)topbgColor With:(NSArray *)bottombgColor With:(NSArray *)leftbtnbgColor With:(NSArray *)rightbtnbgColor With:(NSArray *)centerbtnColor withFrame:(CGRect)pickerFrame
{
    // 创建picker容器
    self.pickerContainer = [[UIView alloc] initWithFrame:pickerFrame];
    self.pickerContainer.backgroundColor = [UIColor clearColor];
    // 确保picker容器在背景遮罩之上
    [self insertSubview:self.pickerContainer aboveSubview:self.modalBackgroundView];
    
    UIView *view = [[UIView alloc] initWithFrame:CGRectMake(0,0, pickerFrame.size.width, 40)];
    view.backgroundColor = [self colorWith:topbgColor];
    [self.pickerContainer addSubview:view];
    
    self.leftBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    self.leftBtn.frame = CGRectMake(0, 0, 90, 40);
    self.leftBtn.font = [UIFont fontWithName:_pickerFontFamily size:[_pickerToolBarFontSize integerValue]];
    self.leftBtn.contentHorizontalAlignment = UIControlContentHorizontalAlignmentLeft;
    [self.leftBtn setTitleEdgeInsets:UIEdgeInsetsMake(0, 10.0, 0, 0)];
    [self.leftBtn setTitle:self.leftStr forState:UIControlStateNormal];
    [self.leftBtn setTitleColor:[self colorWith:leftbtnbgColor] forState:UIControlStateNormal];
    [self.leftBtn addTarget:self action:@selector(cancleAction) forControlEvents:UIControlEventTouchUpInside];
    [view addSubview:self.leftBtn];
    
    self.rightBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    self.rightBtn.frame = CGRectMake(view.frame.size.width-90,0, 90, 40);
    self.rightBtn.font = [UIFont fontWithName:_pickerFontFamily size:[_pickerToolBarFontSize integerValue]];
    self.rightBtn.contentHorizontalAlignment=UIControlContentHorizontalAlignmentRight;
    [self.rightBtn setTitleEdgeInsets:UIEdgeInsetsMake(0, 0, 0, 10.0)];
    [self.rightBtn setTitle:self.rightStr forState:UIControlStateNormal];
    [self.rightBtn setTitleColor:[self colorWith:rightbtnbgColor] forState:UIControlStateNormal];
    [self.rightBtn addTarget:self action:@selector(cfirmAction) forControlEvents:UIControlEventTouchUpInside];  
    [view addSubview:self.rightBtn];
    
    UILabel *cenLabel=[[UILabel alloc]initWithFrame:CGRectMake(90, 5, pickerFrame.size.width-180, 30)];
    cenLabel.text=self.centStr;
    cenLabel.textAlignment=NSTextAlignmentCenter;
    cenLabel.font = [UIFont fontWithName:_pickerFontFamily size:[_pickerToolBarFontSize integerValue]];
    [cenLabel setTextColor:[self colorWith:centerbtnColor]];
    [view addSubview:cenLabel];

    self.pick = [[UIPickerView alloc] initWithFrame:CGRectMake(0, 40, pickerFrame.size.width, pickerFrame.size.height - 40)];
    self.pick.delegate = self;
    self.pick.dataSource = self;
    self.pick.showsSelectionIndicator=YES;
    [self.pickerContainer addSubview:self.pick];
    
    self.pick.backgroundColor=[self colorWith:bottombgColor];
}
//返回显示的列数
-(NSInteger)numberOfComponentsInPickerView:(UIPickerView *)pickerView
{
    if (_Correlation) {
        //这里是关联的
        if ([_numberCorrela isEqualToString:@"three"]) {
            return 3;
            
        }else if ([_numberCorrela isEqualToString:@"two"]){
            
            return 2;
        }
        
    }
    //这里是不关联的
    if (_noArryElementBool) {
        
        return 1;
        
    }else{
        
        return self.noCorreArry.count;
    }
}
//返回当前列显示的行数
-(NSInteger)pickerView:(UIPickerView *)pickerView numberOfRowsInComponent:(NSInteger)component
{
    if (_Correlation) {
        
        if (component == 0) {
            
            return self.provinceArray.count;
            
        } else if (component == 1) {
            
            return self.cityArray.count;
            
        } else {
            
            return self.townArray.count;
        }
    }
    
    //NSLog(@"%@",[self.noCorreArry objectAtIndex:component]);
    
    if (self.noCorreArry.count==1) {
        
        return [self.noCorreArry count];
        
    }else
    {
        
        if (_noArryElementBool) {
            
            return [self.noCorreArry count];
            
        }
        
        return  [[self.noCorreArry objectAtIndex:component] count];
    }
    
}

#pragma mark Picker Delegate Methods

//返回当前行的内容,此处是将数组中数值添加到滚动的那个显示栏上
-(NSString*)pickerView:(UIPickerView *)pickerView titleForRow:(NSInteger)row forComponent:(NSInteger)component
{
    if (_Correlation) {
        
        if (component == 0) {
            
            return [NSString stringWithFormat:@"%@",[self.provinceArray objectAtIndex:row]];
            
        } else if (component == 1) {
            
            return [NSString stringWithFormat:@"%@",[self.cityArray objectAtIndex:row]];
        } else {
            
            return [NSString stringWithFormat:@"%@",[self.townArray objectAtIndex:row]];
        }
    }else{
        
        if (_noArryElementBool) {
            
            return [NSString stringWithFormat:@"%@",[self.noCorreArry objectAtIndex:row]];
            
        }else{
            return [NSString stringWithFormat:@"%@",[[self.noCorreArry objectAtIndex:component] objectAtIndex:row]];
        }
    }
    
}
- (CGFloat)pickerView:(UIPickerView *)pickerView widthForComponent:(NSInteger)component {
    
    if (_Correlation) {
        if ([_numberCorrela isEqualToString:@"three"]) {
            
            _lineWith=SCREEN_WIDTH-2*linSpace;
            
            if (self.weightArry.count>=3) {
                NSString *onestr=[NSString stringWithFormat:@"%@",[self.weightArry firstObject]];
                NSString *twostr=[NSString stringWithFormat:@"%@",self.weightArry[1]];
                NSString *threestr=[NSString stringWithFormat:@"%@",self.weightArry[2]];
                double totalweight=onestr.doubleValue+twostr.doubleValue+threestr.doubleValue;
                if (component==0) {
                    return _lineWith*onestr.doubleValue/totalweight;
                }else if (component==1){
                    return _lineWith*twostr.doubleValue/totalweight;
                }else{
                    return _lineWith*threestr.doubleValue/totalweight;
                }
            }else{
                if (self.weightArry.count>0) {
                    NSInteger totalNum=self.weightArry.count;
                    double totalweight=0;
                    
                    for (NSInteger i=0; i<self.weightArry.count; i++) {
                        NSString *str=[NSString stringWithFormat:@"%@",[self.weightArry objectAtIndex:i]];
                        totalweight=totalweight+str.doubleValue;
                    }
                    if (component>totalNum-1) {
                        NSString *str=[NSString stringWithFormat:@"%f",totalweight+3-totalNum];
                        return _lineWith/str.doubleValue;;
                        
                    }else{
                        
                        NSString *str=[NSString stringWithFormat:@"%f",totalweight+3-totalNum];
                        
                        return  _lineWith*[NSString stringWithFormat:@"%@",[self.weightArry objectAtIndex:component]].doubleValue/str.doubleValue;
                        
                    }
                }else{
                    return _lineWith/3;
                }
            }
        }
        else{
            
            _lineWith=SCREEN_WIDTH-linSpace;
            if (self.weightArry.count>=2) {
                NSString *onestr=[NSString stringWithFormat:@"%@",[self.weightArry firstObject]];
                NSString *twostr=[NSString stringWithFormat:@"%@",self.weightArry[1]];
                
                double totalweight=onestr.doubleValue+twostr.doubleValue;
                if (component==0) {
                    return _lineWith*onestr.doubleValue/totalweight;
                }else{
                    return _lineWith*twostr.doubleValue/totalweight;
                }
            }
            else{
                if (self.weightArry.count>0) {
                    double twonum=[NSString stringWithFormat:@"%@",[self.weightArry firstObject]].doubleValue;
                    if (component==0) {
                        
                        NSString *str=[NSString stringWithFormat:@"%f",twonum+1];
                        return _lineWith*twonum/str.doubleValue;
                        
                    }else{
                        NSString *str=[NSString stringWithFormat:@"%f",twonum+1];
                        return _lineWith/str.doubleValue;
                        
                    }
                }
                else{
                    return _lineWith/2;
                }
            }
        }
    }else{
        if (_noArryElementBool) {
            //表示一个数组 特殊情况
            return SCREEN_WIDTH;
        }else{
            
            _lineWith=(SCREEN_WIDTH-linSpace*(self.dataDry.count-1));
            
            if (self.weightArry.count>=self.dataDry.count) {
                
                double totalweight=0;
                
                for (NSInteger i=0; i<self.dataDry.count; i++) {
                    NSString *str=[NSString stringWithFormat:@"%@",[self.weightArry objectAtIndex:i]];
                    totalweight=totalweight+str.doubleValue;
                }
                NSString *comStr=[NSString stringWithFormat:@"%@",[self.weightArry objectAtIndex:component]];
                
                return _lineWith*comStr.doubleValue/totalweight;
            }else
            {
                if (self.weightArry.count>0) {
                    NSInteger totalNum=self.weightArry.count;
                    double totalweight=0;
                    for (NSInteger i=0; i<self.weightArry.count; i++) {
                        NSString *str=[NSString stringWithFormat:@"%@",[self.weightArry objectAtIndex:i]];
                        totalweight=totalweight+str.doubleValue;
                    }
                    if (component>totalNum-1) {
                        
                        NSString *str=[NSString stringWithFormat:@"%f",totalweight+self.dataDry.count-totalNum];
                        return _lineWith/str.doubleValue;
                    }else{
                        
                        NSString *str=[NSString stringWithFormat:@"%f",totalweight+self.dataDry.count-totalNum];
                        return _lineWith*[NSString stringWithFormat:@"%@",[self.weightArry objectAtIndex:component]].doubleValue/str.doubleValue;
                    }
                }else{
                    return _lineWith/self.dataDry.count;
                }
            }
        }
    }
}

- (void)pickerView:(UIPickerView *)pickerView didSelectRow:(NSInteger)row inComponent:(NSInteger)component {
    
    if (!row) {
        row=0;
    }
    [self.backArry removeAllObjects];
    [self.infoArry removeAllObjects];
    
    if (_Correlation) {
        //这里是关联的
        
        if ([_numberCorrela isEqualToString:@"three"]) {
            
            if (component == 0)
            {
                [self.cityArray removeAllObjects];
                
                NSInteger setline=[_pick selectedRowInComponent:0];
                
                if (setline) {
                    
                    self.selectthreeAry =[[self.dataDry objectAtIndex:setline]objectForKey:[self.provinceArray objectAtIndex:setline]];
                }else{
                    
                    setline=0;
                    
                    self.selectthreeAry =[[self.dataDry objectAtIndex:0] objectForKey:[self.provinceArray objectAtIndex:0]];
                }
                
                if (self.selectthreeAry) {
                    //遍历数组
                    for (NSInteger i=0; i<self.selectthreeAry.count; i++) {
                        NSDictionary *dic=self.selectthreeAry[i];
                        NSArray *ary=[dic allKeys];
                        [self.cityArray addObject:[ary firstObject]];
                    }
                }
                else
                {
                    self.cityArray = nil;
                }
                if (self.cityArray.count > 0)
                {
                    
                    self.townArray=[[self.selectthreeAry objectAtIndex:0]objectForKey:[self.cityArray objectAtIndex:0]];
                    
                }
                else
                {
                    self.townArray = nil;
                }
                [pickerView reloadAllComponents];
                [pickerView selectRow:0 inComponent:1 animated:YES];
                [pickerView selectRow:0 inComponent:2 animated:YES];
                
            }
            
            if (component == 1)
            {
                
                NSInteger setline=[_pick selectedRowInComponent:0];
                
                self.selectthreeAry =[[self.dataDry objectAtIndex:setline]objectForKey:[self.provinceArray objectAtIndex:setline]];
                
                //NSLog(@"%@",_selectthreeAry);
                if (row<self.selectthreeAry.count) {
                    self.townArray=[[self.selectthreeAry objectAtIndex:row]objectForKey:[self.cityArray objectAtIndex:row]];
                }
                
                [pickerView reloadAllComponents];
                [pickerView selectRow:0 inComponent:2 animated:YES];
                
            }
            
        }else if ([_numberCorrela isEqualToString:@"two"]){
            
            if (component == 0)
            {
                [self.cityArray removeAllObjects];
                
                self.selectArry =[[self.dataDry objectAtIndex:row]objectForKey:[self.provinceArray objectAtIndex:row]];
                
                if (self.selectArry.count>0) {
                    
                    [self.cityArray addObjectsFromArray:self.selectArry];
                }
                else
                {
                    self.cityArray = nil;
                }
            }
            [pickerView reloadComponent:1];
            if (component==1) {
                [pickerView selectRow:row inComponent:1 animated:YES];
                
            }else{
                [pickerView selectRow:0 inComponent:1 animated:YES];
            }
        }
    }
    //返回选择的值就可以了
    
    if (_Correlation) {
        
        //有关联的,里面有分两种情况
        if ([_numberCorrela isEqualToString:@"three"]) {
            NSString *a=[self.provinceArray objectAtIndex:[self.pick selectedRowInComponent:0]];
            NSString *b=[self.cityArray objectAtIndex:[self.pick selectedRowInComponent:1]];
            NSString *c=[self.townArray objectAtIndex:[self.pick selectedRowInComponent:2]];
            
            if (a&&b&&c) {
                [self.backArry addObject:a];
                [self.backArry addObject:b];
                [self.backArry addObject:c];
            }
            
        }else if ([_numberCorrela isEqualToString:@"two"]){
            
            NSString *a=[self.provinceArray objectAtIndex:[self.pick selectedRowInComponent:0]];
            NSString *b=[self.cityArray objectAtIndex:[self.pick selectedRowInComponent:1]];
            // NSLog(@"%@---%@",a,b);
            if (a&&b) {
                [self.backArry addObject:a];
                [self.backArry addObject:b];
            }
        }
        
    }else
    {
        
        if (_noArryElementBool) {
            
            [self.backArry addObject:[self.noCorreArry objectAtIndex:row]];
            
            
        }else{
            //无关联的，直接给三个选项就行
            for (NSInteger i=0; i<self.noCorreArry.count; i++) {
                
                NSArray *eachAry=self.noCorreArry[i];
                
                [self.backArry addObject:[eachAry objectAtIndex:[self.pick selectedRowInComponent:i]]];
                
            }
        }
    }
    
    NSMutableDictionary *dic=[[NSMutableDictionary alloc]init];
    [dic setValue:self.backArry forKey:@"selectedValue"];
    
    [dic setValue:@"select" forKey:@"type"];
    NSMutableArray *value = [self getselectIndexArry];
    
    if([value count] == 0) {
        value = [[NSMutableArray alloc] init];
        [dic setValue:[NSNumber numberWithInt:[_pick selectedRowInComponent:0]] forKey:@"selectedIndex"];
    } else {
        [dic setValue:value forKey:@"selectedIndex"];
    }
    
    if (self.backArry.count>0) {
        self.bolock(dic);
    }
}

- (CGFloat)pickerView:(UIPickerView *)pickerView rowHeightForComponent:(NSInteger)component
{
    return self.pickerRowHeight.integerValue;
}

//判断进来的类型是那种
-(void)getStyle
{
    
    self.dataDry=[self.pickerDic objectForKey:@"pickerData"];
    
    id firstobject=[self.dataDry firstObject];
    
    _seleNum = 1;
    
    if ([firstobject isKindOfClass:[NSArray class]]) {
        
        _seleNum=self.dataDry.count;
        
        _Correlation=NO;
        
    }else if ([firstobject isKindOfClass:[NSDictionary class]]){
        
        //_Correlation为YES的话是关联的情况 为NO的话 是不关联的情况
        _Correlation=YES;
        
        NSDictionary *dic=(NSDictionary *)firstobject;
        
        NSArray * twoOrthree=[dic allKeys];
        
        
        id scendObjct=[[dic objectForKey:[twoOrthree firstObject]] firstObject];
        
        if ([scendObjct isKindOfClass:[NSDictionary class]]) {
            
            _numberCorrela=@"three";
            _seleNum=3;
            
        }else{
            _numberCorrela=@"two";
            _seleNum=2;
        }
    }
}
-(void)getnumStyle{
    
    if (_Correlation) {
        
        //这里是关联的
        if ([_numberCorrela isEqualToString:@"three"]) {
            //省 市
            for (NSInteger i=0; i<self.dataDry.count; i++) {
                
                NSDictionary *dic=[self.dataDry objectAtIndex:i];
                
                NSArray *ary=[dic allKeys];
                if ([ary firstObject]) {
                    [self.provinceArray addObject:[ary firstObject]];
                }
            }
            
            NSDictionary *dic=[self.dataDry firstObject];
            
            NSArray *ary=[dic objectForKey:[self.provinceArray objectAtIndex:0]];
            
            
            if (self.provinceArray.count > 0) {
                
                for (NSInteger i=0; i<ary.count; i++) {
                    
                    NSDictionary *dic=[ary objectAtIndex:i];
                    
                    NSArray *ary=[dic allKeys];
                    
                    [self.cityArray addObject:[ary firstObject]];
                    
                }
            }
            
            if (self.cityArray.count > 0) {
                
                NSDictionary *dic=[ary firstObject];
                
                self.townArray=[dic objectForKey:[self.cityArray firstObject]];
                
            }
        }else if ([_numberCorrela isEqualToString:@"two"]){
            
            for (NSInteger i=0; i<self.dataDry.count; i++) {
                
                NSDictionary *dic=[self.dataDry objectAtIndex:i];
                
                NSArray *ary=[dic allKeys];
                
                [self.provinceArray addObject:[ary firstObject]];
            }
            [self.cityArray addObjectsFromArray:[[self.dataDry objectAtIndex:0] objectForKey:[self.provinceArray objectAtIndex:0]]];
        }
    }else
    {
        //这里是不关联的
        self.noCorreArry=self.dataDry;
        
        id noArryElement=[self.dataDry firstObject];
        
        if ([noArryElement isKindOfClass:[NSArray class]]) {
            
            _noArryElementBool=NO;
            
        }else{
            //这里为yes表示里面就就一行数据 表示的是只有一行的特殊情况
            _noArryElementBool=YES;
        }
    }
}

// 检查picker数据是否为空
-(BOOL)isPickerDataEmpty
{
    self.dataDry = [self.pickerDic objectForKey:@"pickerData"];
    
    if (!self.dataDry || self.dataDry.count == 0) {
        return YES;
    }
    
    // 检查数据内容是否为空
    for (id item in self.dataDry) {
        if ([item isKindOfClass:[NSArray class]]) {
            NSArray *array = (NSArray *)item;
            if (array.count > 0) {
                return NO;
            }
        } else if ([item isKindOfClass:[NSDictionary class]]) {
            NSDictionary *dict = (NSDictionary *)item;
            if (dict.count > 0) {
                return NO;
            }
        } else {
            // 如果是其他类型的数据，认为不为空
            return NO;
        }
    }
    
    return YES;
}

// 创建空状态视图
-(void)createEmptyStateViewWithFrame:(CGRect)pickerFrame
                          topbgColor:(NSArray *)topbgColor
                        bottombgColor:(NSArray *)bottombgColor
                        leftbtnbgColor:(NSArray *)leftbtnbgColor
                       rightbtnbgColor:(NSArray *)rightbtnbgColor
                        centerbtnColor:(NSArray *)centerbtnColor
{
    // 创建picker容器
    self.pickerContainer = [[UIView alloc] initWithFrame:pickerFrame];
    self.pickerContainer.backgroundColor = [UIColor clearColor];
    // 确保picker容器在背景遮罩之上
    [self insertSubview:self.pickerContainer aboveSubview:self.modalBackgroundView];
    
    // 创建顶部工具栏
    UIView *view = [[UIView alloc] initWithFrame:CGRectMake(0, 0, pickerFrame.size.width, 40)];
    view.backgroundColor = [self colorWith:topbgColor];
    [self.pickerContainer addSubview:view];
    
    // 取消按钮
    self.leftBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    self.leftBtn.frame = CGRectMake(0, 0, 90, 40);
    self.leftBtn.font = [UIFont fontWithName:_pickerFontFamily size:[_pickerToolBarFontSize integerValue]];
    self.leftBtn.contentHorizontalAlignment = UIControlContentHorizontalAlignmentLeft;
    [self.leftBtn setTitleEdgeInsets:UIEdgeInsetsMake(0, 10.0, 0, 0)];
    [self.leftBtn setTitle:self.leftStr forState:UIControlStateNormal];
    [self.leftBtn setTitleColor:[self colorWith:leftbtnbgColor] forState:UIControlStateNormal];
    [self.leftBtn addTarget:self action:@selector(cancleAction) forControlEvents:UIControlEventTouchUpInside];
    [view addSubview:self.leftBtn];
    
    // 确认按钮
    self.rightBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    self.rightBtn.frame = CGRectMake(view.frame.size.width - 90, 0, 90, 40);
    self.rightBtn.font = [UIFont fontWithName:_pickerFontFamily size:[_pickerToolBarFontSize integerValue]];
    self.rightBtn.contentHorizontalAlignment = UIControlContentHorizontalAlignmentRight;
    [self.rightBtn setTitleEdgeInsets:UIEdgeInsetsMake(0, 0, 0, 10.0)];
    [self.rightBtn setTitle:self.rightStr forState:UIControlStateNormal];
    [self.rightBtn setTitleColor:[self colorWith:rightbtnbgColor] forState:UIControlStateNormal];
    [self.rightBtn addTarget:self action:@selector(cfirmAction) forControlEvents:UIControlEventTouchUpInside];
    [view addSubview:self.rightBtn];
    
    // 标题标签
    UILabel *cenLabel = [[UILabel alloc] initWithFrame:CGRectMake(90, 5, pickerFrame.size.width - 180, 30)];
    cenLabel.text = self.centStr;
    cenLabel.textAlignment = NSTextAlignmentCenter;
    cenLabel.font = [UIFont fontWithName:_pickerFontFamily size:[_pickerToolBarFontSize integerValue]];
    [cenLabel setTextColor:[self colorWith:centerbtnColor]];
    [view addSubview:cenLabel];
    
    // 创建空状态内容区域
    UIView *emptyContentView = [[UIView alloc] initWithFrame:CGRectMake(0, 40, pickerFrame.size.width, pickerFrame.size.height - 40)];
    emptyContentView.backgroundColor = [self colorWith:bottombgColor];
    [self.pickerContainer addSubview:emptyContentView];
    
    // 创建居中的空状态文字标签
    UILabel *emptyLabel = [[UILabel alloc] init];
    emptyLabel.text = self.emptyText;
    emptyLabel.textAlignment = NSTextAlignmentCenter;
    emptyLabel.font = [UIFont fontWithName:_pickerFontFamily size:[_pickerFontSize integerValue]];
    emptyLabel.textColor = [self colorWith:_pickerFontColor];
    emptyLabel.numberOfLines = 0;
    
    // 设置约束，使标签在容器中垂直水平居中
    emptyLabel.translatesAutoresizingMaskIntoConstraints = NO;
    [emptyContentView addSubview:emptyLabel];
    
    // 添加约束
    [NSLayoutConstraint activateConstraints:@[
        [emptyLabel.centerXAnchor constraintEqualToAnchor:emptyContentView.centerXAnchor],
        [emptyLabel.centerYAnchor constraintEqualToAnchor:emptyContentView.centerYAnchor],
        [emptyLabel.leadingAnchor constraintGreaterThanOrEqualToAnchor:emptyContentView.leadingAnchor constant:20],
        [emptyLabel.trailingAnchor constraintLessThanOrEqualToAnchor:emptyContentView.trailingAnchor constant:-20]
    ]];
}

//按了取消按钮
-(void)cancleAction
{
    NSMutableDictionary *dic=[[NSMutableDictionary alloc]init];
    
    // 检查是否为空状态
    if ([self isPickerDataEmpty]) {
        // 空状态下的处理
        [dic setValue:@[] forKey:@"selectedValue"];
        [dic setValue:@"cancel" forKey:@"type"];
        [dic setValue:@[] forKey:@"selectedIndex"];
        self.bolock(dic);
    } else {
        // 正常状态下的处理
        if (self.backArry.count>0) {
            [dic setValue:self.backArry forKey:@"selectedValue"];
            [dic setValue:@"cancel" forKey:@"type"];
            
            [dic setValue:[self getselectIndexArry] forKey:@"selectedIndex"];
            
            self.bolock(dic);
        }else{
            [self getNOselectinfo];
            
            [dic setValue:self.backArry forKey:@"selectedValue"];
            [dic setValue:@"cancel" forKey:@"type"];
            [dic setValue:[self getselectIndexArry] forKey:@"selectedIndex"];
            self.bolock(dic);
        }
    }
    
    [self hidePickerWithAnimation];
}
//按了确定按钮
-(void)cfirmAction
{
    //判断当前是否在滚动选择 如果是 则禁用确定按钮
    if (self.pick && [self anySubViewScrolling:self.pick]) {
        return ;
    }
    
    NSMutableDictionary *dic=[[NSMutableDictionary alloc]init];
    
    // 检查是否为空状态
    if ([self isPickerDataEmpty]) {
        // 空状态下的处理
        [dic setValue:@[] forKey:@"selectedValue"];
        [dic setValue:@"confirm" forKey:@"type"];
        [dic setValue:@[] forKey:@"selectedIndex"];
        self.bolock(dic);
    } else {
        // 正常状态下的处理
        if (self.backArry.count>0) {
            
            [dic setValue:self.backArry forKey:@"selectedValue"];
            [dic setValue:@"confirm" forKey:@"type"];
            NSMutableArray *arry=[[NSMutableArray alloc]init];
            [dic setValue:[self getselectIndexArry] forKey:@"selectedIndex"];
    //        [dic setValue:arry forKey:@"selectedIndex"];
            
            self.bolock(dic);
            
        }else{
            [self getNOselectinfo];
            [dic setValue:self.backArry forKey:@"selectedValue"];
            [dic setValue:@"confirm" forKey:@"type"];
            
            [dic setValue:[self getselectIndexArry] forKey:@"selectedIndex"];
            
            self.bolock(dic);
        }
    }
    
    [self hidePickerWithAnimation];
}
-(void)selectRow
{
    if (_Correlation) {
        //关联的一开始的默认选择行数
        if ([_numberCorrela isEqualToString:@"three"]) {
            
            [self selectValueThree];
            
        }else if ([_numberCorrela isEqualToString:@"two"]){
            
            [self selectValueTwo];
        }
    }else{
        //一行的时候
        [self selectValueOne];
    }
}
//三行时候的选择哪个的逻辑
-(void)selectValueThree
{
    NSString *selectStr=[NSString stringWithFormat:@"%@",self.selectValueArry.firstObject];
    
    for (NSInteger i=0; i<self.provinceArray.count; i++) {
        NSString *str=[NSString stringWithFormat:@"%@",[self.provinceArray objectAtIndex:i]];
        if ([selectStr isEqualToString:str]) {
            _num=i;
            [_pick reloadAllComponents];
            
            [_pick selectRow:i  inComponent:0 animated:NO];
            break;
        }
    }
    
    NSArray *selecityAry = [[self.dataDry objectAtIndex:_num] objectForKey:selectStr];
    
    if (selecityAry.count>0) {
        
        [self.cityArray removeAllObjects];
        
        for (NSInteger i=0; i<selecityAry.count; i++) {
            
            NSDictionary *dic=selecityAry[i];
            
            NSArray *ary=[dic allKeys];
            
            [self.cityArray addObject:[ary firstObject]];
        }
    }
    
    NSString *selectStrTwo;
    
    if (self.selectValueArry.count>1) {
        
        selectStrTwo=[NSString stringWithFormat:@"%@",self.selectValueArry[1]];
    }
    for (NSInteger i=0; i<self.cityArray.count; i++) {
        
        NSString *str=[NSString stringWithFormat:@"%@",[self.cityArray objectAtIndex:i]];
        if ([selectStrTwo isEqualToString:str]) {
            
            _threenum=i;
            
            [_pick reloadAllComponents];
            
            [_pick selectRow:i  inComponent:1 animated:NO];
            
            break;
        }
    }
    
    if (selecityAry.count>0) {
        
        if (self.selectValueArry.count>1) {
            
            NSArray *arry =[[selecityAry objectAtIndex:_threenum] objectForKey:[self.selectValueArry objectAtIndex:1]];
            
            self.townArray=arry;
            
        }
    }
    
    NSString *selectStrThree;
    
    
    if (self.selectValueArry.count>2) {
        selectStrThree=[NSString stringWithFormat:@"%@",self.selectValueArry[2]];
    }
    
    if (self.townArray.count>0) {
        
        for (NSInteger i=0; i<self.townArray.count; i++) {
            
            NSString *str=[NSString stringWithFormat:@"%@",[self.townArray objectAtIndex:i]];
            if ([selectStrThree isEqualToString:str]) {
                
                [_pick reloadAllComponents];
                
                [_pick selectRow:i  inComponent:2 animated:NO];
                
                break;
            }
        }
    }else
    {
        NSArray *threekey=[[selecityAry objectAtIndex:0]allKeys];
        self.townArray=[[selecityAry objectAtIndex:0]objectForKey:[threekey firstObject]];
    }
    [_pick reloadAllComponents];
}
//两行时候的选择哪个的逻辑
-(void)selectValueTwo
{
    
    NSString *selectStr=[NSString stringWithFormat:@"%@",self.selectValueArry.firstObject];
    
    for (NSInteger i=0; i<self.provinceArray.count; i++) {
        NSString *str=[NSString stringWithFormat:@"%@",[self.provinceArray objectAtIndex:i]];
        if ([selectStr isEqualToString:str]) {
            
            [_pick reloadAllComponents];
            [_pick selectRow:i  inComponent:0 animated:NO];
            _num=i;
            break;
        }
    }
    NSArray *twoArry=[[self.dataDry objectAtIndex:_num]objectForKey:selectStr];
    
    if (twoArry&&twoArry.count>0) {
        
        [self.cityArray removeAllObjects];
        [self.cityArray addObjectsFromArray:twoArry];
    }
    
    NSString *selectTwoStr;
    if (self.selectValueArry.count>1) {
        
        selectTwoStr =[NSString stringWithFormat:@"%@",[self.selectValueArry objectAtIndex:1]];
    }
    
    for (NSInteger i=0; i<self.cityArray.count; i++) {
        
        NSString *str=[NSString stringWithFormat:@"%@",[self.cityArray objectAtIndex:i]];
        
        if ([selectTwoStr isEqualToString:str]) {
            
            [_pick reloadAllComponents];
            [_pick selectRow:i inComponent:1 animated:NO];
            
            break;
        }
    }
}
//一行时候的选择哪个的逻辑
-(void)selectValueOne
{
    if (_noArryElementBool) {
        //这里表示数组里面就只有一个数组 比较特殊的情况[]
        NSString *selectStr;
        if (self.selectValueArry.count>0) {
            
            selectStr=[NSString stringWithFormat:@"%@",[self.selectValueArry firstObject]];
        }
        for (NSInteger i=0; i<self.noCorreArry.count; i++) {
            NSString *str=[NSString stringWithFormat:@"%@",[self.noCorreArry objectAtIndex:i]];
            if ([selectStr isEqualToString:str]) {
                [_pick reloadAllComponents];
                [_pick selectRow:i  inComponent:0 animated:NO];
                break;
            }
        }
        
    }else{
        //这里就比较复杂了 [[],[],[]]
        if (self.selectValueArry.count>0) {
            
            if (self.selectValueArry.count>self.noCorreArry.count) {
                
                for (NSInteger i=0; i<self.noCorreArry.count; i++) {
                    
                    NSString *selectStr=[NSString stringWithFormat:@"%@",[self.selectValueArry objectAtIndex:i]];
                    
                    NSArray *arry=[self.noCorreArry objectAtIndex:i];
                    
                    for (NSInteger j=0; j<arry.count; j++) {
                        
                        NSString *str=[NSString stringWithFormat:@"%@",[arry objectAtIndex:j]];
                        
                        if ([selectStr isEqualToString:str]) {
                            [_pick reloadAllComponents];
                            [_pick selectRow:j inComponent:i animated:YES];
                            
                            break;
                        }
                    }
                }
            }else{
                for (NSInteger i=0; i<self.selectValueArry.count; i++) {
                    
                    NSString *selectStr=[NSString stringWithFormat:@"%@",[self.selectValueArry objectAtIndex:i]];
                    
                    NSArray *arry=[self.noCorreArry objectAtIndex:i];
                    
                    for (NSInteger j=0; j<arry.count; j++) {
                        
                        NSString *str=[NSString stringWithFormat:@"%@",[arry objectAtIndex:j]];
                        
                        if ([selectStr isEqualToString:str]) {
                            [_pick reloadAllComponents];
                            [_pick selectRow:j inComponent:i animated:YES];
                            
                            break;
                        }
                    }
                }
            }
        }
    }
}
-(void)getNOselectinfo
{
    if (_Correlation) {
        
        //有关联的,里面有分两种情况
        if ([_numberCorrela isEqualToString:@"three"]) {
            NSString *a=[self.provinceArray objectAtIndex:[self.pick selectedRowInComponent:0]];
            NSString *b=[self.cityArray objectAtIndex:[self.pick selectedRowInComponent:1]];
            NSString *c=[self.townArray objectAtIndex:[self.pick selectedRowInComponent:2]];
            
            [self.backArry addObject:a];
            [self.backArry addObject:b];
            [self.backArry addObject:c];
            
        }else if ([_numberCorrela isEqualToString:@"two"]){
            
            NSString *a=[self.provinceArray objectAtIndex:[self.pick selectedRowInComponent:0]];
            NSString *b=[self.cityArray objectAtIndex:[self.pick selectedRowInComponent:1]];
            //NSLog(@"%@---%@",a,b);
            [self.backArry addObject:a];
            [self.backArry addObject:b];
        }
        
    }else
    {
        
        if (_noArryElementBool) {
            
            if (self.selectValueArry.count>0) {
                NSString *selectStr=[NSString stringWithFormat:@"%@",[self.selectValueArry firstObject]];
                [self.backArry addObject:selectStr];
            }else{
                
                [self.backArry addObject:[self.noCorreArry objectAtIndex:0]];
            }
            
        }else{
            //无关联的，直接给几个选项就行
            for (NSInteger i=0; i<self.noCorreArry.count; i++) {
                
                NSArray *eachAry=self.noCorreArry[i];
                
                [self.backArry addObject:[eachAry objectAtIndex:[self.pick selectedRowInComponent:i]]];
                
            }
        }
    }
}

-(UIColor *)colorWith:(NSArray *)colorArry
{
    NSString *ColorA=[NSString stringWithFormat:@"%@",colorArry[0]];
    NSString *ColorB=[NSString stringWithFormat:@"%@",colorArry[1]];
    NSString *ColorC=[NSString stringWithFormat:@"%@",colorArry[2]];
    NSString *ColorD=[NSString stringWithFormat:@"%@",colorArry[3]];
    
    UIColor *color=[[UIColor alloc]initWithRed:[ColorA integerValue]/255.0 green:[ColorB integerValue]/255.0 blue:[ColorC integerValue]/255.0 alpha:[ColorD floatValue]];
    return color;
}
-(NSArray *)getselectIndexArry{
    
    NSMutableArray *arry=[[NSMutableArray alloc]init];
    for (NSInteger i=0; i<_seleNum; i++) {
        NSNumber *num=[[NSNumber alloc]initWithInteger:[self.pick selectedRowInComponent:i]];
        [arry addObject:num];
        
    }
    return arry;
}

-(UIView *)pickerView:(UIPickerView *)pickerView viewForRow:(NSInteger)row forComponent:(NSInteger)component reusingView:(UIView *)view{
    
    UILabel *lbl = (UILabel *)view;
    
    if (lbl == nil) {
        lbl = [[UILabel alloc]init];
        lbl.font = [UIFont fontWithName:_pickerFontFamily size:[_pickerFontSize integerValue]];
        lbl.textColor = [self colorWith:_pickerFontColor];
        lbl.textAlignment = UITextAlignmentCenter;
    }
    
    //重新加载lbl的文字内容
    lbl.text = [self pickerView:pickerView titleForRow:row forComponent:component];
    
    return lbl;
    
}

- (BOOL)anySubViewScrolling:(UIView *)view{
    if ([view isKindOfClass:[UIScrollView class]]) {
        UIScrollView *scrollView = (UIScrollView *)view;
        if (scrollView.dragging || scrollView.decelerating) {
            return YES;
        }
    }
    for (UIView *theSubView in view.subviews) {
        if ([self anySubViewScrolling:theSubView]) {
            return YES;
        }
    }
    return NO;
}

// 显示picker的动画
-(void)showPickerWithAnimation
{
    self.pick.hidden=NO;
    dispatch_async(dispatch_get_main_queue(), ^{
        // 显示前重新启用整个BzwPicker的用户交互和显示modalBackgroundView
        self.userInteractionEnabled = YES;
        self.modalBackgroundView.hidden = NO;
        
        [UIView animateWithDuration:0.3 animations:^{
            self.modalBackgroundView.alpha = 1.0;
            CGRect frame = self.pickerContainer.frame;
            frame.origin.y = SCREEN_HEIGHT - frame.size.height;
            self.pickerContainer.frame = frame;
        }];
    });
}

// 隐藏picker的动画
-(void)hidePickerWithAnimation
{
    dispatch_async(dispatch_get_main_queue(), ^{
        [UIView animateWithDuration:0.3 animations:^{
            self.modalBackgroundView.alpha = 0.0;
            CGRect frame = self.pickerContainer.frame;
            frame.origin.y = SCREEN_HEIGHT;
            self.pickerContainer.frame = frame;
        } completion:^(BOOL finished) {
            // 动画完成后，禁用整个BzwPicker的用户交互，防止影响RN视图的事件触发
            self.userInteractionEnabled = NO;
            // 同时隐藏modalBackgroundView，确保不会阻挡用户操作
            self.modalBackgroundView.hidden = YES;
        }];
    });
    
    self.pick.hidden = YES;
}

@end

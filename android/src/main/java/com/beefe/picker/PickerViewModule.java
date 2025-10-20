package com.beefe.picker;

import android.content.res.AssetManager;
import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.os.Build;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.FrameLayout;
import android.view.KeyEvent;
import android.content.DialogInterface;

import com.beefe.picker.util.MIUIUtils;
import com.beefe.picker.view.OnSelectedListener;
import com.beefe.picker.view.PickerViewAlone;
import com.beefe.picker.view.PickerViewLinkage;
import com.beefe.picker.view.ReturnData;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.ArrayList;

import static android.graphics.Color.argb;

import androidx.annotation.Nullable;

/**
 * Author: <a href="https://github.com/shexiaoheng">heng</a>
 * <p>
 * Created by heng on 16/9/5.
 * <p>
 * Edited by heng on 16/9/22.
 * 1. PopupWindow height : full screen -> assignation
 * 2. Added pickerToolBarHeight support
 * <p>
 * Edited by heng on 2016/10/19.
 * 1. Added weights support
 * 2. Fixed return data bug
 * <p>
 * Edited by heng on 2016/11/16.
 * 1. Used WindowManager replace PopupWindow
 * 2. Removed method initOK() toggle() show() isPickerShow()
 * 3. Implements Application.ActivityLifecycleCallbacks
 * <p>
 * Edited by heng on 2016/11/17
 * 1. Used Dialog replace WindowManger
 * 2. Restore method show() isPickerShow()
 * <p>
 * Edited by heng on 2016/12/23
 * 1. Changed returnData type
 * 2. Added pickerToolBarFontSize
 * <p>
 * Edited by heng on 2016/12/26
 * 1. Fixed returnData bug
 * 2. Added pickerFontColor
 * 3. Added pickerFontSize
 * 4. Used LifecycleEventListener replace Application.ActivityLifecycleCallbacks
 * 5. Fixed other bug
 *
 * Edited by heng on 2017/01/17
 * 1. Added select(ReadableArray array, Callback callback)
 * 2. Optimization code
 */

public class PickerViewModule extends ReactContextBaseJavaModule implements LifecycleEventListener {
    
    private static final String FONTS = "fonts/";
    private static final String OTF = ".otf";
    private static final String TTF = ".ttf";

    private static final String REACT_CLASS = "BEEPickerManager";

    private static final String PICKER_DATA = "pickerData";
    private static final String SELECTED_VALUE = "selectedValue";

    private static final String IS_LOOP = "isLoop";

    private static final String WEIGHTS = "wheelFlex";

    private static final String PICKER_BG_COLOR = "pickerBg";

    private static final String PICKER_TOOL_BAR_BG = "pickerToolBarBg";
    private static final String PICKER_TOOL_BAR_HEIGHT = "pickerToolBarHeight";
    private static final String PICKER_TOOL_BAR_TEXT_SIZE = "pickerToolBarFontSize";

    private static final String PICKER_CONFIRM_BTN_TEXT = "pickerConfirmBtnText";
    private static final String PICKER_CONFIRM_BTN_COLOR = "pickerConfirmBtnColor";

    private static final String PICKER_CANCEL_BTN_TEXT = "pickerCancelBtnText";
    private static final String PICKER_CANCEL_BTN_COLOR = "pickerCancelBtnColor";

    private static final String PICKER_TITLE_TEXT = "pickerTitleText";
    private static final String PICKER_TITLE_TEXT_COLOR = "pickerTitleColor";

    private static final String PICKER_TEXT_COLOR = "pickerFontColor";
    private static final String PICKER_TEXT_SIZE = "pickerFontSize";
    private static final String PICKER_TEXT_ELLIPSIS_LEN = "pickerTextEllipsisLen";
    private static final String PICKER_ROW_HEIGHT = "pickerRowHeight";

    private static final String PICKER_FONT_FAMILY = "pickerFontFamily";
    private static final String PICKER_EMPTY_TEXT = "pickerEmptyText";
    private static final String PICKER_MASK_COLOR = "pickerMaskColor";

    private static final String PICKER_EVENT_NAME = "pickerEvent";
    private static final String EVENT_KEY_CONFIRM = "confirm";
    private static final String EVENT_KEY_CANCEL = "cancel";
    private static final String EVENT_KEY_SELECTED = "select";

    private static final String ERROR_NOT_INIT = "please initialize the component first";
    
    // 动画持续时间
    private static final int ANIMATION_DURATION = 300;

    private Dialog dialog = null;
    private RelativeLayout modalContainer = null; // 存储modal容器引用，用于动画
    private View pickerContentView = null; // 存储picker内容视图引用，用于平移动画

    private boolean isLoop = true;

    private String confirmText;
    private String cancelText;
    private String titleText;
    private int pickerTextEllipsisLen;

    private double[] weights;

    private ArrayList<ReturnData> returnData;

    private int curStatus;

    private PickerViewLinkage pickerViewLinkage;
    private PickerViewAlone pickerViewAlone;

    public PickerViewModule(ReactApplicationContext reactContext) {
        super(reactContext);
        reactContext.addLifecycleEventListener(this);
    }

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @ReactMethod
    public void _init(ReadableMap options) {
        Activity activity = getCurrentActivity();
        if (activity != null && options.hasKey(PICKER_DATA)) {
            View view = activity.getLayoutInflater().inflate(R.layout.picker_view, null);
            RelativeLayout barLayout = (RelativeLayout) view.findViewById(R.id.barLayout);
            TextView cancelTV = (TextView) view.findViewById(R.id.cancel);
            TextView titleTV = (TextView) view.findViewById(R.id.title);
            TextView confirmTV = (TextView) view.findViewById(R.id.confirm);
            RelativeLayout pickerLayout = (RelativeLayout) view.findViewById(R.id.pickerLayout);
            pickerViewLinkage = (PickerViewLinkage) view.findViewById(R.id.pickerViewLinkage);
            pickerViewAlone = (PickerViewAlone) view.findViewById(R.id.pickerViewAlone);

            int barViewHeight;
            if (options.hasKey(PICKER_TOOL_BAR_HEIGHT)) {
                try {
                    barViewHeight = options.getInt(PICKER_TOOL_BAR_HEIGHT);
                } catch (Exception e) {
                    barViewHeight = (int) options.getDouble(PICKER_TOOL_BAR_HEIGHT);
                }
            } else {
                barViewHeight = (int) (activity.getResources().getDisplayMetrics().density * 40);
            }
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    barViewHeight);
            barLayout.setLayoutParams(params);

            if (options.hasKey(PICKER_TOOL_BAR_BG)) {
                ReadableArray array = options.getArray(PICKER_TOOL_BAR_BG);
                int[] colors = getColor(array);
                barLayout.setBackgroundColor(argb(colors[3], colors[0], colors[1], colors[2]));
            }

            if (options.hasKey(PICKER_TOOL_BAR_TEXT_SIZE)) {
                int toolBarTextSize = options.getInt(PICKER_TOOL_BAR_TEXT_SIZE);
                cancelTV.setTextSize(toolBarTextSize);
                titleTV.setTextSize(toolBarTextSize);
                confirmTV.setTextSize(toolBarTextSize);
            }

            if (options.hasKey(PICKER_CONFIRM_BTN_TEXT)) {
                confirmText = options.getString(PICKER_CONFIRM_BTN_TEXT);
            }
            confirmTV.setText(!TextUtils.isEmpty(confirmText) ? confirmText : "");

            if (options.hasKey(PICKER_CONFIRM_BTN_COLOR)) {
                ReadableArray array = options.getArray(PICKER_CONFIRM_BTN_COLOR);
                int[] colors = getColor(array);
                confirmTV.setTextColor(argb(colors[3], colors[0], colors[1], colors[2]));
            }
            confirmTV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (curStatus) {
                        case 0:
                            returnData = pickerViewAlone.getSelectedData();
                            break;
                        case 1:
                            returnData = pickerViewLinkage.getSelectedData();
                            break;
                        case -1:
                            // 空picker状态，返回空数组
                            returnData = new ArrayList<>();
                            break;
                    }
                    commonEvent(EVENT_KEY_CONFIRM);
                    hide();
                }
            });


            if (options.hasKey(PICKER_TITLE_TEXT)) {
                titleText = options.getString(PICKER_TITLE_TEXT);
            }
            titleTV.setText(!TextUtils.isEmpty(titleText) ? titleText : "");
            if (options.hasKey(PICKER_TITLE_TEXT_COLOR)) {
                ReadableArray array = options.getArray(PICKER_TITLE_TEXT_COLOR);
                int[] colors = getColor(array);
                titleTV.setTextColor(argb(colors[3], colors[0], colors[1], colors[2]));
            }

            if (options.hasKey(PICKER_CANCEL_BTN_TEXT)) {
                cancelText = options.getString(PICKER_CANCEL_BTN_TEXT);
            }
            cancelTV.setText(!TextUtils.isEmpty(cancelText) ? cancelText : "");
            if (options.hasKey(PICKER_CANCEL_BTN_COLOR)) {
                ReadableArray array = options.getArray(PICKER_CANCEL_BTN_COLOR);
                int[] colors = getColor(array);
                cancelTV.setTextColor(argb(colors[3], colors[0], colors[1], colors[2]));
            }
            cancelTV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (curStatus) {
                        case 0:
                            returnData = pickerViewAlone.getSelectedData();
                            break;
                        case 1:
                            returnData = pickerViewLinkage.getSelectedData();
                            break;
                        case -1:
                            // 空picker状态，返回空数组
                            returnData = new ArrayList<>();
                            break;
                    }
                    commonEvent(EVENT_KEY_CANCEL);
                    hide();
                }
            });

            if(options.hasKey(PICKER_TEXT_ELLIPSIS_LEN)){
                pickerTextEllipsisLen = options.getInt(PICKER_TEXT_ELLIPSIS_LEN);
            }

            if (options.hasKey(IS_LOOP)) {
                isLoop = options.getBoolean(IS_LOOP);
            }

            if (options.hasKey(WEIGHTS)) {
                ReadableArray array = options.getArray(WEIGHTS);
                weights = new double[array.size()];
                for (int i = 0; i < array.size(); i++) {
                    switch (array.getType(i).name()) {
                        case "Number":
                            try {
                                weights[i] = array.getInt(i);
                            } catch (Exception e) {
                                weights[i] = array.getDouble(i);
                            }
                            break;
                        case "String":
                            try {
                                weights[i] = Double.parseDouble(array.getString(i));
                            } catch (Exception e) {
                                weights[i] = 1.0;
                            }
                            break;
                        default:
                            weights[i] = 1.0;
                            break;
                    }
                }
            }

            int pickerTextColor = 0xff000000;
            if (options.hasKey(PICKER_TEXT_COLOR)) {
                ReadableArray array = options.getArray(PICKER_TEXT_COLOR);
                int[] colors = getColor(array);
                pickerTextColor = Color.argb(colors[3], colors[0], colors[1], colors[2]);
            }

            int pickerTextSize = 16;
            if (options.hasKey(PICKER_TEXT_SIZE)) {
                try {
                    pickerTextSize = options.getInt(PICKER_TEXT_SIZE);
                } catch (Exception e) {
                    pickerTextSize = (int) options.getDouble(PICKER_TEXT_SIZE);
                }
            }

            ReadableArray pickerData = options.getArray(PICKER_DATA);

            // 检查pickerData是否为空数组，避免IndexOutOfBoundsException
            if (pickerData == null || pickerData.size() == 0) {
                // 如果数据为空，记录警告并初始化空picker
                Log.w(REACT_CLASS, "PickerData is null or empty, initializing empty picker");
                
                // 计算正常情况下应该有的picker高度
                int normalPickerHeight = 0;
                // 先设置picker组件以获取其默认高度
                pickerViewAlone.setVisibility(View.VISIBLE);
                pickerViewLinkage.setVisibility(View.GONE);
                
                // 设置基本配置以获取准确的高度
                pickerViewAlone.setTextColor(pickerTextColor);
                pickerViewAlone.setTextSize(pickerTextSize);
                pickerViewAlone.setTextEllipsisLen(pickerTextEllipsisLen);
                pickerViewAlone.setIsLoop(isLoop);
                
                // 设置行高
                if (options.hasKey(PICKER_ROW_HEIGHT)) {
                    try {
                        int rowHeight = options.getInt(PICKER_ROW_HEIGHT);
                        pickerViewAlone.setRowHeight(rowHeight);
                    } catch (Exception e) {
                        pickerViewAlone.setRowHeight((float) options.getDouble(PICKER_ROW_HEIGHT));
                    }
                }
                
                // 获取默认高度
                normalPickerHeight = pickerViewAlone.getViewHeight();
                
                // 确保有一个最小高度
                if (normalPickerHeight <= 0) {
                    if (options.hasKey(PICKER_ROW_HEIGHT)) {
                        try {
                            int rowHeight = options.getInt(PICKER_ROW_HEIGHT);
                            normalPickerHeight = rowHeight * 5; // 5行的高度
                        } catch (Exception e) {
                            float rowHeight = (float) options.getDouble(PICKER_ROW_HEIGHT);
                            normalPickerHeight = (int) (rowHeight * 5);
                        }
                    } else {
                        normalPickerHeight = 250; // 默认高度
                    }
                }
                
                initEmptyPicker(view, activity, barViewHeight, normalPickerHeight, options);
                return;
            }

            int pickerViewHeight;
            String name = pickerData.getType(0).name();
            switch (name) {
                case "Map":
                    curStatus = 1;
                    pickerViewLinkage.setVisibility(View.VISIBLE);
                    pickerViewAlone.setVisibility(View.GONE);

                    pickerViewLinkage.setPickerData(pickerData, weights);
                    pickerViewLinkage.setTextColor(pickerTextColor);
                    pickerViewLinkage.setTextSize(pickerTextSize);
                    pickerViewLinkage.setTextEllipsisLen(pickerTextEllipsisLen);
                    pickerViewLinkage.setIsLoop(isLoop);
                    
                    // 设置行高
                    if (options.hasKey(PICKER_ROW_HEIGHT)) {
                        try {
                            int rowHeight = options.getInt(PICKER_ROW_HEIGHT);
                            pickerViewLinkage.setRowHeight(rowHeight);
                        } catch (Exception e) {
                            pickerViewLinkage.setRowHeight((float) options.getDouble(PICKER_ROW_HEIGHT));
                        }
                    }

                    pickerViewLinkage.setOnSelectListener(new OnSelectedListener() {
                        @Override
                        public void onSelected(ArrayList<ReturnData> selectedList) {
                            returnData = selectedList;
                            commonEvent(EVENT_KEY_SELECTED);
                        }
                    });
                    pickerViewHeight = pickerViewLinkage.getViewHeight();
                    break;
                default:
                    curStatus = 0;
                    pickerViewAlone.setVisibility(View.VISIBLE);
                    pickerViewLinkage.setVisibility(View.GONE);

                    pickerViewAlone.setPickerData(pickerData, weights);
                    pickerViewAlone.setTextColor(pickerTextColor);
                    pickerViewAlone.setTextSize(pickerTextSize);
                    pickerViewAlone.setTextEllipsisLen(pickerTextEllipsisLen);
                    pickerViewAlone.setIsLoop(isLoop);
                    
                    // 设置行高
                    if (options.hasKey(PICKER_ROW_HEIGHT)) {
                        try {
                            int rowHeight = options.getInt(PICKER_ROW_HEIGHT);
                            pickerViewAlone.setRowHeight(rowHeight);
                        } catch (Exception e) {
                            pickerViewAlone.setRowHeight((float) options.getDouble(PICKER_ROW_HEIGHT));
                        }
                    }

                    pickerViewAlone.setOnSelectedListener(new OnSelectedListener() {
                        @Override
                        public void onSelected(ArrayList<ReturnData> selectedList) {
                            returnData = selectedList;
                            commonEvent(EVENT_KEY_SELECTED);
                        }
                    });

                    pickerViewHeight = pickerViewAlone.getViewHeight();
                    break;
            }

            if (options.hasKey(PICKER_FONT_FAMILY)) {
                Typeface typeface = null;
                AssetManager assetManager = activity.getApplicationContext().getAssets();
                final String fontFamily = options.getString(PICKER_FONT_FAMILY);
                try {
                    String path = FONTS + fontFamily + TTF;
                    typeface = Typeface.createFromAsset(assetManager, path);
                } catch (Exception ignored) {
                    try {
                        String path = FONTS + fontFamily + OTF;
                        typeface = Typeface.createFromAsset(assetManager, path);
                    } catch (Exception ignored2) {
                        try {
                            typeface = Typeface.create(fontFamily, Typeface.NORMAL);
                        } catch (Exception ignored3) {
                        }
                    }
                }
                cancelTV.setTypeface(typeface);
                titleTV.setTypeface(typeface);
                confirmTV.setTypeface(typeface);

                pickerViewAlone.setTypeface(typeface);
                pickerViewLinkage.setTypeface(typeface);
            }

            if (options.hasKey(SELECTED_VALUE)) {
                ReadableArray array = options.getArray(SELECTED_VALUE);
                String[] selectedValue = getSelectedValue(array);
                select(selectedValue);
            }

            if (options.hasKey(PICKER_BG_COLOR)) {
                ReadableArray array = options.getArray(PICKER_BG_COLOR);
                int[] colors = getColor(array);
                pickerLayout.setBackgroundColor(argb(colors[3], colors[0], colors[1], colors[2]));
            }

            int height = barViewHeight + pickerViewHeight;
            if (dialog == null) {
                // 创建带有modal遮罩层的容器
                modalContainer = createModalContainer(activity, view, height, options);
                // 存储picker内容视图引用
                pickerContentView = view;
                
                dialog = new Dialog(activity, R.style.Dialog_Full_Screen);
                dialog.setContentView(modalContainer);
                
                // 添加返回键监听，支持手势回退
                dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                            // 获取当前选中的数据
                            switch (curStatus) {
                                case 0:
                                    returnData = pickerViewAlone.getSelectedData();
                                    break;
                                case 1:
                                    returnData = pickerViewLinkage.getSelectedData();
                                    break;
                                case -1:
                                    returnData = new ArrayList<>();
                                    break;
                            }
                            // 触发取消事件
                            commonEvent(EVENT_KEY_CANCEL);
                            // 关闭picker
                            hide();
                            return true;
                        }
                        return false;
                    }
                });
                
                WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                Window window = dialog.getWindow();
                if (window != null) {
                    // 设置window background为空，避免背景干扰
                    window.setBackgroundDrawable(null);
                    
                    // 首先设置layoutParams的基本属性
                    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        window.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                    }else{
                        if (MIUIUtils.isMIUI()) {
                            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION;
                        }else {
                            //layoutParams.type = WindowManager.LayoutParams.TYPE_TOAST;
                        }
                    }
                    
                    // 设置基本flags，确保全屏和透明
                    // 注意：移除FLAG_NOT_FOCUSABLE以支持返回键监听
                    int baseFlags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                            | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
                    
                    // 设置状态栏透明相关flags
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        baseFlags |= WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
                    }
                    
                    // 对于Android 5.0及以上版本，使用更现代的方式设置状态栏透明
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        baseFlags &= ~WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
                        baseFlags |= WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
                                | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR;
                        window.setStatusBarColor(Color.TRANSPARENT);
                    }
                    
                    layoutParams.flags = baseFlags;
                    layoutParams.format = PixelFormat.TRANSPARENT;
                    // 移除默认动画，使用自定义透明度动画
                    // layoutParams.windowAnimations = R.style.PickerAnim;
                    layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
                    layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT; // 全屏以支持遮罩层
                    layoutParams.gravity = Gravity.CENTER;
                    window.setAttributes(layoutParams);   
                }
            } else {
                dialog.dismiss();
                // 创建带有modal遮罩层的容器
                modalContainer = createModalContainer(activity, view, height, options);
                // 存储picker内容视图引用
                pickerContentView = view;
                
                dialog.setContentView(modalContainer);
                
                // 重新设置状态栏透明
                Window window = dialog.getWindow();
                if (window != null) {
                    // 设置基本flags，确保全屏和透明
                    // 注意：移除FLAG_NOT_FOCUSABLE以支持返回键监听
                    int baseFlags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                            | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
                    
                    // 设置状态栏透明相关flags
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        baseFlags |= WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
                    }
                    
                    // 对于Android 5.0及以上版本，使用更现代的方式设置状态栏透明
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        baseFlags &= ~WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
                        baseFlags |= WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
                                | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR;
                        window.setStatusBarColor(Color.TRANSPARENT);
                    }
                    
                    // 应用所有flags
                    WindowManager.LayoutParams layoutParams = window.getAttributes();
                    layoutParams.flags = baseFlags;
                    window.setAttributes(layoutParams);
                }
            }
        }
    }

    private void initEmptyPicker(View view, Activity activity, int barViewHeight, int normalPickerHeight, ReadableMap options) {
        RelativeLayout barLayout = (RelativeLayout) view.findViewById(R.id.barLayout);
        TextView cancelTV = (TextView) view.findViewById(R.id.cancel);
        TextView titleTV = (TextView) view.findViewById(R.id.title);
        TextView confirmTV = (TextView) view.findViewById(R.id.confirm);
        RelativeLayout pickerLayout = (RelativeLayout) view.findViewById(R.id.pickerLayout);
        
        // 隐藏picker组件
        pickerViewLinkage = (PickerViewLinkage) view.findViewById(R.id.pickerViewLinkage);
        pickerViewAlone = (PickerViewAlone) view.findViewById(R.id.pickerViewAlone);
        pickerViewLinkage.setVisibility(View.GONE);
        pickerViewAlone.setVisibility(View.GONE);
        
        // 创建透明行区域来模拟picker的外观
        LinearLayout transparentRowsContainer = new LinearLayout(activity);
        transparentRowsContainer.setOrientation(LinearLayout.VERTICAL);
        
        // 计算行高
        int rowHeight = 40; // 默认行高
        if (options.hasKey(PICKER_ROW_HEIGHT)) {
            try {
                rowHeight = options.getInt(PICKER_ROW_HEIGHT);
            } catch (Exception e) {
                rowHeight = (int) options.getDouble(PICKER_ROW_HEIGHT);
            }
        }
        
        // 计算需要多少行来填满picker区域
        int numberOfRows = Math.max(normalPickerHeight / rowHeight, 3); // 至少3行
        
        // 添加调试日志
        Log.d(REACT_CLASS, "initEmptyPicker - normalPickerHeight: " + normalPickerHeight + 
              ", rowHeight: " + rowHeight + ", numberOfRows: " + numberOfRows);
        
        // 创建透明行
        for (int i = 0; i < numberOfRows; i++) {
            View transparentRow = new View(activity);
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, rowHeight);
            transparentRow.setLayoutParams(rowParams);
            transparentRow.setBackgroundColor(Color.TRANSPARENT);
            
            transparentRowsContainer.addView(transparentRow);
        }
        
        // 添加空数据提示文字，覆盖在透明行的中央
        final TextView emptyHint = new TextView(activity);
        String emptyText = "NO DATA";
        if (options.hasKey(PICKER_EMPTY_TEXT)) {
            emptyText = options.getString(PICKER_EMPTY_TEXT);
        }
        emptyHint.setText(emptyText);
        emptyHint.setTextSize(16);
        emptyHint.setTextColor(Color.GRAY);
        emptyHint.setGravity(Gravity.CENTER);
        
        // 创建一个FrameLayout来叠加透明行和文字
        FrameLayout contentContainer = new FrameLayout(activity);
        
        // 计算实际的透明行容器高度
        int actualRowsHeight = numberOfRows * rowHeight;
        Log.d(REACT_CLASS, "initEmptyPicker - actualRowsHeight: " + actualRowsHeight);
        
        // 添加透明行容器
        FrameLayout.LayoutParams transparentRowsParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                actualRowsHeight);
        transparentRowsContainer.setLayoutParams(transparentRowsParams);
        contentContainer.addView(transparentRowsContainer);
        
        // 添加空文字提示，居中显示
        FrameLayout.LayoutParams hintParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        hintParams.gravity = Gravity.CENTER;
        emptyHint.setLayoutParams(hintParams);
        contentContainer.addView(emptyHint);
        
        // 设置整个容器的布局参数，使用实际计算的高度
        RelativeLayout.LayoutParams containerParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                actualRowsHeight);
        contentContainer.setLayoutParams(containerParams);
        
        // 设置提示文本的颜色和大小
        if (options.hasKey(PICKER_TEXT_COLOR)) {
            ReadableArray array = options.getArray(PICKER_TEXT_COLOR);
            int[] colors = getColor(array);
            emptyHint.setTextColor(Color.argb(colors[3], colors[0], colors[1], colors[2]));
        }
        if (options.hasKey(PICKER_TEXT_SIZE)) {
            try {
                int textSize = options.getInt(PICKER_TEXT_SIZE);
                emptyHint.setTextSize(textSize);
            } catch (Exception e) {
                emptyHint.setTextSize((int) options.getDouble(PICKER_TEXT_SIZE));
            }
        }
        
        pickerLayout.addView(contentContainer);
        
        // 设置工具栏高度
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                barViewHeight);
        barLayout.setLayoutParams(params);

        // 设置工具栏背景色
        if (options.hasKey(PICKER_TOOL_BAR_BG)) {
            ReadableArray array = options.getArray(PICKER_TOOL_BAR_BG);
            int[] colors = getColor(array);
            barLayout.setBackgroundColor(argb(colors[3], colors[0], colors[1], colors[2]));
        }

        // 设置工具栏文字大小
        if (options.hasKey(PICKER_TOOL_BAR_TEXT_SIZE)) {
            int toolBarTextSize = options.getInt(PICKER_TOOL_BAR_TEXT_SIZE);
            cancelTV.setTextSize(toolBarTextSize);
            titleTV.setTextSize(toolBarTextSize);
            confirmTV.setTextSize(toolBarTextSize);
        }

        // 设置确认按钮
        if (options.hasKey(PICKER_CONFIRM_BTN_TEXT)) {
            confirmText = options.getString(PICKER_CONFIRM_BTN_TEXT);
        }
        confirmTV.setText(!TextUtils.isEmpty(confirmText) ? confirmText : "确定");
        if (options.hasKey(PICKER_CONFIRM_BTN_COLOR)) {
            ReadableArray array = options.getArray(PICKER_CONFIRM_BTN_COLOR);
            int[] colors = getColor(array);
            confirmTV.setTextColor(argb(colors[3], colors[0], colors[1], colors[2]));
        }
        confirmTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnData = new ArrayList<>();
                commonEvent(EVENT_KEY_CONFIRM);
                hide();
            }
        });

        // 设置标题
        if (options.hasKey(PICKER_TITLE_TEXT)) {
            titleText = options.getString(PICKER_TITLE_TEXT);
        }
        if (options.hasKey(PICKER_TITLE_TEXT_COLOR)) {
            ReadableArray array = options.getArray(PICKER_TITLE_TEXT_COLOR);
            int[] colors = getColor(array);
            titleTV.setTextColor(argb(colors[3], colors[0], colors[1], colors[2]));
        }

        // 设置取消按钮
        if (options.hasKey(PICKER_CANCEL_BTN_TEXT)) {
            cancelText = options.getString(PICKER_CANCEL_BTN_TEXT);
        }
        cancelTV.setText(!TextUtils.isEmpty(cancelText) ? cancelText : "取消");
        if (options.hasKey(PICKER_CANCEL_BTN_COLOR)) {
            ReadableArray array = options.getArray(PICKER_CANCEL_BTN_COLOR);
            int[] colors = getColor(array);
            cancelTV.setTextColor(argb(colors[3], colors[0], colors[1], colors[2]));
        }
        cancelTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnData = new ArrayList<>();
                commonEvent(EVENT_KEY_CANCEL);
                hide();
            }
        });

        // 设置picker背景色
        if (options.hasKey(PICKER_BG_COLOR)) {
            ReadableArray array = options.getArray(PICKER_BG_COLOR);
            int[] colors = getColor(array);
            pickerLayout.setBackgroundColor(argb(colors[3], colors[0], colors[1], colors[2]));
        }

        // 设置字体
        if (options.hasKey(PICKER_FONT_FAMILY)) {
            Typeface typeface = null;
            AssetManager assetManager = activity.getApplicationContext().getAssets();
            final String fontFamily = options.getString(PICKER_FONT_FAMILY);
            try {
                String path = FONTS + fontFamily + TTF;
                typeface = Typeface.createFromAsset(assetManager, path);
            } catch (Exception ignored) {
                try {
                    String path = FONTS + fontFamily + OTF;
                    typeface = Typeface.createFromAsset(assetManager, path);
                } catch (Exception ignored2) {
                    try {
                        typeface = Typeface.create(fontFamily, Typeface.NORMAL);
                    } catch (Exception ignored3) {
                    }
                }
            }
            if (typeface != null) {
                cancelTV.setTypeface(typeface);
                titleTV.setTypeface(typeface);
                confirmTV.setTypeface(typeface);
                emptyHint.setTypeface(typeface);
            }
        }

        // 创建dialog
        int pickerHeight = actualRowsHeight; // 使用实际计算的行高度
        int totalHeight = barViewHeight + pickerHeight; // 总高度 = 工具栏高度 + picker内容高度
        
        Log.d(REACT_CLASS, "initEmptyPicker - pickerHeight: " + pickerHeight + 
              ", totalHeight: " + totalHeight + ", barViewHeight: " + barViewHeight);
        
        if (dialog == null) {
            // 创建带有modal遮罩层的容器
            modalContainer = createModalContainer(activity, view, totalHeight, options);
            // 存储picker内容视图引用
            pickerContentView = view;
            
            dialog = new Dialog(activity, R.style.Dialog_Full_Screen);
            dialog.setContentView(modalContainer);
            
            // 添加返回键监听，支持手势回退
            dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                        // 获取当前选中的数据
                        switch (curStatus) {
                            case 0:
                                returnData = pickerViewAlone.getSelectedData();
                                break;
                            case 1:
                                returnData = pickerViewLinkage.getSelectedData();
                                break;
                            case -1:
                                returnData = new ArrayList<>();
                                break;
                        }
                        // 触发取消事件
                        commonEvent(EVENT_KEY_CANCEL);
                        // 关闭picker
                        hide();
                        return true;
                    }
                    return false;
                }
            });
            
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            Window window = dialog.getWindow();
            if (window != null) {
                // 设置window background为空，避免背景干扰
                window.setBackgroundDrawable(null);
                
                // 首先设置layoutParams的基本属性
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    window.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                }else{
                    if (MIUIUtils.isMIUI()) {
                        layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION;
                    }
                }
                
                // 设置基本flags，确保全屏和透明
                // 注意：移除FLAG_NOT_FOCUSABLE以支持返回键监听
                int baseFlags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
                
                // 设置状态栏透明相关flags
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    baseFlags |= WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
                }
                
                // 对于Android 5.0及以上版本，使用更现代的方式设置状态栏透明
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    baseFlags &= ~WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
                    baseFlags |= WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
                            | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR;
                    window.setStatusBarColor(Color.TRANSPARENT);
                }
                
                layoutParams.flags = baseFlags;
                layoutParams.format = PixelFormat.TRANSPARENT;
                // 移除默认动画，使用自定义透明度动画
                // layoutParams.windowAnimations = R.style.PickerAnim;
                layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
                layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT; // 全屏以支持遮罩层
                layoutParams.gravity = Gravity.CENTER;
                window.setAttributes(layoutParams);   
            }
        } else {
            dialog.dismiss();
            // 创建带有modal遮罩层的容器
            modalContainer = createModalContainer(activity, view, totalHeight, options);
            // 存储picker内容视图引用
            pickerContentView = view;
            
            dialog.setContentView(modalContainer);
            
            // 重新设置状态栏透明
            Window window = dialog.getWindow();
            if (window != null) {
                // 设置基本flags，确保全屏和透明
                // 注意：移除FLAG_NOT_FOCUSABLE以支持返回键监听
                int baseFlags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
                
                // 设置状态栏透明相关flags
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    baseFlags |= WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
                }
                
                // 对于Android 5.0及以上版本，使用更现代的方式设置状态栏透明
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    baseFlags &= ~WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
                    baseFlags |= WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
                            | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR;
                    window.setStatusBarColor(Color.TRANSPARENT);
                }
                
                // 应用所有flags
                WindowManager.LayoutParams layoutParams = window.getAttributes();
                layoutParams.flags = baseFlags;
                window.setAttributes(layoutParams);
            }
        }
        
        curStatus = -1; // 标记为空picker状态
    }

    @ReactMethod
    public void select(ReadableArray array, Callback callback) {
        if (dialog == null) {
            if (callback != null) {
                callback.invoke(ERROR_NOT_INIT);
            }
            return;
        }
        String[] selectedValue = getSelectedValue(array);
        select(selectedValue);
    }

    @ReactMethod
    public void show() {
        if (dialog == null) {
            return;
        }
        if (!dialog.isShowing()) {
            // 在显示前确保window配置正确
            Window window = dialog.getWindow();
            if (window != null) {
                // 设置基本flags，确保全屏和透明
                // 注意：移除FLAG_NOT_FOCUSABLE以支持返回键监听
                int baseFlags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
                
                // 设置状态栏透明相关flags
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    baseFlags |= WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
                }
                
                // 对于Android 5.0及以上版本，使用更现代的方式设置状态栏透明
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    baseFlags &= ~WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
                    baseFlags |= WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
                            | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR;
                    window.setStatusBarColor(Color.TRANSPARENT);
                }
                
                // 应用所有flags
                WindowManager.LayoutParams layoutParams = window.getAttributes();
                layoutParams.flags = baseFlags;
                window.setAttributes(layoutParams);
            }
            
            dialog.show();
            // 同时执行modal透明度淡入动画和picker容器平移动画
            if (modalContainer != null) {
                modalContainer.animate()
                    .alpha(1f)
                    .setDuration(ANIMATION_DURATION)
                    .start();
            }
            if (pickerContentView != null) {
                pickerContentView.animate()
                    .translationY(0)
                    .setDuration(ANIMATION_DURATION)
                    .start();
            }
        }
    }

    @ReactMethod
    public void hide() {
        if (dialog == null) {
            return;
        }
        if (dialog.isShowing()) {
            // 同时执行modal透明度淡出动画和picker容器平移动画
            if (modalContainer != null && pickerContentView != null) {
                // 获取picker容器的高度用于平移动画
                int pickerHeight = pickerContentView.getHeight();
                
                modalContainer.animate()
                    .alpha(0f)
                    .setDuration(ANIMATION_DURATION)
                    .start();
                    
                pickerContentView.animate()
                    .translationY(pickerHeight)
                    .setDuration(ANIMATION_DURATION)
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
            dialog.dismiss();
                        }
                    })
                    .start();
            } else {
                dialog.dismiss();
            }
        }
    }

    @ReactMethod
    public void isPickerShow(Callback callback) {
        if (callback == null)
            return;
        if (dialog == null) {
            callback.invoke(ERROR_NOT_INIT);
        } else {
            callback.invoke(null, dialog.isShowing());
        }
    }

    private int[] getColor(ReadableArray array) {
        int[] colors = new int[4];
        if (array == null || array.size() == 0) {
            return colors;
        }
        for (int i = 0; i < array.size(); i++) {
            switch (i) {
                case 0:
                case 1:
                case 2:
                    colors[i] = array.getInt(i);
                    break;
                case 3:
                    colors[i] = (int) (array.getDouble(i) * 255);
                    break;
                default:
                    break;
            }
        }
        return colors;
    }

    private String[] getSelectedValue(ReadableArray array) {
        if (array == null || array.size() == 0) {
            return new String[0];
        }
        String[] selectValue = new String[array.size()];
        String value = "";
        for (int i = 0; i < array.size(); i++) {
            switch (array.getType(i).name()) {
                case "Boolean":
                    value = String.valueOf(array.getBoolean(i));
                    break;
                case "Number":
                    try {
                        value = String.valueOf(array.getInt(i));
                    } catch (Exception e) {
                        value = String.valueOf(array.getDouble(i));
                    }
                    break;
                case "String":
                    value = array.getString(i);
                    break;
            }
            selectValue[i] = value;
        }
        return selectValue;
    }

    private void select(String[] selectedValue) {
        switch (curStatus) {
            case 0:
                pickerViewAlone.setSelectValue(selectedValue);
                break;
            case 1:
                pickerViewLinkage.setSelectValue(selectedValue);
                break;
            case -1:
                // 空picker状态，忽略选择操作
                Log.w(REACT_CLASS, "Cannot select values in empty picker");
                break;
        }
    }

    private void commonEvent(String eventKey) {
        WritableMap map = Arguments.createMap();
        map.putString("type", eventKey);
        WritableArray indexes = Arguments.createArray();
        WritableArray values = Arguments.createArray();
        for (ReturnData data : returnData) {
            indexes.pushInt(data.getIndex());
            values.pushString(data.getItem());
        }
        map.putArray("selectedValue", values);
        map.putArray("selectedIndex", indexes);
        sendEvent(getReactApplicationContext(), PICKER_EVENT_NAME, map);
    }

    private void sendEvent(ReactContext reactContext,
                           String eventName,
                           @Nullable WritableMap params) {
        reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, params);
    }

    @Override
    public void onHostResume() {

    }

    @Override
    public void onHostPause() {
        hide();
    }

    @Override
    public void onHostDestroy() {
        hide();
        dialog = null;
        modalContainer = null;
        pickerContentView = null;
    }

    /**
     * 配置window以支持状态栏覆盖
     */
    private void configureWindowForStatusBarOverlay(Window window) {
        if (window == null) return;
        
        // 设置基本flags，确保全屏和透明
        // 注意：移除FLAG_NOT_FOCUSABLE以支持返回键监听
        int baseFlags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        
        // 设置状态栏透明相关flags
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            baseFlags |= WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        }
        
        // 对于Android 5.0及以上版本，使用更现代的方式设置状态栏透明
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            baseFlags &= ~WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
            baseFlags |= WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
                    | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR;
            window.setStatusBarColor(Color.TRANSPARENT);
        }
        
        // 应用所有flags
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.flags = baseFlags;
        window.setAttributes(layoutParams);
    }

    /**
     * 创建带有modal遮罩层的容器
     */
    private RelativeLayout createModalContainer(Activity activity, View pickerView, int pickerHeight, ReadableMap options) {
        // 创建根容器，覆盖整个屏幕
        RelativeLayout modalContainer = new RelativeLayout(activity);
        modalContainer.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT));
        
        // 设置初始透明度为0，用于淡入动画
        modalContainer.setAlpha(0f);
        
        // 创建半透明遮罩层，覆盖整个屏幕包括状态栏
        View maskView = new View(activity);
        
        // 设置遮罩层颜色，支持自定义或使用默认值
        int maskColor = Color.argb(128, 0, 0, 0); // 默认：50% 透明度的黑色
        if (options.hasKey(PICKER_MASK_COLOR)) {
            ReadableArray array = options.getArray(PICKER_MASK_COLOR);
            int[] colors = getColor(array);
            maskColor = Color.argb(colors[3], colors[0], colors[1], colors[2]);
        }
        maskView.setBackgroundColor(maskColor);
        
        // 遮罩层布局参数 - 覆盖整个屏幕
        RelativeLayout.LayoutParams maskParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        maskView.setLayoutParams(maskParams);
        
        // 添加点击遮罩层关闭picker的功能
        maskView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 点击遮罩层时执行取消操作
                switch (curStatus) {
                    case 0:
                        returnData = pickerViewAlone.getSelectedData();
                        break;
                    case 1:
                        returnData = pickerViewLinkage.getSelectedData();
                        break;
                    case -1:
                        // 空picker状态，返回空数组
                        returnData = new ArrayList<>();
                        break;
                }
                commonEvent(EVENT_KEY_CANCEL);
                hide();
            }
        });
        
        // 设置picker内容容器的布局参数，定位在底部
        RelativeLayout.LayoutParams pickerParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                pickerHeight);
        pickerParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        pickerView.setLayoutParams(pickerParams);
        
        // 设置picker容器的初始位置为屏幕底部外侧（用于平移动画）
        pickerView.setTranslationY(pickerHeight);
        
        // 添加遮罩层和picker内容到容器
        modalContainer.addView(maskView);
        modalContainer.addView(pickerView);
        
        return modalContainer;
    }
}

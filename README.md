# react-native-picker

[![npm version](https://img.shields.io/npm/v/@see_you/react-native-picker.svg)](https://www.npmjs.com/package/@see_you/react-native-picker)
[![npm](https://img.shields.io/npm/dependency-version/see_you/react-native-picker)](https://www.npmjs.com/package/react-native-picker)

A Native Picker with high performance for React Native.

## Preview

<video src="./doc/android.webm" controls width="100%" style="" height=700></video>

## 🚀 Improvements Over Original

This is a fork of [beefe/react-native-picker](https://github.com/beefe/react-native-picker) with the following enhancements:

- **Fixed overlay rendering issues** - Resolves white screen problems that may occur in renderer
- **Android row height support** - Added support for customizing row height on Android platform
- **Better performance** - Optimized rendering with overlay
- **Enhanced stability** - Fixed various edge cases and crashes

## 📦 Installation

```bash
npm install @see_you/react-native-picker --save
```

## 🔗 Linking

### React Native 0.60+

No manual linking required - it's automatic!

### React Native < 0.60

```bash
react-native link @see_you/react-native-picker
```

## 📱 Platform Support

| Platform | Version |
| -------- | ------- |
| iOS      | 8.0+    |
| Android  | 4.1+    |

## 🎯 Usage

### Basic Example

```javascript
import Picker from "@see_you/react-native-picker";

// Simple data array
let data = [];
for (let i = 0; i < 100; i++) {
  data.push(i);
}

Picker.init({
  pickerData: data,
  selectedValue: [59],
  onPickerConfirm: (data) => {
    console.log("Selected:", data);
  },
  onPickerCancel: (data) => {
    console.log("Cancelled:", data);
  },
  onPickerSelect: (data) => {
    console.log("Selected:", data);
  },
});

Picker.show();
```

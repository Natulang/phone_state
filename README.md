<div align="center">
    <img src="https://raw.githubusercontent.com/andreamainella98/phone_state/master/images/icon.png">
</div>

## DESCRIPTION

This plugin allows you to know quickly and easily if your Android or iOS device is receiving a call and to know the status of the call.

- Native Android: [TelephonyManager](https://developer.android.com/reference/android/telephony/TelephonyManager)
- Native iOS: [CallKit](https://developer.apple.com/documentation/callkit)

## PAY ATTENTION

- In the iOS simulator doesn't work
- The caller phone number is only available on Android, not iOS
- This plugin reports phone call state only. It does not answer, reject, end, or record calls, and it does not change whether other packages can capture call audio.
- Android detects system telephony call state only. Third-party VoIP calls from apps such as Telegram or WhatsApp are not supported. iOS CallKit behavior can differ by app.
- The iOS implementation uses CallKit. For apps distributed in regions where CallKit is restricted, including China App Store review contexts, confirm availability and territory rules with Apple before submission.
- `PhoneState.stream` requires the app process to be running with an active Flutter engine and listener. It does not send callbacks after the app is terminated.
- On Android, if `PhoneState.stream` does not emit, confirm `READ_PHONE_STATE` is declared and granted at runtime. Some OEM battery or background restrictions, including MIUI settings on Xiaomi devices, can also delay or block callbacks.

[![Buy Me A Coffee](https://www.buymeacoffee.com/assets/img/custom_images/orange_img.png)](https://www.buymeacoffee.com/maine98)

## HOW TO INSTALL
#### Flutter
> **Note**: This version requires Flutter >= 3.44.0 and Dart >= 3.12.2.
> Add `phone_state` to your app's `pubspec.yaml`, not to this package's `pubspec.yaml`. Your app/package name must be different from `phone_state`.

```yaml
dependencies:
  flutter:
    sdk: flutter
  phone_state: 4.0.1
```
#### Android: Added permission on manifest
```xml
<uses-permission android:name="android.permission.READ_PHONE_STATE" />
<uses-permission android:name="android.permission.READ_CALL_LOG" />
```
`READ_PHONE_STATE` is required. `READ_CALL_LOG` is optional and only needed for caller phone numbers.
> **Warning**: Adding `READ_CALL_LOG` permission, your app will be removed from the Play Store if you don't have a valid reason to use it. [Read more](https://support.google.com/googleplay/android-developer/answer/9047303?hl=en).
#### iOS: Not needed permissions on info.plist

## HOW TO USE

### Get stream phone state status

```dart
StreamBuilder<PhoneState>(
  stream: PhoneState.stream,
  ...
```

### Troubleshooting package conflicts

If `phone_state` stops receiving updates after adding another package:

- Confirm the required Android runtime permissions are granted before listening. `READ_PHONE_STATE` is required, and `READ_CALL_LOG` is only needed for caller phone numbers.
- Keep a single active `PhoneState.stream` listener for each app flow. Cancel the previous subscription before starting another one when screens, services, or background handlers change.
- When reporting a conflict, include the other package names, platform versions, a minimal reproduction, and the relevant Flutter, Logcat, or Xcode logs.

## SCREENSHOT

<img src="https://raw.githubusercontent.com/andreamainella98/phone_state/master/images/example.gif" width=300/>

Write me in the [GitHub](https://github.com/andreamainella98/phone_state/issues) issues the new features you need and, if they are approved of course, I will implement them as soon as I can.

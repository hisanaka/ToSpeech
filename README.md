ToSpeech
===

日本語の説明は[こちら](https://github.com/hisanaka/ToSpeech/blob/master/README.ja.md)

APK file for the operation check [here](https://github.com/hisanaka/ToSpeech/blob/master/app/app-debug.apk)

# Overview

This app is a sample to work with Ring app by logbar Inc. using intent.

# Description

This app guide the time or wether forecast by voice when receiving the intent, that send from Ring app by logbar Inc. with the registered gesture.

# Usage

## Setting of Text To Speech.

1. Open "Settings > Language & input > Text-to-speech output > Preferred engine".

1. Select English or Japanese.

1. Install English Or Japanese voice data via "Install voice data".

## Setting of Ring app by logbar inc.

1. Add action "Send Intent".

1. Submit your favorite gesture.

1. Input "jp.or.ixqsware.tospeech.action.SPEECH" to "Action Name".

1. Input "content" to "Extra key".

1. Input "time" for time signal, or "weather" for weather forecast to "Extra value".

## This App

1. Install this app.

## Remarks

If you want to use the weather forecast, you need to the location of your terminal to "ON".

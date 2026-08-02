# SidTop - Sit Down To PC

> An Android app that forces you to put your phone down

<p align="center">
   <img src="docs/images/home-screen.jpg" width="250" alt="Home screen" />
   <img src="docs/images/history-screen.jpg" width="250" alt="History screen" />
   <img src="docs/images/settings-screen.jpg" width="250" alt="Settings screen" />
</p>

<p align="center">
	<a href="https://github.com/fortlis/SidTop/releases">
		<img src="https://img.shields.io/github/v/release/Fortlis/SidTop?include_prereleases" alt="GitHub release" />
	</a>
</p>

## Overview

I built SidTop because I got tired of productivity apps that treat you like an addict. They block your screen, shame you for screen time, and force you to keep streaks. I just needed a tool to help me stop scrolling and start working.

The idea is simple: you promise yourself you'll sit down and work at a specific time. When that time arrives, an alarm starts ringing. The primary way to stop it is to flip your phone face-down and leave it there for several minutes.

This creates a physical and psychological buffer zone. By forcing you to put the device away and wait, your brain gets the necessary time to detach from the screen and transition into work mode.

If something real comes up and you need to stop the alarm without flipping, you can. Tap Cancel and enter a reason. But that's the exception, not how it's meant to be used.

## User Flow

```
[Scheduled] --(time reached)--> [Alarm ringing]
                                      |
                    +-----------------+-----------------+
                    |                                   |
             Cancel tapped                     Phone flipped screen-down
             + reason entered                  and held for N seconds
                    |                                   |
                    v                                   v
              status: cancelled                status: completed
```

## Session Statuses

| Status      | How it's reached                                   |
| ----------- | -------------------------------------------------- |
| `scheduled` | Session created, scheduled time hasn't arrived yet |
| `active`    | Scheduled time reached, alarm sound is playing     |
| `cancelled` | Cancel tapped + cancellation reason entered        |
| `completed` | Phone held face-down for the configured duration   |

# Tech Stack & Architecture 

## Tech Stack

- **Framework:** [React Native](https://reactnative.dev/) with [Expo](https://expo.dev)
- **Native Module:** [Expo Modules API](https://docs.expo.dev/modules/overview/)
- **Local Storage:** SQLite (JS thread) + DataStore (Native thread)
- **Package Manager:** Yarn

## Android Native Layer

- **Scheduling:** `AlarmManager` schedules precise alarm times
    
- **Reboot Handling:** `BootReceiver` restores scheduled sessions if the device restarts
    
- **Alarm Execution:** A `BroadcastReceiver` triggers a `ForegroundService` when the scheduled time arrives
    
- **Background Playback:** `ForegroundService` keeps playing the default system alarm sound even when the app is backgrounded or killed
    
- **Motion Detection:** Reads sensor data via `SensorManager` inside the native service to detect when the device is flipped face-down and holds that state
	
- **Data Sync:** Sessions are temporarily held in `Datastore` so native code functions without relying on the JS thread. Once the JS thread wakes up, data synchronizes to `SQLite` and clears from `Datastore`

## Required Android permissions

| Permission                                                  | Why it's needed                                         |
| ----------------------------------------------------------- | ------------------------------------------------------- |
| `USE_EXACT_ALARM`                                           | precise triggering of the session at the scheduled time |
| `RECEIVE_BOOT_COMPLETED`                                    | restoring scheduled sessions after a device reboot      |
| `FOREGROUND_SERVICE`<br>`FOREGROUND_SERVICE_MEDIA_PLAYBACK` | playing the alarm even when the app is backgrounded     |
| `POST_NOTIFICATIONS`                                        | allows the app to send notifications                    |

# Getting Started

## Installation

```bash
yarn install
npx expo prebuild --clean
```

## Running in development

> ⚠️ The app **does not work in Expo Go**, since it relies on a custom native module (Kotlin). A dev client is required.

```bash
npx expo run:android
```

or via EAS:

```bash
eas build --profile development --platform android
```

## Building a release APK

```bash
cd android
./gradlew assembleRelease
```

The APK will be located at `android/app/build/outputs/apk/release/`.

Or via EAS Build:

```bash
eas build --platform android --profile production
```


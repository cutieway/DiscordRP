<div align="center">
  <img src="docs/logo.png" width="160" alt="DiscordRP logo">
</div>

<div align="center">
  <h1>DiscordRP</h1>
  <h4>Restore Push / Rich Presence</h4>
</div>

<p align="center">
  <img alt="Platform" src="https://img.shields.io/badge/platform-Android%208.0%2B-FF7EB6">
  <img alt="No Play Services" src="https://img.shields.io/badge/Play%20Services-not%20required-A97BFF">
  <img alt="License" src="https://img.shields.io/badge/license-GPL--3.0-blue">
</p>

## About

DiscordRP is built for de-Googled Android in mind: GrapheneOS, CalyxOS, LineageOS and /e/OS.

DiscordRP keeps its own connection to Discord, so your messages arrive and the music you
play shows on your profile.

## Features

- [x] DMs and mentions as notifications, with reply and mark as read from the shade
- [x] Music rich presence from any player, with album art
- [x] Choose what notifies you: everything, mentions and DMs, or DMs only
- [x] Set your status and mobile badge from your phone
- [x] Starts on boot and stays connected

## Screenshots

<div align="center">
  <img src="docs/screens/home.png" width="30%" alt="Home: connection state and now playing">
  <img src="docs/screens/presence.png" width="30%" alt="Presence: status and card contents">
  <img src="docs/screens/alerts.png" width="30%" alt="Alerts: notification scope and filters">
</div>

## Download

Get the latest APK from [Releases](../../releases). Android 8.0 or newer, no Google account
or Play Services required.

> **Warning**
> This is an unofficial client that signs in with a Discord user token. Discord's
> developer policy prohibits applications from obtaining user login tokens, and
> using one can lead to account restriction or termination. Use a test account
> unless you accept that risk.

## Updates

Fork builds check `cutieway/DiscordRP` releases. The updater requires the APK's
SHA-256 digest from the release metadata and rejects an asset without one. Before
handing a download to the installer it also verifies the package id and signing
certificate against the running app. Android enforces the same signature rule at
install time, so publish fork releases with the key the installed app was signed
with.

## Setup

1. Install and open the app
2. Grant notification access and allow notifications
3. Sign in with Discord
4. Turn the connection on

Enable **Unrestricted battery** during setup. Aggressive battery management is the usual
reason messages stop arriving while the screen is off.

## Privacy

No analytics, no crash reporting, and no server in the middle. The app talks to Discord,
MusicBrainz, the Cover Art Archive, and GitHub for update checks directly. The login
token is sealed with an Android Keystore key and is removed by signing out.

## Credits

The rich presence implementation draws on ideas from [Kizzy](https://github.com/dead8309/Kizzy)
by **dead8309**.

## License

[GPL-3.0](License)

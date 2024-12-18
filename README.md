<p align="center">
    <img align="center" src="https://f.mwhitney.dev/icons/pipaa-128.png" style="width: 128px; height: 128px; image-rendering: crisp-edges;">
    <br>
    <h1 align="center">PiPAA</h1>
</p>
<p align="center">
    Picture-in-Picture Anything Anywhere — Play almost any media, whatever the source, within simple, always-on-top windows.
    <br><br>
    <img src="https://img.shields.io/badge/designed for-windows-blue?style=flat&logo=windows" alt="Designed for and Tested on Windows">
    <img src="https://img.shields.io/badge/latest release-0.9.4-00456B" alt="PiPAA v0.9.4">
    <img src="https://img.shields.io/badge/language-java-F58219?logo=openjdk" alt="Written in Java">
    <a target="_blank" href="https://github.com/mwhitney57/PiPAA/blob/main/LICENSE"><img src="https://img.shields.io/badge/license-GPL%203.0-yellow" alt="GPL License v3.0"></a>
</p>

### Table of Contents
- 📃 [Description](#-description)
- ✨ [Features](#-features)
- 📸 [Images](#-images)
- 💾 [Installation and Usage](#-installation-and-usage)
- ⌨ [Shortcuts](#-shortcuts)
- 📜 [Guides](#-guides)
- 🤔 [F.A.Q.](#-faq)
- 🔐 [Privacy](#-privacy)
- 🚩 [Known Issues](#-known-issues)
- 🧱 [Building](#-building)
- ⚙ [Binaries](#-binaries)
- 📖 [Libraries](#-libraries)
- ℹ [Additional Information](#%E2%84%B9-additional-information)

### 📃 Description
Create any number of independent windows, each capable of playing its own media, whether it be video, images, or audio! Since each window is in an always-on-top state, it will stay above all other windows on your system, which is great for multitasking. Windows can be moved, resized, duplicated, fullscreened, minimized, hidden, and more, all with simple keyboard and mouse controls.

Once the app is started, the PiPAA icon will appear in the system tray, and an empty window will open by default. The **tray** is your control hub for windows and configuring the application. It also allows you to fully exit the application.

Media can be played from a local source on your computer, and it can often be downloaded straight from the web. Either way, simply **drag and drop** or **copy and paste** your media into a PiPAA window to start playback. If a site or media type doesn't work, check out the **configuration** via the **tray icon**. If things still aren't working, feel free to drop an [Issue](https://github.com/mwhitney57/PiPAA/issues) with details, and a developer can look into it.

With no windows running, PiPAA is designed to take up as little processing power and memory as possible. Since the app is programmed in Java, it may hold onto more memory than it needs at any given time. If it does, it should let go of that memory once your computer needs it. Otherwise, the memory will likely be released as the app idles. These factors allow for PiPAA to be used when needed, then forgotten about, running in the background until you need it again.

### ✨ Features
- Play media locally from your computer, or remotely from the web!
- Run as many media windows as you need, within the limits of your computer.
- Control windows and media with ease via simple **[keyboard and mouse shortcuts](#-shortcuts)**.
- Advanced functionality, such as downloading/caching media from the web.
- Display embedded album art on audio files, or add your own.
- Configurable, over-the-air (OTA) updating ensures you stay up-to-date. Be notified when an update is available.
- Themes! Custom themes coming soon.
- Much more! Read on for more info or download PiPAA from the [Releases](https://github.com/mwhitney57/PiPAA/releases) section.

## 📸 Images
With numerous features to explore, here's an introduction to some basics of PiPAA!
<details open><summary><h3>Tray Menu</h3></summary>
<img src="https://f.mwhitney.dev/projects/pipaa/demo/demoTrayMenu.jpg" alt="PiPAA Tray Menu."></details>
<details><summary><h3>Configuration Menu</h3></summary>
<img src="https://f.mwhitney.dev/projects/pipaa/demo/demoConfigMain.jpg" alt="PiPAA Configuration Menu."><br><br>
<img src="https://f.mwhitney.dev/projects/pipaa/demo/demoConfigDark.jpg" alt="PiPAA Dark Theme."></details>
<details><summary><h3>Loading Media</h3></summary>
<img src="https://f.mwhitney.dev/projects/pipaa/demo/demoWebLoad1.jpg" alt="PiPAA Web Load."><br><br>
<img src="https://f.mwhitney.dev/projects/pipaa/demo/demoWebLoad2.jpg" alt="PiPAA Web Load Finished."><br><br>
<img src="https://f.mwhitney.dev/projects/pipaa/demo/demoWebLoadCache-2.jpg" alt="PiPAA Web Load Cache."></details>
<details><summary><h3>Resizing Windows</h3></summary>
<img src="https://f.mwhitney.dev/projects/pipaa/demo/demoWindowResize1.jpg" alt="PiPAA Resizing Windows."><br><br>
<img src="https://f.mwhitney.dev/projects/pipaa/demo/demoWindowResize2.jpg" alt="PiPAA Resizing Windows Result."><br><br>
<img src="https://f.mwhitney.dev/projects/pipaa/demo/demoWindowDuplicates.jpg" alt="PiPAA Duplicate Windows."></details>
<details><summary><h3>YouTube Example</h3></summary>
<img src="https://f.mwhitney.dev/projects/pipaa/demo/demoWebLoadCopyPaste.jpg" alt="PiPAA YouTube Loading Example."><br><br>
<img src="https://f.mwhitney.dev/projects/pipaa/demo/demoLoading.jpg" alt="PiPAA YouTube Loading Example Progress."><br><br>
<img src="https://f.mwhitney.dev/projects/pipaa/demo/demoLoadingDone.jpg" alt="PiPAA YouTube Loading Example Done"></details>
<details><summary><h3>Multitasking</h3></summary>
<img src="https://f.mwhitney.dev/projects/pipaa/demo/demoMultitasking.jpg" alt="PiPAA Multitasking."></details>

> [!NOTE]
> Click on a title to display the associated image(s).

## 💾 Installation and Usage
PiPAA is a portable application and requires no installation. It should work with almost any machine running Windows 10 or 11. It may also run on older versions of Windows, but this has not been tested.

Pick an option below:

<details><summary><h4>✔️ I have Java 17 or later installed.</h4></summary>

1. Download either `PiPAA.jar` or `PiPAA.exe` from a [release](https://github.com/mwhitney57/PiPAA/releases).
2. Run the application.

> ℹ️ _If you like having an application icon, you may prefer to use the `.exe` over the `.jar`, but they should function the same on a computer with Java installed._

</details>
<details><summary><h4>❌ I do not have Java 17 or later.</h4></summary>

Download the `PiPAA-with-Java.zip` asset from a [release](https://github.com/mwhitney57/PiPAA/releases). This ZIP archive contains a Java bundle, so you are not required to install Java!

1. Extract `PiPAA-with-Java.zip`.
2. Run `PiPAA.exe` within the extracted folder.

> ⚠️ Do not move `PiPAA.exe` outside of this folder unless you have Java installed on your system. It will not be able to locate the Java bundle it shipped with and you will receive an error.

</details>

That's it! You are set and PiPAA should be working on your system.

### ⚙ Loading Media
Media can be loaded in two ways:
1. Drag and Drop
2. Copy and Paste

- Media can be dragged and dropped into any PiPAA window. If the window is empty, it will load there. If the window has media, a new window will be opened for it.
- Media can originate from a local file, or from a web source (i.e. YouTube, Google Images, etc.).
- You can quickly paste a link, source location, or the media itself by double-clicking inside an empty window.
- **Loading web media can take some time**, especially if PiPAA has to download it in order to start playback.

> [!TIP]
> You can quickly load drag and drop images if you disable `Prefer Links with Drag and Drop` in the PiPAA configuration, but they _may_ be lower resolution.

### ▶ Media Playback and Display
There are many [controls](#-shortcuts) available to you once media has loaded. They may differ depending on where the media was loaded from and what kind of media it is.

---
The following is more technical. Continue if you would like to understand how PiPAA works or its advanced features.

### 📁 Application Folder
After running PiPAA, an application folder will be created at `%APPDATA%\PiPAA`. PiPAA almost exclusively operates within this folder. PiPAA extracts its dependencies to `%APPDATA%\PiPAA\bin`. Any cached media, by default, will be saved and categorized within `%APPDATA%\PiPAA\cache`.

### 🍪 Web Media and Cookies
PiPAA can play media from many web sources. To do so, it may have to cache the media, which means downloading it to the application's cache folder. You can clear the cache at any time in the PiPAA configuration panel or by manually deleting the cache folder.

> [!IMPORTANT]
> Some websites will not let you play or download media from them without being logged in. For these sites, PiPAA can utilize a Netscape HTTP Cookie File containing login cookies. The sites' cookies are stored in this file and used to authorize downloads. To do this, follow the [guide](#how-to-create-and-use-a-cookies-file-with-pipaa).

### <img src="https://upload.wikimedia.org/wikipedia/commons/e/e6/VLC_Icon.svg" width="20"> Custom VLC Installation
Under the hood, PiPAA uses [libVLC](https://www.videolan.org/vlc/libvlc.html), developed by the VideoLAN Organization and used in [VLC media player](https://www.videolan.org/vlc/), to play video and audio. This library is bundled with PiPAA and extracted to the [application folder](#-application-folder) when the application is first launched. PiPAA allows the user to utilize the VLC installation on their system, if preferred. This option is located here: `PiPAA Tray Icon -> Config... -> Advanced`

> [!WARNING]
> Enabling this option is generally not recommended, as it can slow down application startup and cause issues if using an old version of VLC.

## ⌨ Shortcuts
PiPAA is designed to be powerful and easy-to-use. There are a bunch of default shortcuts available to get things done quicker and in a more convenient way.
Please reference the tables below with the available shortcuts and how they can be used.

<details><summary><b>🔑 Key</b></summary>

| Symbol                 | Meaning                                                                                                                        |
| :--------:             | :-----                                                                                                                         |
| `LMB`                  | Left Mouse Button                                                                                                              |
| `MMB`                  | Middle Mouse Button                                                                                                            |
| `RMB`                  | Right Mouse Button                                                                                                             |
| `*`                    | Respects `CTRL` and `SHIFT` modifiers:                                                                                         |
| `CTRL`                 | Increases severity or scale of shortcut. (e.g. skip forward more, raise volume more.)                                          |
| `SHIFT`                | Decreases severity or scale of shortcut. (e.g. skip forward less, lower volume less.)                                          |
| `CTRL` + `SHIFT`       | Maximum severity or scale of shortcut to bounds. (e.g. skip backwards to start, max volume, reset playback speed to 1x.)       |

</details>
<details><summary><b>✂️ Default Shortcuts Table</b></summary>

| Action                                   | Keyboard               | Mouse                             | Works on Video    | Works on Audio    | Works on Images   |
| :---------                               | :------                | :------                           | :---:             | :---:             | :---:             |
| Play/Pause                               | `Spacebar`             | `LMB`                             | ✔️                | ✔️                | ❌                |
| Seek Forwards`*`                         | `Right Arrow`          | `MMB` -> `RMB`                    | ✔️                | ✔️                | ❌                |
| Seek Backwards`*`                        | `Left Arrow`           | `MMB` -> `LMB`                    | ✔️                | ✔️                | ❌                |
| Volume Up`*`                             | `Up Arrow`             | `Scroll Up`                       | ✔️                | ✔️                | ❌                |
| Volume Down`*`                           | `Down Arrow`           | `Scroll Down`                     | ✔️                | ✔️                | ❌                |
| Increase Playback Rate`*`                | `+`                    | `RMB` -> `Scroll Up`              | ✔️                | ✔️                | ❌                |
| Decrease Playback Rate`*`                | `-`                    | `RMB` -> `Scroll Down`            | ✔️                | ✔️                | ❌                |
| Mute / Unmute                            | `M`                    | ➖                                 | ✔️                | ✔️                | ❌                |
| Global Mute / Unmute                     | `CTRL` + `SHIFT` + `M` | ➖                                 | ✔️                | ✔️                | ❌                |
| Seek to 0%, 10%, ... 90%                 | `0`, `1`, ... `9`      | ➖                                 | ✔️                | ✔️                | ❌                |
| Cycle Audio Track                        | `T`                    | ➖                                 | ✔️                | ✔️                | ❌                |
| Add Artwork to Audio File                | `A`                    | ➖                                 | ❌                | ✔️                | ❌                |
| Flash Window Borders                     | `B`                    | ➖                                 | ✔️                | ✔️                | ✔️                |
| Fullscreen                               | `F`                    | Double-Click `LMB`                | ✔️                | ✔️                | ✔️                |
| Show Window and Media Information        | `I`                    | ➖                                 | ✔️                | ✔️                | ✔️                |
| Relocate Window Back On-Screen           | `L`                    | ➖                                 | ✔️                | ✔️                | ✔️                |
| Move Window                              | ➖                     | `RMB` and Drag                     | ✔️                | ✔️                | ✔️                |
| Add Window                               | `SHIFT` + `A`          | Double-Click `MMB`                | ✔️                | ✔️                | ✔️                |
| Hide Window                              | `CTRL` + `H`           | ➖                                 | ✔️                | ✔️                | ✔️                |
| Hide All Windows                         | `CTRL` + `SHIFT` + `H` | ➖                                 | ✔️                | ✔️                | ✔️                |
| Duplicate Window                         | `SHIFT` + `D`          | ➖                                 | ✔️                | ✔️                | ✔️                |
| Close Window                             | `ESC`                  | Triple-Click `RMB` (Empty Window) | ✔️                | ✔️                | ✔️                |
| Close All Windows                        | `SHIFT` + `ESC`        | ➖                                 | ✔️                | ✔️                | ✔️                |
| Paste Media (Open from Clipboard)        | `CTRL` + `V`           | Double-Click `LMB` (Empty Window) | ✔️                | ✔️                | ✔️                |
| Reload Media                             | `CTRL` + `R`           | ➖                                 | ✔️                | ✔️                | ✔️                |
| Open Media Location (if Local or Cached) | `CTRL` + `O`           | ➖                                 | ✔️                | ✔️                | ✔️                |
| Save Current Media to Cache              | `CTRL` + `S`           | ➖                                 | ✔️                | ✔️                | ✔️                |
| Quick-Save Current Media to Cache        | `CTRL` + `ALT` + `S`   | ➖                                 | ✔️                | ✔️                | ✔️                |
| Close Media                              | `CTRL` + `C`           | Triple-Click `RMB`                | ✔️                | ✔️                | ✔️                |
| Close and Delete Cached Media            | `CTRL` + `SHIFT` + `D` | ➖                                 | ✔️                | ✔️                | ✔️                |
| Zoom                                     | ➖                     | `Scroll Up/Down`                   | ❌                | ❌                | ✔️                |
| Reset Zoom                               | ➖                     | `CTRL` + `MMB`                     | ❌                | ❌                | ✔️                |
| Pan (While Zoomed)                       | ➖                     | `LMB` and Drag                     | ❌                | ❌                | ✔️                |

</details>

> [!NOTE]
> _Shortcut customization will come in a future release._

## 📜 Guides
A collection of simple guides to help new PiPAA users.
<details><summary><h3>How to create and use a cookies file with PiPAA.</h3></summary>

1. Download a browser extension to easily export your cookies, or export them manually. A few open-source solutions:  
    - [Chrome](https://chromewebstore.google.com/detail/get-cookiestxt-locally/cclelndahbckbenkjhflpdbgdldlbecc)  
    - [Firefox](https://addons.mozilla.org/en-US/firefox/addon/cookies-txt/)
2. Go to the website you would like to export your cookies from.
3. Export the cookies from that site to a Netscape HTTP Cookie File. If you already have a `cookies.txt` file, copy the _content_ of the new file to your current one.
4. Ensure the file is named `cookies.txt` and is located in `%APPDATA%\PiPAA`.

It is not recommended to export your cookies from _every_ site, as that is unnecessary and could include critical logins. Only give access to the specific sites you might use with PiPAA.

</details>
<details><summary><h3>How to bypass Windows Defender SmartScreen warning.</h3></summary>
Windows Defender SmartScreen may display a warning when you first try to launch the application. This warning often displays when an application does not have a verified publisher. It is <b><i>not</i></b> an indicator of malicious code. To bypass this warning and run the application:<br><br>
<img src="https://f.mwhitney.dev/projects/pipaa/demo/demoSmartScreen1.jpg" alt="PiPAA SmartScreen1."><br><br><img src="https://f.mwhitney.dev/projects/pipaa/demo/demoSmartScreen2.jpg" alt="PiPAA SmartScreen2.">
</details>

> [!NOTE]
> _Video demonstrations and more guides will likely come in the future._

## 🤔 F.A.Q.
<details><summary><h3>What is the point of PiPAA? How should I use it?</h3></summary>

At its core, PiPAA is a basic, lightweight media player that supports loading media from the web. However, it has a few other key features which differentiate it. The always-on-top behavior of the windows allows for easy multitasking. Only have one monitor? No problem. You can still enjoy your content and you get far more control than, for example, the built-in picture-in-picture windows on most browsers. Here's a table with a few of the feature differences:
| Feature                                  | Most Browser PiP Windows    | PiPAA Windows   |
| :---------                               | :-----:                     | :---:           |
| Stay on top of other windows.            | ✔️                          | ✔️             |
| Full Media Player Controls               | ❌                          | ✔️             |
| Support for Images                       | ❌                          | ✔️             |
| Support for Audio                        | ❌                          | ✔️             |
| Easy and Limitless Resizing              | ❌                          | ✔️             |
| Save Media                               | ❌                          | ✔️             |
| Lightweight*                             | ❌                          | ✔️             |
> *_The browser PiP windows themselves may be "lightweight" but they require the entire browser to be open, and browsers are notorious memory hogs._

You can also use PiPAA as a media downloader. In fact, it has potential to be much faster than other downloaders. All it takes is a simple copy/paste or drag and drop onto a PiPAA window. In some cases, the media is _instantly_ saved.

If you have any suggestions on how to make PiPAA better in any way, even as simple as changing a shortcut, please feel free to create an [Issue](https://github.com/mwhitney57/PiPAA/issues).
</details>
<details><summary><h3>Why are PiPAA windows not showing up above my fullscreen application, like a game?</h3></summary>
  
Many applications, especially games, have the option to run in fullscreen mode. There are two main types of fullscreen modes. Without getting too technical, **Exclusive Fullscreen** is when the application takes up the entire screen and gets more control over what is displayed. Other windows, even always-on-top ones like PiPAA's, get suppressed behind it, so they do not show up on top of it. **Windowed Fullscreen** or **Borderless Fullscreen** is when the window is simply maximized to take up the entire screen, but it does not have the additional permissions and complexity of Exclusive Fullscreen.

If your application is running in **Exclusive Fullscreen** mode, PiPAA windows will not be able to show on top of it. This is a limitation of the app in its current state, but it could be possible to add this functionality in the future. PiPAA windows should still show up on top of other types of fullscreen windows. If a window shares the same always-on-top property as a PiPAA window, they will have equal priority, and the last focused window will be on top.
</details>
<details><summary><h3>How do I update PiPAA?</h3></summary>

The easiest way to update PiPAA is using the built-in over-the-air (OTA) updating feature. It should be enabled by default, but you can tweak the update settings within the `Updates` panel. Go to `PiPAA Tray Icon -> Config... -> Updates`. Here you can adjust the update frequency, as well as which PiPAA builds you would like to receive when checking for updates. Release builds are recommended and will be the most stable. If you are more technical and accepting of bugs, you can use Beta or Snapshot builds for the bleeding edge of PiPAA development.

If you prefer updating manually, you can simply replace the `PiPAA.jar` or `PiPAA.exe` file you downloaded previously. Simple as that!
</details>
<details><summary><h3>I got a Windows Defender SmartScreen warning? Is this bad? How do I bypass it?</h3></summary>

This is completely normal. When you first launch a new application that does not have a verified publisher, you may get a Windows Defender SmartScreen pop-up. This is just a precautionary warning, and it can be quickly bypassed with two clicks.
Check the guide [here](#how-to-bypass-windows-defender-smartscreen-warning).
</details>

> [!NOTE]
> Click on a question to see the answer.

## 🔐 Privacy
All projects I create adhere to a basic principle. Privacy first. Do not track the user. Only collect data that is *absolutely necessary* for the application to work, and be maximally transparent if doing so. I write this section not because I feel obligated to, but because I _want_ to. I care about privacy. I care about transparency. 

In this case, **PiPAA does not perform any tracking or collection of data.** It was designed to be noninvasive and require very little of your system under normal use. PiPAA relies on multiple projects maintained by other developers. These projects have been listed and linked to in full below in the [Binaries](#-binaries) and [Libraries](#-libraries) sections, so you can easily get more information about their privacy practices. To my knowledge, none of PiPAA's dependencies track the user. **Please** reach out if you know this to be false so that I may look into it further.

**Connections**
- PiPAA offers over-the-air (OTA) updating. This makes basic requests to the PiPAA API to get the available versions. **No user data is sent to the API. Each request is anonymous.** You can view the source code for this part of the application [here](https://github.com/mwhitney57/PiPAA/tree/main/src/dev/mwhitney/update/api). Furthermore, you can completely disable automatic update checks within the configuration.
- The `yt-dlp` and `gallery-dl` projects that PiPAA utilizes also feature OTA updating. This can be similarly controlled in the application's configuration, including being disabled entirely. However, it is not recommended to do so. These projects are critical to the process of playing and caching media from the web, and the updates often keep them working properly. If disabled and certain sources stop working, this is likely why.
- Besides that, PiPAA will only make connections to the Internet to perform tasks at the **user's** discretion (not randomly in the background).
    - For example, when PiPAA loads a video from the web, it must obviously make a request to that URL.

## 🚩 Known Issues
PiPAA is actively being developed and not yet at a Release v1.0 state. Major known issues will be catalogued here for maximum transparency until then.
- Background processes, such as downloading/caching media, will continue until terminated or finished executing, even after the application has closed.
    - This is due to how PiPAA asynchronously executes these tasks. A fix is already planned.
    - If you encounter this issue, open `Task Manager` and terminate the processes.

## 🧱 Building
PiPAA should be simple to build on your own. It requires Java 17 or later and [Maven](https://maven.apache.org/). If you already have an IDE such as [Eclipse](https://eclipseide.org/), Maven should already be available within the application.

Download the [project code archive](https://github.com/mwhitney57/PiPAA/archive/refs/heads/main.zip). If using Eclipse, import as a Maven project. Run Maven `install` on the project's [`pom.xml`](https://github.com/mwhitney57/PiPAA/blob/main/pom.xml). Use the shaded JAR file from the output. To wrap within an `EXE`, you can use [launch4j](https://launch4j.sourceforge.net/).

## ⚙ Binaries
Projects bundled with PiPAA which help make it work, even if certain files are missing on your system.

For video and audio playback.
- `libVLC` @ <a target="_blank" href="https://www.videolan.org/vlc/libvlc.html">https://www.videolan.org/vlc/libvlc.html</a>
    - Licensed under <a target="_blank" href="https://github.com/videolan/vlc/blob/master/COPYING.LIB">LGPL 2.1</a>
    - Bundled with PiPAA and exported to the application's folder in `%HOMEPATH%\AppData\Roaming\PiPAA` if not using system VLC installation in the configuration.
    - No changes to library's source code.

For media caching, attribution, and sourcing.
- `yt-dlp` @ <a target="_blank" href="https://github.com/yt-dlp/yt-dlp">https://github.com/yt-dlp/yt-dlp</a>
    - Licensed under <a target="_blank" href="https://github.com/yt-dlp/yt-dlp/blob/master/LICENSE">Unlicense</a>
    - Bundled with PiPAA and exported to the application's bin folder in `%HOMEPATH%\AppData\Roaming\PiPAA\bin` if not using system binaries in the configuration.
    - No changes to library's source code.
- `gallery-dl` @ <a target="_blank" href="https://github.com/mikf/gallery-dl">https://github.com/mikf/gallery-dl</a>
    - Licensed under <a target="_blank" href="https://github.com/mikf/gallery-dl/blob/master/LICENSE">GPL 2.0</a>
    - Bundled with PiPAA and exported to the application's bin folder in `%HOMEPATH%\AppData\Roaming\PiPAA\bin` if not using system binaries in the configuration.
    - No changes to library's source code.

For media conversions.
- `FFmpeg` @ <a target="_blank" href="https://github.com/FFmpeg/FFmpeg">https://github.com/FFmpeg/FFmpeg</a>
    - Licensed under <a target="_blank" href="https://github.com/FFmpeg/FFmpeg/blob/master/LICENSE.md">LGPL 2.1+/GPL 2.0+</a>
    - Bundled with PiPAA and exported to the application's bin folder in `%HOMEPATH%\AppData\Roaming\PiPAA\bin\ffmpeg` if not using system binaries in the configuration.
    - No changes to library's source code.
- `ImageMagick` @ <a target="_blank" href="https://github.com/ImageMagick/ImageMagick">https://github.com/ImageMagick/ImageMagick</a>
    - Licensed under <a target="_blank" href="https://github.com/ImageMagick/ImageMagick/blob/main/LICENSE">ImageMagick License</a>
    - Bundled with PiPAA and exported to the application's bin folder in `%HOMEPATH%\AppData\Roaming\PiPAA\bin\imagemagick` if not using system binaries in the configuration.
    - No changes to library's source code.
    
## 📖 Libraries
For VLC/libVLC integration, allowing video and audio playback. Huge thanks to the developers of vlcj, including [caprica](https://github.com/caprica).
- `vlcj` @ <a target="_blank" href="https://github.com/caprica/vlcj">https://github.com/caprica/vlcj</a>
    - Licensed under <a target="_blank" href="https://github.com/caprica/vlcj/blob/master/doc/README.LICENSE">GPL 3.0</a>
    - No changes to library's source code.
    
For a much prettier and more functional system tray experience.
- `SystemTray` @ <a target="_blank" href="https://github.com/dorkbox/SystemTray">https://github.com/dorkbox/SystemTray</a>
    - Licensed under <a target="_blank" href="https://github.com/dorkbox/SystemTray/blob/master/LICENSE">Apache 2.0</a>
    - No changes to library's source code.

For simplifying the configuration window's layout.
- `MiGLayout` @ <a target="_blank" href="https://github.com/mikaelgrev/miglayout">https://github.com/mikaelgrev/miglayout</a>
    - Licensed under <a target="_blank" href="https://github.com/mikaelgrev/miglayout/blob/master/src/site/resources/docs/license.txt">BSD-3</a>
    - No changes to library's source.

Miscellaneous:
- `commons-io` @ <a target="_blank" href="https://github.com/apache/commons-io">https://github.com/apache/commons-io</a>
    - Licensed under <a target="_blank" href="https://github.com/apache/commons-io/blob/master/LICENSE.txt">Apache 2.0</a>
    - No changes to library's source code.
    
- `public-suffix-list` @ <a target="_blank" href="https://github.com/whois-server-list/public-suffix-list">https://github.com/whois-server-list/public-suffix-list</a>
    - Licensed under <a target="_blank" href="https://github.com/whois-server-list/public-suffix-list/blob/master/LICENSE">WTFPL</a>
    - No changes to library's source code.
    
- `json` @ <a target="_blank" href="https://github.com/stleary/JSON-java">https://github.com/stleary/JSON-java</a>
    - <a target="_blank" href="https://github.com/stleary/JSON-java/blob/master/LICENSE">Public</a>
    - <a target="_blank" href="https://mvnrepository.com/artifact/org.json/json">Maven</a>
    - No changes to library's source code.
    
- `slf4j-simple` @ <a target="_blank" href="https://www.slf4j.org/index.html">https://www.slf4j.org/index.html</a>
    - Licensed under the <a target="_blank" href="https://www.slf4j.org/license.html">MIT License</a>
    - No changes to library's source code.

### ℹ Additional Information
Many thanks again to all developers who created or contributed to the projects listed in the [Binaries](#-binaries) and [Libraries](#-libraries) sections. These libraries heavily streamlined the process of developing PiPAA, and they have vastly increased its capability.

**System Compatibility**<br/>
PiPAA was designed just for computers running versions of Windows. Other operating systems are highly unlikely to work. Support for them may come with future updates, but do not expect positive results on other systems until then.

**Java Compatibility**<br/>
PiPAA is built using Java 17. If you do not have Java 17 or higher installed, you may download the `PiPAA-with-Java.zip` asset from a full PiPAA release. Unzip after downloading and keep `PiPAA.exe` in the unzipped folder.
To update, simply replace the `PiPAA.exe` file with the new one. Instead, you can use PiPAA's over-the-air updating feature within the app to automatically update for you.
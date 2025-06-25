# IntelliSay

Makes intellij more useful for screen reader users by adding various features
<ul>
<li>Play a sound when navigating to a line with a warning</li>
<li>Play a sound when navigating to a line with an error</li>
<li>Play a different sound when caret enters an error</li>
<li>Play a sound when navigating to line with a breakpoint (includes when breakpoint is hit)</li>
<li>Play a sound when adding or removing breakpoints</li>
<li>Play a sound compilation succeeds or fails</li>
<li>Help dialog containing all the actions and their shortcuts</li>
<li>Ability to say a variable value while debugging via keyboard shortcut or quick action</li>
<li>Ability to say the file name via keyboard shortcut or quick action</li>
<li>Automatically announce file name when opened from the project explorer</li>
<li>Announce when IDE enters or exists dumb mode (when functionality is limited)</li>
<li>Read compile errors and warnings for compiled languages</li>
<li>Announce the current content including method name, arguments and class name</li>
<li>If python plugin installed: Set the python interpreter via a dialog and typing the path to the folder that contains the virtual env (will automatically append bin/python), or an absolute path to a python binary</li>
</ul>
<br/>
Sounds can be customised via a setting and each sound can be enabled or disabled via a setting, keyboard shortcut or quick action,
the volume can also be adjusted.

This has only been tested on MacOS so far.

## Prerequisites

### MacOS

If VoiceOver can be controlled by AppleScript then IntelliSay will use Voiceover, if not it will use the built-in say command.

To enable AppleScript with VoiceOver open the VoiceOver utility app and on the General tab select "Allow VoiceOver to be controlled with AppleScript"
To use the built-in say command even when VoiceOver is setup use the quick actions (press shift twice) to find the setting "Use VoiceOver" 

### Linux

For spoken words you will need a say command that accepts a string of text.
The standard way for Ubuntu is to use the speech dispatcher.
Install with the below

```commandline
sudo apt-get install speech-dispatch
```

Then locate the spd-say command with the below

```commandline
which spd-say
```

Then create a symlink from your /usr/local/bin folder to the spd-say with the below

```commandline
sudo ln -s your-spd-say-location /usr/local/bin/say
```

### Windows (untested)

For spoken words you will need a say command that accepts a string of text.
The instructions below have not been tested or verified, so use at your own risk!
[Wsay](https://github.com/p-groarke/wsay) is a library that provides an executable called `wsay.exe` which
you can download from the [Wsay releases page](https://github.com/p-groarke/wsay/releases).
Rename the file to `say.exe` and the containing folder to your PATH variable (instructions below).

1. Right-click the Start menu and select System.
2. Go to Advanced system settings > Environment Variables…
3. Select PATH under "System variables" or "User variables."
4. Click Edit.
5. Click New
6. Enter the directory path and click OK.

### Downloading
Download the latest release called intellisay.zip from the [latest release page](https://github.com/hbk619/intellisay/releases/latest)

#### Add to IntelliJ

Press shift twice to bring up the quick actions menu and select "Install plugin from Disk".
Select the downloaded zip and restart IntelliJ. You should hear "IntelliSay has started",
if not check [troubleshooting](#troubleshooting)

### Building
If you prefer to build yourself you will need to install a JDK ([OracleJDK](https://www.oracle.com/java/technologies/downloads/#jdk21-mac) can be downloaded or
brew can [install OpenJDK](https://stackoverflow.com/a/65601197))

Clone this repo

`
git clone https://github.com/hbk619/intellisay
`

Build the plugin

`
./gradlew buildPlugin
`

#### To add to IntelliJ
Press shift twice to bring up the quick actions menu and select "Install plugin from Disk".
Open the file ./build/distributions/intellisay-1.0-SNAPSHOT.zip then restart IntelliJ. You should hear "IntelliSay has started",
if not check [troubleshooting](#troubleshooting)

### Shortcuts

On Windows/Linux command is meta and option is alt.

- control command shift option h - Help dialog
- control command option l - Say File Name
- control command option v - Say Variable
- control command option shift e - Say Number of Errors
- control command option shift i - Say Issue
- control command option c - Say compile errors
- control command option h - Toggle Automatic File Name Announcement
- control command option w - Toggle Warning Beep
- control command option e - Toggle Error Beep
- control command option b - Toggle Breakpoint Beep
- control command option v - Toggle VoiceOver
- control command option d - Toggle Dumb Mode Announcements
- control command option shift down - Volume down
- control command option shift up - Volume up

## Customising sounds

### Possible sounds
You can customise the sounds by opening the preferences panel with command comma and navigating to 
Tools -> Intelli Say. To the right there is a form with an input field for Sounds location
enter a path on your file system to a folder that contains you sound files

Below is the list of file names to use to replace sounds

- error.wav
- warning.wav
- compile_error.wav
- compile_warning.wav
- breakpoint-added.wav
- breakpoint-removed.wav
- breakpoint.wav

## Tests

Tests require a mock jdk which you can get from Intellij community sources

```commandline
cd ~
mkdir mock-jdk
cd mock-jdk
git init
git remote add -f origin git@github.com:JetBrains/intellij-community.git
git config core.sparseCheckout true
echo "java/mockJDK-1.9/jre/lib" > .git/info/sparse-checkout
git pull origin master --depth 1
```

## Troubleshooting
### I can't hear "IntelliSay has started"
From a terminal run `say "hello there"`. If this fails, check the output, you might not have a say command in which
case go to [prerequisites](#prerequisites) and follow instructions.

Check your default sound output device on your system.

### I set it to use Voiceover but it's using say
Check [Allow VoiceOver to be controlled with AppleScript](#macos) is selected and Voiceover is on (control command option v)
or "Use Voiceover when available" is selected in the preferences dialog (File -> Settings -> Tools -> IntelliSay) .
Restart IntelliJ

### I can't hear beeps
Check beeps for warnings/errors etc are enabled, you can use the [shortcuts](#shortcuts) or preferences dialog
(File -> Settings -> Tools -> IntelliSay)

Check your default sound output device on your system.

### Shortcuts don't work on Windows/linux

There is an [open issue about the meta key on Windows/Linux](https://youtrack.jetbrains.com/issue/IJPL-61243/Unable-to-use-Windows-as-a-modifier-key-in-keymap)
with a workaround in the IDE, select Help then Edit Custom Properties... and paste the below into the file that opens and restart the IDE

`keymap.windows.as.meta=true`

Alternatively, if you're comfortable with xmodmap on linux you can use it to globally remap the super key to meta e.g.

```
remove mod4 = Super_L
add mod4 = Meta_L
```

However, this may affect other applications so use with caution!

### The sounds sound awful!
If it's not just personal taste, try lowering the volume (control command option shift down or control meta alt shift down).

## Thank yous
Many thanks to the work from [AudioCue](https://github.com/philfrei/AudioCue-maven/tree/main) who's source
has been copied into this repo for easier auditing by those who work for places that want tighter control
over installed software, along with unrestricting the maximum volume. Without this [audio on Linux often crashed](https://github.com/hbk619/intellisay/issues/1)!
The original Java was auto converted to Kotlin with IntelliJ.
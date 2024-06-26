# IntelliSay

Makes intellij more useful for screen reader users by various features
<ul>
<li>Play a sound when navigating to a line with a warning</li>
<li>Play a sound when navigating to a line with an error</li>
<li>Play a different sound when caret enters an error</li>
<li>Play a sound when navigating to line with a breakpoint (includes when breakpoint is hit)</li>
<li>Play a sound when adding or removing breakpoints</li>
<li>Ability to say a variable value while debugging via keyboard shortcut or quick action</li>
<li>Ability to say the file name via keyboard shortcut or quick action</li>
<li>Automatically announce file name when opened from the project explorer</li>
</ul>
<br/>
Sounds can be customised via a setting and each sound can be enabled or disabled via a setting, keyboard shortcut or quick action

This has only been tested on MacOS so far.

## Prerequisites

For spoken words you will need a say command that accepts a string of text

### MacOS

This is built in and nothing is required

### Linux

You will need to map the say command to something. The standard way for Ubuntu is to use the speech dispatcher.
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


### Building
Install a JDK ([OracleJDK](https://www.oracle.com/java/technologies/downloads/#jdk21-mac) can be downloaded or brew can [install OpenJDK](https://stackoverflow.com/a/65601197))

Clone this repo

`
git clone https://github.com/hbk619/intellisay
`

Build the plugin

`
./gradlew buildPlugin
`

### Add to IntelliJ

Press shift twice to bring up the quick actions menu and select "Install plugin from Disk".
Open the file ./build/distributions/intellisay-1.0-SNAPSHOT.zip then restart IntelliJ

### Shortcuts

- control command option l - Say File Name
- control command option v - Say Variable
- control command option shift e - Say Number of Errors
- control command option shift i - Say Issue
- control command option h - Toggle Automatic File Name Announcement
- control command option w - Toggle Warning Beep
- control command option e - Toggle Error Beep
- control command option b - Toggle Breakpoint Beep

## Customising sounds

### Possible sounds
You can customise the sounds by opening the preferences panel with command comma and navigating to 
Tools -> Intelli Say. To the right there is a form with an input field for Sounds location
enter a path on your file system to a folder that contains you sound files

Below is the list of file names to use to replace sounds

- error.wav
- warning.wav
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
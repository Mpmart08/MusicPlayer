# MusicPlayer

MusicPlayer is an open source music player for Mac and Windows.

![MusicPlayer](https://cloud.githubusercontent.com/assets/9737241/16364455/56527484-3ba4-11e6-97de-d8b9869f36a4.PNG)


## Features

##### Playlists
* Add songs to a playlist by:
  * Clicking the "Add Song to Playlist" button when a song is selected.
  * Dragging a song, artist, or album and dropping on the desired playlist to add the dragged contents to the playlist.
  * Selecting multiple songs using `CTRL` or `SHIFT` keys, then dragging & dropping on a playlist.

##### Automatic Music Library Updates
* The app's music library updates on startup if a song has been added or deleted from the user's music directory.

##### Compatible with Media Keys on Keyboard
* The `Play/Pause`, `Next`, and `Previous` media keys can be used to play, pause, or skip songs.
* Please note that this feature is only available on Windows.

##### Search
* Easily search for any artist, album, or song.
![MusicPlayerSearch](https://cloud.githubusercontent.com/assets/9737241/16364460/818dbf1e-3ba4-11e6-9568-babbc3b059cc.gif)


## Installation Instructions

1. Download the `MusicPlayer.jar` file from the `releases` directory in the repository. 

2. Create a directory in your computer to store the JAR file. When the app runs for the first time, it will create an `img` directory and a `library.xml` file to store song data in the directory where the JAR is located.

3. **NOTE: THIS STEP IS ONLY NEEDED ON WINDOWS.** Download the `JIntellitype.dll` file from the `releases` directory and place it in the directory created in Step 2. This file is required by the `jintellitype` library to enable media key functionality on keyboards. The app will not start properly if the `JIntellitype.dll` file is not in the same directory as the `MusicPlayer.jar` file.

4. Run the app by double-clicking the `MusicPlayer.jar` file.

(OPTIONAL)

5. Create a desktop shortcut for `MusicPlayer.jar`.

6. Download `MusicPlayer_Icon.ico` from the `releases` directory and set it as the icon for the desktop shortcut.


## Supported File Types

MusicPlayer supports the following file types:
```java
// MP3
case "mp3":
// MP4
case "mp4":
case "m4a":
case "m4v":
// WAV
case "wav":
```


## Build

The project was built in eclipse with the following directory structure.

* `lib`: Contains [jaudiotagger](https://bitbucket.org/ijabz/jaudiotagger/overview) library used for audio metatagging and [jintellitype](https://github.com/melloware/jintellitype) library used to enable media key functionality on keyboards.

* `releases`: Contains the JAR file for the latest release

* `src`: Contains project source code


## License

Code released under the MIT license. See [LICENSE](https://github.com/Mpmart08/MusicPlayer/blob/master/LICENSE.txt) for details.

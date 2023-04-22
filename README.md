# `Smart Gallery`

![Min API](https://img.shields.io/badge/API-21%2B-orange.svg?style=flat)
[![Platform](https://img.shields.io/badge/platform-Android-green.svg)](http://developer.android.com/index.html)

<b>Smart Gallery</b> Android application is a fast, reliable, and beautiful media manager; with lots
of features you have been missing on your default Android gallery:
<ul>
<li>Completely ad free</li>
<li>Sorting and grouping files</li>
<li>Editing photos and videos</li>
<li>Privacy protection in various ways(pin, pattern, and even fingerprint!)</li>
<li>Customizing app's colors and themes</li>
<li>Recycle bin</li>
<li>Favorites</li>
<li>internal video player</li>
<li>the ability to view SVG, GIF, and other rare media formats</li>
<li>And so much more ....</li>
</ul>

The trello board I use for keeping track of all the tickets of this project is shared
in [here](https://trello.com/b/PkJ1D5pO/smart-gallery).

## Video demo

<details>
<summary><b>Overall video demo</b></summary>


https://user-images.githubusercontent.com/8706521/227848954-49983ef8-609a-4a19-ad26-8807ebd2f67b.mp4
</details>

## Screenshots

<p>
<img src="/media/screenshot1.png" width="32%"/>
<img src="/media/screenshot2.png" width="32%"/>
<img src="/media/screenshot3.png" width="32%"/>
</p>
<p>
<img src="/media/screenshot4.png" width="32%"/>
<img src="/media/screenshot5.png" width="32%"/>
<img src="/media/screenshot6.png" width="32%"/>
</p>

## Tech Stack

- [Kotlin](https://kotlinlang.org/) - First class and official programming language for Android
  development.
- [MVVM + Clean Architecture](https://developer.android.com/jetpack/guide) - Official recommended
  architecture for building robust, production-quality apps (the legacy codebase didn't have any
  specific architecture and was sorted according to class types; I'm still in the process of
  applying a complete architecture to it).
- [Android Jetpack](https://developer.android.com/jetpack) - Jetpack is a suite of libraries to help
  developers build state-of-the-art applications.
    - [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel) - The
      ViewModel is designed to store and manage UI-related data in a lifecycle-aware manner.
    - [Room](https://developer.android.com/topic/libraries/architecture/room) - The Room library
      provides an abstraction layer over SQLite to allow for more robust database access.
    - [SharedPreferences](https://developer.android.com/reference/kotlin/android/content/SharedPreferences)
        - A native library from Android for saving key-value pairs to the disk.
- [Reprint](https://github.com/ajalt/reprint) - An Android library for fingerprint recognition.
- [Glide](https://bumptech.github.io/glide/) - An image loading library.

### Contributors

Main developer: [Hojat Ghasemi](mailto:hojat72elect@gmail.com)
<br/>



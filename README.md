# GDX-Video
A LibGDX cross platform video rendering extension

## Status
This repo is a fork of [an old project](https://github.com/anonl/gdx-video) with an aim on adapting it to the modern LibGDX usage.
As in the original library, the only native backends supported are Desktop (LWJGL3) and Android (min SDK version is 17).
It's still under active development and not yet published anywhere as maven artifacts, however you can download beta jars from [release section](https://github.com/crashinvaders/gdx-video/releases).

Also you can assemble the latest version of the library locally:

#### 1. Install the lib to the local maven repo
`./gradlew installAll`

#### 2. Add the library as a maven dependency to your project
```
repositories {
    mavenLocal()
}

// Core module
dependencies {
    api "com.crashinvaders.gdxvideo:gdx-video-core:0.2.0"
}

// Desktop (LWJGL3) module
dependencies {
    api "com.crashinvaders.gdxvideo:gdx-video-desktop:0.2.0"
}

// Android module
dependencies {
    api "com.crashinvaders.gdxvideo:gdx-video-android:0.2.0"
}
```

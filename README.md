# GDX-Video
A LibGDX cross platform video rendering extension

## Status
This repo is a fork of an old project with an aim on adapting it to the modern LibGDX usage.
As in the original library, the only native backends supported are Desktop (LWJGL3) and Android (min SDK version is 17).
It's still under active development and not yet published anywhere as maven artifacts, howerver you can assemble library locally to play with it:

#### 1. Install the lib to the local maven repo
`./gradlew installAll`

#### 2. Add the library as a maven dependecy to your project
```
repositories {
    mavenLocal()
}

// Core module
dependencies {
    api "com.metaphore.gdx.video:gdx-video-core:0.1.0"
}

// Desktop (LWJGL3) module
dependencies {
    api "com.metaphore.gdx.video:gdx-video-desktop:0.1.0"
}

// Android module
dependencies {
    api "com.metaphore.gdx.video:gdx-video-android:0.1.0"
}
```

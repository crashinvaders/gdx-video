package com.badlogic.gdx.video.demo.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.video.demo.DemoApp;

public class DemoDesktopLauncher {
    public static void main (String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("GDX-Video Demo");
        config.setWindowedMode(640, 480);

        new Lwjgl3Application(new DemoApp(), config);
    }
}

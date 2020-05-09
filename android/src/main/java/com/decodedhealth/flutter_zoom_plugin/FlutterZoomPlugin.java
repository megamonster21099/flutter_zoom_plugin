package com.decodedhealth.flutter_zoom_plugin;

import android.app.Activity;

import io.flutter.plugin.common.PluginRegistry.Registrar;
//import io.flutter.embedding.engine.plugins.activity.ActivityAware;
//import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;

/**
 * FlutterZoomPlugin
 */
public class FlutterZoomPlugin {
    private Activity activity;

    /**
     * Plugin registration.
     */
    public static void registerWith(Registrar registrar) {
        registrar.platformViewRegistry().registerViewFactory("flutter_zoom_plugin", new ZoomViewFactory(registrar.messenger(), registrar.activity()));
    }

//    @Override
//    public void onAttachedToActivity(ActivityPluginBinding binding) {
//        activity = binding.getActivity();
//    }
//
//    @Override
//    public void onDetachedFromActivityForConfigChanges() {
//    }
//
//    @Override
//    public void onReattachedToActivityForConfigChanges(ActivityPluginBinding binding) {
//    }
//
//    @Override
//    public void onDetachedFromActivity() {
//    }
}

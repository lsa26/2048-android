package com.uberspot.a2048;

import android.app.Application;
import android.util.Log;
import io.rollout.android.Rox;

public class App extends Application {

    public static final String TAG = "App";
    public static Flags flags;

    @Override
    public void onCreate() {
        super.onCreate();

        // 1) Créer/retenir le conteneur de flags
        flags = new Flags();

        // 2) Enregistrer les flags
        Rox.register(flags);

        // 3) Setup avec la clé (lue via meta-data rox.apiKey du Manifest)
        //    NB: on n’écrit PAS la clé ici — Rox la lit depuis le Manifest.
        Rox.setup(this);

        // 4) Exemple d’usage sécurisé (éviter == pour les Strings)
        if (flags.enableTutorial.isEnabled()) {
            Log.i(TAG, "Tutorial enabled");
            // TODO: afficher le tutoriel
        }

        String color = flags.titleColors.getValue();
        if ("Blue".equals(color)) {
            Log.i(TAG, "Title color is blue");
        } else if ("Green".equals(color)) {
            Log.i(TAG, "Title color is green");
        } else if ("Yellow".equals(color)) {
            Log.i(TAG, "Title color is yellow");
        } else { // White ou fallback
            Log.i(TAG, "Title color is white");
        }
    }
}

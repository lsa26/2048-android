package com.uberspot.a2048;

import io.rollout.configuration.RoxContainer;
import io.rollout.flags.RoxFlag;
import io.rollout.flags.RoxString;

/**
 * Conteneur de feature flags pour CloudBees Feature Management (ROX)
 */
public class Flags implements RoxContainer {

    // Booléen : affiche/masque le tutoriel (par défaut OFF)
    public RoxFlag enableTutorial = new RoxFlag(false);

    // Multi-varié : couleur du titre
    public RoxString titleColors = new RoxString(
            "White",
            new String[] { "White", "Blue", "Green", "Yellow" }
    );

    // Multi-varié : couleur d'arrière-plan
    public RoxString backgroundColor = new RoxString(
            "White",
            new String[] { "White", "Blue", "Green", "Yellow", "Dark" }
    );
}

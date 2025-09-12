package com.uberspot.a2048;

import io.rollout.configuration.RoxContainer;
import io.rollout.flags.RoxFlag;
import io.rollout.flags.RoxString;

// Conteneur de feature flags
public class Flags implements RoxContainer {
    // Booléen: désactiver le tutoriel par défaut
    public RoxFlag enableTutorial = new RoxFlag(false);

    // Multivarié: couleur du titre, valeur par défaut "White"
    public RoxString titleColors = new RoxString("White", new String[] {"White", "Blue", "Green", "Yellow"});
}


public RoxString backgroundColor = new RoxString(
    "White",
    new String[] {"White", "Blue", "Green", "Yellow", "Dark"}
);


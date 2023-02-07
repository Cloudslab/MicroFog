package com.microfog.controlengine.controllers;

import javax.inject.Singleton;
import java.util.TimerTask;

@Singleton
public class PeriodicSchedular extends TimerTask {

     PlacementLogicExecutor placementLogicExecutor;

    @Override
    public void run() {
        placementLogicExecutor.executePlacement();
    }

    public void submitLogicExecuter(PlacementLogicExecutor placementLogicExecutor) {
        this.placementLogicExecutor = placementLogicExecutor;
    }
}

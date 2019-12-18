package com.kpstv.youtube.utils;

import android.support.design.widget.AppBarLayout;

/**
 * App bar collapsing state
 * @author Paulo Caldeira <paulo.caldeira@acin.pt>.
 */
public abstract class AppBarStateChangeListener implements AppBarLayout.OnOffsetChangedListener {
    // State
    public enum State {
        EXPANDED,
        COLLAPSED,
        IDLE
    }

    private State mCurrentState = State.IDLE;

    @Override
    public final void onOffsetChanged(AppBarLayout appBarLayout, int i) {
        if (i == 0) {
            if (mCurrentState != State.EXPANDED) {
                onStateChanged(appBarLayout, State.EXPANDED);
            }
            mCurrentState = State.EXPANDED;
        } else if (Math.abs(i) >= appBarLayout.getTotalScrollRange()) {
            if (mCurrentState != State.COLLAPSED) {
                onStateChanged(appBarLayout, State.COLLAPSED);
            }
            mCurrentState = State.COLLAPSED;
        } else {
            if (mCurrentState != State.IDLE) {
                onStateChanged(appBarLayout, State.IDLE);
            }
            mCurrentState = State.IDLE;
        }
    }

    /**
     * Notifies on state change
     * @param appBarLayout Layout
     * @param state Collapse state
     */
    public abstract void onStateChanged(AppBarLayout appBarLayout, State state);
}
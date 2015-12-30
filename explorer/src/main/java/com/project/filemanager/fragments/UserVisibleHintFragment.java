package com.project.filemanager.fragments;

import android.app.Fragment;

public abstract class UserVisibleHintFragment extends Fragment {

    private boolean mResumed;

    @Override
    public final void setUserVisibleHint(boolean isVisibleToUser) {
        final boolean needUpdate = mResumed
                && isVisibleToUser != this.getUserVisibleHint();
        super.setUserVisibleHint(isVisibleToUser);
        if (needUpdate) {
            if (isVisibleToUser) {
                this.onVisible();
            } else {
                this.onInvisible();
            }
        }
    }

    @Override
    public final void onResume() {
        super.onResume();
        mResumed = true;
        if (this.getUserVisibleHint()) {
            this.onVisible();
        }
    }

    @Override
    public final void onPause() {
        super.onPause();
        mResumed = false;
        this.onInvisible();
    }

    /**
     * Called when onResume was called and userVisibleHint is set to true or
     * vice-versa
     */
    protected abstract void onVisible();

    /**
     * Called when onStop was called or userVisibleHint is set to false
     */
    protected abstract void onInvisible();
}

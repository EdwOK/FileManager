package com.project.filemanager.activities;

import com.project.filemanager.adapters.ExplorerTabsAdapter;
import com.project.filemanager.fragments.BrowserFragment;

public final class ExplorerActivity extends BaseExplorerActivity {
    @Override
    public BrowserFragment getCurrentBrowserFragment() {
        return ExplorerTabsAdapter.getCurrentBrowserFragment();
    }
}

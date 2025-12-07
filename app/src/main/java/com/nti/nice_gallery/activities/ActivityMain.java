package com.nti.nice_gallery.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.nti.nice_gallery.R;
import com.nti.nice_gallery.fragments.FragmentMediaAll;
import com.nti.nice_gallery.fragments.FragmentMediaTree;
import com.nti.nice_gallery.fragments.FragmentSettings;

public class ActivityMain extends AppCompatActivity {

    private static final String TAG_MEDIA_ALL = "fragment_media_all";
    private static final String TAG_MEDIA_TREE = "fragment_media_tree";
    private static final String TAG_SETTINGS = "fragment_settings";

    private static final int REQUEST_CODE_MANAGE_STORAGE = 101;

    private FragmentMediaAll fragmentMediaAll;
    private FragmentMediaTree fragmentMediaTree;
    private FragmentSettings fragmentSettings;

    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews(savedInstanceState);
        checkAndRequestManageExternalStoragePermission();
    }

    private void initViews(Bundle savedInstanceState) {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        if (savedInstanceState == null) {
            fragmentMediaAll = (FragmentMediaAll) getSupportFragmentManager()
                    .findFragmentByTag(TAG_MEDIA_ALL);
            fragmentMediaTree = (FragmentMediaTree) getSupportFragmentManager()
                    .findFragmentByTag(TAG_MEDIA_TREE);
            fragmentSettings = (FragmentSettings) getSupportFragmentManager()
                    .findFragmentByTag(TAG_SETTINGS);
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.bottom_menu_button_all) {
                showFragmentAll();
                return true;
            } else if (itemId == R.id.bottom_menu_button_folders) {
                showFragmentTree();
                return true;
            } else if (itemId == R.id.bottom_menu_button_settings) {
                showFragmentSettings();
                return true;
            }

            return false;
        });

        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.bottom_menu_button_all);
        }
    }

    private void showFragmentAll() {
        if (fragmentMediaAll == null) {
            fragmentMediaAll = new FragmentMediaAll();
        }
        showFragment(fragmentMediaAll, TAG_MEDIA_ALL);
    }

    private void showFragmentTree() {
        if (fragmentMediaTree == null) {
            fragmentMediaTree = new FragmentMediaTree();
        }
        showFragment(fragmentMediaTree, TAG_MEDIA_TREE);
    }

    private void showFragmentSettings() {
        if (fragmentSettings == null) {
            fragmentSettings = new FragmentSettings();
        }
        showFragment(fragmentSettings, TAG_SETTINGS);
    }

    private void showFragment(Fragment fragment, String tag) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        if (currentFragment != null) {
            transaction.hide(currentFragment);
        }

        if (fragment.isAdded()) {
            transaction.show(fragment);
        } else {
            transaction.add(R.id.contentFrame, fragment, tag);
        }

        currentFragment = fragment;
        transaction.commit();
    }

    private void checkAndRequestManageExternalStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, REQUEST_CODE_MANAGE_STORAGE);
            }
        }
    }
}
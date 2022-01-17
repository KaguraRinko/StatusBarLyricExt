package io.baolong24.statuslyricext;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.Toolbar;

import androidx.fragment.app.FragmentActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import java.util.HashMap;
import java.util.Map;

import io.baolong24.statuslyricext.misc.Constants;
import io.baolong24.statuslyricext.misc.RomUtils;

public class SettingsActivity extends FragmentActivity {

    private final static Map<String, String> mUrlMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.collapsing_toolbar_base_layout);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content_frame, new SettingsFragment())
                    .commit();
        }

        Toolbar collapsingToolbar = findViewById(R.id.action_bar);
        setActionBar(collapsingToolbar);


        // add urls
        mUrlMap.put("app", "https://github.com/baolong24/StatusBarLyricExt");
        mUrlMap.put("lyricview", "https://github.com/markzhai/LyricView");

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(Constants.NOTIFICATION_CHANNEL_LRC, "LRC", NotificationManager.IMPORTANCE_MIN);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private static boolean isNotificationListenerEnabled(Context context) {
        if (context == null) return false;
        String pkgName = context.getPackageName();
        final String flat = Settings.Secure.getString(context.getContentResolver(), Constants.SETTINGS_ENABLED_NOTIFICATION_LISTENERS);
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (String name : names) {
                final ComponentName cn = ComponentName.unflattenFromString(name);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static String getAppVersionName(Context context) {
        String versionName=null;
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = pi.versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return versionName;
    }

    public static class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener {

        private SwitchPreference mEnabledPreference;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            mEnabledPreference = findPreference(Constants.PREFERENCE_KEY_ENABLED);
            if (!(RomUtils.checkIsMeizuRom() || RomUtils.checkIsbaolong24Rom() || RomUtils.checkIsexTHmUIRom())) {
                mEnabledPreference.setEnabled(false);
                mEnabledPreference.setTitle(R.string.unsupport);
                mEnabledPreference.setSummary(R.string.unsupport_rom);
            }
            if (RomUtils.checkIsEvolutionRom() && Build.VERSION.RELEASE.equals("12")) {
                mEnabledPreference.setEnabled(false);
                mEnabledPreference.setTitle(R.string.unsupport);
                mEnabledPreference.setSummary(R.string.unsupport_evolution);
            }
            if (mEnabledPreference != null) {
                mEnabledPreference.setChecked(isNotificationListenerEnabled(getContext()));
                mEnabledPreference.setOnPreferenceClickListener(this);
            }
            Preference appInfoPreference = findPreference("app");
            if (appInfoPreference != null) {
                appInfoPreference.setSummary(getAppVersionName(getContext()));
            }
            PreferenceCategory aboutCategory = findPreference(Constants.PREFERENCE_KEY_ABOUT);
            if (aboutCategory != null) {
                for (int i = 0; i < aboutCategory.getPreferenceCount(); i++) {
                    aboutCategory.getPreference(i).setOnPreferenceClickListener(this);
                }
            }

        }

        @Override
        public void onResume() {
            super.onResume();
            if (mEnabledPreference != null) {
                mEnabledPreference.setChecked(isNotificationListenerEnabled(getContext()));
            }
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            if (preference == mEnabledPreference) {
                startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
            } else {
                String url = mUrlMap.get(preference.getKey());
                if (TextUtils.isEmpty(url)) return false;
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
            return true;
        }
    }
}
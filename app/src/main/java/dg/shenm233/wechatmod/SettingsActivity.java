package dg.shenm233.wechatmod;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;

import static dg.shenm233.wechatmod.Common.dipTopx;
import static dg.shenm233.wechatmod.ObfuscationHelper.isSupportedVersion;

@SuppressLint("WorldReadableFiles")
public class SettingsActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {
    private SharedPreferences prefs;

    private Preference mLicense;
    private CheckBoxPreference mForceStatusBarColor;
    private CheckBoxPreference mHideLauncherIcon;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences(Common.MOD_PREFS, Context.MODE_WORLD_READABLE);
        addPreferencesFromResource(R.xml.preference);
        PackageManager pm = getPackageManager();
        StringBuilder ver = new StringBuilder("MOD Version: ");
        ver.append(BuildConfig.VERSION_NAME).append("\n");
        ver.append("Wechat Version: ");
        try {
            int versionCode = pm.getPackageInfo(Common.WECHAT_PACKAGENAME, 0).versionCode;
            String versionName = pm.getPackageInfo(Common.WECHAT_PACKAGENAME, 0).versionName;
            ver.append(versionName).append("(").append(versionCode).append(")");
            if (isSupportedVersion(versionCode, versionName) < 0) {
                ver.append("\n").append(getString(R.string.unsupported));
            } else {
                ver.append("\n").append(getString(R.string.supported));
            }
        } catch (PackageManager.NameNotFoundException e) {
            ver.append("not installed.");
        }
        findPreference("version").setSummary(ver);

        findPreference("dev").setSummary("shenm233 (darkgenlotus@gmail.com)");
        findPreference("donate").setSummary(getText(R.string.alipay) + " darkgentry@hotmail.com");
        findPreference("donate").setOnPreferenceClickListener(this);

        mLicense = findPreference("license");
        mForceStatusBarColor = (CheckBoxPreference) findPreference(Common.KEY_FORCE_STATUSBAR_COLOR);
        mHideLauncherIcon = (CheckBoxPreference) findPreference("hide_launcher_icon");
        mLicense.setOnPreferenceClickListener(this);
        mForceStatusBarColor.setOnPreferenceChangeListener(this);
        mHideLauncherIcon.setOnPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        String navMode = prefs.getString(Common.KEY_SETNAV, "default");


        String actionBarColor = prefs.getString(Common.KEY_ACTIONBAR_COLOR, "#263238");


        boolean forceStatusbarColor = prefs.getBoolean(Common.KEY_FORCE_STATUSBAR_COLOR, false);
        mForceStatusBarColor.setChecked(forceStatusbarColor);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        prefs = null;
        mLicense.setOnPreferenceClickListener(null);
        mForceStatusBarColor.setOnPreferenceChangeListener(null);
        mHideLauncherIcon.setOnPreferenceChangeListener(null);
        findPreference("donate").setOnPreferenceClickListener(null);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mForceStatusBarColor) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(Common.KEY_FORCE_STATUSBAR_COLOR, (boolean) newValue);
            editor.commit();
            Toast.makeText(this, R.string.preference_reboot_note, Toast.LENGTH_SHORT).show();
            return true;
        } else if (preference == mHideLauncherIcon) {
            PackageManager packageManager = this.getPackageManager();
            ComponentName aliasName = new ComponentName(this, Common.MOD_PACKAGENAME + ".SettingsActivityLauncher");
            if ((boolean) newValue) {
                packageManager.setComponentEnabledSetting(aliasName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            } else {
                packageManager.setComponentEnabledSetting(aliasName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference == mLicense) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_MAIN);
            intent.setClass(this, LicenseActivity.class);
            startActivity(intent);
            return true;
        } else if (preference.getKey().equals("donate")) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("donate", "darkgentry@hotmail.com");
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, getText(R.string.copy_to_clipboard), Toast.LENGTH_LONG).show();
            return true;
        }
        return false;
    }


    private File getFile(String path) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File file = new File(this.getExternalFilesDir(null), "/" + path);
            return file;
        }
        return null;
    }

    private Uri getUriFromFile(File file) {
        if (file != null) {
            return Uri.fromFile(file);
        } else {
            return null;
        }
    }

}

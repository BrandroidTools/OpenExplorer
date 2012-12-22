
package org.brandroid.openmanager.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;

import org.brandroid.openmanager.R;
import org.brandroid.openmanager.activities.OpenExplorer;
import org.brandroid.openmanager.data.OpenPath;
import org.brandroid.openmanager.util.OpenIntentChooser.IntentSelectedListener;
import org.brandroid.utils.Logger;
import org.brandroid.utils.Preferences;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.widget.Toast;

public class IntentManager {
    public static Intent getIntent(OpenPath file, OpenExplorer app) {
        return getIntent(file, app, Intent.ACTION_VIEW, Intent.CATEGORY_DEFAULT);
    }

    public static Intent getIntent(OpenPath file, OpenExplorer app, String action,
            String... categories) {
        if (app == null)
            return null;
        String name = file.getName();

        if (file.isDirectory() && !file.isArchive())
            return null;

        Intent ret = new Intent();
        ret.setAction(action);
        for (String category : categories)
            ret.addCategory(category);
        ret.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        ret.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        ret.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        ret.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        // ret.putExtra(name, value)
        // ret.set

        String mimeType = OpenExplorer.getMimeTypes(app).getMimeType(name);

        ret.setDataAndType(file.getUri(), mimeType);

        PackageManager pm = app.getPackageManager();
        List<ResolveInfo> lApps = pm.queryIntentActivities(ret, 0);
        // for(ResolveInfo ri : lApps)
        // Logger.LogDebug("ResolveInfo: " + ri.toString());
        if (lApps.size() == 0)
            ret = null;

        /* generic intent */
        // else ret.setDataAndType(file.getUri(), "application/*");
        return ret;
    }

    public static boolean startIntent(OpenPath file, OpenExplorer app) {
        return startIntent(file, app, Preferences.Pref_Intents_Internal);
    }

    public static boolean startIntent(final OpenPath file, final OpenExplorer app,
            boolean bInnerChooser) {
        final PackageManager pm = app.getPackageManager();
        if (getIntentsAvailable(file, app) < 1) {
            Logger.LogWarning("No matching intents!");
            return false;
        }
        Logger.LogDebug("Intents match. Use inner chooser? " + bInnerChooser);
        if (bInnerChooser) {
            final Intent intent = getIntent(file, app);
            String mime = file.getMimeType();
            if (mime == null || mime.equals("*/*"))
                mime = file.getExtension();
            final String mext = mime;
            String cls = app.getPreferences().getSetting("mimes", mext, "");
            if (cls.indexOf("$") > -1) {
                Logger.LogVerbose("Found default for " + mime + ": " + cls);
                String pck = cls.substring(0, cls.indexOf("$"));
                cls = cls.substring(cls.indexOf("$") + 1);
                intent.setClassName(pck, cls);
                try {
                    if (pck.equals("org.brandroid.openmanager"))
                        app.editFile(file);
                    else
                        app.startActivity(intent);
                } catch (Exception e) {
                    Logger.LogError("Unable to start default activity for " + mime, e);
                }
            }
            Logger.LogDebug("Chooser Intent: " + intent.toString());
            final List<ResolveInfo> mResolves = pm.queryIntentActivities(intent,
                    PackageManager.MATCH_DEFAULT_ONLY);
            final ArrayList<String> mNames = new ArrayList<String>();
            for (int i = mResolves.size() - 1; i >= 0; i--) {
                ResolveInfo ri = mResolves.get(i);
                String name = pm.getApplicationLabel(ri.activityInfo.applicationInfo).toString(); // ri.activityInfo.applicationInfo.className
                if (!mNames.contains(name))
                    mNames.add(name);
                else
                    mResolves.remove(i);
            }
            Collections.sort(mResolves, new Comparator<ResolveInfo>() {
                @Override
                public int compare(ResolveInfo lhs, ResolveInfo rhs) {
                    String a = pm.getApplicationLabel(lhs.activityInfo.applicationInfo).toString();
                    String b = pm.getApplicationLabel(rhs.activityInfo.applicationInfo).toString();
                    // String a = lhs.loadLabel(pm).toString();
                    // String b = rhs.loadLabel(pm).toString();
                    return a.compareTo(b);
                }
            });
            if (mResolves.size() == 1) {
                ResolveInfo item = mResolves.get(0);
                PackageInfo packInfo = null;
                try {
                    pm.getPackageInfo(item.activityInfo.packageName,
                            PackageManager.GET_INTENT_FILTERS);
                    intent.setClassName(packInfo != null ? packInfo.packageName
                            : item.activityInfo.packageName, item.activityInfo.name);
                    Logger.LogInfo("Starting Intent(1): " + intent.toString());
                    if (intent.toString().indexOf("nitrodesk") == -1) {
                        app.startActivity(intent);
                        return true;
                    }
                } catch (NameNotFoundException e) {
                    Logger.LogError("Package not found for " + item.activityInfo.toString(), e);
                } catch (ActivityNotFoundException ae) {
                    Logger.LogError("Activity not found for " + item.activityInfo.name, ae);
                }
            }
            if (mResolves.size() > 0) {
                new OpenIntentChooser(app, mResolves)
                        .setTitle(
                                app.getResources().getString(R.string.whichApplication) + " ("
                                        + file.getName() + "):")
                        .setOnIntentSelectedListener(new IntentSelectedListener() {
                            @Override
                            public void onUseSystemClicked() {
                                startIntent(file, app, false);
                            }

                            public void onIntentSelected(ResolveInfo item, boolean defaultSelected) {
                                // app.showToast("Package? [" +
                                // item.activityInfo.packageName + " / " +
                                // item.activityInfo.targetActivity + "]");
                                PackageInfo packInfo = null;
                                try {
                                    packInfo = pm.getPackageInfo(item.activityInfo.packageName,
                                            PackageManager.GET_INTENT_FILTERS);
                                    if (packInfo != null && packInfo.activities != null) {
                                        for (ActivityInfo info : packInfo.activities) {
                                            Logger.LogDebug("Activity Info: " + info.toString());
                                        }
                                        Logger.LogDebug("Intent chosen: "
                                                + item.activityInfo.toString());
                                    }
                                    // Intent activityIntent = new Intent();
                                    String pck = packInfo != null ? packInfo.packageName
                                            : item.activityInfo.packageName;
                                    String cls = item.activityInfo.name;
                                    intent.setClassName(pck, cls);

                                    if (defaultSelected)
                                        app.getPreferences().setSetting("mimes", mext,
                                                pck + "$" + cls);

                                    // intent.setData(file.getUri());
                                    // intent.setType(file.ge)
                                    if (!pck.equals("org.brandroid.openmanager"))
                                        app.startActivity(intent);
                                    else
                                        app.editFile(file);
                                } catch (NameNotFoundException e) {
                                    Logger.LogError(
                                            "Package not found for " + item.activityInfo.toString(),
                                            e);
                                } catch (ActivityNotFoundException ae) {
                                    Logger.LogError("Activity not found for "
                                            + item.activityInfo.name, ae);
                                }
                            }
                        }).show();
                return true;
            } else {
                Toast.makeText(app, app.getText(R.string.noApplications), Toast.LENGTH_LONG).show();
            }
        }
        Intent intent = getIntent(file, app);
        // intent.addFlags(Intent.FL);
        if (intent != null) {
            try {
                app.startActivity(Intent.createChooser(intent, file.getName()));
            } catch (ActivityNotFoundException e) {
                Logger.LogWarning("Couldn't launch intent for " + file.getPath(), e);
                return false;
            } catch (SecurityException e) {
                Logger.LogError("No permissions?!", e);
                return false;
            }
            return true;
        }
        return false;
    }

    public static int getIntentsAvailable(OpenPath file, OpenExplorer app) {
        Intent toCheck = getIntent(file, app);
        if (toCheck == null)
            return 0;
        return app.getPackageManager()
                .queryIntentActivities(toCheck, PackageManager.MATCH_DEFAULT_ONLY).size();
    }

    public static List<ResolveInfo> getResolvesAvailable(OpenPath file, OpenExplorer app) {
        Intent toCheck = getIntent(file, app);
        if (toCheck == null)
            return new ArrayList<ResolveInfo>();
        return app.getPackageManager().queryIntentActivities(toCheck, 0);
    }

    public static List<ResolveInfo> getResolvesAvailable(Intent toCheck, OpenExplorer app) {
        if (toCheck == null)
            return new ArrayList<ResolveInfo>();
        List<ResolveInfo> ret = app.getPackageManager().queryIntentActivities(toCheck, 0);
        for(int i = ret.size() - 1; i >= 0; i--)
        {
            if(ret.get(i).activityInfo.packageName.startsWith("org.brandroid.openmanager"))
                ret.remove(i);
        }
        return ret;
    }

    public static ResolveInfo getResolveInfo(final OpenPath file, final OpenExplorer app) {
        return getResolveInfo(getIntent(file, app), app);
    }

    public static ResolveInfo getResolveInfo(Intent toCheck, final OpenExplorer app) {
        if (toCheck == null)
            return null;
        List<ResolveInfo> lResolves = app.getPackageManager().queryIntentActivities(toCheck,
                PackageManager.MATCH_DEFAULT_ONLY);
        if (lResolves.size() > 0)
            return lResolves.get(0);
        return null;
    }

    public static Drawable getDefaultIcon(final OpenPath file, final OpenExplorer app) {
        ResolveInfo info = getResolveInfo(file, app);
        if (info == null)
            return null;
        return info.loadIcon(app.getPackageManager());
    }

    public static Drawable getDefaultIcon(final Intent intent, final OpenExplorer app) {
        ResolveInfo info = getResolveInfo(intent, app);
        if (info == null)
            return null;
        return info.loadIcon(app.getPackageManager());
    }

}

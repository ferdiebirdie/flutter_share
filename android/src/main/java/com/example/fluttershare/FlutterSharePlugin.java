package com.example.fluttershare;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.content.FileProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;


/** FlutterSharePlugin */
public class FlutterSharePlugin implements MethodCallHandler {

    private Registrar mRegistrar;

    private FlutterSharePlugin(Registrar mRegistrar) {
        this.mRegistrar = mRegistrar;
    }

    /** Plugin registration. */
    public static void registerWith(Registrar registrar) {
        final MethodChannel channel = new MethodChannel(registrar.messenger(), "flutter_share");
        FlutterSharePlugin plugin = new FlutterSharePlugin(registrar);
        channel.setMethodCallHandler(plugin);
    }


    @Override
    public void onMethodCall(MethodCall call, Result result) {
        if (call.method.equals("shareFile")) {
            shareFile(call, result);
        } else if (call.method.equals("share")) {
            share(call, result);
        } else {
            result.notImplemented();
        }
    }

    private void share(MethodCall call, Result result) {
        try
        {
            String title = call.argument("title");
            String text = call.argument("text");
            String linkUrl = call.argument("linkUrl");
            String chooserTitle = call.argument("chooserTitle");

            if (title == null || title.isEmpty())
            {
                Log.println(Log.ERROR, "", "FlutterShare Error: Title null or empty");
                result.error("FlutterShare: Title cannot be null or empty", null, null);
                return;
            }

            ArrayList<String> extraTextList = new ArrayList<>();

            if (text != null && !text.isEmpty()) {
                extraTextList.add(text);
            }
            if (linkUrl != null && !linkUrl.isEmpty()) {
                extraTextList.add(linkUrl);
            }

            String extraText = "";

            if (!extraTextList.isEmpty()) {
                extraText = TextUtils.join("\n\n", extraTextList);
            }

            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, title);
            intent.putExtra(Intent.EXTRA_TEXT, extraText);

            Intent chooserIntent = Intent.createChooser(intent, chooserTitle);
            chooserIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            chooserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mRegistrar.context().startActivity(chooserIntent);

            result.success(true);
        }
        catch (Exception ex)
        {
            Log.println(Log.ERROR, "", "FlutterShare: Error");
            result.error(ex.getMessage(), null, null);
        }
    }

    private void shareFile(MethodCall call, Result result) {
        try
        {
            String title = call.argument("title");
            String text = call.argument("text");
            String filePath = call.argument("filePath");
            String chooserTitle = call.argument("chooserTitle");

            if (filePath == null || filePath.isEmpty())
            {
                Log.println(Log.ERROR, "", "FlutterShare: ShareLocalFile Error: filePath null or empty");
                result.error("FlutterShare: FilePath cannot be null or empty", null, null);
                return;
            }

            File file = new File(filePath);

            Uri fileUri = FileProvider.getUriForFile(mRegistrar.context(), mRegistrar.context().getApplicationContext().getPackageName() + ".provider", file);

            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(Intent.ACTION_SEND);
            intent.setType("*/*");
            intent.putExtra(Intent.EXTRA_SUBJECT, title);
            intent.putExtra(Intent.EXTRA_TEXT, text);
            intent.putExtra(Intent.EXTRA_STREAM, fileUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            Intent chooser = Intent.createChooser(intent, chooserTitle);
            chooser.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            chooser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            List<ResolveInfo> resInfoList = mRegistrar.context().getPackageManager().queryIntentActivities(chooser, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolveInfo : resInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                mRegistrar.context().grantUriPermission(packageName, fileUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            mRegistrar.context().startActivity(chooser);

            result.success(true);
        }
        catch (Exception ex)
        {
            result.error(ex.getMessage(), null, null);
            Log.println(Log.ERROR, "", "FlutterShare: Error");
        }
    }
}

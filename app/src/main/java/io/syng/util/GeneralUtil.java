/*
 * Copyright (c) 2015 Jarrad Hope
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package io.syng.util;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.MaterialDialog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

import io.syng.R;
import io.syng.adapter.BackgroundArrayAdapter;
import io.syng.entity.Dapp;
import io.syng.entity.Profile;

public final class GeneralUtil {

    public static void hideKeyBoard(View view, Context context) {
        if (view == null) return;
        InputMethodManager imm = (InputMethodManager)
                context.getApplicationContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void showKeyBoard(View view, Context context) {
        if (view == null) return;
        InputMethodManager imm = (InputMethodManager) context.getApplicationContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

    public static void createHomeScreenIcon(final Context context, final String name, final String url) {
        Intent shortcutIntent = new Intent(Intent.ACTION_VIEW);
        shortcutIntent.setData(Uri.parse(url));
        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        Intent addIntent = new Intent();
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, name);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                Intent.ShortcutIconResource.fromContext(context.getApplicationContext(), R.mipmap.ic_launcher));
        addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        context.sendBroadcast(addIntent);
    }

    public static void showWarningDialogIfNeed(final Context context) {
        if (PrefsUtil.isFirstLaunch()) {
            PrefsUtil.setFirstLaunch(false);
            new AlertDialogWrapper.Builder(context)
                    .setTitle(R.string.warning_title)
                    .setMessage(R.string.warning_message)
                    .setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
        }
    }

    public static boolean processCreateDialog(Context context, MaterialDialog dialog) {

        EditText name = (EditText) dialog.findViewById(R.id.et_profile_name);
        EditText pass1 = (EditText) dialog.findViewById(R.id.et_profile_pass_1);
        EditText pass2 = (EditText) dialog.findViewById(R.id.et_profile_pass_2);

        String nameString = name.getText().toString();
        String pass1String = pass1.getText().toString();
        String pass2String = pass2.getText().toString();

        if (TextUtils.isEmpty(nameString)) {
            Toast.makeText(context, "Profile name can't be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(pass1String) || TextUtils.isEmpty(pass2String)) {
            Toast.makeText(context, "Password name can't be empty", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!pass1.getText().toString().equals(pass2.getText().toString())) {
            Toast.makeText(context, "Passwords should be the same!", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            Profile profile = new Profile();
            profile.setName(name.getText().toString());
            profile.setPassword(pass1String);
            ProfileManager.addProfile(profile);
            ProfileManager.setCurrentProfile(profile);
            GeneralUtil.hideKeyBoard(name, context);
            GeneralUtil.hideKeyBoard(pass1, context);
            GeneralUtil.hideKeyBoard(pass2, context);
            return true;
        }
    }

    public static void showProfileCreateDialog(final Context context, boolean cancelable, MaterialDialog.ButtonCallback callback) {

        if (callback == null) {
            callback = new MaterialDialog.ButtonCallback() {
                @Override
                public void onPositive(MaterialDialog dialog) {
                    if (processCreateDialog(context, dialog)) {
                        dialog.dismiss();
                    }
                }

                @Override
                public void onNegative(MaterialDialog dialog) {
                    dialog.dismiss();
                }
            };
        }
        MaterialDialog dialog = new MaterialDialog.Builder(context)
                .title("New profile")
                .positiveText(R.string.dialog_button_create)
                .negativeText(R.string.dialog_button_cancel)
                .customView(R.layout.profile_create_dialog, true)
                .autoDismiss(false)
                .cancelable(cancelable)
                .callback(callback)
                .show();
        EditText name = (EditText) dialog.findViewById(R.id.et_profile_name);
        GeneralUtil.showKeyBoard(name, context);
    }

    public static void showDAppEditDialog(final Dapp dapp, final Context context) {
        MaterialDialog dialog = new MaterialDialog.Builder(context)
                .title("Edit")
                .customView(R.layout.dapp_form, true)
                .positiveText(R.string.save)
                .negativeText(R.string.cancel)
                .neutralText("Remove")
                .neutralColorRes(android.R.color.holo_red_dark)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        EditText dappNameEdit = (EditText) dialog.findViewById(R.id.dapp_name);
                        EditText dappUrlEdit = (EditText) dialog.findViewById(R.id.dapp_url);
                        CheckBox checkBox = (CheckBox) dialog.findViewById(R.id.dapp_home_icon);
                        boolean homeScreenIcon = checkBox.isChecked();
                        String url = dappUrlEdit.getText().toString();
                        String name = dappNameEdit.getText().toString();
                        if (Patterns.WEB_URL.matcher(url.replace("dapp://", "http://")).matches()) {
                            dapp.setName(name);
                            dapp.setUrl(url);
                            System.out.println(url);
                            ProfileManager.updateDAppInProfile(ProfileManager.getCurrentProfile(), dapp);
                            if (homeScreenIcon) {
                                GeneralUtil.createHomeScreenIcon(context, name, url);
                            }
                            dialog.hide();
                        } else {
                            Toast.makeText(context, R.string.invalid_url, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        dialog.hide();
                    }

                    @Override
                    public void onNeutral(MaterialDialog dialog) {
                        ProfileManager.removeDAppInProfile(ProfileManager.getCurrentProfile(), dapp);
                        dialog.hide();
                    }
                })
                .autoDismiss(false)
                .build();
        EditText dappNameEdit = (EditText) dialog.findViewById(R.id.dapp_name);
        dappNameEdit.setText(dapp.getName());
        EditText dappUrlEdit = (EditText) dialog.findViewById(R.id.dapp_url);
        dappUrlEdit.setText(dapp.getUrl());
        dialog.show();
    }

    public static void showDAppCreateDialog(final Context context) {
        Dialog dialog = new MaterialDialog.Builder(context)
                .title("Add new DApp")
                .customView(R.layout.dapp_form, true)
                .positiveText(R.string.save)
                .negativeText(R.string.cancel)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        EditText dappNameEdit = (EditText) dialog.findViewById(R.id.dapp_name);
                        EditText dappUrlEdit = (EditText) dialog.findViewById(R.id.dapp_url);
                        CheckBox checkBox = (CheckBox) dialog.findViewById(R.id.dapp_home_icon);
                        boolean homeScreenIcon = checkBox.isChecked();
                        String url = dappUrlEdit.getText().toString();
                        String name = dappNameEdit.getText().toString();
                        if (Patterns.WEB_URL.matcher(url.replace("dapp://", "http://")).matches()) {
                            Dapp dapp = new Dapp(name);
                            dapp.setUrl(url);
                            ProfileManager.addDAppToProfile(ProfileManager.getCurrentProfile(), dapp);
                            if (homeScreenIcon) {
                                GeneralUtil.createHomeScreenIcon(context, name, url);
                            }
                            dialog.hide();
                        } else {
                            Toast.makeText(context, R.string.invalid_url, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        dialog.hide();
                    }
                })
                .autoDismiss(false)
                .show();
        EditText dappNameEdit = (EditText) dialog.findViewById(R.id.dapp_name);
        GeneralUtil.showKeyBoard(dappNameEdit, context);
    }

    public static void showProfileImportDialog(final Context context) {
        new MaterialDialog.Builder(context)
                .title(R.string.wallet_title)
                .customView(R.layout.wallet_import, true)
                .positiveText(R.string.sImport)
                .negativeText(R.string.cancel)
                .callback(new MaterialDialog.ButtonCallback() {
                    @SuppressWarnings("TryFinallyCanBeTryWithResources")
                    @Override
                    public void onPositive(MaterialDialog dialog) {

                        Logger logger = LoggerFactory.getLogger("SyngApplication");

                        RadioButton importJsonRadio = (RadioButton) dialog.findViewById(R.id.radio_import_json);
                        EditText importPathEdit = (EditText) dialog.findViewById(R.id.wallet_import_path);
                        EditText walletPasswordEdit = (EditText) dialog.findViewById(R.id.wallet_password);
                        String importPath = importPathEdit.getText().toString();
                        String password = walletPasswordEdit.getText().toString();
                        String fileContents = null;
                        try {
                            File walletFile = new File(importPath);
                            if (walletFile.exists()) {
                                FileInputStream stream = new FileInputStream(walletFile);
                                try {
                                    FileChannel fileChannel = stream.getChannel();
                                    MappedByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
                                    fileContents = Charset.defaultCharset().decode(buffer).toString();
                                } finally {
                                    stream.close();
                                }
                            } else {
                                Toast.makeText(context, R.string.file_not_found, Toast.LENGTH_SHORT).show();
                                logger.warn("Wallet file not found: " + importPath);
                                return;
                            }
                        } catch (Exception e) {
                            Toast.makeText(context, R.string.error_reading_file, Toast.LENGTH_SHORT).show();
                            logger.error("Error reading wallet file", e);
                        }
                        if (importJsonRadio.isChecked()) {
                            Profile profile = ProfileManager.getCurrentProfile();
                            if (profile.importWallet(fileContents, password)) {
                                ProfileManager.updateProfile(profile);
                                ProfileManager.setCurrentProfile(profile);
                            } else {
                                Toast.makeText(context, R.string.invalid_wallet_password, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Profile profile = ProfileManager.getCurrentProfile();
                            profile.importPrivateKey(fileContents, password);
                            ProfileManager.updateProfile(profile);
                        }
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        dialog.hide();
                    }
                })
                .build().show();
    }

    public static void showHeaderBackgroundDialog(final Context context) {
        new MaterialDialog.Builder(context)
                .adapter(new BackgroundArrayAdapter(context),
                        new MaterialDialog.ListCallback() {
                            @SuppressWarnings("ConstantConditions")
                            @Override
                            public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                                BackgroundArrayAdapter adapter = (BackgroundArrayAdapter) dialog.getListView().getAdapter();
                                int imageResourceId = adapter.getImageResourceIdByPosition(which);
                                ProfileManager.setCurrentProfileBackgroundResourceId(imageResourceId);
                                dialog.dismiss();
                            }
                        })
                .show();
    }

}

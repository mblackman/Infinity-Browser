/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package blackman.matt.infinitebrowser;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

/**
 * Dialog that asks the user to verify that they are over the age of 18 for legal reasons.
 *
 * Created by Matt on 11/3/2014.
 */
public class AgeGuardDialogFragment extends DialogFragment {
    /**
     * Called when the dialog gets created.
     *
     * @param savedInstanceState Something
     * @return The new dialog.
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.pref_description_age_guard)
                .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SharedPreferences prefs =
                                PreferenceManager.getDefaultSharedPreferences(getActivity());
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean("age_guard_accept", true);
                        editor.commit();
                    }
                })
                .setNegativeButton("Decline", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SharedPreferences prefs =
                                PreferenceManager.getDefaultSharedPreferences(getActivity());
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean("age_guard_accept", false);
                        editor.commit();
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}

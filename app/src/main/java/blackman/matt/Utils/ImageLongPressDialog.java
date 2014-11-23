/*
 * Infinity Browser 2014  Matt Blackman
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package blackman.matt.Utils;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;

import blackman.matt.board.ImageFile;
import blackman.matt.infinitebrowser.R;

/**
 * The alert dialog to perform actions on an image. General case to be used across the app.
 * Created by Matt on 11/23/2014.
 */
public class ImageLongPressDialog {
    private ImageFile mImage;
    private Context mContext;

    /**
     * Default constructor to run this operation.
     *
     * @param context Context of the caller.
     * @param image Image being saved or whatnot.
     */
    public ImageLongPressDialog(Context context, ImageFile image) {
        mContext = context;
        mImage = image;
        createDialog();
    }

    /**
     * Creates the dialog and shows it with the options needed to keep the customer happy.
     */
    private void createDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(mImage.getFileName())
                .setItems(R.array.image_popup_choices, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0: // Copy image url
                                ClipboardManager clipboard = (ClipboardManager)
                                        mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                                clipboard.setPrimaryClip(ClipData.newPlainText("8chan image URL",
                                        mImage.getFullUrl()));
                                break;
                            case 1: // Save image
                                new ImageDownloader(mImage).execute();
                                break;
                            case 2: // Open image in browser
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                                        Uri.parse(mImage.getFullUrl()));
                                mContext.startActivity(browserIntent);
                                break;
                        }
                    }
                });
        builder.create().show();
    }
}

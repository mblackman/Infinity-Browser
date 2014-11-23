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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import blackman.matt.board.ImageFile;

/**
 * Downloads images from 8chan based on a image file.
 *
 * Created by Matt on 11/23/2014.
 */
public class ImageDownloader extends AsyncTask<Void, Void, Bitmap> {
    private ImageFile mImage;

    /**
     * Constructor to store the image file being saved.
     *
     * @param image Image file to be saved.
     */
    public ImageDownloader(ImageFile image) {
        mImage = image;
    }

    /**
     * Downloads an image from the web and stores it in a local bitmap.
     *
     * @param params Nothing
     * @return The bitmap from the web or null if loading failed.
     */
    @Override
    protected Bitmap doInBackground(Void... params) {
        Bitmap bmp = null;
        try {
            URL url = new URL(mImage.getFullUrl());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            bmp = BitmapFactory.decodeStream(input);

        } catch (IOException e) {
            e.printStackTrace();
            Log.e("getBmpFromUrl error: ", e.getMessage().toString());
        }

        return bmp;
    }

    /**
     * After the image has been loaded from the web, this saves the image in a variety of
     * formats on disk in external storage.
     *
     * @param bmp Bitmap being saved on local storage.
     */
    @Override
    protected void onPostExecute(Bitmap bmp) {
        if (bmp != null) {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();

            if(mImage.getExtension().equals(".jpeg") || mImage.getExtension().equals(".jpg")) {
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            } else if(mImage.getExtension().equals(".png")) {
                bmp.compress(Bitmap.CompressFormat.PNG, 100, bytes);
            } else if(mImage.getExtension().equals(".gif")) {
                // TODO: Save gifs properly
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            } else {
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            }

            File pictureDir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES) + File.separator + "8chan");

            if(!pictureDir.exists()) {
                pictureDir.mkdirs();
            }

            File file = new File(pictureDir, mImage.getFileName());

            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            FileOutputStream fos;

            try {
                fos = new FileOutputStream(file);
                fos.write(bytes.toByteArray());
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

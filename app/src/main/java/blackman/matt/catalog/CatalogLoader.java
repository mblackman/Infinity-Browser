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

package blackman.matt.catalog;

import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Loads a board catalog from a selected board and sends it up to the user.
 *
 * Created by Matt on 11/24/2014.
 */
class CatalogLoader  extends AsyncTask<URL, Void, JSONArray> {
    private CatalogLoadedNotifier mNotifier;

    public interface CatalogLoadedNotifier {
        public void pageLoaded(JSONArray threads);
    }

    /**
     * Empty constructor.
     */
    public CatalogLoader() {

    }

    /**
     * Sets the notifier for when the page has loaded.
     * @param loaded The notifier from the activity that created this.
     */
    public void setNotifier(CatalogLoadedNotifier loaded) {
        mNotifier = loaded;
    }

    /**
     * Shows the progress bar and its text to the user.
     */
    @Override
    protected void onPreExecute() {
    }

    /**
     * Reads in urls sent by user to download the html from.
     *
     * @param urls the urls sent by the user.
     * @return returns the html document
     */
    @Override
    protected JSONArray doInBackground(URL... urls) {
        JSONArray ochPage = null;
        String pageUrl = urls[0].toString();

        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(pageUrl);
        try {
            HttpResponse response = client.execute(request);

            InputStream in = response.getEntity().getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder str = new StringBuilder();
            String line;
            while((line = reader.readLine()) != null)
            {
                str.append(line);
            }
            in.close();
            ochPage = new JSONArray(str.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return ochPage;
    }

    /**
     * After the page is read in, the pages are turned into fragments and are put on the
     * screen.
     *
     * @param jsonArray New array of catalog entries.
     */
    @Override
    protected void onPostExecute(JSONArray jsonArray) {
        mNotifier.pageLoaded(jsonArray);
    }
}

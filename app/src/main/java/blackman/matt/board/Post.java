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


package blackman.matt.board;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Holds the information of a post to be loaded into a post view.
 *
 * Created by Matt on 11/3/2014.
 */
public class Post {
    private static final String jsonPostNo = "no";
    private static final String postSubject = "sub";
    private static final String postComment = "com";
    private static final String postReplies = "replies";
    private static final String postOmittedReplies = "omitted_posts";
    private static final String postOmittedImages = "omitted_images";
    private static final String postName = "name";
    //private static final String postStickied = "sticky";
    //private static final String postLocked = "locked";
    private static final String postTime = "time";
    //private static final String postLastModified = "last_modified";
    private static final String postFileName = "filename";
    private static final String postFileSiteName = "tim";
    private static final String postFileExt = "ext";
    private static final String postFileSize = "fsize";
    private static final String postFileHeight = "h";
    private static final String postFileWidth = "w";
    private static final String postFileThumbHeight = "tn_h";
    private static final String postFileThumbWidth = "tn_w";

    public String userName;
    public String postDate;
    public String postNo;
    public String topic;
    public String postBody;
    public String rootBoard;
    public String numReplies;
    public String omittedReplies;
    public String omittedImages;
    public List<ImageFile> images;
    public List<String> repliedTo;
    public List<String> repliedBy;
    public Boolean isThumbnail;

    /**
     * Basic constructor
     *
     * @param object The json object being turned into a post.
     * @param rootBoard The root board of the post.
     */
    public Post(JSONObject object, String rootBoard) {
        this.repliedTo = new ArrayList<String>();
        this.repliedBy = new ArrayList<String>();
        this.images = new ArrayList<ImageFile>();
        this.rootBoard = rootBoard;
        this.isThumbnail = true;

        try {
            this.userName = object.optString(postName);
            this.postDate = object.getString(postTime);
            this.postNo = object.getString(jsonPostNo);
            this.topic = object.optString(postSubject);
            this.postBody = formatPostBody(object.optString(postComment));
            this.numReplies = object.optString(postReplies);
            this.omittedReplies = object.optString(postOmittedReplies);
            this.omittedImages = object.optString(postOmittedImages);

            String fileName = object.optString(postFileName);
            if(fileName != null && !fileName.equals("")) {
                images.add(new ImageFile(rootBoard, fileName,
                        object.optString(postFileExt),
                        object.getString(postFileSiteName),
                        object.optInt(postFileWidth),
                        object.optInt(postFileHeight),
                        object.optInt(postFileThumbWidth),
                        object.optInt(postFileThumbHeight),
                        object.getInt(postFileSize)));
            }

            if(object.has("extra_files")) {
                JSONArray multiFiles = object.getJSONArray("extra_files");
                for (int i = 0; i < multiFiles.length(); i++) {
                    JSONObject imageJson = multiFiles.getJSONObject(i);
                    images.add(new ImageFile(rootBoard,
                            imageJson.getString(postFileName),
                            imageJson.getString(postFileExt),
                            imageJson.getString(postFileSiteName),
                            imageJson.optInt(postFileWidth),
                            imageJson.optInt(postFileHeight),
                            imageJson.optInt(postFileThumbWidth),
                            imageJson.optInt(postFileThumbHeight),
                            imageJson.getInt(postFileSize)));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Formats the HTML on the post text to accurately display it on the post.
     *
     * @param post The unformatted text of the post.
     * @return A formatted version of the post.
     */
    private String formatPostBody(String post) {
        Document formattedText = Jsoup.parse(post);
        Pattern p = Pattern.compile("^/.*/index\\.html");

        // Red Text
        Elements redTexts = formattedText.getElementsByClass("heading");
        for(Element text : redTexts) {
            text.wrap("<font color=\"#AF0A0F\"><strong></strong></font>");
        }

        // Green text
        Elements greenTexts = formattedText.getElementsByClass("quote");
        for(Element text : greenTexts) {
            text.wrap("<font color=\"#789922\"></font>");
        }

        // Board Links
        Elements boardLinks = formattedText.select("a");
        for(Element link : boardLinks) {
            String url = link.attr("href");
            Matcher m = p.matcher(url);
            if(m.matches()) {
                link.attr("href", "http://8chan.co" + url);
            }
        }

        // Reply links
        Elements replyLinks = formattedText.select("a[onclick^=highlightReply");
        for(Element reply : replyLinks) {
            repliedTo.add(reply.attr("href").split("#")[1]);
            boardLinks.attr("href", "http://8chan.co" + reply.attr("href"));
        }

        // Post too long text removal
        Elements tooLongs = formattedText.getElementsByClass("toolong");
        for(Element text : tooLongs) {
            text.text("");
        }

        return formattedText.toString();
    }

    public static ArrayList<Post> fromJson(JSONArray jsonArray, String rootBoard) {
        ArrayList<Post> posts = new ArrayList<Post>();
        for (int j = 0; j < jsonArray.length(); j++) {
            try {
                posts.add(new Post(jsonArray.getJSONObject(j), rootBoard));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return posts;
    }
}

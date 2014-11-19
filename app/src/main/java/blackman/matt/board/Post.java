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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Holds the information of a post to be loaded into a post view.
 *
 * Created by Matt on 11/3/2014.
 */
public class Post {
    public final String userName;
    public final String postDate;
    public final String postNo;
    public final String topic;
    public final String postBody;
    public final String rootBoard;
    public final String numReplies;
    public final List<ImageFile> images;
    public List<String> repliedTo;
    public List<String> repliedBy;
    public Boolean isThumbnail;

    /**
     * Basic constructor
     * @param userName Post user name.
     * @param postDate Date the post was made.
     * @param postNumber Post's number.
     * @param topic Topic of the post
     * @param postText Body of the post.
     * @param numReplies Replies to post.
     * @param boardRoot Link to the board.
     */
    public Post(String userName, String postDate, String postNumber, String topic,
                String postText, String numReplies, List<ImageFile> images, String boardRoot) {
        this.repliedTo = new ArrayList<String>();
        this.repliedBy = new ArrayList<String>();
        this.userName = userName;
        this.postDate = postDate;
        this.postNo = postNumber;
        this.topic = topic;
        this.images = images;
        this.postBody = formatPostBody(postText);
        this.numReplies = numReplies;
        this.rootBoard = boardRoot;

        this.isThumbnail = true;
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
}

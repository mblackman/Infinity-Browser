package blackman.matt.board;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Holds the information of a post to be loaded into a post view.
 *
 * Created by Matt on 11/3/2014.
 */
public class Post {
    public String mUserName, mPostDate, mPostNo, mTopic, mPostText, mNumReplies, mBoardLink;
    public List<String> mFullURLS, mThumbURLS;
    public Boolean mIsRootBoard;

    /**
     * Basic constructor
     * @param userName Post user name.
     * @param postDate Date the post was made.
     * @param postNumber Post's number.
     * @param topic Topic of the post
     * @param postText Body of the post.
     * @param numReplies Replies to post.
     * @param imageThumbs All the thumbnails on the post.
     * @param imageFull All the full sized images on the post.
     * @param boardLink Link to the board.
     * @param onRootBoard If this is a OP post.
     */
    public Post(String userName, String postDate, String postNumber, String topic,
                String postText, String numReplies, List<String> imageThumbs,
                List<String> imageFull, String boardLink, boolean onRootBoard) {
        mUserName = userName;
        mPostDate = postDate;
        mPostNo = postNumber;
        mTopic = topic;
        mPostText = formatPostBody(postText);
        mNumReplies = numReplies;
        mThumbURLS = imageThumbs;
        mFullURLS = imageFull;
        mBoardLink = boardLink;
        mIsRootBoard = onRootBoard;
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
        Elements redTexts = formattedText.select("[class=heading]");
        for(Element text : redTexts) {
            text.wrap("<font color=\"#AF0A0F\"><strong></strong></font>");
        }

        // Green text
        Elements greenTexts = formattedText.select("[class=quote]");
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

        return formattedText.toString();
    }
}

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
    public final String userName;
    public final String postDate;
    public final String postNo;
    public final String topic;
    public final String postBody;
    public final String numReplies;
    public final String boardLink;
    public final String rootBoard;
    public final List<String> fullURLS;
    public final List<String> thumbURLS;
    public final List<String> fileNames;
    public final List<String> fileNumbers;
    public final Boolean isRootBoard;
    public final Boolean hasImages;
    public Boolean isThumbnail;
    public final Long Id;

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
                List<String> imageFull, List<String> fileNames, List<String> fileNumbers,
                String boardLink, boolean onRootBoard) {
        this.userName = userName;
        this.postDate = postDate;
        this.postNo = postNumber;
        this.topic = topic;
        this.postBody = formatPostBody(postText);
        this.numReplies = numReplies;
        this.thumbURLS = imageThumbs;
        this.fullURLS = imageFull;
        this.fileNames = fileNames;
        this.fileNumbers = fileNumbers;
        this.boardLink = boardLink;
        this.rootBoard = boardLink.replace("https://8chan.co/", "").split("/")[0];
        this.isRootBoard = onRootBoard;
        this.Id = Long.parseLong(postNo);
        this.isThumbnail = true;
        this.hasImages = !(thumbURLS.isEmpty() && fullURLS.isEmpty());
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

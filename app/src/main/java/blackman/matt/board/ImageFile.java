package blackman.matt.board;

/**
 * Stores the infomation about a image on any board and returns different bits of information
 * for the user.
 *
 * Created by Matt on 11/18/2014.
 */
public class ImageFile {
    private final String mFileName, mRootBoard, mExt, mTim;
    private final int mWidth, mHeight, mThumbWidth, mThumbHeight;

    /**
     * Base constructor used to get all the info 8chan supplies about files.
     *
     * @param rootBoard Board this images is on.
     * @param fileName Name of the file.
     * @param ext Files extension.
     * @param tim Files name stored on site.
     * @param width Width of full sized image.
     * @param height Height of full sized image.
     * @param thumbWidth Width of thumbnail.
     * @param thumbHeight Height of thumbnail.
     */
    public ImageFile(String rootBoard, String fileName, String ext, String tim, int width,
                     int height, int thumbWidth, int thumbHeight) {
        this.mRootBoard = rootBoard;
        this.mFileName = fileName;
        this.mExt = ext;
        this.mTim = tim;
        this.mWidth = width;
        this.mHeight = height;
        this.mThumbHeight = thumbHeight;
        this.mThumbWidth = thumbWidth;
    }

    /**
     * Gets the url of the thumbnail.
     * @return thumbnail url.
     */
    public String getThumbnailUrl() {
        return "https://media.8chan.co/" + mRootBoard + "/thumb/" + mTim + mExt;
    }

    /**
     * Gets the url of the full image.
     * @return full sized image url.
     */
    public String getFullUrl() {
        return "https://media.8chan.co/" + mRootBoard + "/src/" + mTim + mExt;
    }

    /**
     * Gets the files name.
     * @return Files name.
     */
    public String getFileName() {
        return mFileName + mExt;
    }
}

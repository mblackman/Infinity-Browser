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

/**
 * Stores the infomation about a image on any board and returns different bits of information
 * for the user.
 *
 * Created by Matt on 11/18/2014.
 */
public class ImageFile {
    private final String mFileName, mRootBoard, mExt, mTim;
    private final int mWidth, mHeight, mThumbWidth, mThumbHeight, mSize;

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
                     int height, int thumbWidth, int thumbHeight, int fileSize) {
        this.mRootBoard = rootBoard;
        this.mFileName = fileName;
        this.mExt = ext;
        this.mTim = tim;
        this.mWidth = width;
        this.mHeight = height;
        this.mThumbHeight = thumbHeight;
        this.mThumbWidth = thumbWidth;
        this.mSize = fileSize;
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

    public String getFileInfo() {
        return mWidth + " X " + mHeight + ", " + Math.round(mSize / 1024) + "KB, "
                + mFileName + mExt;
    }
}

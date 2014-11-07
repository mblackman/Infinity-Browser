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

package blackman.matt.board;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;

import java.util.Collections;
import java.util.List;

/**
 * A custom adapter to handle loading posts on a board page. Sets up the views and recycles them.
 *
 * Created by Matt on 10/26/2014.
 */
public class PostArrayAdapter extends BaseAdapter {
    private List<Post> mPosts = Collections.emptyList();
    private final Context mContext;
    private Board.OnReplyClickedListener mListener;

    /**
     * Public constructor to handle taking in the list of views.
     * @param context Context of the caller.
     */
    public PostArrayAdapter(Context context) {
        mContext = context;
    }

    public void updatePosts(List<Post> posts, Board.OnReplyClickedListener listener) {
        mPosts = posts;
        mListener = listener;
        notifyDataSetChanged();
    }

    /**
     * Gets the number of posts being stored.
     *
     * @return The number of posts.
     */
    @Override
    public int getCount() {
        return mPosts.size();
    }

    /**
     * Gets a post view at a given position.
     *
     * @param position Where to get the post from.
     * @return The post view.
     */
    @Override
    public Post getItem(int position) {
        return mPosts.get(position);
    }

    /**
     * Gets the position.
     *
     * @param position The position
     * @return Returns the position.
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Gets a view from the list and returns it.
     *
     * @param position The selected position.
     * @param convertView The view that might be taking the place of the view.
     * @param parent The parent view group with all the stuffs.
     * @return The view you will see.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        PostView postView;

        if(convertView == null) {
            postView = new PostView(mContext, mListener);
        } else {
            postView = (PostView) convertView;
        }

        postView.setUpPost(getItem(position));

        return postView;
    }
}

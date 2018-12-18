package com.example.usman.social_medely_app.interfaces;

import com.tumblr.jumblr.types.Post;

import java.util.List;

/**
 * Created by Farhad on 18/12/2018.
 */

public interface TumblrResult {
    void onSuccess(List<Post> posts);
    void onError(String error);
}

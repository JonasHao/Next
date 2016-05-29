package com.jonashao.musicrecommender.util;

import com.jonashao.musicrecommender.models.Music;

public interface DisplayCallback {
    void display(Music music);

    void refreshProgress(int progress);

    void feedback(int action_code);

}
    
package com.commitstrip.commitstripreader.listfavorite;


import android.os.Bundle;
import android.support.annotation.NonNull;

import com.commitstrip.commitstripreader.BaseActivity;
import com.commitstrip.commitstripreader.MyApp;
import com.commitstrip.commitstripreader.R;
import com.commitstrip.commitstripreader.data.source.StripRepositorySingleton;
import com.commitstrip.commitstripreader.util.ActivityUtils;

import javax.inject.Inject;

public class ListFavoriteActivity extends BaseActivity {

    @Inject
    ListFavoritePresenter mListFavoritePresenter;

    @NonNull
    public static String ARGUMENT_STRIP_ID = "ARGUMENT_STRIP_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(getString(R.string.favorite));

        // Create the fragment
        ListFavoriteFragment listFavoriteFragment = (ListFavoriteFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);

        if (listFavoriteFragment == null) {
            listFavoriteFragment = ListFavoriteFragment.newInstance();

            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), listFavoriteFragment, R.id.contentFrame);
        }

        // Create the presenter
        DaggerListFavoriteComponent.builder()
            .stripRepositoryComponent(StripRepositorySingleton.getInstance(getApplicationContext()).getStripRepositoryComponent())
            .listFavoritePresenterModule(new ListFavoritePresenterModule(listFavoriteFragment)).build()
            .inject(this);
    }

}

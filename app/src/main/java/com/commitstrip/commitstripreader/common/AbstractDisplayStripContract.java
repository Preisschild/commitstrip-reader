package com.commitstrip.commitstripreader.common;

import com.commitstrip.commitstripreader.BasePresenter;
import com.commitstrip.commitstripreader.BaseView;
import com.commitstrip.commitstripreader.strip.StripContract;
import com.squareup.picasso.RequestCreator;

/**
 * This specifies the contract between the view and the presenter.
 */
public interface AbstractDisplayStripContract {

    interface View {

        void displayImage(RequestCreator requestCreator);
        void setTitle(String title);
        void displayIconIsFavorite(boolean favorite);

        void setAbstractPresenter(AbstractDisplayStripPresenter abstractDisplayStripPresenter);

        void displayError();
    }

    interface Presenter extends BasePresenter {
        void fetchStrip(Long id);

        Long askForNextIdStrip();
        Long askForPreviousIdStrip();

        void addFavorite();
        void deleteFavorite();
    }

}

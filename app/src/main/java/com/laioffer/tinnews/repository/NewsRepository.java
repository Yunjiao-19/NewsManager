package com.laioffer.tinnews.repository;

import android.os.AsyncTask;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.laioffer.tinnews.TinNewsApplication;
import com.laioffer.tinnews.database.TinNewsDatabase;
import com.laioffer.tinnews.model.Article;
import com.laioffer.tinnews.model.NewsResponse;
import com.laioffer.tinnews.network.NewsApi;
import com.laioffer.tinnews.network.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewsRepository {

   private final NewsApi newsApi;
    private final TinNewsDatabase database;

   public NewsRepository() {
      newsApi = RetrofitClient.newInstance().create(NewsApi.class);
       database = TinNewsApplication.getDatabase();
   }

   public LiveData<NewsResponse> getTopHeadlines(String country) {
      MutableLiveData<NewsResponse> topHeadlinesLiveData = new MutableLiveData<>();
      newsApi.getTopHeadlines(country)
              .enqueue(new Callback<NewsResponse>() {
                 @Override
                 public void onResponse(Call<NewsResponse> call, Response<NewsResponse> response) {
                    if (response.isSuccessful()) {
                       topHeadlinesLiveData.setValue(response.body());
                    } else {
                       topHeadlinesLiveData.setValue(null);
                    }
                 }

                 @Override
                 public void onFailure(Call<NewsResponse> call, Throwable t) {
                    topHeadlinesLiveData.setValue(null);
                 }
              });
      return topHeadlinesLiveData;
   }
   public LiveData<NewsResponse> searchNews(String query) {
      MutableLiveData<NewsResponse> everyThingLiveData = new MutableLiveData<>();
      newsApi.getEverything(query, 40)
              .enqueue(
                      new Callback<NewsResponse>() {
                         @Override
                         public void onResponse(Call<NewsResponse> call, Response<NewsResponse> response) {
                            if (response.isSuccessful()) {
                               everyThingLiveData.setValue(response.body());
                            } else {
                               everyThingLiveData.setValue(null);
                            }
                         }

                         @Override
                         public void onFailure(Call<NewsResponse> call, Throwable t) {
                            everyThingLiveData.setValue(null);
                         }
                      });
      return everyThingLiveData;
   }

   public LiveData<Boolean> favoriteArticle(Article article){
       MutableLiveData<Boolean> saveResultLiveData = new MutableLiveData<>();

       //Call
       FavoriteAsyncTask favoriteTask = new FavoriteAsyncTask(database, saveResultLiveData);
       favoriteTask.execute(article);
       return saveResultLiveData;
   }
    public LiveData<List<Article>> getAllSavedArticles() {
        return database.articleDao().getAllArticles();
    }


    public void deleteSavedArticle(Article article) {
        AsyncTask.execute(() -> database.articleDao().deleteArticle(article));
    }


    private static class FavoriteAsyncTask extends AsyncTask<Article, Void, Boolean> {

       //control + o

        private final TinNewsDatabase database;
        private final MutableLiveData<Boolean> liveData;

        private FavoriteAsyncTask(TinNewsDatabase database, MutableLiveData<Boolean> liveData) {
            this.database = database;
            this.liveData = liveData;
        }

        //background thread: any thread not main thread
        @Override
        protected Boolean doInBackground(Article... articles) {
            Article article = articles[0];
            try {
                //time consuming database operation, so it is in background thread
                database.articleDao().saveArticle(article);
            } catch (Exception e) {
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            liveData.setValue(success);
        }
    }


}

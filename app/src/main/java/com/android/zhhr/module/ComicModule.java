package com.android.zhhr.module;

import android.content.Context;

import com.android.zhhr.data.commons.Url;
import com.android.zhhr.data.entity.Comic;
import com.android.zhhr.db.helper.DaoHelper;
import com.android.zhhr.db.manager.DaoManager;
import com.android.zhhr.db.manager.GreenDaoManager;
import com.android.zhhr.listener.DBhelperListener;
import com.android.zhhr.net.ComicService;
import com.android.zhhr.net.MainFactory;
import com.android.zhhr.utils.TencentComicAnalysis;
import com.android.zhr.greendao.gen.ComicDao;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by 皓然 on 2017/7/31.
 */

public class ComicModule {
    public static final ComicService comicService = MainFactory.getComicServiceInstance();
    public Context context;
    private DaoHelper mHelper;
    public ComicModule(Context context){
        this.context = context;
        mHelper = new DaoHelper(context);
    }

    public void getData(Subscriber subscriber){
        Observable ComicListObservable = Observable.create(new Observable.OnSubscribe<List<Comic>>() {
            @Override
            public void call(Subscriber<? super List<Comic>> subscriber) {
                try {
                    Document doc = Jsoup.connect(Url.TencentTopUrl+"1").get();
                    Document doc2 = Jsoup.connect(Url.TencentTopUrl+"2").get();
                    List<Comic>  mdats = TencentComicAnalysis.TransToComic(doc);
                    mdats.addAll(TencentComicAnalysis.TransToComic(doc2));
                    subscriber.onNext(mdats);
                } catch (IOException e) {
                    subscriber.onError(e);
                    e.printStackTrace();
                }finally {
                    subscriber.onCompleted();
                }
            }
        });
        Observable ComicBannerObservable = Observable.create(new Observable.OnSubscribe<List<Comic>>() {
            @Override
            public void call(Subscriber<? super List<Comic>> subscriber) {
                try {
                    Document doc = Jsoup.connect(Url.TencentUpdateTimeUrl).get();
                    List<Comic>  mdats = TencentComicAnalysis.TransToComic(doc);
                    subscriber.onNext(mdats);
                } catch (IOException e) {
                    subscriber.onError(e);
                    e.printStackTrace();
                }finally {
                    subscriber.onCompleted();
                }
            }
        });
        Observable.merge(ComicListObservable, ComicBannerObservable).subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }


    public void getMoreComicList(final int page, Subscriber subscriber){
        Observable.create(new Observable.OnSubscribe<List<Comic>>() {
            @Override
            public void call(Subscriber<? super List<Comic>> subscriber) {
                try {
                    Document doc = Jsoup.connect(Url.TencentTopUrl+page).get();
                    List<Comic> mdats = TencentComicAnalysis.TransToComic(doc);
                    subscriber.onNext(mdats);
                } catch (IOException e) {
                    subscriber.onError(e);
                    e.printStackTrace();
                }finally {
                    subscriber.onCompleted();
                }
            }
        }) .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);

    }

    public void getCollectedComicList(Subscriber subscriber){
        Observable.create(new Observable.OnSubscribe<List<Comic>>() {
            @Override
            public void call(Subscriber<? super List<Comic>> subscriber) {
                try {
                    List<Comic> comics = mHelper.listComicAll();
                    subscriber.onNext(comics);
                } catch (Exception e) {
                    subscriber.onError(e);
                    e.printStackTrace();
                }finally {
                    subscriber.onCompleted();
                }
            }
        }) .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);

    }

    public void getCmoicDetail(final String mComicId,Subscriber subscriber){
       Observable.create(new Observable.OnSubscribe<Comic>() {
            @Override
            public void call(Subscriber<? super Comic> subscriber) {
                try {
                    Document doc = Jsoup.connect(Url.TencentDetail+mComicId).get();
                    Comic comic = TencentComicAnalysis.TransToComicDetail(doc,context);
                    subscriber.onNext(comic);
                } catch (Exception e) {
                    subscriber.onError(e);
                    e.printStackTrace();
                }finally {
                    subscriber.onCompleted();
                }
            }
        }).subscribeOn(Schedulers.io())
               .unsubscribeOn(Schedulers.io())
               .observeOn(AndroidSchedulers.mainThread())
               .subscribe(subscriber);
    }

    public void getPreNowChapterList(String comic_id,int comic_chapters,Subscriber subscriber){
        comicService.getPreNowChapterList(comic_id,comic_chapters+"")
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    public void getChaptersList(String comic_id,int comic_chapters,Subscriber subscriber){
        comicService.getChapters(comic_id,comic_chapters+"")
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);

    }

    public void collectComic(final Comic comic,Subscriber subscriber){
        Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                try{
                    if(mHelper.findComic(comic.getId())==null){
                        if(mHelper.insert(comic)){
                            subscriber.onNext(true);
                        }else{
                            subscriber.onNext(false);
                        }
                    }else{
                        subscriber.onNext(false);
                    }
                }catch (Exception e){
                    subscriber.onError(e);
                }finally {
                    subscriber.onCompleted();
                }
            }
        }) .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

    public void isCollected(final long id,Subscriber subscriber) {
        Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                try{
                    if(mHelper.findComic(id)!=null) {
                        subscriber.onNext(false);
                    }else{
                        subscriber.onNext(true);
                    }
                }catch (Exception e){
                    subscriber.onError(e);
                }finally {
                    subscriber.onCompleted();
                }
            }
        }) .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }
}

package edu.cnm.deepdive.nasaapod.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import edu.cnm.deepdive.nasaapod.BuildConfig;
import edu.cnm.deepdive.nasaapod.model.dao.ApodDao;
import edu.cnm.deepdive.nasaapod.model.entity.Apod;
import edu.cnm.deepdive.nasaapod.service.ApodDatabase;
import edu.cnm.deepdive.nasaapod.service.ApodService;
import io.reactivex.schedulers.Schedulers;
import java.text.ParseException;
import java.util.Date;

public class MainViewModel extends AndroidViewModel {

  private Date apodDate;
  private MutableLiveData<Apod> apod;
  private MutableLiveData<Throwable> throwable;
  private ApodDatabase database;
  private ApodService nasa;

  public MainViewModel(@NonNull Application application) {
    super(application);
    database = ApodDatabase.getInstance();
    nasa = ApodService.getInstance();
    apod = new MutableLiveData<>();
    throwable = new MutableLiveData<>();
    Date today = new Date();
    String formattedDate = ApodService.DATE_FORMATTER.format(today);
    try {
      setApodDate(ApodService.DATE_FORMATTER.parse(formattedDate)); // TODO investigate adjustment for NASA APoD-relevant timezone.
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
  }

  public LiveData<Apod> getApod() {
    return apod;
  }

  public LiveData<Throwable> getThrowable() {
    return throwable;
  }

  public void setApodDate(Date date) {
    ApodDao dao = database.getApodDao(); // assign a method to a local variable when you know you'll be using it a lot.
    dao.select(date)
        .subscribeOn(Schedulers.io())
        .subscribe(
            (apod) -> this.apod.postValue(apod),
            (throwable) -> this.throwable.postValue(throwable),
            () -> nasa.get(BuildConfig.API_KEY, ApodService.DATE_FORMATTER.format(date))
                .subscribeOn(Schedulers.io())
                .subscribe(
                    (apod) -> dao.insert(apod)
                        .subscribeOn(Schedulers.io())
                        .subscribe(
                            (id) -> {
                              apod.setId(id);
                              this.apod.postValue(apod);
                            },
                            (throwable) -> this.throwable.postValue(throwable)
                        ),
                    (throwable) -> this.throwable.postValue(throwable)
                )
        );
  }

}

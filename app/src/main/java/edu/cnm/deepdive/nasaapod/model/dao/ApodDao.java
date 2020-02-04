package edu.cnm.deepdive.nasaapod.model.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import edu.cnm.deepdive.nasaapod.model.entity.Apod;
import java.util.Date;
import java.util.List;

@Dao // Data Access Object
public interface ApodDao {
  // CRUD - Create, Read, Update, Delete

  @Insert //Create
  long insert(Apod apod);

  @Delete //Delete
  int delete(Apod apod); //returns # of records affected

  @Delete
  int delete(Apod... apods);

  @Query("SELECT * FROM Apod ORDER BY date DESC") //Read
  List<Apod> select();

  @Query("SELECT * FROM Apod WHERE date = :date") //"colon name" in SQL is a placeholder for a parameter
  Apod select (Date date);

  @Query("SELECT * FROM Apod WHERE apod_id = :id")
  Apod select (long id);
}

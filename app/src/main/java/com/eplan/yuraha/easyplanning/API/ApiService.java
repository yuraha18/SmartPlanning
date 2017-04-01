package com.eplan.yuraha.easyplanning.API;


import com.eplan.yuraha.easyplanning.ListAdapters.Task;
import com.eplan.yuraha.easyplanning.URL;
import com.eplan.yuraha.easyplanning.dto.DTOTask;
import com.eplan.yuraha.easyplanning.dto.Time;

import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

   @GET(URL.GET_TIME)
    Call<Time> getCurrentTime();

   @GET(URL.GET_SYNCH_DATA)
   Call<List<SynchObject>> getSynchData(@Query("userId") long userId, @Query("time") long time);

    /* Api for Adding to TASK_TABLE */

    @POST(URL.TASK_API)
    Call<DTOTask> saveTask(@Body DTOTask task);

    @GET(URL.TASK_API + "/{id}")
    Call<DTOTask> getTask(@Path("id") Long id) ;





}

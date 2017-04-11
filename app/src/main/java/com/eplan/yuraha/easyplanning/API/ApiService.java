package com.eplan.yuraha.easyplanning.API;


import com.eplan.yuraha.easyplanning.URL;
import com.eplan.yuraha.easyplanning.dto.DTOGoal;
import com.eplan.yuraha.easyplanning.dto.DTOTask;
import com.eplan.yuraha.easyplanning.dto.Day;
import com.eplan.yuraha.easyplanning.dto.DeletedTask;
import com.eplan.yuraha.easyplanning.dto.DoneGoal;
import com.eplan.yuraha.easyplanning.dto.DoneTask;
import com.eplan.yuraha.easyplanning.dto.InProgressGoal;
import com.eplan.yuraha.easyplanning.dto.InProgressTask;
import com.eplan.yuraha.easyplanning.dto.MonthRepeating;
import com.eplan.yuraha.easyplanning.dto.Notification;
import com.eplan.yuraha.easyplanning.dto.Reminding;
import com.eplan.yuraha.easyplanning.dto.Repeating;
import com.eplan.yuraha.easyplanning.dto.TaskLifecycle;
import com.eplan.yuraha.easyplanning.dto.TaskToGoal;
import com.eplan.yuraha.easyplanning.dto.Time;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
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

    @POST(URL.TASK_URL+"/{id}")
    Call<DTOTask> saveTask(@Body DTOTask dto, @Path("id") long userId);

    @GET(URL.TASK_URL +"/{id}")
    Call<DTOTask> getTask(@Path("id") long id);

    ///////////////////////////////////////////////////////
    @POST(URL.DAYS_URL+"/{id}")
    Call<Day> saveDays(@Body Day dto, @Path("id") long userId);

    @GET(URL.DAYS_URL +"/{id}")
    Call<Day> getDay(@Path("id") long id);

    ///////////////////////////////////////////////////////

    @POST(URL.DELETED_TASKS_URL+"/{id}")
    Call<DeletedTask> saveDeletedTask(@Body DeletedTask dto, @Path("id") long userId);

    @GET(URL.DELETED_TASKS_URL +"/{id}")
    Call<DeletedTask> getDeletedTask(@Path("id") long id);

    /////////////////////////////////////////////////////////

    @POST(URL.DONE_GOALS_URL+"/{id}")
    Call<DoneGoal> saveDoneGoal(@Body DoneGoal dto, @Path("id") long userId);

    @GET(URL.DONE_GOALS_URL +"/{id}")
    Call<DoneGoal> getDoneGoal(@Path("id") long id);

    //////////////////////////////////////////////////////////////
    @POST(URL.DONE_TASKS_URL+"/{id}")
    Call<DoneTask> saveDoneTask(@Body DoneTask dto, @Path("id") long userId);

    @DELETE(URL.DONE_TASKS_URL +"/{id}")
    Call<DoneTask> removeDoneTask(@Path("id") long id);

    @GET(URL.DONE_TASKS_URL +"/{id}")
    Call<DoneTask> getDoneTask(@Path("id") long id);

    /////////////////////////////////////////////////////////////

    @POST(URL.GOALS_URL+"/{id}")
    Call<DTOGoal> saveGoal(@Body DTOGoal dto, @Path("id") long userId);

    @DELETE(URL.GOALS_URL +"/{id}")
    Call<DTOGoal> removeGoal(@Path("id") long id);

    @GET(URL.GOALS_URL +"/{id}")
    Call<DTOGoal> getGoal(@Path("id") long id);

    ///////////////////////////////////////////////////////////////
    @POST(URL.IN_PROGRESS_GOALS_URL+"/{id}")
    Call<InProgressGoal> saveInProgressGoal(@Body InProgressGoal dto, @Path("id") long userId);

    @DELETE(URL.IN_PROGRESS_GOALS_URL +"/{id}")
    Call<InProgressGoal> removeInProgressGoal(@Path("id") long id);

    @GET(URL.IN_PROGRESS_GOALS_URL +"/{id}")
    Call<InProgressGoal> getInProgressGoal(@Path("id") long id);

    //////////////////////////////////////////////////////////////

    @POST(URL.IN_PROGRESS_TASKS_URL+"/{id}")
    Call<InProgressTask> saveInProgressTask(@Body InProgressTask dto, @Path("id") long userId);

    @GET(URL.IN_PROGRESS_TASKS_URL +"/{id}")
    Call<InProgressTask> getInProgressTask(@Path("id") long id);

    //////////////////////////////////////////////////////////////

    @POST(URL.MONTH_REPEATING_URL+"/{id}")
    Call<MonthRepeating> saveMonthRepeating(@Body MonthRepeating dto, @Path("id") long userId);

    @DELETE(URL.MONTH_REPEATING_URL +"/{id}")
    Call<MonthRepeating> removeMonthRepeating(@Path("id") long id);

    @GET(URL.MONTH_REPEATING_URL +"/{id}")
    Call<MonthRepeating> getMonthRepeating(@Path("id") long id);

    /////////////////////////////////////////////////////////////

    @POST(URL.NOTIFICATIONS_URL+"/{id}")
    Call<Notification> saveNotification(@Body Notification dto, @Path("id") long userId);

    @DELETE(URL.NOTIFICATIONS_URL +"/{id}")
    Call<Notification> removeNotification(@Path("id") long id);

    @GET(URL.NOTIFICATIONS_URL +"/{id}")
    Call<Notification> getNotification(@Path("id") long id);

    /////////////////////////////////////////////////////////////

    @POST(URL.REMINDING_URL+"/{id}")
    Call<Reminding> saveReminding(@Body Reminding dto, @Path("id") long userId);

    @GET(URL.REMINDING_URL +"/{id}")
    Call<Reminding> getReminding(@Path("id") long id);

    //////////////////////////////////////////////////////////////

    @POST(URL.REPEATING_URL+"/{id}")
    Call<Repeating> saveRepeating(@Body Repeating dto, @Path("id") long userId);

    @DELETE(URL.REPEATING_URL +"/{id}")
    Call<Repeating> removeRepeating(@Path("id") long id);

    @GET(URL.REPEATING_URL +"/{id}")
    Call<Repeating> getRepeating(@Path("id") long id);

    /////////////////////////////////////////////////////////////

    @POST(URL.TASK_LIFECYCLE_URL+"/{id}")
    Call<TaskLifecycle> saveTaskLifecycle(@Body TaskLifecycle dto, @Path("id") long userId);

    @GET(URL.TASK_LIFECYCLE_URL +"/{id}")
    Call<TaskLifecycle> getTaskLifecycle(@Path("id") long id);

    /////////////////////////////////////////////////////////////

    @POST(URL.TASK_TO_GOAL_URL+"/{id}")
    Call<TaskToGoal> saveTaskToGoal(@Body TaskToGoal dto, @Path("id") long userId);

    @DELETE(URL.TASK_TO_GOAL_URL +"/{id}")
    Call<TaskToGoal> removeTaskToGoal(@Path("id") long id);

    @GET(URL.TASK_TO_GOAL_URL +"/{id}")
    Call<TaskToGoal> getTaskToGoal(@Path("id") long id);











}

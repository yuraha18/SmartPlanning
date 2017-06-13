package com.eplan.yuraha.easyplanning.dto;


import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class MainDTO {

   private HashSet<Day> dayList;
    private HashSet<DeletedTask> deletedTasksList;
    private HashSet<DoneGoal> doneGoalList;
    private HashSet<DoneTask> doneTaskList;
    private HashSet<DTOGoal> dtoGoalList;
    private HashSet<DTOTask> dtoTaskList;
    private HashSet<InProgressGoal> inProgressGoalList;
    private HashSet<InProgressTask> inProgressTaskList;
    private HashSet<MonthRepeating> monthRepeatingList;
    private HashSet<Notification> notificationList;
    private HashSet<Reminding> remindingList;
    private HashSet<Repeating> repeatingList;
    private HashSet<TaskLifecycle> taskLifecycleList;
    private HashSet<TaskToGoal> taskToGoalList;
    public HashMap<Long, Integer> deletedItems;

   public MainDTO dtoWithLocalIds;

 public MainDTO() {
  dayList = new HashSet<>();
  deletedTasksList = new HashSet<>();
  doneGoalList = new HashSet<>();
  doneTaskList = new HashSet<>();
  dtoGoalList = new HashSet<>();
  dtoTaskList = new HashSet<>();
  inProgressGoalList = new HashSet<>();
  inProgressTaskList = new HashSet<>();
  monthRepeatingList = new HashSet<>();
  notificationList = new HashSet<>();
  remindingList = new HashSet<>();
  repeatingList = new HashSet<>();
  taskLifecycleList = new HashSet<>();
  taskToGoalList = new HashSet<>();
  deletedItems = new HashMap<>();
  dtoWithLocalIds = new MainDTO();
 }

 public HashSet<Day> getDayList() {
  return dayList;
 }

 public void setDayList(HashSet<Day> dayList) {
  this.dayList = dayList;
 }

 public HashSet<DeletedTask> getDeletedTasksList() {
  return deletedTasksList;
 }

 public void setDeletedTasksList(HashSet<DeletedTask> deletedTasksList) {
  this.deletedTasksList = deletedTasksList;
 }

 public HashSet<DoneGoal> getDoneGoalList() {
  return doneGoalList;
 }

 public void setDoneGoalList(HashSet<DoneGoal> doneGoalList) {
  this.doneGoalList = doneGoalList;
 }

 public HashSet<DoneTask> getDoneTaskList() {
  return doneTaskList;
 }

 public void setDoneTaskList(HashSet<DoneTask> doneTaskList) {
  this.doneTaskList = doneTaskList;
 }

 public HashSet<DTOGoal> getDtoGoalList() {
  return dtoGoalList;
 }

 public void setDtoGoalList(HashSet<DTOGoal> dtoGoalList) {
  this.dtoGoalList = dtoGoalList;
 }

 public HashSet<DTOTask> getDtoTaskList() {
  return dtoTaskList;
 }

 public void setDtoTaskList(HashSet<DTOTask> dtoTaskList) {
  this.dtoTaskList = dtoTaskList;
 }

 public HashSet<InProgressGoal> getInProgressGoalList() {
  return inProgressGoalList;
 }

 public void setInProgressGoalList(HashSet<InProgressGoal> inProgressGoalList) {
  this.inProgressGoalList = inProgressGoalList;
 }

 public HashSet<InProgressTask> getInProgressTaskList() {
  return inProgressTaskList;
 }

 public void setInProgressTaskList(HashSet<InProgressTask> inProgressTaskList) {
  this.inProgressTaskList = inProgressTaskList;
 }

 public HashSet<MonthRepeating> getMonthRepeatingList() {
  return monthRepeatingList;
 }

 public void setMonthRepeatingList(HashSet<MonthRepeating> monthRepeatingList) {
  this.monthRepeatingList = monthRepeatingList;
 }

 public HashSet<Notification> getNotificationList() {
  return notificationList;
 }

 public void setNotificationList(HashSet<Notification> notificationList) {
  this.notificationList = notificationList;
 }

 public HashSet<Reminding> getRemindingList() {
  return remindingList;
 }

 public void setRemindingList(HashSet<Reminding> remindingList) {
  this.remindingList = remindingList;
 }

 public HashSet<Repeating> getRepeatingList() {
  return repeatingList;
 }

 public void setRepeatingList(HashSet<Repeating> repeatingList) {
  this.repeatingList = repeatingList;
 }

 public HashSet<TaskLifecycle> getTaskLifecycleList() {
  return taskLifecycleList;
 }

 public void setTaskLifecycleList(HashSet<TaskLifecycle> taskLifecycleList) {
  this.taskLifecycleList = taskLifecycleList;
 }

 public HashSet<TaskToGoal> getTaskToGoalList() {
  return taskToGoalList;
 }

 public void setTaskToGoalList(HashSet<TaskToGoal> taskToGoalList) {
  this.taskToGoalList = taskToGoalList;
 }

 @Override
 public String toString() {
  return "MainDTO{" +
          "dayList=" + dayList +
          ", deletedTasksList=" + deletedTasksList +
          ", doneGoalList=" + doneGoalList +
          ", doneTaskList=" + doneTaskList +
          ", dtoGoalList=" + dtoGoalList +
          ", dtoTaskList=" + dtoTaskList +
          ", inProgressGoalList=" + inProgressGoalList +
          ", inProgressTaskList=" + inProgressTaskList +
          ", monthRepeatingList=" + monthRepeatingList +
          ", notificationList=" + notificationList +
          ", remindingList=" + remindingList +
          ", repeatingList=" + repeatingList +
          ", taskLifecycleList=" + taskLifecycleList +
          ", taskToGoalList=" + taskToGoalList +
          ", deletedItems=" + deletedItems +
          '}';
 }
}

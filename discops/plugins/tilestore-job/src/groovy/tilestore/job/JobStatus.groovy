package tilestore.job

/**
 * Created by gpotts on 4/16/15.
 */
public enum JobStatus {
   READY('READY'),
   RUNNING('RUNNING'),
   PAUSED('PAUSED'),
   CANCELED('CANCELED'),
   FINISHED('FINISHED'),
   FAILED('FAILED')

   String name

   JobStatus(String name) {
      this.name = name
   }

}
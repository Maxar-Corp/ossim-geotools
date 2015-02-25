package joms.geotools.tileapi.app

import groovy.json.JsonSlurper
import joms.geotools.tileapi.hibernate.TileCacheHibernate
import org.springframework.amqp.rabbit.core.RabbitAdmin
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.AmqpTemplate
import org.springframework.amqp.core.AmqpAdmin
import org.springframework.amqp.core.BindingBuilder
import groovy.json.JsonSlurper
import groovy.json.JsonBuilder

/**
 * Created by gpotts on 2/25/15.
 */
class TileCacheRabbitJob {
  def tileCacheAppConfig

  static void main(String[] args) {
    TileCacheRabbitJob tileCacheApp = new TileCacheRabbitJob()


    tileCacheApp.run(args)

  }

  def getArgumentParser() {
    def cli = new CliBuilder(usage: 'TileCacheRabbitJob [options]')
    // Create the list of options.
    cli.with {
      h longOpt: 'help', argName: "help", 'Show usage information'
      _ longOpt: 'db-config', args: 1, argName: 'dbConfig', 'Postgres and accumulo definitions accumulo definitions'
      _ longOpt: 'queue', args: 1, argName: "queue", 'Name of the queue.  Default is omar.job.tilecache'
      _ longOpt: 'host', args: 1, argName: "host", 'hostname where the queue resides.  Default is localhost'
      _ longOpt: 'port', args: 1, argName: "port", 'port where the queue resides.  Default is 5672'
      _ longOpt: 'username', args: 1, argName: "username", 'username that has read write privileges'
      _ longOpt: 'password', args: 1, argName: "password", "username's password"

    }

    cli
  }

  boolean initializeParameters(String[] args) {
    tileCacheAppConfig = [queue   : "omar.job.tilecache",
                          host    : "localhost",
                          port    : 5672,
                          username: "omar",
                          password: "abc123!@#"]
    def cli = getArgumentParser()
    def options = cli.parse(args)

    if (!options.'db-config') {
      throw new Exception("Need to specify config XML for the databases.  \nUse --db-config-template to generate a template for you to fill in")
    }
    File dbConfig = new File(options.'db-config')
    def rootNode = new XmlSlurper().parse(dbConfig)
    if (dbConfig.exists()) {
      if (rootNode) {
        def hibernateOptions = [
                driverClassName     : rootNode.postgres.driverClassName,
                username            : rootNode.postgres.username,
                password            : rootNode.postgres.password,
                url                 : rootNode.postgres.url,
                accumuloInstanceName: rootNode.accumulo.instanceName,
                accumuloPassword    : rootNode.accumulo.password,
                accumuloUsername    : rootNode.accumulo.username,
                accumuloZooServers  : rootNode.accumulo.zooServers]

        def hibernate = new TileCacheHibernate()
        hibernate.initialize(hibernateOptions)
        tileCacheAppConfig.hibernateOptions = hibernateOptions
        tileCacheAppConfig.hibernate = hibernate
        tileCacheAppConfig.tileCacheServiceDao = hibernate.applicationContext.getBean("tileCacheServiceDAO");
      } else {
        throw new Exception("Config file specified does not exist: ${dbConfig}")
      }
    }
    if (options.queue) {
      tileCacheAppConfig.queue = options.queue
    }
    if (options.port) {
      tileCacheAppConfig.port = options.port.toInteger()
    }
    if (options.host) {
      tileCacheAppConfig.host = options.host
    }
    if (options.username) {
      tileCacheAppConfig.username = options.username
    }
    if (options.password) {
      tileCacheAppConfig.password = options.password
    }

    true
  }

  void run(String[] args) {
    if (initializeParameters(args)) {

      println "PARAMS: ${tileCacheAppConfig}"
      def cf = new CachingConnectionFactory(tileCacheAppConfig.host)


      cf.username=tileCacheAppConfig.username
      cf.password=tileCacheAppConfig.password
      cf.host=tileCacheAppConfig.host
      cf.port=tileCacheAppConfig.port
      //def admin = new RabbitAdmin(cf);

      def queue = new Queue(tileCacheAppConfig.queue,true, false, false);



      def template = new RabbitTemplate(cf);
// set up the listener and container
      def container = new SimpleMessageListenerContainer(cf);
      def listener = new Object() {
        public void handleMessage(byte[] bytes)
        {
          def result = new JsonSlurper().parseText(new String(bytes, "UTF-8"))

          println "As bytes groovy: ${new String(bytes, "UTF-8")}";
          def builder = new groovy.json.JsonBuilder()
          builder{
            jobId result.jobId
            message "started running"
            status  "RUNNING"
            percentComplete 100.0
          }

          // template.convertAndSend("", "omar.job.status",
          //                         builder.toString());

          //builder = new groovy.json.JsonBuilder()
          //    builder{
          //      jobId result.jobId
          //      message "Finished executing"
          //      status  "FINISHED"
          //      percentComplete 100.0
          //   }

          // template.convertAndSend("", "omar.job.status",
          //                          builder.toString());
//

          // java.io.ObjectInputStream objIn =
          //     new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bytes));
          // println objIn.readObject() as String
        }
        public void handleMessage(String value) {

          println value;

          // def result = new JsonSlurper().parseText(value)
          //  println "As String groovy: ${value}";
          // template.convertAndSend("omar.exchange", "omar.job.stage",
          //                         "{jobID:${result.jobID},status:FINISHED, percentComplete:100.0}".toString());

        }
        public void handleMessage(Map foo) {
          println foo;
        }
      }
      def adapter = new MessageListenerAdapter(listener);
      container.setPrefetchCount(1);
      container.setMaxConcurrentConsumers(8);
      container.setConcurrentConsumers(3);
      container.setMessageListener(adapter);
      container.setQueueNames(queue.name);
      container.start();
// send something
  println "Listening for messages:  hit CTRL-C to stop"
     while(true)
     {

     }
     container.shutdown()

    }
  }
}
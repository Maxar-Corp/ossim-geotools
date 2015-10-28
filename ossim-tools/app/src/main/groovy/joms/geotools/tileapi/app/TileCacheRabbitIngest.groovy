package joms.geotools.tileapi.app

import joms.geotools.tileapi.TwoWayPasswordEncoder
import joms.geotools.tileapi.ingest.TileCacheIngest
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.core.Queue

import java.util.concurrent.ArrayBlockingQueue

/**
 * Created by gpotts on 2/25/15.
 */
class TileCacheRabbitIngest
{
   def tileCacheAppConfig
   def ingestOptions

   static void main(String[] args) {
      TileCacheRabbitIngest tileCacheApp = new TileCacheRabbitIngest()

      tileCacheApp.run(args)
   }

   def getArgumentParser() {
      def cli = new CliBuilder(usage: 'TileCacheRabbitJob [options]')
      // Create the list of options.
      cli.with {
         h longOpt: 'help', argName: "help", 'Show usage information'
         _ longOpt: 'db-config', args: 1, argName: 'dbConfig', 'Postgres and accumulo definitions accumulo definitions'
         _ longOpt: 'queue', args: 1, argName: "queue", 'Name of the queue.  Default is omar.tilestore.ingest'
         _ longOpt: 'host', args: 1, argName: "host", 'hostname where the queue resides.  Default is localhost'
         _ longOpt: 'port', args: 1, argName: "port", 'port where the queue resides.  Default is 5672'
         _ longOpt: 'username', args: 1, argName: "username", 'username that has read write privileges'
         _ longOpt: 'password', args: 1, argName: "password", "username's password"

      }

      cli
   }

   boolean initializeParameters(String[] args) {
      tileCacheAppConfig = [queue   : "omar.tilestore.ingest",
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
            ingestOptions = [
                    driverClassName     : rootNode.postgres.driverClassName.toString(),
                    username            : rootNode.postgres.username.toString(),
                    password            : rootNode.postgres.password.toString(),
                    url                 : rootNode.postgres.url.toString(),
                    accumuloInstanceName: rootNode.accumulo.instanceName.toString(),
                    accumuloPassword    : TwoWayPasswordEncoder.decryptPasswordOptionallyEncrypted(rootNode.accumulo.password.toString()),
                    accumuloUsername    : rootNode.accumulo.username.toString(),
                    accumuloZooServers  : rootNode.accumulo.zooServers.toString()]

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
         tileCacheAppConfig.password = TwoWayPasswordEncoder.decryptPasswordOptionallyEncrypted(options.password)
      }

      true
   }


   void handleMessage(byte[] bytes)
   {
      String message =  new String(bytes, "UTF-8")

      handleMessage(message)

   }


   void handleMessage(String value) {

      println "HANDLING MESSAGE: ${value}"

      def tileCacheIngest = new TileCacheIngest(ingestOptions)

      tileCacheIngest.execute(value);
   }
   void handleMessage(Map foo) {
      println foo;
   }


   void run(String[] args) {
      if (initializeParameters(args)) {

         // println "PARAMS: ${tileCacheAppConfig}"
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
         def adapter = new MessageListenerAdapter(this);
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
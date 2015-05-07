package tilestore.job

import joms.geotools.tileapi.TwoWayPasswordEncoder
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate

/**
 * Created by gpotts on 4/1/15.
 */
class RabbitMQProducer
{
   def grailsApplication
   String  host
   Integer port
   String  username
   String  password
   def cf
   def template
   def ingest
   def product

   void init()
   {
      if(cf) this.destroy()

      password = (new TwoWayPasswordEncoder()).decryptPasswordOptionallyEncrypted(password)
      cf = new CachingConnectionFactory();
      cf.username=username
      cf.password=password
      cf.host=host
      cf.port=port?:5672
      template = new RabbitTemplate(cf);
   }

   void sendIngestMessage(String message)
   {
      this.sendMessage(ingest?.queue, message)
   }

   void sendProductMessage(String message)
   {
      this.sendMessage(product?.queue, message)
   }
   void sendMessage(String queue, String message)
   {
      template?.convertAndSend(queue, message)
   }
   void destroy()
   {
      cf?.destroy()
      cf = null
      template = null
   }
}

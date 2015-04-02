import tilecache.RabbitMQProducer

// Place your Spring DSL code here
beans = {
   rabbitProducer(RabbitMQProducer){ bean ->
      bean.autowire      = 'rabbitProducer'
      bean.initMethod    = 'init'
      bean.destroyMethod = 'destroy'
      host            = grailsApplication.config?.rabbitmq?.connection?.host?:""
      port            = grailsApplication.config?.rabbitmq?.connection?.port?:5672
      username        = grailsApplication.config?.rabbitmq?.connection?.username?:""
      password        = grailsApplication.config?.rabbitmq?.connection?.password?:""
   }
}

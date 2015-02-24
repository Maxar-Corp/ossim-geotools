package org.ossim.core


class RabbitType
{
	enum ExchangeType
	{
		DIRECT(0), 
		TOPIC(1), 
//		FANOUT(2), 
//		HEADERS(3),
		private final int value
		ExchangeType(int value){this.value = value}
      static def valuesAsString(){list()}
      static def list(){this.values().collect(){it.toString()}}
   }
}

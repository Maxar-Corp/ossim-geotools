package org.ossim.omar.hibernate.domain.io

class XmlIoHints
{
	static def NONE          = 0
	static def STORE_META    = 1

	// will remove the meta hierarchy and collapse all children o have the
	// same parent.  Currently the parent name is <metadata> ... </metadata>
	static def COLLAPSE_META = 2  

	static def DEFAULTS = NONE
	
	private def hints

	XmlIoHints(){
		hints = this.DEFAULTS
	}
	XmlIoHints(def hint){
		hints = hint
	}
	def set(def hint)
	{
		hints |= hint;

		this
	}
	def isSet(def hint)
	{
		hints & hint
	}
	def unset(def hint)
	{
		hints = hints & ~hint;

		this
	}
	def clear()
	{
		hints = NONE

		this
	}
}
package jel.ui.communicator
{
	import flash.events.ErrorEvent;
	
	[Bindable]
	[RemoteClass(alias="jel.server.JelException")]
	public class JelException
	{
		public var description : String;
	}
}
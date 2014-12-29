package jel.ui.communicator
{
	[Bindable]
	[RemoteClass(alias="jel.security.LoginSession")]
	public class Session
	{
		public var userID : int;    
	    public var token : String;	    
	}
}
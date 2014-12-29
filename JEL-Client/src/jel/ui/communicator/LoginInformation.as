package jel.ui.communicator
{
	[Bindable]
	[RemoteClass(alias="jel.security.LoginInformation")]
	public class LoginInformation
	{
		public var session : Session;
	    public var firstName : String;
	    public var lastName : String;
	    public var isMasterAdmin : Boolean;
	    public var serverTime : Date;
	    
	}
}
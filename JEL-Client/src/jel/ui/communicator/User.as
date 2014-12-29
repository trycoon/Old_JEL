package jel.ui.communicator
{
	[Bindable]
	[RemoteClass(alias="jel.user.User")]
	public class User
	{
	   public var ID : int;
	   public var username : String;
	   public var firstname : String;
	   public var lastname : String;
	   public var description : String;
	   public var password : String;
	   public var createTime : Date;
	   public var lastLoggedInTime : Date;
	   public var isDisabled : Boolean;
	}
}
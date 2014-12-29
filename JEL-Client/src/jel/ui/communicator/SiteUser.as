package jel.ui.communicator
{
	[Bindable]
	[RemoteClass(alias="jel.site.SiteUser")]
	public class SiteUser
	{
		public var userID : int;
		public var siteID : int;
		public var username : String;
		public var permissionType : int;
	}
}
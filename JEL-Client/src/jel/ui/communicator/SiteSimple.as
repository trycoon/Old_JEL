package jel.ui.communicator
{
	[Bindable]
	[RemoteClass(alias="jel.site.SiteSimple")]
	public class SiteSimple
	{
    	public var ID : int;
    	public var name : String;
    	public var isAdmin : Boolean;

	}
}
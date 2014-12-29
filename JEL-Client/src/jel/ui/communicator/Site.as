package jel.ui.communicator
{
	[Bindable]
	[RemoteClass(alias="jel.site.Site")]
	public class Site
	{
	    public var ID : int;	    
	    public var name : String;	    
	    public var description : String;	
	    public var createTime : Date;
	    public var width : int;  
	    public var height : int;    
	    public var backgroundImageUrl : String;
	    public var backgroundColor : String;	    
	    public var imageRepeatX : Boolean;	    
	    public var imageRepeatY : Boolean;
	    public var allowAnonymousUsers : Boolean;
	    public var isAdmin : Boolean;
	}
}
package jel.ui.communicator
{
	import flash.utils.ByteArray;
	
	[Bindable]
	[RemoteClass(alias="jel.site.SitePersistantContainer")]
	public class SitePersistantContainer
	{
		public var site : Site;
		public var rawImage : ByteArray;
		public var imageName : String;
	}
}
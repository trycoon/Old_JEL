package jel.ui.communicator
{
	[Bindable]
	[RemoteClass(alias="jel.hardware.adapter.AdapterDescription")]
	public class AdapterDescription
	{		
		public var name : String;
		public var version : String;
    	public var selectedPort : String;
    	public var possiblePorts : Array;
    	public var isPressent : Boolean;    	
	}
}
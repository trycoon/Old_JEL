package jel.ui.admin
{
	import flash.system.Capabilities;
	
	import jel.ui.communicator.Communicator;
	
	import flash.events.Event;
	import mx.containers.Canvas;
	import mx.controls.Alert;
	import mx.controls.Button;
	import mx.controls.TextArea;
	import mx.events.FlexEvent;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	import mx.messaging.messages.ErrorMessage;
	
	
	public class SystemTabClass extends Canvas implements JelTab
	{
		private var mServer : Communicator;
		
		// GUI
		public var clientInfoTextArea : TextArea;
		public var serverInfoTextArea : TextArea;
		public var restartButton : Button;
		public var upgradeButton : Button;
		
		
		public function SystemTabClass()
		{
			this.addEventListener(FlexEvent.CREATION_COMPLETE, init);
		}


		// Called when MXML-file is fully loaded.
		private function init(event : FlexEvent) : void
		{

		}
		
		
		public function selected() : void
		{
			mServer = Communicator.getInstance();
			
			getClientInformation();
			getServerInformation();
		}
		
		
		public function unselected() : void
		{
			mServer = null;
		}
		
		
		private function getClientInformation() : void
		{
			var result : String = "Operatingsystem: " + flash.system.Capabilities.os + "\n";
			result += "Flashplayer: " + flash.system.Capabilities.version + ", type: " + flash.system.Capabilities.playerType + "\n";
			result += "Playermanufacturer: " + flash.system.Capabilities.manufacturer + "\n";
			result += "Flash total consumed memory: " + Math.floor(flash.system.System.totalMemory / 1024) + "KB\n";
			result += "Screen resolution: " + flash.system.Capabilities.screenResolutionX + "x" + flash.system.Capabilities.screenResolutionY + "\n";
			result += "Has printing support: " + flash.system.Capabilities.hasPrinting + "\n";
			result += "Language: " + flash.system.Capabilities.language + "\n";
			result += "Support encryption: " + flash.system.Capabilities.hasTLS;
		
			clientInfoTextArea.text = result;
		}
		
		
		private function getServerInformation() : void
		{
			mServer.getServerInformation(onGetServerInformation, onServerFault);
		}
		
		
		private function onGetServerInformation(event : ResultEvent) : void
		{			
			var result : String = "";

			for each (var line : String in  event.result)
				result += line + "\n";
				
			serverInfoTextArea.text = result;	
		}
		
		
		private function onServerFault(event : FaultEvent) : void
		{
			var errorMessage : ErrorMessage = event.message as ErrorMessage;
			var errorText : String = errorMessage.rootCause.message;
			if (errorText == null || errorText.length == 0)
				errorText = "Unspecified servererror";
			
			mx.controls.Alert.show(errorText, "Error", Alert.OK, this);
		}
	}
}
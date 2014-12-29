package jel.ui.admin
{
	import flash.events.MouseEvent;	
	import jel.ui.communicator.*;	
	import mx.containers.Canvas;
	import mx.controls.Alert;
	import mx.controls.Button;
	import mx.controls.Label;
	import mx.controls.List;
	import mx.events.CloseEvent;
	import mx.events.FlexEvent;
	import mx.events.ListEvent;
	import mx.messaging.messages.ErrorMessage;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	
	
	public class DevicesTabClass extends Canvas implements JelTab
	{
		private var mServer : Communicator;
		
		// GUI
		
		
		public function DevicesTabClass()
		{
			this.addEventListener(FlexEvent.CREATION_COMPLETE, init);
		}

		// Called when MXML-file is fully loaded.
		private function init(event : FlexEvent) : void
		{	
			
		}
		
		
		// Called when tab is selected, setup and initialize stuff here.
		public function selected() : void
		{
			mServer = Communicator.getInstance();
			
		}
		
		
		// Called when tab is unselected, free and remove stuff here.
		public function unselected() : void
		{
			mServer = null;
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
package jel.ui.admin
{
	import flash.events.Event;	
	import jel.ui.communicator.*;	
	import mx.containers.TitleWindow;
	import mx.events.CloseEvent;
	import mx.events.FlexEvent;	
	import flash.events.MouseEvent;	
	
	
	public class AddDeviceDialogClass extends TitleWindow
	{
		public function AddDeviceDialogClass()
		{
			this.addEventListener(FlexEvent.CREATION_COMPLETE, init);
		}
		
		
		// Called when MXML-file is fully loaded.
		private function init(event : FlexEvent) : void
		{
			addEventListener(CloseEvent.CLOSE, hideDialog, false, 0, true);
		}
		
		
		private function hideDialog(event : CloseEvent) : void
		{
			removeEventListener(FlexEvent.CREATION_COMPLETE, init);
			removeEventListener(CloseEvent.CLOSE, hideDialog);

		
		}

	}
}
package jel.ui.admin
{
	import flash.events.FocusEvent;
	import flash.events.MouseEvent;
	
	import jel.ui.communicator.*;
	
	import mx.collections.ArrayCollection;
	import mx.containers.Canvas;
	import mx.controls.Alert;
	import mx.controls.Button;
	import mx.controls.ComboBox;
	import mx.controls.DataGrid;
	import mx.events.CloseEvent;
	import mx.events.FlexEvent;
	import mx.events.ListEvent;
	import mx.messaging.messages.ErrorMessage;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	
	
	public class AdapterTabClass extends Canvas implements JelTab
	{
		private var mServer : Communicator;
		private var mAvailableAdapters : Array;
		private var mUsedAdapters : Array;	
		private var mSelectedAdapter : AdapterDescription;	
		
		// GUI
		public var adapterDataGrid : DataGrid;
		public var adaptersComboBox : ComboBox;
		public var portsComboBox : ComboBox;
		public var addAdapterButton : Button;
		public var removeAdapterButton : Button;
		
		
		public function AdapterTabClass()
		{
			this.addEventListener(FlexEvent.CREATION_COMPLETE, init);
		}


		// Called when MXML-file is fully loaded.
		private function init(event : FlexEvent) : void
		{	
			addAdapterButton.addEventListener(MouseEvent.CLICK, onAddAdapterButtonClick, false, 0, true);
			removeAdapterButton.addEventListener(MouseEvent.CLICK, onRemoveAdapterButtonClick, false, 0, true);
			adaptersComboBox.addEventListener(ListEvent.CHANGE, onAdapterComboBoxSelect, false, 0, true);
			adapterDataGrid.addEventListener(ListEvent.ITEM_CLICK, onAdapterClick, false, 0, true);
			//adapterDataGrid.addEventListener(FocusEvent.FOCUS_OUT, onAdapterListLoseFocus, false, 0, true);
		}
		
		
		// Called when tab is selected, setup and initialize stuff here.
		public function selected() : void
		{
			mServer = Communicator.getInstance();
			removeAdapterButton.enabled = false;
			
			getAvailableAdapters();
			
		}
		
		
		// Called when tab is unselected, free and remove stuff here.
		public function unselected() : void
		{
			mServer = null;
		}
		
		
		private function onAddAdapterButtonClick(event : MouseEvent) : void
		{
		}	
		
		
		private function onRemoveAdapterButtonClick(event : MouseEvent) : void
		{
			mSelectedAdapter = AdapterDescription(adapterDataGrid.selectedItem);
			
			mx.controls.Alert.show("Are you sure you want to remove this adapter?", "Remove adapter", Alert.YES|Alert.NO, this, onRemoveAdapterDialogAnswer);
		}
		
		
		private function onRemoveAdapterDialogAnswer(event : CloseEvent) : void
		{
			if (event.detail == Alert.YES)
			{
				mServer.removeUsedAdapter(onRemoveAdapter, onServerFault, mSelectedAdapter);
			}
		}
		
		
		private function onAdapterComboBoxSelect(event : ListEvent) : void
		{
			var selectedAdapter : AdapterDescription = AdapterDescription(event.target.selectedItem);
			
			if (selectedAdapter != null && selectedAdapter.possiblePorts != null)
			{
				portsComboBox.dataProvider = selectedAdapter.possiblePorts;
			}
			else
			{
				portsComboBox.dataProvider = null;
			}
		}
		
		
		private function onAdapterClick(event : ListEvent) : void
		{
			removeAdapterButton.enabled = true;				
		}
		
		/*
		private function onAdapterListLoseFocus(event : FocusEvent) : void
		{
			removeAdapterButton.enabled = false;				
		}*/
		
		
		private function filterAndSetAvailableAdapters() : void
		{
			var availableAdapters : ArrayCollection = new ArrayCollection(mAvailableAdapters);
			var usedAdapters : ArrayCollection = new ArrayCollection(mUsedAdapters);
			var addableAdapters : ArrayCollection = new ArrayCollection();
			
			for each (var availableAdapter : AdapterDescription in availableAdapters)
			{
				var uniquePorts : ArrayCollection = new ArrayCollection();
				
				for each (var availablePort : String in availableAdapter.possiblePorts)
				{		
					var portUnique : Boolean = true;					
					for each (var usedAdapter : AdapterDescription in usedAdapters)
					{						
						if  (availablePort == usedAdapter.selectedPort)
						{
							portUnique = false;
						}
					}
					
					if (portUnique)
						uniquePorts.addItem(availablePort);
				}
				
				if (uniquePorts.length > 0)
				{
					availableAdapter.possiblePorts = uniquePorts.toArray();
					addableAdapters.addItem(availableAdapter);		
				}
			}
			
			
			adaptersComboBox.dataProvider = addableAdapters;
			adaptersComboBox.labelField = "name";
			
			if (adaptersComboBox.selectedItem != null && adaptersComboBox.selectedItem.possiblePorts != null)
			{
				portsComboBox.dataProvider = adaptersComboBox.selectedItem.possiblePorts;
			}
			else
			{
				portsComboBox.dataProvider = null;
			}
		}
		
		
		private function onServerFault(event : FaultEvent) : void
		{
			var errorMessage : ErrorMessage = event.message as ErrorMessage;
			var errorText : String = errorMessage.rootCause.message;
			if (errorText == null || errorText.length == 0)
				errorText = "Unspecified servererror";
			
			mx.controls.Alert.show(errorText, "Error", Alert.OK, this);
		}
		
		
		private function getAvailableAdapters() : void
		{
			mServer.getAvailableAdapters(onGetAvailableAdapters, onServerFault);
		}
		
		
		private function getUsedAdapters() : void
		{
			mServer.getUsedAdapters(onGetUsedAdapters, onServerFault);
		}		
		
		
		///////////////////////////////////////////////////////////
		///////// Server callbacks ////////////////////////////////
		///////////////////////////////////////////////////////////
		private function onGetAvailableAdapters(event : ResultEvent) : void
		{
			mAvailableAdapters = event.result.list.source;
			getUsedAdapters();			
		}
		
	
		private function onGetUsedAdapters(event : ResultEvent) : void
		{
			removeAdapterButton.enabled = false;	
			
			mUsedAdapters = event.result.list.source;
			adapterDataGrid.dataProvider = mUsedAdapters;	
			
			filterAndSetAvailableAdapters();			
		}	
		
		
		private function onRemoveAdapter(event : ResultEvent) : void
		{
			getAvailableAdapters();
		}	
	}
}
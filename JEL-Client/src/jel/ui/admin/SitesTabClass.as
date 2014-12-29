package jel.ui.admin
{
	import flash.events.Event;
	import flash.events.MouseEvent;
	import flash.net.FileFilter;
	import flash.net.FileReference;
	import flash.utils.ByteArray;
	
	import jel.ui.communicator.Communicator;
	import jel.ui.communicator.Site;
	import jel.ui.communicator.SitePersistantContainer;
	
	import mx.containers.Canvas;
	import mx.controls.Alert;
	import mx.controls.Button;
	import mx.controls.CheckBox;
	import mx.controls.ColorPicker;
	import mx.controls.Image;
	import mx.controls.Label;
	import mx.controls.List;
	import mx.controls.NumericStepper;
	import mx.controls.TextArea;
	import mx.controls.TextInput;
	import mx.events.CloseEvent;
	import mx.events.FlexEvent;
	import mx.events.ListEvent;
	import mx.managers.PopUpManager;
	import mx.messaging.messages.ErrorMessage;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	import mx.utils.ObjectUtil;
	import mx.utils.StringUtil;
	
	
	public class SitesTabClass extends Canvas implements JelTab
	{
		private var mServer : Communicator;
		private var mFileRef : FileReference;
		private var mNewImageRaw : ByteArray;
		private var mNewImageName : String;
		
		// GUI
		public var createdTimeLabel : Label;
		public var nameTextInput : TextInput;
		public var descriptionTextArea : TextArea;
		public var widthNumericStepper : NumericStepper;
		public var heightNumericStepper : NumericStepper;
		public var backgroundColorPicker : ColorPicker;
		public var backgroundImage : Image;
		public var imageBrowseButton : Button;
		public var imageNoneButton : Button;
		public var imageRepeatXCheckBox : CheckBox;
		public var imageRepeatYCheckBox : CheckBox;
		public var anonymousUsersCheckBox : CheckBox;
		public var usersButton : Button;
		public var saveButton : Button;
		public var addButton : Button;
		public var removeButton : Button;
		public var siteList : List;

		
		public function SitesTabClass()
		{
			this.addEventListener(FlexEvent.CREATION_COMPLETE, init);
		}


		// Called when MXML-file is fully loaded.
		private function init(event : FlexEvent) : void
		{
			imageBrowseButton.addEventListener(MouseEvent.CLICK, onImageBrowseButtonClick, false, 0, true);
			imageNoneButton.addEventListener(MouseEvent.CLICK, onImageNoneButtonClick, false, 0, true);
			usersButton.addEventListener(MouseEvent.CLICK, onUsersButtonClick, false, 0, true);
			siteList.addEventListener(ListEvent.ITEM_CLICK, onListClick, false, 0, true);	
			addButton.addEventListener(MouseEvent.CLICK, onAddButtonClick, false, 0, true);
			removeButton.addEventListener(MouseEvent.CLICK, onRemoveButtonClick, false, 0, true);
			saveButton.addEventListener(MouseEvent.CLICK, onSaveButtonClick, false, 0, true);
			
			// For image uploads
			mFileRef = new FileReference();
			mFileRef.addEventListener(Event.SELECT, onFileRefSelect, false, 0, true);
            mFileRef.addEventListener(Event.COMPLETE, onFileRefComplete, false, 0, true);
		}
			
		
		// Called when tab is selected, setup and initialize stuff here.
		public function selected() : void
		{
			mNewImageRaw = null;
			mNewImageName = null;
			
			mServer = Communicator.getInstance();
			getSiteList();
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
		
		
		private function getSiteList() : void
		{
			mServer.getSites(onGetSites, onServerFault);
			clearSiteFields();
			removeButton.enabled = false;
		}
		
		
		private function onGetSites(event : ResultEvent) : void
		{
			var sites : Array = event.result.list.source;

			siteList.dataProvider = sites;
			siteList.labelField = "name";
			siteList.selectedIndex = -1;
		}
		
		
		private function clearSiteFields() : void
		{
			nameTextInput.text = null;
			descriptionTextArea.text = null;
			widthNumericStepper.value = -1;
			heightNumericStepper.value = -1;
			backgroundColorPicker.selectedColor = 8821927;	//#869ca7
			backgroundImage.source = null;
			imageRepeatXCheckBox.selected = false;
			imageRepeatYCheckBox.selected = false;
			anonymousUsersCheckBox.selected = false;
			createdTimeLabel.text = null;
			usersButton.enabled = false; 
			usersButton.toolTip = "Create site first";
			
			mNewImageRaw = null;
			mNewImageName = null;
		}
		
		
		private function onListClick(event : ListEvent) : void
		{
			var site : Site = Site(event.currentTarget.selectedItem);
			
			nameTextInput.text = site.name;
			descriptionTextArea.text = site.description;
			widthNumericStepper.value = site.width;
			heightNumericStepper.value = site.height;
			backgroundColorPicker.selectedColor = uint(site.backgroundColor);
			backgroundImage.source = site.backgroundImageUrl;
			imageRepeatXCheckBox.selected = site.imageRepeatX;
			imageRepeatYCheckBox.selected = site.imageRepeatY;
			anonymousUsersCheckBox.selected = site.allowAnonymousUsers;
			createdTimeLabel.text = site.createTime.toLocaleString();
			usersButton.enabled = true;
			usersButton.toolTip = null;
			
			removeButton.enabled = true;
		}
	
		
		private function onUsersButtonClick(event : MouseEvent) : void
		{
			var site : Site = Site(siteList.selectedItem);
			
			var mSiteUserWindow : SiteUsersDialog = new SiteUsersDialog();
			mSiteUserWindow.setSite(site.ID);
			
            PopUpManager.addPopUp(mSiteUserWindow, this, true);
            PopUpManager.centerPopUp(mSiteUserWindow);
		}		
		
		
		private function onAddButtonClick(event : MouseEvent) : void
		{
			clearSiteFields();
			siteList.selectedIndex = -1;
			removeButton.enabled = false;
			nameTextInput.setFocus();
		}
		
		
		private function onRemoveButtonClick(event : MouseEvent) : void
		{
			var site : Site = Site(siteList.selectedItem);
			
			Alert.show("Are you sure you would like to remove site? Removing site will also delete all logrecords for devices bound to this site!", "Remove site", Alert.YES | Alert.NO, this, function (event : CloseEvent) : void 
			{
				if (event.detail == Alert.YES)	
					mServer.removeSite(onSiteRemoved, onServerFault, site.ID);
			}, null, Alert.NO );				
		}
		
		
		private function onSiteRemoved(event : ResultEvent) : void
		{
			getSiteList();
		}
		
		
		private function onSaveButtonClick(event : MouseEvent) : void
		{
			if (StringUtil.trim(nameTextInput.text).length < 1)
			{
				Alert.show("You must enter a sitename.", "Missing sitename", Alert.OK, this);
				nameTextInput.setFocus();
				return;
			}

			var site : Site = Site(siteList.selectedItem);
			
			var siteContainer : SitePersistantContainer = new SitePersistantContainer();
			
			// New site?
			if (site == null)
			{			
				site = new Site();
				site.name = StringUtil.trim(nameTextInput.text);
				site.description = StringUtil.trim(descriptionTextArea.text);
				site.width = widthNumericStepper.value;
				site.height = heightNumericStepper.value;
				site.backgroundColor = backgroundColorPicker.selectedColor.toString();
				site.backgroundImageUrl = null;				
				site.imageRepeatX = imageRepeatXCheckBox.selected;
				site.imageRepeatY = imageRepeatYCheckBox.selected;
				site.allowAnonymousUsers = anonymousUsersCheckBox.selected;
				siteContainer.rawImage = mNewImageRaw;
				siteContainer.imageName = mNewImageName;
				
				siteContainer.site = site;
				
				mServer.addSite(onAddSite, onServerFault, siteContainer);
			}
			else
			{
				var updatedSite : Site = Site(ObjectUtil.copy(site));
				
				updatedSite.name = StringUtil.trim(nameTextInput.text);
				updatedSite.description = StringUtil.trim(descriptionTextArea.text);
				updatedSite.width = widthNumericStepper.value;
				updatedSite.height = heightNumericStepper.value;
				updatedSite.backgroundColor = backgroundColorPicker.selectedColor.toString();

				// If image has been changed, clear URL.
				if (backgroundImage.source is String)
					updatedSite.backgroundImageUrl = String(backgroundImage.source);
				else
					updatedSite.backgroundImageUrl = null;
					
				updatedSite.imageRepeatX = imageRepeatXCheckBox.selected;
				updatedSite.imageRepeatY = imageRepeatYCheckBox.selected;
				updatedSite.allowAnonymousUsers = anonymousUsersCheckBox.selected;
				siteContainer.rawImage = mNewImageRaw;
				siteContainer.imageName = mNewImageName;
				
				siteContainer.site = updatedSite;
				
				mServer.updateSite(onUpdateSite, onServerFault, siteContainer);
			}
		}
		
		
		private function onAddSite(event : ResultEvent) : void
		{
			mNewImageRaw = null;
			mNewImageName = null;
			
			Alert.show("New site \"" + Site(event.result).name + "\" successfully created.", "Add site", Alert.OK, this);
			getSiteList();
		}
		
		
		private function onUpdateSite(event : ResultEvent) : void
		{
			mNewImageRaw = null;
			mNewImageName = null;
			
			getSiteList();	
		}
		
		
		private function onImageBrowseButtonClick(event : MouseEvent) : void
		{			
			mFileRef.browse([new FileFilter("All images", "*.gif;*.jpeg;*.jpg;*.png"), new FileFilter("JPEG(*.jpg;*.jpeg)", "*.jpeg;*.jpg"), new FileFilter("GIF(*.gif)", "*.gif"), new FileFilter("PNG(*.png)", "*.png")]);
		}
		
		
        private function onFileRefSelect(event : Event) : void 
        {
            try {
            	
            		var fileRef : FileReference = FileReference(event.target);
           			fileRef.load();   
           			  
            } catch (error : Error) 
            {
              Alert.show(error.message, "Error");
            }
        }


        private function onFileRefComplete(event : Event) : void 
        {
        	var fileRef : FileReference = FileReference(event.target);
        	
        	backgroundImage.source = fileRef.data;
        	mNewImageRaw = fileRef.data;
        	mNewImageName = fileRef.name;
        }
            
            
		private function onImageNoneButtonClick(event : MouseEvent) : void
		{
			backgroundImage.source = null;
			mNewImageRaw = null;
			mNewImageName = null;
		}
	}
}
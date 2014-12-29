package jel.ui.admin
{
	import flash.events.Event;
	import flash.events.FocusEvent;
	import flash.events.MouseEvent;
	
	import jel.ui.communicator.*;
	
	import mx.collections.ArrayCollection;
	import mx.collections.Sort;
	import mx.collections.SortField;
	import mx.containers.TitleWindow;
	import mx.controls.Alert;
	import mx.controls.Button;
	import mx.controls.CheckBox;
	import mx.controls.List;
	import mx.events.CloseEvent;
	import mx.events.FlexEvent;
	import mx.events.ListEvent;
	import mx.managers.PopUpManager;
	import mx.messaging.messages.ErrorMessage;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	
	
	public class SiteUsersDialogClass extends TitleWindow
	{
		private var mSiteID : int;
		private var mServer : Communicator;
		[Bindable]
		private var mUsers : ArrayCollection;		// Array of SiteUser
		[Bindable]
		private var mSiteUsers : ArrayCollection;	// Array of SiteUser
		 
		// GUI
		public var availableUsersList : List;
		public var siteUsersList : List;
		public var addUserButton : Button;
		public var removeUserButton :Button;
		public var administratorCheckBox : CheckBox;
		public var saveButton : Button;
		public var closeButton : Button;		
		
		
		public function SiteUsersDialogClass()
		{
			this.addEventListener(FlexEvent.CREATION_COMPLETE, init);
		}


		// Called when MXML-file is fully loaded.
		private function init(event : FlexEvent) : void
		{
			availableUsersList.dragEnabled = true;
			availableUsersList.dragMoveEnabled = true; 
			availableUsersList.dropEnabled = true;
			siteUsersList.dragEnabled = true;
			siteUsersList.dragMoveEnabled = true;
			siteUsersList.dropEnabled = true;
			siteUsersList.toolTip = "Drag or add users here to become member of site";
			addUserButton.enabled = false;
			removeUserButton.enabled = false;	
			administratorCheckBox.enabled = false;					
			mServer = Communicator.getInstance();
			
			addEventListener(CloseEvent.CLOSE, hideDialog, false, 0, true);
			closeButton.addEventListener(MouseEvent.CLICK, hideDialog, false, 0, true);
			availableUsersList.addEventListener(ListEvent.ITEM_CLICK, onUsersListItemClick, false, 0 ,true);
			availableUsersList.addEventListener(FocusEvent.FOCUS_IN, onAvailableUsersListClick, false, 0 ,true);
			siteUsersList.addEventListener(ListEvent.ITEM_CLICK, onSiteUsersListItemClick, false, 0 ,true);	
			siteUsersList.addEventListener(FocusEvent.FOCUS_IN, onSiteUsersListClick, false, 0 ,true);
			addUserButton.addEventListener(MouseEvent.CLICK, onAddUserButtonClick, false, 0, true);
			removeUserButton.addEventListener(MouseEvent.CLICK, onRemoveUserButtonClick, false, 0, true);
			administratorCheckBox.addEventListener(MouseEvent.CLICK, onAdminCheckBoxChanged, false, 0, true);
			availableUsersList.labelField = "username";
			siteUsersList.labelField = "username";
			saveButton.addEventListener(MouseEvent.CLICK, onSaveButtonClick, false, 0, true);
			
			getUserList();
		}
		
		
		public function setSite(siteID : int) : void
		{
			mSiteID = siteID;
		}
		
		private function hideDialog(event : Event) : void
		{
			//mServer = null;	// Strange, this kills  the reference for all other consumers of the communicator! 
			removeEventListener(FlexEvent.CREATION_COMPLETE, init);
			removeEventListener(CloseEvent.CLOSE, hideDialog);	
			closeButton.removeEventListener(MouseEvent.CLICK, hideDialog);
			availableUsersList.removeEventListener(ListEvent.ITEM_CLICK, onUsersListItemClick);
			availableUsersList.removeEventListener(FocusEvent.FOCUS_IN, onAvailableUsersListClick);
			siteUsersList.removeEventListener(ListEvent.ITEM_CLICK, onSiteUsersListItemClick);	
			siteUsersList.removeEventListener(FocusEvent.FOCUS_IN, onSiteUsersListClick);
			addUserButton.removeEventListener(MouseEvent.CLICK, onAddUserButtonClick);
			removeUserButton.removeEventListener(MouseEvent.CLICK, onRemoveUserButtonClick);
			administratorCheckBox.removeEventListener(MouseEvent.CLICK, onAdminCheckBoxChanged);
			saveButton.removeEventListener(MouseEvent.CLICK, onSaveButtonClick);
			
			PopUpManager.removePopUp(this);				
		}
		
		
		private function onAvailableUsersListClick(event : FocusEvent) : void
		{
			siteUsersList.selectedItem = null;
			removeUserButton.enabled = false;
			administratorCheckBox.enabled = false;	
			administratorCheckBox.selected = false;
		}
		
		
		private function onSiteUsersListClick(event : FocusEvent) : void
		{
			availableUsersList.selectedItem = null;
			addUserButton.enabled = false;
		}
		
		
		private function onServerFault(event : FaultEvent) : void
		{
			var errorMessage : ErrorMessage = event.message as ErrorMessage;
			var errorText : String = errorMessage.rootCause.message;
			if (errorText == null || errorText.length == 0)
				errorText = "Unspecified servererror";
			
			mx.controls.Alert.show(errorText, "Error", Alert.OK, this);
		}
		
		
		private function getUserList() : void
		{
			mUsers = null;
			mServer.getUsers(onGetUsers, onServerFault);			
		}
		
		
		private function onGetUsers(event : ResultEvent) : void
		{
			var tmpUsers : Array = event.result.list.source;
			
			if (tmpUsers != null && tmpUsers.length > 0)
			{				
				mUsers = new ArrayCollection();

				// "cast" User to SiteUser so that we can manage them as the same type of objects when moving users between lists.  
				for each (var user : User in tmpUsers)
				{
					if (user.ID != 1)	// Filter away master-admin.
					{
						var siteUser : SiteUser = new SiteUser();
						
						siteUser.userID = user.ID;
						siteUser.siteID = mSiteID;
						siteUser.username = user.username;
						siteUser.permissionType = 0;
						
						mUsers.addItem(siteUser);
					}
				}
	
				getSiteUsersList();
			}
		}
		
		
		private function getSiteUsersList() : void
		{		
			mSiteUsers = null;
			mServer.getSiteUsers(onGetSiteUsers, onServerFault, mSiteID);
		}
		
		
		private function onGetSiteUsers(event : ResultEvent) : void
		{
			mSiteUsers = new ArrayCollection(event.result.list.source);			
			
			// Remove users that has been added to site from userlist.
			if (mSiteUsers.length > 0)
			{
				for (var userIndex : int = 0; userIndex < mUsers.length; userIndex++)
				{
					for (var siteUserIndex : int = 0; siteUserIndex < mSiteUsers.length; siteUserIndex++)
					{
						if (mUsers[userIndex].userID == mSiteUsers[siteUserIndex].userID)
						{
							mUsers.removeItemAt(userIndex);
							--userIndex;
							break;
						}				
					}
				}	
			}
				
			availableUsersList.dataProvider = mUsers;
			availableUsersList.selectedIndex = -1;
			
			siteUsersList.dataProvider = mSiteUsers;
			siteUsersList.selectedIndex = -1;
		}


		private function onUsersListItemClick(event : ListEvent) : void
		{
			addUserButton.enabled = true;	
			administratorCheckBox.enabled = false;	
			administratorCheckBox.selected = false;
		}
		
		
		private function onSiteUsersListItemClick(event : ListEvent) : void
		{
			var user : SiteUser = SiteUser(event.currentTarget.selectedItem);	
			administratorCheckBox.selected = user.permissionType > 0 ? true : false
			
			removeUserButton.enabled = true;
			administratorCheckBox.enabled = true;				
		}
		
		
		private function onAddUserButtonClick(event : MouseEvent) : void
		{
			var user : SiteUser = SiteUser(availableUsersList.selectedItem);
			
			if (user != null)
			{
				mSiteUsers.addItem(user);
				mUsers.removeItemAt(availableUsersList.selectedIndex);				
				sortSiteUsersList();
				
				// Check if user has already been added.
				/*var userExists : Boolean;
				for each (var tmpSiteUser : SiteUser in mSiteUsers)
				{
					if (tmpSiteUser.userID == user.ID)
					{
						userExists = true;
						break;
					}				
				}
			
				if (userExists)
				{
					Alert.show("User has already been added", "User exists", Alert.OK, this); 
				}
				else
				{
					var siteUser : SiteUser = new SiteUser();				
					siteUser.userID = user.ID;
					siteUser.siteID = mSiteID;
					siteUser.username = user.username;
					siteUser.permissionType = 0;	// You set permissions when user has been added to siteuser-list.
					
					mSiteUsers.addItem(siteUser);
					
					sortSiteUsersList();
				}
				*/
			}
		}
		
		
		private function onRemoveUserButtonClick(event : MouseEvent) : void
		{
			var user : SiteUser = SiteUser(siteUsersList.selectedItem);
			
			if (user != null)
			{
				mUsers.addItem(user);
				mSiteUsers.removeItemAt(siteUsersList.selectedIndex);
				sortUsersList();
				
				/*
				var index : int = siteUsersList.selectedIndex;
	
				mSiteUsers.removeItemAt(index);				
						
				if (mSiteUsers.length - 1 < index)
					index = mSiteUsers.length - 1;
						
				if (index >= 0)
				{
					siteUsersList.selectedItem = mSiteUsers.getItemAt(index);
					administratorCheckBox.selected = SiteUser(mSiteUsers.getItemAt(index)).permissionType > 0 ? true : false;
				}
				else
				{
					siteUsersList.selectedItem = null;
					administratorCheckBox.selected = false;
				}*/
			}
		}
		

		private function onAdminCheckBoxChanged(event : MouseEvent) : void
		{
			var user : SiteUser = SiteUser(mSiteUsers.getItemAt(siteUsersList.selectedIndex));
	
			if (user != null)
			{
				user.permissionType = administratorCheckBox.selected == true ? 1 : 0;
			}
		}
		
		
		private function onSaveButtonClick(event : MouseEvent) : void
		{
			mServer.setSiteUsers(onSaveSettings, onServerFault, mSiteID, mSiteUsers.toArray());			
		}
		
		
		private function onSaveSettings(event : ResultEvent) : void
		{
			Alert.show("Siteusers has successfully been updated", "Settings saved", Alert.OK, this);
		}
		
		
		private function sortSiteUsersList() : void
		{
			if (mSiteUsers != null)
			{
				var dataSortField : SortField = new SortField();
				dataSortField.name = "username";
				dataSortField.caseInsensitive = true;
						
				mSiteUsers.sort = new Sort();
				mSiteUsers.sort.fields = [dataSortField];
						
				mSiteUsers.refresh();
			}
		}
		
		
		private function sortUsersList() : void
		{
			if (mUsers != null)
			{
				var dataSortField : SortField = new SortField();
				dataSortField.name = "username";
				dataSortField.caseInsensitive = true;
						
				mUsers.sort = new Sort();
				mUsers.sort.fields = [dataSortField];
						
				mUsers.refresh();
			}
		}
	}
}	// http://www.adobe.com/devnet/flex/quickstart/adding_drag_and_drop/
	// När man lägger till en användare så skall den försvinna från andra listan, och tvärtom...
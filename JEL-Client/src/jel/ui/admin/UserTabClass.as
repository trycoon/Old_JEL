package jel.ui.admin
{	
	import flash.events.MouseEvent;
	
	import jel.ui.communicator.*;
	
	import mx.containers.Canvas;
	import mx.controls.Alert;
	import mx.controls.Button;
	import mx.controls.CheckBox;
	import mx.controls.DataGrid;
	import mx.controls.Label;
	import mx.controls.List;
	import mx.controls.TextArea;
	import mx.controls.TextInput;
	import mx.events.CloseEvent;
	import mx.events.FlexEvent;
	import mx.events.ListEvent;
	import mx.messaging.messages.ErrorMessage;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	import mx.utils.StringUtil;
	import mx.utils.ObjectUtil;
	
	
	public class UserTabClass extends Canvas implements JelTab
	{
		private var mServer : Communicator;
		
		// GUI
		public var usernameTextInput : TextInput;
		public var firstnameTextInput : TextInput;
		public var lastnameTextInput : TextInput;
		public var passwordTextInput : TextInput;
		public var repeatPasswordTextInput : TextInput;
		public var descriptionTextArea : TextArea;
		public var userDisabledCheckBox : CheckBox;
		public var createdTimeLabel : Label;
		public var userEventDataGrid : DataGrid;
		public var userList : List;
		public var saveButton : Button;
		public var addButton : Button;
		public var removeButton : Button;
		
		
		public function UserTabClass()
		{
			this.addEventListener(FlexEvent.CREATION_COMPLETE, init);
		}
		
		
		// Called when MXML-file is fully loaded.
		private function init(event : FlexEvent) : void
		{	
			userList.addEventListener(ListEvent.ITEM_CLICK, onListClick, false, 0 ,true);	
			addButton.addEventListener(MouseEvent.CLICK, onAddButtonClick, false, 0, true);
			removeButton.addEventListener(MouseEvent.CLICK, onRemoveButtonClick, false, 0, true);
			saveButton.addEventListener(MouseEvent.CLICK, onSaveButtonClick, false, 0, true);
		}


		// Called when tab is selected, setup and initialize stuff here.
		public function selected() : void
		{
			mServer = Communicator.getInstance();
			getUserList();
		}
		
		
		// Called when tab is unselected, free and remove stuff here.
		public function unselected() : void
		{
			mServer = null;
		}
		
		
		private function getUserList() : void
		{
			mServer.getUsers(onGetUsers, onServerFault);
			clearUserFields();
			removeButton.enabled = false;
		}
		
		
		private function onGetUsers(event : ResultEvent) : void
		{
			var users : Array = event.result.list.source;
			
			userList.dataProvider = users;
			userList.labelField = "username";
			userList.selectedIndex = -1;
		}
		
		
		private function clearUserFields() : void
		{
			usernameTextInput.enabled = true;
			usernameTextInput.text = null;
			firstnameTextInput.text = null;
			lastnameTextInput.text = null;
			passwordTextInput.text = null;
			repeatPasswordTextInput.text = null;
			descriptionTextArea.text = null;
			userDisabledCheckBox.enabled = true;
			userDisabledCheckBox.selected = false;
			createdTimeLabel.text = null;
			userEventDataGrid.dataProvider = null;
		}
		
		
		private function onServerFault(event : FaultEvent) : void
		{
			var errorMessage : ErrorMessage = event.message as ErrorMessage;
			var errorText : String = errorMessage.rootCause.message;
			if (errorText == null || errorText.length == 0)
				errorText = "Unspecified servererror";
			
			mx.controls.Alert.show(errorText, "Error", Alert.OK, this);
		}
		
		
		private function onListClick(event : ListEvent) : void
		{
			var user : User = User(event.currentTarget.selectedItem);
			
			usernameTextInput.text = user.username;
			firstnameTextInput.text = user.firstname;
			lastnameTextInput.text = user.lastname;
			passwordTextInput.text = user.password;
			repeatPasswordTextInput.text = user.password;
			descriptionTextArea.text = user.description;
			userDisabledCheckBox.selected = user.isDisabled;
			
			createdTimeLabel.text = user.createTime.toLocaleString();	
			
			// Can't disable or remove MasterAdmin
			if (user.ID > 1)
			{
				removeButton.enabled = true;
				removeButton.toolTip = null;
				usernameTextInput.enabled = true;
				usernameTextInput.toolTip = null;
				userDisabledCheckBox.enabled = true;
				userDisabledCheckBox.toolTip = null;
			}
			else
			{
				removeButton.enabled = false;
				removeButton.toolTip = "It's not allowed to remove master-admin account";
				usernameTextInput.enabled = false;
				usernameTextInput.toolTip = "It's not allowed to rename master-admin account";
				userDisabledCheckBox.enabled = false;
				userDisabledCheckBox.toolTip = "It's not allowed to disable master-admin account";
			}				
		}
		
		
		private function onAddButtonClick(event : MouseEvent) : void
		{
			clearUserFields();
			userList.selectedIndex = -1;
			removeButton.enabled = false;
			usernameTextInput.setFocus();
		}		
		
		
		private function onRemoveButtonClick(event : MouseEvent) : void
		{
			var user : User = User(userList.selectedItem);
			
			if (user != null && user.ID > 1)
			{
				Alert.show("Are you sure you would like to remove user?", "Remove user", Alert.YES | Alert.NO, this, function (event : CloseEvent) : void 
				{
					if (event.detail == Alert.YES)	
						mServer.removeUser(onUserRemoved, onServerFault, user.ID);
				}, null, Alert.NO );				
			}
		}
		
		
		private function onUserRemoved(event : ResultEvent) : void
		{
			getUserList();
		}
		
		
		private function onSaveButtonClick(event : MouseEvent) : void
		{
			if (StringUtil.trim(usernameTextInput.text).length < 1)
			{
				Alert.show("You must enter a username.", "Missing username", Alert.OK, this);
				usernameTextInput.setFocus();
				return;
			}
			
			if (StringUtil.trim(passwordTextInput.text).length < 1)
			{
				Alert.show("You must enter a password.", "Missing password", Alert.OK, this);
				passwordTextInput.setFocus();
				return;
			}
			if (StringUtil.trim(repeatPasswordTextInput.text) != StringUtil.trim(passwordTextInput.text))
			{
				Alert.show("Passwords missmatch, please enter passwords again.", "Wrong password", Alert.OK, this);
				passwordTextInput.setFocus();
				return;
			}

			var user : User = User(userList.selectedItem);
			
			// New user?
			if (user == null)
			{
				user = new User();
				user.username = StringUtil.trim(usernameTextInput.text);
				user.firstname = StringUtil.trim(firstnameTextInput.text);
				user.lastname = StringUtil.trim(lastnameTextInput.text);
				user.password = StringUtil.trim(passwordTextInput.text);
				user.description = StringUtil.trim(descriptionTextArea.text);
				user.isDisabled = userDisabledCheckBox.selected;
				
				mServer.addUser(onAddUser, onServerFault, user);
			}
			else
			{
				var updatedUser : User = User(ObjectUtil.copy(user));	// Deep clone to not affect object in userlist.
				
				// master-admin can't change username. Safetycheck.
				if (updatedUser.ID > 1)
					updatedUser.username = StringUtil.trim(usernameTextInput.text);
				
				updatedUser.firstname = StringUtil.trim(firstnameTextInput.text);
				updatedUser.lastname = StringUtil.trim(lastnameTextInput.text);
				
				if (StringUtil.trim(passwordTextInput.text) != updatedUser.password)
					updatedUser.password = StringUtil.trim(passwordTextInput.text);
				else
					updatedUser.password = null;	// If password has not changed, then send null to use old one.
					
				updatedUser.description = StringUtil.trim(descriptionTextArea.text);
			
				// master-admin can't be disabled. Safetycheck.
				if (updatedUser.ID > 1)
					updatedUser.isDisabled = userDisabledCheckBox.selected;
				
				mServer.updateUser(onUpdateUser, onServerFault, updatedUser);
			}
		}
		
		
		private function onAddUser(event : ResultEvent) : void
		{
			Alert.show("New user \"" + User(event.result).username + "\" successfully created.", "Add user", Alert.OK, this);
			getUserList();
		}
		
		
		private function onUpdateUser(event : ResultEvent) : void
		{
			getUserList();	
		}
	}
}
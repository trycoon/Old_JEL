package jel.ui
{
	import flash.events.Event;
	import flash.events.KeyboardEvent;
	import flash.events.MouseEvent;
	
	import jel.ui.admin.AdminDialog;
	import jel.ui.communicator.*;
	import jel.ui.event.*;
	import jel.util.Utils;
	
	import mx.containers.Canvas;
	import mx.controls.Alert;
	import mx.controls.Button;
	import mx.controls.ComboBox;
	import mx.controls.Label;
	import mx.controls.TextInput;
	import mx.core.Application;
	import mx.events.FlexEvent;
	import mx.events.ListEvent;
	import mx.managers.PopUpManager;
	import mx.messaging.messages.ErrorMessage;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;	
	
	public class JelClass extends Application
	{
		private var mServer : Communicator;
		private var mSession : Session;
		private var mCurrentSite : Site;
		private var mInAdminMode : Boolean;
		private var mAdminWindow : AdminDialog;
		
		// GUI
		public var usernameTextLabel : Label;
		public var passwordTextLabel : Label;
		public var loginButton : Button;
		public var adminButton : Button;
		public var sitesComboBox : ComboBox;
		public var usernameTextInput : TextInput;
		public var passwordTextInput : TextInput;
		public var loggedInUserLabel : Label;
		public var desktopCanvas : Canvas;
		
		
		public function JelClass()
		{
			this.addEventListener(FlexEvent.CREATION_COMPLETE, init);
		}
		
		
		// Called when MXML-file is fully loaded.
		private function init(event : FlexEvent) : void
		{
			mSession = null;
			mCurrentSite = null;
			mInAdminMode = false;
			
			mServer = Communicator.getInstance();
			
			initEventListeners();
			
			mServer.getSiteList(onGetSiteList, onServerFault);
			usernameTextInput.setFocus();
		}
		
		
		private function initEventListeners() : void
		{
			loginButton.addEventListener(MouseEvent.CLICK, loginButtonPressed, false, 0, true);
			adminButton.addEventListener(MouseEvent.CLICK, adminButtonPressed, false, 0, true);
			sitesComboBox.addEventListener(ListEvent.CHANGE, sitesComboBoxChanged, false, 0, true);
			
			usernameTextInput.addEventListener(KeyboardEvent.KEY_UP, onKeyboard, false, 0, true);
			passwordTextInput.addEventListener(KeyboardEvent.KEY_UP, onKeyboard, false, 0, true);
			loginButton.addEventListener(KeyboardEvent.KEY_UP, onKeyboard, false, 0, true);
		}
		
		
		private function onKeyboard( event : KeyboardEvent ) : void
		{
			if (event.keyCode == 13)	// Enter-key.
			{
				loginButtonPressed(null);
			} 			
		}
		
		
		private function loginButtonPressed( event : MouseEvent ) : void
		{
			if (mSession == null)	// If not logged in, do it.
			{				
				if (usernameTextInput.text.length > 0 && passwordTextInput.text.length > 0)
					mServer.login(onLogin, onServerFault, usernameTextInput.text, passwordTextInput.text);
				else
					Alert.show("You must enter a valid username and password before trying to login.", "Error", Alert.OK, this);
			}
			else					// If logged in, then logout.
			{
				mServer.logout(onLogout, onServerFault);
			}
		}
		
		
		private function adminButtonPressed( event : MouseEvent ) : void
		{
			if (mInAdminMode)
			{
				mInAdminMode = false;
				adminButton.label = "Admin";
				PopUpManager.removePopUp(mAdminWindow);
				mAdminWindow = null;
				loginButton.enabled = true;
			
				this.dispatchEvent(new Event(JelEvent.EXIT_ADMIN));
			}
			else
			{
				adminButton.label = "Exit admin";
				mInAdminMode = true;				
						
				mAdminWindow = new AdminDialog();
				mAdminWindow.x = stage.width / 2 - mAdminWindow.width / 2;
				mAdminWindow.y = stage.height / 2 - mAdminWindow.height / 2;
				
                PopUpManager.addPopUp(mAdminWindow, this, false);
              	loginButton.enabled = false;	// So we can't logout before exiting adminmode and save settings.
              	
              	this.dispatchEvent(new Event(JelEvent.ENTER_ADMIN));
			}
		}
		
		
		private function sitesComboBoxChanged( event : ListEvent ) : void
		{
			var site : SiteSimple = SiteSimple(event.currentTarget.selectedItem);
			
			if (site.ID == 0)
			{
				mCurrentSite = null;
				desktopCanvas.height = 0;
				desktopCanvas.width = 0;
			}
			else
			{
				mServer.getSite(onGetSite, onServerFault, site.ID);			
			}			
		}

		
		
		///////////////////////////////////////////////////////////
		///////// Server callbacks ////////////////////////////////
		///////////////////////////////////////////////////////////
		private function onLogin( event : ResultEvent ) : void
		{
			var response : LoginInformation = LoginInformation(event.result);

			if (response.isMasterAdmin)
				adminButton.visible = true;
				
			loginButton.label = "Logout";
			
			usernameTextLabel.visible = false;
			usernameTextInput.visible = false;
			usernameTextInput.text = "";
			
			passwordTextLabel.visible = false;
			passwordTextInput.visible = false;
			passwordTextInput.text = "";
			
			loggedInUserLabel.text = Utils.clearNulls(response.firstName) + " " + Utils.clearNulls(response.lastName);
			loggedInUserLabel.visible = true;
		
			mSession = response.session;
			mServer.getSiteList(onGetSiteList, onServerFault);	// Get personal sites.
			
			this.dispatchEvent(new Event(JelEvent.LOGIN));
		}		
		
		
		private function onLogout( event : ResultEvent ) : void
		{
			mSession = null;
			mCurrentSite = null;
			
			adminButton.visible = false;
			
			loggedInUserLabel.text = "";
			loggedInUserLabel.visible = false;
			
			loginButton.label = "Login";
			usernameTextLabel.visible = true;
			usernameTextInput.visible = true;
			passwordTextLabel.visible = true;
			passwordTextInput.visible = true;
		
			sitesComboBox.dataProvider = null;
			mServer.getSiteList(onGetSiteList, onServerFault);	// Get public sites.
			
			this.dispatchEvent(new Event(JelEvent.LOGOUT));
		}
		
		
		private function onGetSiteList( event : ResultEvent ) : void
		{
			var sites : Array = event.result.list.source;
			
			var dummySite : SiteSimple = new SiteSimple();
			dummySite.ID = 0; dummySite.name = "-";			
			sites.unshift(dummySite);			

			sitesComboBox.dataProvider = sites;
			sitesComboBox.labelField = "name"; 
		}
		
		
		private function onGetSite( event : ResultEvent ) : void
		{
			var site : Site = Site(event.result);
			mCurrentSite = site;
			
			desktopCanvas.width = site.width;
			desktopCanvas.height = site.height;	
		}
		
		
		private function onServerFault( event : FaultEvent ) : void
		{
			var errorMessage : ErrorMessage = event.message as ErrorMessage;
			
			var errorText : String = "Unspecified servererror";
			
			if (errorMessage.rootCause.message != null || errorMessage.rootCause.message != null || errorMessage.rootCause.message.length > 0)
			{
				errorText = errorMessage.rootCause.message;
			}
			mx.controls.Alert.show(errorText, "Error", Alert.OK, this);
		}
	}	
}
// TODO:
// - Fixa en modal "waiter" som används när man hämtar info från servern.
// - Fixa så servertiden visas nere i högra hörnet. http://www.adobe.com/devnet/flash/samples/time_2/index.html
// - När man uppdaterar informationen om en användare så skall ett event triggas så att t.ex. användarinformationen uppe i högra hörnet uppdateras.
// - När man uppdaterar informationen om en site så skall ett event triggas så att t.ex. sitebläddraren uppdateras och om det är den site man har valt nu
//   så skall även bakgrundsbild och "upplösning" uppdateras. 
// - Kalla på metod i AdminDialogClass när admin-rutan skall stängas ner som frigör och städar, den skall bl.a. kalla på "unselect" på den aktiva tabben!
// - När ett exception returneras så bör meddelandet sparas i klippboardet, så att man snabbt kan klistra in det i ett mail.



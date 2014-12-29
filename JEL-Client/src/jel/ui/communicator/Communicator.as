package jel.ui.communicator
{
	import mx.messaging.Channel;
	import mx.messaging.ChannelSet;
	import mx.messaging.Consumer;
	import mx.messaging.Producer;
	import mx.messaging.channels.AMFChannel;
	import mx.messaging.config.ServerConfig;
	import mx.messaging.events.MessageAckEvent;
	import mx.messaging.events.MessageEvent;
	import mx.messaging.events.MessageFaultEvent;
	import mx.messaging.messages.AsyncMessage;
	import mx.rpc.AbstractOperation;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	import mx.rpc.remoting.RemoteObject;
	
	
	public final class Communicator
	{
		private static const instance : Communicator = new Communicator();
		
		private var mJelService :  RemoteObject;
		private static var mSession : Session;		// Global session, shared between all instances.
		private var mLoginListener : Function;
		private var mLogoutListener : Function;
		private var mConsumer : Consumer;
		private var mProducer : Producer;
							
		
		public static function getInstance() : Communicator
		{
			return instance;
		}
				
		
		public function Communicator()
		{
			if (instance)
				throw new Error("Class could only be access through the public instance-property!");
				
			mJelService = new RemoteObject("jelService");
			
			var amfChanSet : ChannelSet = new ChannelSet();
			var amfChannel : Channel = null;
			
			try {
				amfChannel = ServerConfig.getChannel("jel-amf");
			} catch (exception : Error) { }
			
			// If not running from server, set to local debugging.
			if (mJelService.channelSet == null && amfChannel == null)
			{
				trace("Communicator is running against localhost server.");				
				amfChannel = new AMFChannel("jel-amf", "http://localhost:8080/JEL_Web/messagebroker/amf");
			}
			amfChanSet.addChannel(amfChannel);			
			mJelService.channelSet = amfChanSet;
			
			
			
			/*var streamingAmfChanSet : ChannelSet = new ChannelSet();
			var streamingAmfChannel : Channel = null;			
			mConsumer = new Consumer();
			mConsumer.destination = "jelStreamingService";
			
			try {
				streamingAmfChannel = ServerConfig.getChannel("jel-streaming-amf");
			} catch (exception : Error) { }
			
			streamingAmfChanSet.addChannel(streamingAmfChannel);			
			mConsumer.channelSet = streamingAmfChanSet;
			
			mConsumer.addEventListener(MessageEvent.MESSAGE, onMessageEvent, false, 0, true);
			mConsumer.addEventListener(MessageFaultEvent.FAULT, onMessageFaultEvent, false, 0, true);
			mConsumer.subscribe();*/
				    	
			// If not running from server, set to local debugging.
			/*if (mConsumer.channelSet == null)
			{
				var chanSet2 : ChannelSet = new ChannelSet();
				chanSet2.addChannel(new AMFChannel("jel-streaming-amf", "http://localhost:8080/JEL_Web/messagebroker/streamingamf"));
				mConsumer.channelSet = chanSet2;
			}
			mConsumer.destination = "jeldevices";
			mConsumer.addEventListener(MessageEvent.MESSAGE, onMessageEvent, false, 0, true);
			mConsumer.addEventListener(MessageFaultEvent.FAULT, onMessageFaultEvent, false, 0, true);
			//mConsumer.subscribe(); // TODO: Fix this
			*/
			/*mProducer = new Producer;
			mProducer.destination = "jelStreamingService";
			mProducer.channelSet = streamingAmfChanSet;
			mProducer.addEventListener(MessageAckEvent.ACKNOWLEDGE, onMessageAckEvent, false, 0, true);
			mProducer.addEventListener(MessageFaultEvent.FAULT, onMessageFaultEvent, false, 0, true);
			
			sendMessage();*/
			/*
			// If not running from server, set to local debugging.
			if (mProducer.channelSet == null)
			{
				var chanSet3 : ChannelSet = new ChannelSet();
				chanSet3.addChannel(new AMFChannel("jel-streaming-amf", "http://localhost:8080/JEL_Web/messagebroker/streamingamf"));
				mProducer.channelSet = chanSet2;
			}
			mProducer.destination = "jeldevices";
			mProducer.addEventListener(MessageAckEvent.ACKNOWLEDGE, onMessageAckEvent, false, 0, true);
			mProducer.addEventListener(MessageFaultEvent.FAULT, onMessageFaultEvent, false, 0, true);
			*/
		}
		
		
		// Trap and store session for future servercalls.
		private function onLogin( event : ResultEvent ) : void
		{
			var response : LoginInformation = LoginInformation(event.result);
			mSession = response.session;			
			mLoginListener.call(this, event.clone()); 
		}
		        
		private function onLogout( event : ResultEvent ) : void
		{
			mSession = null;			
			mLogoutListener.call(this, event.clone()); 
		}
		
		private function onMessageEvent(event : MessageEvent) : void
		{
					
		}
		
		private function onMessageFaultEvent(event : MessageFaultEvent) : void
		{
			
		}
		    
		private function onMessageAckEvent(event : MessageAckEvent) : void
		{
				
		}		    
		////////// Remote methods ////////////////////////////////////////////////
		// These are the methods exposed on the serverside
		//////////////////////////////////////////////////////////////////////////
		
		
		public function login(onResult : Function, onFault : Function, username : String, password : String) : void {
			var op : mx.rpc.AbstractOperation = mJelService.getOperation("login");
			mLoginListener = onResult;
			op.addEventListener(ResultEvent.RESULT, onLogin, false, 0, true);
			op.addEventListener(FaultEvent.FAULT, onFault, false, 0, true);
			op.send.apply(null, new Array(username, password));
    	}

    	public function logout(onResult : Function, onFault : Function) : void {
			var op : mx.rpc.AbstractOperation = mJelService.getOperation("logout");
			mLogoutListener = onResult;
			op.addEventListener(ResultEvent.RESULT, onLogout, false, 0, true);
			op.addEventListener(FaultEvent.FAULT, onFault, false, 0, true);
			op.send.apply(null, new Array(mSession));
   		}

	    public function verifySession(onResult : Function, onFault : Function) : void {
			var op : mx.rpc.AbstractOperation = mJelService.getOperation("verifySession");
			op.addEventListener(ResultEvent.RESULT, onResult, false, 0, true);
			op.addEventListener(FaultEvent.FAULT, onFault, false, 0, true);
			op.send.apply(null, new Array(mSession));
	    }
    
	    public function getServerVersion(onResult : Function, onFault : Function) : void {
			var op : mx.rpc.AbstractOperation = mJelService.getOperation("getServerVersion");
			op.addEventListener(ResultEvent.RESULT, onResult, false, 0, true);
			op.addEventListener(FaultEvent.FAULT, onFault, false, 0, true);
			op.send();

	    }

	    public function getDiskFull(onResult : Function, onFault : Function) : void {
			var op : mx.rpc.AbstractOperation = mJelService.getOperation("getDiskFull");
			op.addEventListener(ResultEvent.RESULT, onResult, false, 0, true);
			op.addEventListener(FaultEvent.FAULT, onFault, false, 0, true);
			op.send();
	    }

	    public function getServerInformation(onResult : Function, onFault : Function) : void {
			var op : mx.rpc.AbstractOperation = mJelService.getOperation("getServerInformation");
			op.addEventListener(ResultEvent.RESULT, onResult, false, 0, true);
			op.addEventListener(FaultEvent.FAULT, onFault, false, 0, true);
			op.send.apply(null, new Array(mSession));
	    }
    
    
    	// begin Site
	    public function addSite(onResult : Function, onFault : Function, siteContainer : SitePersistantContainer) : void {
			var op : mx.rpc.AbstractOperation = mJelService.getOperation("addSite");
			op.addEventListener(ResultEvent.RESULT, onResult, false, 0, true);
			op.addEventListener(FaultEvent.FAULT, onFault, false, 0, true);
			op.send.apply(null, new Array(mSession, siteContainer));
	    }

	    public function getSite(onResult : Function, onFault : Function, id : int) : void {
			var op : mx.rpc.AbstractOperation = mJelService.getOperation("getSite");
			op.addEventListener(ResultEvent.RESULT, onResult, false, 0, true);
			op.addEventListener(FaultEvent.FAULT, onFault, false, 0, true);
			op.send.apply(null, new Array(mSession, id));
	    }
	
	    public function getSiteList(onResult : Function, onFault : Function) : void {
			var op : mx.rpc.AbstractOperation = mJelService.getOperation("getSiteList");
			op.addEventListener(ResultEvent.RESULT, onResult, false, 0, true);
			op.addEventListener(FaultEvent.FAULT, onFault, false, 0, true);
			op.send.apply(null, new Array(mSession));
	    }
	
	    public function getSites(onResult : Function, onFault : Function) : void {
			var op : mx.rpc.AbstractOperation = mJelService.getOperation("getSites");
			op.addEventListener(ResultEvent.RESULT, onResult, false, 0, true);
			op.addEventListener(FaultEvent.FAULT, onFault, false, 0, true);
			op.send.apply(null, new Array(mSession));
	    }
	
	    public function removeSite(onResult : Function, onFault : Function, id : int) : void {
			var op : mx.rpc.AbstractOperation = mJelService.getOperation("removeSite");
			op.addEventListener(ResultEvent.RESULT, onResult, false, 0, true);
			op.addEventListener(FaultEvent.FAULT, onFault, false, 0, true);
			op.send.apply(null, new Array(mSession, id));
	    }
	
	    public function updateSite(onResult : Function, onFault : Function, siteContainer : SitePersistantContainer) : void {
			var op : mx.rpc.AbstractOperation = mJelService.getOperation("updateSite");
			op.addEventListener(ResultEvent.RESULT, onResult, false, 0, true);
			op.addEventListener(FaultEvent.FAULT, onFault, false, 0, true);
			op.send.apply(null, new Array(mSession, siteContainer));
	    }
	    
	    public function addSiteUser(onResult : Function, onFault : Function, userID : int, siteID : int, permissionType : int) : void {
			var op : mx.rpc.AbstractOperation = mJelService.getOperation("addSiteUser");
			op.addEventListener(ResultEvent.RESULT, onResult, false, 0, true);
			op.addEventListener(FaultEvent.FAULT, onFault, false, 0, true);
			op.send.apply(null, new Array(mSession, userID, siteID, permissionType));
	    }

	    public function removeSiteUser(onResult : Function, onFault : Function, userID : int, siteID : int) : void {
			var op : mx.rpc.AbstractOperation = mJelService.getOperation("removeSiteUser");
			op.addEventListener(ResultEvent.RESULT, onResult, false, 0, true);
			op.addEventListener(FaultEvent.FAULT, onFault, false, 0, true);
			op.send.apply(null, new Array(mSession, userID, siteID));
	    }
	    
	    public function updateSiteUser(onResult : Function, onFault : Function, userID : int, siteID : int, permissionType : int) : void {
			var op : mx.rpc.AbstractOperation = mJelService.getOperation("updateSiteUser");
			op.addEventListener(ResultEvent.RESULT, onResult, false, 0, true);
			op.addEventListener(FaultEvent.FAULT, onFault, false, 0, true);
			op.send.apply(null, new Array(mSession, userID, siteID, permissionType));
	    }
	    
	    public function getSiteUsers(onResult : Function, onFault : Function, siteID : int) : void {
			var op : mx.rpc.AbstractOperation = mJelService.getOperation("getSiteUsers");
			op.addEventListener(ResultEvent.RESULT, onResult, false, 0, true);
			op.addEventListener(FaultEvent.FAULT, onFault, false, 0, true);
			op.send.apply(null, new Array(mSession, siteID));
	    }	  
	    	    
	    public function setSiteUsers(onResult : Function, onFault : Function, siteID : int, siteUsers : Array) : void {
			var op : mx.rpc.AbstractOperation = mJelService.getOperation("setSiteUsers");
			op.addEventListener(ResultEvent.RESULT, onResult, false, 0, true);
			op.addEventListener(FaultEvent.FAULT, onFault, false, 0, true);
	    	op.send.apply(null, new Array(mSession, siteID, siteUsers));
	    }  	    	    
	    // end Site
	    
	    
	    // begin User
	    public function addUser(onResult : Function, onFault : Function, user : User) : void {
			var op : mx.rpc.AbstractOperation = mJelService.getOperation("addUser");
			op.addEventListener(ResultEvent.RESULT, onResult, false, 0, true);
			op.addEventListener(FaultEvent.FAULT, onFault, false, 0, true);
			op.send.apply(null, new Array(mSession, user));
	    }
	    
	    public function getUser(onResult : Function, onFault : Function, id : int) : void {
			var op : mx.rpc.AbstractOperation = mJelService.getOperation("getUser");
			op.addEventListener(ResultEvent.RESULT, onResult, false, 0, true);
			op.addEventListener(FaultEvent.FAULT, onFault, false, 0, true);
			op.send.apply(null, new Array(mSession, id));
	    }
	    
	    public function getUsers(onResult : Function, onFault : Function) : void {
			var op : mx.rpc.AbstractOperation = mJelService.getOperation("getUsers");
			op.addEventListener(ResultEvent.RESULT, onResult, false, 0, true);
			op.addEventListener(FaultEvent.FAULT, onFault, false, 0, true);
			op.send.apply(null, new Array(mSession));
	    }
	    
	    public function removeUser(onResult : Function, onFault : Function, id : int) : void {
			var op : mx.rpc.AbstractOperation = mJelService.getOperation("removeUser");
			op.addEventListener(ResultEvent.RESULT, onResult, false, 0, true);
			op.addEventListener(FaultEvent.FAULT, onFault, false, 0, true);
			op.send.apply(null, new Array(mSession, id));
	    }
	    	    
	    public function updateUser(onResult : Function, onFault : Function, user : User) : void {
			var op : mx.rpc.AbstractOperation = mJelService.getOperation("updateUser");
			op.addEventListener(ResultEvent.RESULT, onResult, false, 0, true);
			op.addEventListener(FaultEvent.FAULT, onFault, false, 0, true);
			op.send.apply(null, new Array(mSession, user));
	    }		    	    
	    // end User
	    
	    // begin Adapter
	    public function getAvailableAdapters(onResult : Function, onFault : Function) : void {
	    	var op : mx.rpc.AbstractOperation = mJelService.getOperation("getAvailableAdapters");
			op.addEventListener(ResultEvent.RESULT, onResult, false, 0, true);
			op.addEventListener(FaultEvent.FAULT, onFault, false, 0, true);
			op.send.apply(null, new Array(mSession));
	    }

	    public function getUsedAdapters(onResult : Function, onFault : Function) : void {
	    	var op : mx.rpc.AbstractOperation = mJelService.getOperation("getUsedAdapters");
			op.addEventListener(ResultEvent.RESULT, onResult, false, 0, true);
			op.addEventListener(FaultEvent.FAULT, onFault, false, 0, true);
			op.send.apply(null, new Array(mSession));
	    }

	    public function addUsedAdapter(onResult : Function, onFault : Function, adapter : AdapterDescription) : void {
	    	var op : mx.rpc.AbstractOperation = mJelService.getOperation("addUsedAdapter");
			op.addEventListener(ResultEvent.RESULT, onResult, false, 0, true);
			op.addEventListener(FaultEvent.FAULT, onFault, false, 0, true);
			op.send.apply(null, new Array(mSession, adapter));
	    }
	    
	    public function removeUsedAdapter(onResult : Function, onFault : Function, adapter : AdapterDescription) : void {
	    	var op : mx.rpc.AbstractOperation = mJelService.getOperation("removeUsedAdapter");
			op.addEventListener(ResultEvent.RESULT, onResult, false, 0, true);
			op.addEventListener(FaultEvent.FAULT, onFault, false, 0, true);
			op.send.apply(null, new Array(mSession, adapter));
	    }
    // end Adapter
	    
	    private function sendMessage() : void
	    {
	    	var message : AsyncMessage = new AsyncMessage();
	    	message.body = "Hej hej";
	    	message.clientId = mProducer.id;
	    	
	    	mProducer.send(message);
	    }
	}
}
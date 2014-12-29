package jel.ui.admin
{
	public interface JelTab
	{
		// Called when tab is selected, setup and initialize stuff here.
		function selected() : void;	
		
		// Called when tab is unselected, free and remove stuff here.	
		function unselected() : void;
	}
}
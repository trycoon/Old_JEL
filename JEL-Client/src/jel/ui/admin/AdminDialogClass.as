package jel.ui.admin
{
	import caurina.transitions.Tweener;
	
	import flash.system.*;

	import mx.containers.TabNavigator;
	import mx.containers.TitleWindow;
	import mx.events.CloseEvent;
	import mx.events.FlexEvent;
	import mx.events.IndexChangedEvent;
	
	public class AdminDialogClass extends TitleWindow
	{
		public var tabsTabNavigator : TabNavigator;
		 
		
		public function AdminDialogClass()
		{
			this.addEventListener(FlexEvent.CREATION_COMPLETE, init);
		}
		
		
		// Called when MXML-file is fully loaded.
		private function init(event : FlexEvent) : void
		{
			addEventListener(CloseEvent.CLOSE, onHideDialog, false, 0, true);

			tabsTabNavigator.addEventListener(IndexChangedEvent.CHANGE, onTabChanged, false, 0, true);
			JelTab(tabsTabNavigator.getChildAt(0)).selected();
		}


		private function onHideDialog(event : CloseEvent) : void
		{
			Tweener.addTween(this, {scaleX:0.60, time:0.6, transition:"linear"});
			Tweener.addTween(this, {rotationZ:90, time:0.6, transition:"easeInOutBack"});
			Tweener.addTween(this, {x:20, y:10, time:0.8, transition:"easeOutBounce"});
			

			
		}
		
		
		private function onTabChanged(event : IndexChangedEvent) : void
		{
			JelTab(tabsTabNavigator.getChildAt(event.oldIndex)).unselected();
			JelTab(tabsTabNavigator.getChildAt(event.newIndex)).selected();
		}

	}
}
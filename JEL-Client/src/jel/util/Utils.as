package jel.util
{
	public class Utils
	{
		public function Utils()
		{
		}


		public static function clearNulls(text : String) : String
		{
			if (text == null)
				return "";
			else
				return text;
		}
	}
}
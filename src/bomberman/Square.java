package bomberman;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// TODO Work on the idea of more than one object on a square.
// This skeleton class is a good place to start
public class Square implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1121507013822267342L;
	List<Object> objects;
	public Square () {
		objects = new ArrayList<Object>();
	}
	
	public List<Object> getObjects() {
		return objects;
	}
	
	public void addObject(Object o) {
		objects.add(o);
	}
	
	public void removePlayers() {
		List<Object> newObjects = new ArrayList<Object>();
		for (int x=0;x<objects.size();x++) {
			if (!(objects.get(x) instanceof Player)) {
				newObjects.add(objects.get(x));
			}
		}
		objects = newObjects;
	}


}
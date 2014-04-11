package dataset.db;

import java.io.FileInputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.google.common.collect.Maps;

public class DBProvider {
	public final static String PERSISTENT_UNIT = "us_cities"; 
	
	private static DBProvider instance;
	
	private final EntityManager entityManager;

	private DBProvider() {
		final EntityManagerFactory factory = Persistence
				.createEntityManagerFactory(PERSISTENT_UNIT, loadDBProperties());
		this.entityManager = factory.createEntityManager();
	}

	private Map<String, String> loadDBProperties() {
		final Map<String, String> properties = Maps.newHashMap();
		
		System.out.println("DB properties:");
		try{
			Properties configuration = new Properties();
			configuration.load(new FileInputStream("conf/db.conf"));
			for(Map.Entry<Object, Object> entry: configuration.entrySet()){
				properties.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
				System.out.println(entry.getKey()+" = "+entry.getValue());
			}
		}catch(Exception ex){
			System.err.println("Database configuration load error: "+ ex.getMessage());
			ex.printStackTrace();
		}
		return properties;
	}

	public static DBProvider getInstance() {
		if (instance == null) {
			instance = new DBProvider();
		}

		return instance;
	}
	
	@SuppressWarnings("rawtypes")
	public List<Zips> getAllZips(){
		return entityManager.createNamedQuery("findAllZips",Zips.class).getResultList();
	}
	
	public Map<String, String> getCityStateMap(){
		 Map<String, String>  map = Maps.newHashMap();
		 List<Zips> zips = getAllZips();
		 for(Zips zip: zips){
			 map.put(zip.getCity(), zip.getState());
		 }
		 return map;
	}
}

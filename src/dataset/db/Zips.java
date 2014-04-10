package dataset.db;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQuery;

/**
 * The Zips entity represent Database table Zips which saves the city
 * information.
 * 
 * @author qinjin.wang
 *
 */
@NamedQuery(name = "findAllZips", query = "SELECT z FROM Zips z")
@Entity
public class Zips {
	@Id
	private int zip;

	@Column(columnDefinition = "TEXT CHARACTER SET utf8")
	private String state;

	@Column(columnDefinition = "TEXT CHARACTER SET utf8")
	private String city;

	private BigDecimal lat;
	private BigDecimal lng;

	/**
	 * 
	 * @return unique zip code.
	 */
	public int getZip() {
		return zip;
	}

	/**
	 * @return The state name.
	 */
	public String getState() {
		return state;
	}

	/**
	 * @return The city name.
	 */
	public String getCity() {
		return city;
	}

	/**
	 * @return The latitude of the city.
	 */
	public BigDecimal getLat() {
		return lat;
	}

	/**
	 * @return The longitude of the city.
	 */
	public BigDecimal getLng() {
		return lng;
	}
}

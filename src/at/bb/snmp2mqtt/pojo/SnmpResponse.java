package at.bb.snmp2mqtt.pojo;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "snmp")
public class SnmpResponse
{
	@XmlElement(name = "entry", type = SnmpEntry.class)
	private List<SnmpEntry> entries = new ArrayList<SnmpEntry>();

	public List<SnmpEntry> getEntries()
	{
		return entries;
	}

}

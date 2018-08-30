package at.bb.snmp2mqtt.pojo;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "entry")
public class SnmpEntry
{
	private String oid;
	private String value;
	
	public String getOid()
	{
		return oid;
	}
	public void setOid(String oid)
	{
		this.oid = oid;
	}
	public String getValue()
	{
		return value;
	}
	public void setValue(String value)
	{
		this.value = value;
	}
	
	public String toString()
	{
		return this.oid + " = " + this.value;
	}
}

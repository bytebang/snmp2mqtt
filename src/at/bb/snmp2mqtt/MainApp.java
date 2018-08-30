package at.bb.snmp2mqtt;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.apache.camel.main.Main;

import at.bb.snmp2mqtt.pojo.SnmpEntry;
import at.bb.snmp2mqtt.pojo.SnmpResponse;

/**
 * This is the mainclass of the project
 * 
 * @author gue
 *
 */
public class MainApp
{

	public static void main(String[] args) throws Exception
	{
		MainApp application = new MainApp();
		application.boot();
	}

	public void boot() throws Exception
	{
		// create a Main instance
		Main main = new Main();

		// add routes
		main.addRouteBuilder(new MyRouteBuilder());

		// run until you terminate the JVM
		System.out.println("Starting Camel. Use ctrl + c to terminate the JVM.\n");
		main.run();
	}

	private static class MyRouteBuilder extends RouteBuilder
	{
		@Override
		public void configure() throws Exception
		{
			List<String> snmpEndpoints = new ArrayList<>();
			snmpEndpoints.add("snmp:192.168.5.250?type=POLL&oids=1.3.6.1.2.1.43.11.1.1.9");

			// mqtt server konfigurieren ?
			//"1.3.6.1.2.1.43.11.1.1.9:Druckerstatus@192.168.5.250"
			//"1.3.6.1.2.1.43.11.1.1.9:Druckerstatus,1.3.6.1.2.1.43.11.1.1.10:Blaue Tinte@192.168.5.250"
			
			// XML Data Format
			JaxbDataFormat xmlDataFormat = new JaxbDataFormat();
			JAXBContext con = JAXBContext.newInstance(SnmpResponse.class);
			xmlDataFormat.setContext(con);

			// Create an snmp endpoint for each line
			Pattern p = Pattern.compile("snmp:([^?]+)?(.*)");
			
			
			for (String ep : snmpEndpoints) {
				// Find the host from the url
				Matcher m = p.matcher(ep);
				String host;
				if(m.find())
				{
					host = m.group(1);
				}
				else
				{
					host = "unknown";	
				}
				
				// Create the processors
				from(ep).process(new Processor()
				{
					@Override
					public void process(Exchange exchange) throws Exception
					{
						exchange.getIn().setHeader("snmphost", host);
						System.out.println(exchange.getIn().getBody());
					}
				}).to("direct:entries");
			}

			// Take the reponse from the snmp endpoints and extract the eingle entries therefrom
			from("direct:entries").split().tokenizeXML("entry").streaming().unmarshal(xmlDataFormat).to("direct:entry")
					.end();

			// Publish each single entry via mqtt
			from("direct:entry").log("One Entry").process(new Processor()
			{
				@Override
				public void process(Exchange exchange) throws Exception
				{
					SnmpEntry r = exchange.getIn().getBody(SnmpEntry.class);
					
					// calculate the mqtt topic
					String dynamicTopic = "public/" + exchange.getIn().getHeader("snmphost") + "/" + r.getOid();
					// set the topic you want in the string
					exchange.getIn().setHeader("CamelMQTTPublishTopic", dynamicTopic);
					exchange.getIn().setBody(r.getValue());
					System.out.println("Publishing '" + r.getValue() + "' to " + dynamicTopic);
				}
			})
			.to("mqtt://irgeneinserver?host=tcp://mqtt.simpledashboard.io:1883&byDefaultRetain=true")
			.end();



		}
	}

}

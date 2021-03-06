package org.OpenGeoPortal.Ingest.Metadata;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.OpenGeoPortal.Layer.LocationLink;
import org.OpenGeoPortal.Layer.LocationLink.LocationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class FgdcLocationResolver extends AbstractLocationResolver implements
		LocationResolver {
	final Logger logger = LoggerFactory.getLogger(this.getClass());
 
	
	@Override
	public Set<LocationLink> resolveLocation(Document xmlDocument) {
		/*
		 * <onlink>ftp://congo.iluci.org/CARPE_data_explorer/Products/DFCM_products/ltlt/ltlt_457_time2.tif.gz</onlink>
		 * 
		 * 
		 * <browse>
<browsen>ftp://congo.iluci.org/CARPE_data_explorer/Products/DFCM_products/ltlt/ltlt_457_t2.jpg</browsen>
<browsed>jpg</browsed>
<browset>thumbnail</browset>
</browse>
<browse>
<browsen>ftp://congo.iluci.org/CARPE_data_explorer/Products/DFCM_products/ltlt/ltlt_457_t2.jpg</browsen>
<browsed>jpg</browsed>
<browset>large_thumbnail</browset>
</browse>
		 */
		Set<LocationLink> links = new HashSet<LocationLink>();
		Set<String> serviceLinks = getLinksFromTagType("onlink", xmlDocument);
		//look at the links, determine if it's an ows, zip, other filetype
		Iterator<String> serviceIterator = serviceLinks.iterator();
		if (serviceIterator.hasNext()){
			String serviceLink = serviceIterator.next();
			URL serviceURL = null;
			try {
				serviceURL = new URL(serviceLink);
				//determine locationType
				links.add(new LocationLink(parseServiceLocationType(serviceLink), serviceURL));
			} catch (MalformedURLException e) {
				logger.error("Invalid URL:" + serviceLink);
			} catch (Exception e) {
				logger.error(e.getMessage());
			}
		}
		
		Set<String> browseLinks = getLinksFromTagType("browsen", xmlDocument);
		Iterator<String> browseIterator = browseLinks.iterator();
		if (browseIterator.hasNext()){
			//we just need one
			String browseLink = browseIterator.next();
			URL browseURL = null;
			try {
				browseURL = new URL(browseLink);
				links.add(new LocationLink(LocationType.browseGraphic, browseURL));
			} catch (MalformedURLException e) {
				logger.error("Invalid URL:" + browseLink);
			}
		}
		return links;
	}
	
	public Set<String> getLinksFromTagType(String tagName, Document xmlDocument){
		Set<String> linkValueSet = new HashSet<String>();
		NodeList linkNodes = xmlDocument.getElementsByTagName(tagName);
		for (int j = 0; j < linkNodes.getLength(); j++){
			String nodeValue = linkNodes.item(j).getTextContent().trim();
			if (nodeValue.startsWith("http")||nodeValue.startsWith("ftp")){
				linkValueSet.add(nodeValue);
			}
		}
		return linkValueSet;
	}
}

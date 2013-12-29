package com.xx_dev.apn.proxy.config;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.ParsingException;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * User: xmx
 * Date: 13-12-29
 * Time: PM11:57
 */
public abstract class ApnProxyAbstractXmlConfigReader {

    private static final Logger logger = Logger.getLogger(ApnProxyAbstractXmlConfigReader.class);

    public final void read(InputStream xmlConfigFileInputStream) {
        Document doc = null;
        try {
            Builder parser = new Builder();
            doc = parser.build(xmlConfigFileInputStream);
        } catch (ParsingException ex) {
            logger.error(ex.getMessage(), ex);
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex);
        }
        if (doc == null) {
            return;
        }
        Element rootElement = doc.getRootElement();

        realReadProcess(rootElement);
    }

    protected abstract void realReadProcess(Element rootElement);

    public final void read(File xmlConfigFile) throws FileNotFoundException {
        if (xmlConfigFile.exists() && xmlConfigFile.isFile()) {
            read(new FileInputStream(xmlConfigFile));
        }
    }
}

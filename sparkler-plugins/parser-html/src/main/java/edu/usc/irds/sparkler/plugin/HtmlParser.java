package edu.usc.irds.sparkler.plugin;

import edu.usc.irds.sparkler.AbstractExtensionPoint;
import edu.usc.irds.sparkler.Parser;
import java.io.InputStream;
import org.apache.tika.metadata.Metadata;
import org.pf4j.Extension;

/**
 * Created by tg on 12/19/17. A plugin Template
 */
@Extension
public class HtmlParser extends AbstractExtensionPoint implements Parser {

    public int add(int op1, int op2) {
        ////Dummy plugin method
        return op1 + op2;
    }

    @Override
    public Metadata parse(InputStream content, Metadata meta) throws Exception {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        
        meta.add("html", "html parse test");
        return meta;
    }

    @Override
    public Metadata parse(String content, Metadata meta) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}

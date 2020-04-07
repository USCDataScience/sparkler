package edu.usc.irds.sparkler.plugin;

import edu.usc.irds.sparkler.AbstractExtensionPoint;
import org.pf4j.Extension;

/**
 * Created by tg on 12/19/17.
 * A plugin Template
 */
@Extension
public class MyPlugin extends AbstractExtensionPoint {

    public int add(int op1, int op2){
        ////Dummy plugin method
        return op1 + op2;
    }
}

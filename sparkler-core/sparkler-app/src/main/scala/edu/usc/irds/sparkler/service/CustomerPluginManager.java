/*
package edu.usc.irds.sparkler.service;

import org.pf4j.*;

public class CustomerPluginManager {

    public static DefaultPluginManager getPluginManager(){
        return new DefaultPluginManager(){
            @Override
            protected PluginLoader createPluginLoader() {
                return new CompoundPluginLoader()
                        .add(new PluginManagerLoader(this), this::isNotDevelopment);
                        //.add(new JarPluginLoader(this), this::isNotDevelopment)
                        //.add(new DefaultPluginLoader(this), this::isNotDevelopment);

            }
        };
    }
}
*/

package edu.usc.irds.sparkler.service;

import org.pf4j.*;

import java.nio.file.Path;

public class PluginManagerLoader extends JarPluginLoader {
    public PluginManagerLoader(PluginManager pluginManager) {
        super(pluginManager);
    }

    @Override
    public ClassLoader loadPlugin(Path pluginPath, PluginDescriptor pluginDescriptor) {
        if(pluginPath.toString().contains("fetcher-chrome")) {
            PluginClassLoader pluginClassLoader = new PluginClassLoader(pluginManager, pluginDescriptor, getClass().getClassLoader(), ClassLoadingStrategy.PDA);
            pluginClassLoader.addFile(pluginPath.toFile());
            return pluginClassLoader;
        } else{
            PluginClassLoader pluginClassLoader = new PluginClassLoader(pluginManager, pluginDescriptor, getClass().getClassLoader());
            pluginClassLoader.addFile(pluginPath.toFile());
            return pluginClassLoader;
        }


    }

}

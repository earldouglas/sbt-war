scriptedBufferLog := false

ScriptedPlugin.scriptedSettings

scriptedLaunchOpts <+= version { "-Dplugin.version=" + _ }

watchSources <++= sourceDirectory map { path => (path ** "*").get }

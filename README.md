# subs-checklist-service
This project depends on resources from [data-definitions](https://github.com/EMBL-EBI-SUBS/data-definitions).
Which has been added as a git submodule. Please make sure to initialize it after checking out this project:
```
git submodule update --init
```

To update the submodule to latest version:
```
git submodule update --remote
```

More on submodules:  
https://git-scm.com/book/en/v2/Git-Tools-Submodules

## Notes
### Using parallelStream() inside EnaChecklistService
The highlighted code in that class used parallelStream with JDK8 before and worked perfectly. Later in JDK11, 
parallelStream() started causing a no class found exception deep inside spring data when checklist text is parsed into a 
java object using the mongo converter.
 
This is due to spring finding more than one class loader where some of the loaders are unable to locate
the classes that spring data is looking for. This is possibly due to multi-threading:  
https://stackoverflow.com/questions/55452778/parallelstream-causing-classnotfoundexception-with-jaxb-api
https://stackoverflow.com/questions/49110537/parallel-stream-doesnt-set-thread-contextclassloader-after-tomcat-upgrade

The common forkjoin workaround in one solution above did not solve the problem. So the only way around this for now is to
use the regular serial stream().

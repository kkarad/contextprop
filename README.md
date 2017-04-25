# contextprop

The purpose of the library is to consolidate copies of configuration (based on environment, region, etc) into one easy to maintain configuration file. Complicated configuration directory trees which carry complexity and redundancy can be eliminated by using the 'contextprop' library.

```
src/
  main/
    resources/
      env/
        local/
          configuration.properties
        sit/
          configuration.properties
        uat/
          configuration.properties
        prod/
          configuration.properties
```

Instead of having to maintain a directory tree of redundant configuration for every environment, region, etc (deployment scope) we can have one configuration file of which properties can be overridden for a specific deployment scope by using the `.CTXT()` suffix and adding some context about deployment. Using this method we are able to define a configuration property which is the default for most of the deployments and then with the help of the context identifier (`.CTXT()`) we can define an exception. We don't need to create a copy of the configuration for each deployment scope.

```
my.property.key.CTXT(env[sit,uat],loc[london],group[canary])=true
my.property.key=false
```

## Binaries

You can download or reference using your favourite build tool the latest version of the library from [Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.github.kkarad%22)

## How it works

Properties with context follow the standard java property file syntax. To create one append the`.CTXT()` identifier to the property name and add some key/value entries. There are two type of entries, a key/value pair `.CTXT(key1[value1])` and a key/value set `.CTXT(key1[value1,value2,...])`.

```
property.name.CTXT(key1[value1],key2[value21,value22,...],...)=property_value1
property.name=property_value2
```

In the following section we define the terms used by the library and the documentation below.

### Glossary

```
                                  context
                       .-------------^-------------.
                        condition    condition
                       .----^---..-------^---------.
            context              domain  condition
 property  identifier             key     value(s)         value
.---^---.   .--^--.              .-^-. .-----^-----.      .--^--.
username    .CTXT      (env[prd],loc   [ldn,nyk,hkg])  =  kkarad
```

The domain keys are defined by the user in a form of a java enum type (domain). An example of domain definition is listed below:

```
enum MyDomain { 
    env, //environment, such as prod, uat, sit, dev
    loc, //location, such as ldn, nyk, hkg
    grp, //group, such as canary, retail, institutional
    app, //application
    usr, //user
    hst  //host
}
```

The order of the domain (enum) keys also defines the deployment context order. The lower the ordinal of the enum (key) the higher (or else general) the order of the key in the deployment context. The enum type example defines the following deployment context order:

```
env > loc > grp > app > usr > hst
```

The order of the domain helps the library to apply rules and priorities during the property resolution. 

Property entries with the same name but different context form a **_property group_**. The property without context holds the default value. That is if there is no context much against the deployment scope then the default property is used. Obviously there can be only one default property in a property group.

```
# Property group of 'property.name'
property.name.CTXT(key1[value1])=property_value
property.name.CTXT(key1[value1],key2[value21,value22])=property_value
property.name=property_value
```

In the following code fragment we setup the deployment domain and resolve a property group with two entries:

```
//step 1: define your configuration domain
enum MyDomain {
    env, loc, group, app, host, user
}

//step 2: in your application (startup) specify values for each domain key
DomainPredicates predicates = DomainPredicates.basedOnDomain(MyDomain.class)
    .predicate("env", "uat")
    .predicate("loc", "ldn")
    .predicate("group", "internal")
    .predicate("app", "whatsapp")
    .predicate("host", "localhost")
    .predicate("user", "kkarad")
    .create();

//step 3: create or load from a file the java properties which use the contextprop format (here we create one programmatically)
Properties ctxProperties = new Properties();
ctxProperties.setProperty("my.prop.key.CTXT(env[uat],loc[ldn,nyk],group[internal],app[whatsapp],host[localhost],user[kkarad])", "myValue");
ctxProperties.setProperty("my.prop.key", "defaultValue");

//step 4: resolve the contextualised properties to properties (configuration) specific to this application instance  
Properties properties = ContextProperties.create(predicates)
    .requiresDefault(false)
    .resolve(ctxProperties);
    
assertThat(properties.getProperty("my.prop.key")).isEqualTo("myValue");
```
 
In order to avoid conflicts during property resolution we need to follow some rules (if the rules are violated the parser raises an error):

##### Rule: All properties excluding the default one should define all domain keys already defined in the same property group

So for example the following groups are invalid. We are not able to resolve the properties (name1 & name2). Both entries qualify for the `env[prd],loc[loh],grp[external]` deployment context.

```
property.name1.CTXT(env[prd])=true
property.name1.CTXT(loc[loh])=false #invalid! higher ordered key 'env' should be defined

property.name2.CTXT(env[prd],grp[canary])=true
property.name2.CTXT(env[prd],loc[loh])=false #invalid! higher ordered key 'grp' should be defined
```

The following groups are valid. All properties define all higher order domain keys of their group.

```
property.name1.CTXT(usr[john])=true
property.name1.CTXT(usr[john],hst[localhost])=false

property.name2.CTXT(hst[localhost])=true
```

##### Rule: Properties with identical domain keys should not have overlapping values when these are located in the same group

```
property.name.CTXT(env[prd],loc[ldn,nyk])=true
property.name.CTXT(env[prd],loc[ldn,nyk,hkg])=false #invalid! location loh and nyk are overlapping
```

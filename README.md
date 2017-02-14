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

Instead of having to maintain a directory tree of redundant configuration we can have one configuration file of which properties can be overridden for a specific deployment scope (environment, region, etc) by using the `.CTXT()` suffix and adding the deployment context information. Using this method we are able to define a configuration property which is the default for most of the deployments and then with the help of the context suffix we can define an exception. We don't need to create a copy of the configuration for each deployment scope.

```
my.property.key.CTXT(env[sit,uat],loc[london],group[canary])=true
my.property.key=false
```

## How it works

Properties with context follow the standard java property file syntax. To create one append the`.CTXT()` suffix to the property name and add some key/value entries. There are two type of entries, a key/value pair `.CTXT(key1[value1])` and a key/value set `.CTXT(key1[value1,value2,...])`.

```
property.name.CTXT(key1[value1],key2[value21,value22,...],...)=property_value1
property.name=property_value2
```

The context keys are defined by the user in a form of a java enum type. An example of context keys definition is listed below:

```
enum MyContext { 
    env, //environment, such as prod, uat, sit, dev
    loc, //location, such as ldn, nyk, hkg
    grp, //group, such as canary, retail, institutional
    app, //application
    usr, //user
    hst  //host
}
```

The order of the context (enum) keys also defines the deployment context order. The lower the ordinal of the enum (key) the higher (or else general) the order of the key in the deployment context. The enum type example defines the following deployment context order:

```
env > loc > grp > app > usr > hst
```

Property entries with the same name but different context form a **_property group_**. A property with no context works as a default. 
That is if there is no context much against the deployment scope then the default property is used. Obviously there can be only one
default property in a property group.

```
# Property group of 'property.name'
property.name.CTXT(key1[value1])=property_value
property.name.CTXT(key1[value1],key2[value21,value22])=property_value
property.name=property_value
```

In the following code fragment we setup the a context and resolve a property group with two entries:

```
enum MyContext {
    env, loc, grp, usr, hst;
}

Context<MyContext> context = Context.newContext(MyContext.class)
    .add("env", "sit")
    .add("loc", "ldn")
    .add("grp", "canary")
    .add("usr", "john")
    .add("hst", "localhost")
    .build();

Properties properties = new Properties();
properties.setProperty("my.property.CTXT(env[sit,uat],loc[ldn],grp[canary],hst[localhost],usr[john])", "true");
properties.setProperty("my.property", "false");

Properties ctxProperties = ContextPropParser.create(context)
    .requiresDefault()
    .parse(properties);
```

In order to avoid conflicts during property resolution we need to follow some rules (if the rules are violated the parser raises an error):

##### Rule: All properties excluding the default one should define all context keys already defined in the same property group

So for example the following groups are invalid. We are not able to resolve the properties (name1 & name2). Both entries qualify for the
`env[prd],loc[loh],grp[external]` deployment context.

```
property.name1.CTXT(env[prd])=true
property.name1.CTXT(loc[loh])=false #invalid! higher ordered key 'env' should be defined

property.name2.CTXT(env[prd],grp[canary])=true
property.name2.CTXT(env[prd],loc[loh])=false #invalid! higher ordered key 'grp' should be defined
```

The following groups are valid. All properties define all higher order context keys of their group.

```
property.name1.CTXT(usr[john])=true
property.name1.CTXT(usr[john],hst[localhost])=false

property.name2.CTXT(hst[localhost])=true
```

##### Rule: Properties with identical context keys should not have overlapping values when located in the same group

```
property.name.CTXT(env[prd],loc[ldn,nyk])=true
property.name.CTXT(env[prd],loc[ldn,nyk,hkg])=false #invalid! location loh and nyk are overlapping
```
